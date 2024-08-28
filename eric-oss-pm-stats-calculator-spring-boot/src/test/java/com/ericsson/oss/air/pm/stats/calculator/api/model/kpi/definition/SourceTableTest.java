/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition;

import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SourceTableTest {

    @Test
    void shouldReturnTrue_whenDatasourceIsNonInMemory() {
        final SourceTable objectUnderTest = new SourceTable("pm_stats://counters_cell.counter1");

        Assertions.assertThat(objectUnderTest.isNonInMemory()).isTrue();
    }

    @Test
    void whenCreatingSource_givenExpressionThatHasNoBraces_thenExpressionIsUnchanged() {
        final SourceTable objectUnderTest = new SourceTable("pm_stats://counters_cell.counter1");
        Assertions.assertThat(objectUnderTest.getDatasource().getName()).isEqualTo("pm_stats");
        Assertions.assertThat(objectUnderTest.getTable().getName()).isEqualTo("counters_cell");
    }

    @Test
    void whenCreatingSource_givenExpressionThatHasALeadingBrace_thenExpressionIsUnchanged() {
        final SourceTable objectUnderTest = new SourceTable("(pm_stats://counters_cell.counter1");
        Assertions.assertThat(objectUnderTest.getDatasource().getName()).isEqualTo("pm_stats");
        Assertions.assertThat(objectUnderTest.getTable().getName()).isEqualTo("counters_cell");
    }

    @Test
    void whenCreatingSource_givenExpressionThatHasATrailingBrace_thenExpressionIsUnchanged() {
        final SourceTable objectUnderTest = new SourceTable("pm_stats://counters_cell.counter1)");
        Assertions.assertThat(objectUnderTest.getDatasource().getName()).isEqualTo("pm_stats");
        Assertions.assertThat(objectUnderTest.getTable().getName()).isEqualTo("counters_cell");
    }

    @Test
    void whenCreatingSource_givenExpressionThatHasALeadingAndATrailingBrace_thenExpressionIsUnchanged() {
        final SourceTable objectUnderTest = new SourceTable("(pm_stats://counters_cell.counter1)");
        Assertions.assertThat(objectUnderTest.getDatasource().getName()).isEqualTo("pm_stats");
        Assertions.assertThat(objectUnderTest.getTable().getName()).isEqualTo("counters_cell");
    }

    @Test
    void whenCreatingSource_givenExpressionContainsTwoSparkFunction_thenExpressionIsUnchanged() {
        final SourceTable objectUnderTest = new SourceTable("kpi_inmemory://cell_guid.aggregate(slice(num_samples_rsrp_ta");
        Assertions.assertThat(objectUnderTest.getDatasource().getName()).isEqualTo("kpi_inmemory");
        Assertions.assertThat(objectUnderTest.getTable().getName()).isEqualTo("cell_guid");
    }

    @Test
    void whenCreatingSource_givenExpressionContainsOneSparkFunction_thenExpressionIsUnchanged() {
        final SourceTable objectUnderTest = new SourceTable("kpi_inmemory://cell_guid.slice(num_samples_rsrp_ta");
        Assertions.assertThat(objectUnderTest.getDatasource().getName()).isEqualTo("kpi_inmemory");
        Assertions.assertThat(objectUnderTest.getTable().getName()).isEqualTo("cell_guid");
    }

    @Test
    void whenCreatingSource_givenExpressionContainsTwoSparkFunctionAndComma_thenExpressionIsUnchanged() {
        final SourceTable objectUnderTest = new SourceTable("kpi_inmemory://cell_guid.aggregate(slice(num_samples_rsrp_ta,");
        Assertions.assertThat(objectUnderTest.getDatasource().getName()).isEqualTo("kpi_inmemory");
        Assertions.assertThat(objectUnderTest.getTable().getName()).isEqualTo("cell_guid");
    }

    @Test
    void whenCreatingSource_givenExpressionContainsDatasourcewAndTableOnly_thenExpressionIsUnchanged() {
        final SourceTable objectUnderTest = new SourceTable("kpi_inmemory://cell_guid");
        Assertions.assertThat(objectUnderTest.getDatasource().getName()).isEqualTo("kpi_inmemory");
        Assertions.assertThat(objectUnderTest.getTable().getName()).isEqualTo("cell_guid");
    }

    @ParameterizedTest
    @MethodSource("provideHasSameTableData")
    void shouldVerifyHasSameTable(final Table table, final boolean expected) {
        final SourceTable objectUnderTest = new SourceTable("kpi_inmemory://cell_guid");
        final boolean actual = objectUnderTest.hasSameTable(table);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("provideIsInternalData")
    void shouldVerifyIfInternalSource(final String expression, final boolean expected) {
        final SourceTable objectUnderTest = new SourceTable(expression);
        final boolean actual = objectUnderTest.isInternal();
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> provideHasSameTableData() {
        return Stream.of(
                Arguments.of(Table.of("cell_guid"), true),
                Arguments.of(Table.of("unknown_table"), false)
        );
    }

    static Stream<Arguments> provideIsInternalData() {
        return Stream.of(
                Arguments.of("kpi_inmemory://cell_guid", true),
                Arguments.of("kpi_db://cell_guid", true),
                Arguments.of("kpi_post_agg://cell_guid", true),
                Arguments.of("kpi_something://cell_guid", false)
        );
    }
}