/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util;

import java.util.Collection;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SourceColumn;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.TableColumns;
import com.ericsson.oss.air.pm.stats.calculator.service.util.sql.SqlProcessorDelegator;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Component;

//TODO Reconsider to inline this class to KpiDefinitionHelper
/**
 * Extracts {@link TableColumns} from {@link KpiDefinition}s.
 */
@Component
@AllArgsConstructor
public class TableColumnExtractor {

    private final SqlProcessorDelegator sqlProcessorDelegator;
    private final KpiDefinitionHelperImpl kpiDefinitionHelper;

    /**
     * Extracts all {@link TableColumns} from {@link Collection} of {@link KpiDefinition}s.
     *
     * @param kpiDefinitions
     *         to extract {@link TableColumns} from.
     * @return all extracted {@link TableColumns}.
     *
     */
    public TableColumns extractAllTableColumns(final Collection<? extends KpiDefinition> kpiDefinitions) {
        return TableColumns.merge(
                getColumnsForNonInMemoryDatasources(kpiDefinitions),
                getColumnsFromAggregationElements(kpiDefinitions));
    }

    private TableColumns getColumnsForNonInMemoryDatasources(@NonNull final Collection<? extends KpiDefinition> kpiDefinitions) {
        return kpiDefinitions.stream()
                .map(kpiDefinitionHelper::getSourceColumns)
                .flatMap(Collection::stream)
                .filter(SourceColumn::isNonInMemoryDataSource)
                .collect(TableColumns::of, TableColumns::computeIfAbsent, (left, right) -> {
                });
    }

    private TableColumns getColumnsFromAggregationElements(@NonNull final Collection<? extends KpiDefinition> kpiDefinitions) {
        final TableColumns tableColumns = TableColumns.of();
        for (final KpiDefinition kpiDefinition : kpiDefinitions) {
            sqlProcessorDelegator.aggregationElements(kpiDefinition.getAggregationElements()).forEach(reference -> {
                if (reference.isParameterizedAggregationElement()) {
                    //  parameterized resolved or unresolved aggregation element has no actual table information
                    return;
                }
                tableColumns.computeIfAbsent(reference.requiredTable()).add(reference.requiredColumn());
            });
        }
        return tableColumns;
    }
}
