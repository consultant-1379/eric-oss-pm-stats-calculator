/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection;

import java.util.Set;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SourceColumn;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TableColumnsTest {
    TableColumns objectUnderTest = TableColumns.of();

    @Nested
    @DisplayName("Testing compute if absent")
    class ComputeIfAbsent {
        @Test
        void shouldComputeIfAbsent_fromTable() {
            final Set<Column> actual = objectUnderTest.computeIfAbsent(Table.of("newTable"));

            Assertions.assertThat(actual).isEmpty();
        }

        @Test
        void shouldComputeIfAbsent_fromSourceColumn() {
            objectUnderTest.computeIfAbsent(new SourceColumn("table.column"));

            Assertions.assertThat(objectUnderTest.get(Table.of("table"))).containsExactlyInAnyOrder(Column.of("column"));
        }
    }

    @Nested
    @DisplayName("Testing DIM and FACT Table")
    class TableType {
        final Column column1 = Column.of("column1");
        final Column column2 = Column.of("column2");

        @Test
        void shouldReturnDimTables() {
            final Table dimTable = Table.of("dimTable");

            objectUnderTest.computeIfAbsent(dimTable).add(column1);
            objectUnderTest.computeIfAbsent(dimTable).add(column2);

            final Set<Column> actual = objectUnderTest.dimColumns(dimTable);

            Assertions.assertThat(actual).containsExactlyInAnyOrder(column1, column2);
        }

        @Test
        void shouldReturnFactTables() {
            final Table factTable = Table.of("factTable");

            objectUnderTest.computeIfAbsent(factTable).add(column1);
            objectUnderTest.computeIfAbsent(factTable).add(column2);

            final Set<Column> actual = objectUnderTest.factColumns(factTable);

            Assertions.assertThat(actual).containsExactlyInAnyOrder(column1, column2, Column.of("aggregation_begin_time"), Column.of("aggregation_end_time"));
        }
    }

    @Nested
    @DisplayName("Testing DIM and FACT table if null")
    class TableTypeNull {
        final String THROWN_ERROR_MESSAGE = String.format("Table 'no_table' is not found in the '%s'", TableColumns.class.getSimpleName());
        final Table table = Table.of("no_table");

        @Test
        void dimTableShouldBeNull() {
            Assertions.assertThatThrownBy(() -> objectUnderTest.dimColumns(table))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage(THROWN_ERROR_MESSAGE);
        }

        @Test
        void factTableShouldBeNull() {
            Assertions.assertThatThrownBy(() -> objectUnderTest.factColumns(table))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage(THROWN_ERROR_MESSAGE);
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("Testing merging TableColumns")
    class Merge {
        @MethodSource("provideMergeData")
        @ParameterizedTest(name = "[{index}] Left: ''{0}'' Right: ''{1}'' merged to: ''{2}''")
        void shouldMerge(final TableColumns left, final TableColumns right, final TableColumns expected) {
            final TableColumns actual = TableColumns.merge(left, right);
            Assertions.assertThat(actual).isEqualTo(expected);
        }

        Stream<Arguments> provideMergeData() {
            final TableColumns empty = TableColumns.of();
            final TableColumns tableColumns1 = TableColumns.of();
            final TableColumns tableColumns2 = TableColumns.of();

            final SourceColumn sourceColumn1 = new SourceColumn("table1.column1");
            final SourceColumn sourceColumn2 = new SourceColumn("table2.column1");
            final SourceColumn sourceColumn3 = new SourceColumn("table3.column1");

            tableColumns1.computeIfAbsent(sourceColumn1);
            tableColumns1.computeIfAbsent(sourceColumn2);
            tableColumns1.computeIfAbsent(sourceColumn3);

            tableColumns2.computeIfAbsent(sourceColumn1);

            final TableColumns expected1 = TableColumns.of();
            expected1.computeIfAbsent(sourceColumn1);
            expected1.computeIfAbsent(sourceColumn2);
            expected1.computeIfAbsent(sourceColumn3);

            return Stream.of(
                    Arguments.of(empty, tableColumns1, expected1),
                    Arguments.of(tableColumns1, empty, expected1),
                    Arguments.of(tableColumns1, tableColumns2, expected1)
            );
        }
    }
}
