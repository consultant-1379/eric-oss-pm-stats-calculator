/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util.table.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils;

import com.google.common.base.Preconditions;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data(staticConstructor = "of")
public final class TableCreationInformation {
    private final String tableName;
    private final String aggregationPeriod;
    private final Collection<KpiDefinitionEntity> definitions;

    @SuppressWarnings("unused")
    private TableCreationInformation(@NonNull final String tableName,
                                     @NonNull final String aggregationPeriod,
                                     @NonNull final Collection<KpiDefinitionEntity> definitions) {
        Preconditions.checkArgument(!definitions.isEmpty(), "definitions should not be empty");

        this.tableName = tableName;
        this.aggregationPeriod = aggregationPeriod;
        this.definitions = Collections.unmodifiableCollection(definitions);
    }

    public static TableCreationInformation of(@NonNull final TableDefinitions tableDefinitions) {
        final String name = tableDefinitions.getTable().getName();
        final String aggregationPeriod = tableDefinitions.getAggregationPeriod();
        final Set<KpiDefinitionEntity> definitions = tableDefinitions.getDefinitions();
        return of(name, aggregationPeriod, definitions);
    }

    public Set<String> collectAggregationElements() {
        return definitions.stream()
                .map(KpiDefinitionEntity::aggregationElements)
                .map(KpiDefinitionUtils::getAggregationElements)
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public Set<String> collectAggregationDbTables() {
        return collectNotSimpleDefinitions().stream()
                .map(KpiDefinitionEntity::aggregationElements)
                .map(KpiDefinitionUtils::getAggregationDbTables)
                .flatMap(Collection::stream)
                .filter(aggregationTableName -> !aggregationTableName.equals(tableName))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public Set<KpiDefinitionEntity> collectSimpleDefinitions() {
        return definitions.stream().filter(KpiDefinitionEntity::isSimple).collect(Collectors.toSet());
    }

    public Set<KpiDefinitionEntity> collectNotSimpleDefinitions() {
        return definitions.stream().filter(definition -> !definition.isSimple()).collect(Collectors.toSet());
    }

    public boolean isDefaultAggregationPeriod() {
        return KpiDefinitionUtils.isDefaultAggregationPeriod(aggregationPeriod);
    }

    public boolean isNonDefaultAggregationPeriod() {
        return !isDefaultAggregationPeriod();
    }

}
