/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;

import com.ericsson.oss.air.pm.stats.calculator.api.model.Definition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class DefinitionMapper {

    public KpiDefinitionEntity toEntity(final Definition definition) {
        return DefinitionMappers.toEntity(definition);
    }

    public KpiDefinitionEntity toEntity(final KpiDefinition kpiDefinition) {
        return DefinitionMappers.toEntity(kpiDefinition);
    }

    public Definition toDefinition(final KpiDefinitionEntity kpiDefinitionEntity) {
        return DefinitionMappers.toDefinition(kpiDefinitionEntity);
    }

    public KpiDefinition toKpiDefinition(final KpiDefinitionEntity kpiDefinitionEntity) {
        return DefinitionMappers.toKpiDefinition(kpiDefinitionEntity);
    }

    //TODO remove only when Definition class is eliminated
    public Set<KpiDefinitionEntity> definitionsToEntities(final Set<? extends Definition> definitions) {
        return new HashSet<>(DefinitionMappers.definitionsToEntities(new ArrayList<>(definitions)));
    }

    public List<KpiDefinitionEntity> definitionsToEntities(final List<? extends Definition> definitions) {
        return DefinitionMappers.definitionsToEntities(definitions);
    }

    public List<KpiDefinitionEntity> kpiDefinitionsToEntities(final List<? extends KpiDefinition> definitions) {
        return DefinitionMappers.kpiDefinitionsToEntities(definitions);
    }

    public List<Definition> toDefinitions(final Collection<? extends KpiDefinitionEntity> entities) {
        return DefinitionMappers.toDefinitions(entities);
    }

    public Set<Definition> toDefinitions(final Set<? extends KpiDefinitionEntity> entities) {
        return new HashSet<>(DefinitionMappers.toDefinitions(new ArrayList<>(entities)));
    }

    public Set<KpiDefinitionEntity> toEntities(final Collection<? extends Definition> definitions) {
        return new HashSet<>(DefinitionMappers.definitionsToEntities(new ArrayList<>(definitions)));
    }

    public List<KpiDefinition> toKpiDefinitions(final List<? extends KpiDefinitionEntity> entities) {
        return DefinitionMappers.toKpiDefinitions(entities);
    }

}
