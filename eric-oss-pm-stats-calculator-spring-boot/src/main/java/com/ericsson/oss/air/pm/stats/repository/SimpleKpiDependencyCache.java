/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository;

import static javax.ejb.ConcurrencyManagementType.BEAN;
import static lombok.AccessLevel.PUBLIC;
import static org.apache.commons.lang3.concurrent.locks.LockingVisitors.reentrantReadWriteLockVisitor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.graph.KpiDependencyHelper;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.repository.api.KpiDefinitionRepository;
import com.ericsson.oss.air.pm.stats.service.util.CollectionHelpers;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.locks.LockingVisitors.ReadWriteLockVisitor;

@Slf4j
@Startup
@Singleton
@ConcurrencyManagement(BEAN)
@DependsOn("FlywayIntegration")
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class SimpleKpiDependencyCache {
    @Inject
    private KpiDefinitionRepository kpiDefinitionRepository;
    @Inject
    private KpiDependencyHelper kpiDependencyHelper;

    private final ReadWriteLockVisitor<Map<String, Set<KpiDefinitionEntity>>> lock = reentrantReadWriteLockVisitor(new ConcurrentHashMap<>(128));

    @PostConstruct
    public void populateCache() {
        lock.acceptWriteLocked(cache -> {
            cache.clear();
            cache.putAll(retrieveDependencyMap());
        });
    }

    public Map<String, Set<KpiDefinitionEntity>> loadDependenciesForMultipleDefinitions(@NonNull final Collection<String> kpiDefinitionNames) {
        return lock.applyReadLocked(cache -> kpiDefinitionNames.stream().collect(Collectors.toMap(
                Function.identity(),
                kpiName -> cache.computeIfAbsent(kpiName, missingKpi -> {
                    throw new NoSuchElementException(String.format("The KPI with the name '%s' does not exist", missingKpi));
                })
        )));
    }

    public Collection<String> transitiveDependencyExecutionGroupsOf(final Collection<String> kpiDefinitionNames) {
        return Sets.newHashSet(CollectionHelpers.transform(
                transitiveDependenciesOf(kpiDefinitionNames),
                kpiDefinitionEntity -> kpiDefinitionEntity.executionGroup().name() // TODO: we should work with the execution group entity
        ));
    }

    private Set<KpiDefinitionEntity> transitiveDependenciesOf(final Collection<String> kpiDefinitionNames) {
        return CollectionHelpers.flattenDistinctValues(loadDependenciesForMultipleDefinitions(kpiDefinitionNames));
    }

    private Map<String, Set<KpiDefinitionEntity>> retrieveDependencyMap() {
        final List<KpiDefinitionEntity> definitionSet = kpiDefinitionRepository.findAll();
        return kpiDependencyHelper.findSimpleDependencyMap(definitionSet);
    }

}
