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

import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ColumnDefinitionTest {
    private final static String COLUMN = "column";
    private final static String COLUMN_DIFF = "columnDiff";

    @Test
    void shouldCreateFromDefinition() {
        final ColumnDefinition columnDefinition = ColumnDefinition.from("name", "REAL");

        Assertions.assertThat(columnDefinition.getColumn()).isEqualTo(Column.of("name"));
        Assertions.assertThat(columnDefinition.getDataType()).isEqualTo(KpiDataType.POSTGRES_REAL);
    }

    @MethodSource("provideIsSameColumnData")
    @ParameterizedTest(name = "[{index}] ColumnDefinition: ''{0}'' and columnDefinition: ''{1}'' has same column: ''{2}''")
    void shouldVerifyIsSameColumn(final ColumnDefinition left, final ColumnDefinition right, final boolean expected) {
        final boolean actual = left.isSameColumn(right);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @MethodSource("provideIsDifferentDataTypeData")
    @ParameterizedTest(name = "[{index}] ColumnDefinition: ''{0}'' and columnDefinition: ''{1}'' has different data type: ''{2}''")
    void shouldVerifyIsDifferentDataType(final ColumnDefinition left, final ColumnDefinition right, final boolean expected) {
        final boolean actual = left.isDifferentDataType(right);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @MethodSource("provideHasSameNameData")
    @ParameterizedTest(name = "[{index}] ColumnDefinition: ''{0}'' and columnDefinition: ''{1}'' has same name: ''{2}''")
    void shouldVerifyHasSaneName(final ColumnDefinition columnDefinition, final String name, final boolean expected) {
        final boolean actual = columnDefinition.hasSameName(name);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    private static Stream<Arguments> provideIsSameColumnData() {
        final ColumnDefinition columnDefinition1 = ColumnDefinition.of(Column.of(COLUMN), KpiDataType.POSTGRES_BOOLEAN);
        final ColumnDefinition columnDefinition2 = ColumnDefinition.of(Column.of(COLUMN), KpiDataType.POSTGRES_BOOLEAN);
        final ColumnDefinition columnDefinition3 = ColumnDefinition.of(Column.of(COLUMN_DIFF), KpiDataType.POSTGRES_BOOLEAN);

        return Stream.of(Arguments.of(columnDefinition1, columnDefinition2, true),
                Arguments.of(columnDefinition1, columnDefinition3, false));
    }

    private static Stream<Arguments> provideIsDifferentDataTypeData() {
        final ColumnDefinition columnDefinition1 = ColumnDefinition.of(Column.of(COLUMN), KpiDataType.POSTGRES_BOOLEAN);
        final ColumnDefinition columnDefinition2 = ColumnDefinition.of(Column.of(COLUMN), KpiDataType.POSTGRES_BOOLEAN);
        final ColumnDefinition columnDefinition3 = ColumnDefinition.of(Column.of(COLUMN_DIFF), KpiDataType.POSTGRES_LONG);

        return Stream.of(Arguments.of(columnDefinition1, columnDefinition2, false),
                Arguments.of(columnDefinition1, columnDefinition3, true));
    }

    private static Stream<Arguments> provideHasSameNameData() {
        final ColumnDefinition columnDefinition = ColumnDefinition.of(Column.of(COLUMN), KpiDataType.POSTGRES_BOOLEAN);

        return Stream.of(Arguments.of(columnDefinition, COLUMN, true),
                Arguments.of(columnDefinition, "other-column", false));
    }
}