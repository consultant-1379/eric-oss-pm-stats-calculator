/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util.table.model;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data(staticConstructor = "of")
public final class ColumnDefinition {
    public static final ColumnDefinition AGGREGATION_BEGIN_TIME = of(Column.of("aggregation_begin_time"), KpiDataType.POSTGRES_TIMESTAMP);

    private final Column column;
    private final KpiDataType dataType;

    public static ColumnDefinition from(final String definitionName, final String objectType) {
        return of(Column.of(definitionName), KpiDataType.forValue(objectType));
    }

    public boolean isSameColumn(@NonNull final ColumnDefinition other) {
        return column.equals(other.getColumn());
    }

    public boolean isDifferentDataType(@NonNull final ColumnDefinition other) {
        return dataType != other.getDataType();
    }

    public boolean hasSameName(final String columnName) {
        return column.getName().equals(columnName);
    }
}

