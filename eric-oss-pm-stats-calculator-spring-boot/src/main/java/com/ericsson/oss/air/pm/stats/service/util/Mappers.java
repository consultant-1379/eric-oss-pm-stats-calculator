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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.ColumnDefinition;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

// TODO: Remove this class once Domain specific things are used

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Mappers {

    public static List<Column> toColumns(@NonNull final Collection<String> columns) {
        return columns.stream().map(Column::of).collect(Collectors.toList());
    }

    public static List<ColumnDefinition> toColumnDefinition(@NonNull final Collection<? extends KpiDefinitionEntity> definitions) {
        return definitions.stream()
                .map(definition -> {
                    final String definitionName = definition.name();
                    final String objectType = definition.objectType();
                    return ColumnDefinition.from(definitionName, objectType);
                })
                .collect(Collectors.toList());
    }

    public static List<ColumnDefinition> toColumnDefinition(@NonNull final Collection<String> aggregationElements,
                                                            final Map<String, KpiDataType> columnNameToDataType) {
        return aggregationElements.stream()
                .map(aggregationElement -> convertToColumnDefinition(aggregationElement, columnNameToDataType))
                .collect(Collectors.toList());
    }

    private static ColumnDefinition convertToColumnDefinition(final String aggregationElement,
                                                              @NonNull final Map<String, KpiDataType> validAggregationElements) {
        return ColumnDefinition.of(Column.of(aggregationElement), validAggregationElements.get(aggregationElement));
    }
}
