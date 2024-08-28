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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SourceColumnTest {

    SourceColumn objectUnderTest;

    @Test
    void whenCreatingSourceColumn_givenExpressionThatHasNoBraces_thenExpressionIsUnchanged() {
        objectUnderTest = new SourceColumn("counters_cell.counter1");
        Assertions.assertThat(objectUnderTest.getColumn()).isEqualTo("counter1");
        Assertions.assertThat(objectUnderTest.getTable().getName()).isEqualTo("counters_cell");
    }

    @Test
    void whenCreatingSourceColumn_givenExpressionThatHasALeadingBrace_thenExpressionIsUnchanged() {
        objectUnderTest = new SourceColumn("(counters_cell.counter1");
        Assertions.assertThat(objectUnderTest.getColumn()).isEqualTo("counter1");
        Assertions.assertThat(objectUnderTest.getTable().getName()).isEqualTo("counters_cell");
    }

    @Test
    void whenCreatingSourceColumn_givenExpressionThatHasATrailingBrace_thenExpressionIsUnchanged() {
        objectUnderTest = new SourceColumn("counters_cell.counter1)");
        Assertions.assertThat(objectUnderTest.getColumn()).isEqualTo("counter1");
        Assertions.assertThat(objectUnderTest.getTable().getName()).isEqualTo("counters_cell");
    }

    @Test
    void whenCreatingSourceColumn_givenExpressionThatHasALeadingAndATrailingBrace_thenExpressionIsUnchanged() {
        objectUnderTest = new SourceColumn("(counters_cell.counter1)");
        Assertions.assertThat(objectUnderTest.getColumn()).isEqualTo("counter1");
        Assertions.assertThat(objectUnderTest.getTable().getName()).isEqualTo("counters_cell");
    }

    @Test
    void whenCreatingSourceColumn_givenExpressionContainsTwoSparkFunction_thenExpressionIsUnchanged() {
        objectUnderTest = new SourceColumn("aggregate(slice(cell_guid.num_samples_rsrp_ta");
        Assertions.assertThat(objectUnderTest.getColumn()).isEqualTo("num_samples_rsrp_ta");
        Assertions.assertThat(objectUnderTest.getTable().getName()).isEqualTo("cell_guid");
    }

    @Test
    void whenCreatingSourceColumn_givenExpressionContainsOneSparkFunction_thenExpressionIsUnchanged() {
        objectUnderTest = new SourceColumn("slice(cell_guid.num_samples_rsrp_ta");
        Assertions.assertThat(objectUnderTest.getColumn()).isEqualTo("num_samples_rsrp_ta");
        Assertions.assertThat(objectUnderTest.getTable().getName()).isEqualTo("cell_guid");
    }

    @Test
    void whenCreatingSourceColumn_givenExpressionContainsTwoSparkFunctionAndComma_thenExpressionIsUnchanged() {
        objectUnderTest = new SourceColumn("aggregate(slice(cell_guid.num_samples_rsrp_ta,");
        Assertions.assertThat(objectUnderTest.getColumn()).isEqualTo("num_samples_rsrp_ta");
        Assertions.assertThat(objectUnderTest.getTable().getName()).isEqualTo("cell_guid");
    }

    @Test
    void whenCreatingSourceColumn_givenExpressionContainsTableColumnOnly_thenExpressionIsUnchanged() {
        final String sqlExpression = "table.column";
        objectUnderTest = new SourceColumn(sqlExpression);
        Assertions.assertThat(objectUnderTest.getColumn()).isEqualTo("column");
        Assertions.assertThat(objectUnderTest.getTable().getName()).isEqualTo("table");
    }

    @Test
    void whenCreatingSourceColumn_givenExpressionContainsUnsupportedAggregationType_thenTableIsNotCorrectlyCreated() {
        final String sqlExpression = "UNSUPPORTED_AGGREGATION(table.column)";
        objectUnderTest = new SourceColumn(sqlExpression);
        Assertions.assertThat(objectUnderTest.getColumn()).isEqualTo("column");
        Assertions.assertThat(objectUnderTest.getTable().getName()).isEqualTo("UNSUPPORTED_AGGREGATION(table");
    }

    @MethodSource("provideIsValidSourceData")
    @ParameterizedTest(name = "[{index}] input: ''{0}'', is valid: ''{1}''")
    void shouldDetermineIfValidSource(final String input, final boolean expected) {
        final boolean actual = SourceColumn.isValidSource(input);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> provideIsValidSourceData() {
        return Stream.of(
                Arguments.of("table2.column", true),
                Arguments.of("0.25", false),
                Arguments.of("SUM(-0.25", false),
                Arguments.of("COUNT(0.25", false),
                Arguments.of("0.25)", false),
                Arguments.of("0.2a5", true),
                Arguments.of("0.a25", true),
                Arguments.of("a0.25", false),
                Arguments.of("a0.25a", true)
        );
    }
}
