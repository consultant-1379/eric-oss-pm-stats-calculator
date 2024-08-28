/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats._util.JsonLoaders;
import com.ericsson.oss.air.pm.stats._util.Serialization;
import com.ericsson.oss.air.pm.stats.common.sqlparser.SqlParserImpl;
import com.ericsson.oss.air.pm.stats.common.sqlparser.exception.InvalidSqlSyntaxException;
import com.ericsson.oss.air.pm.stats.common.sqlparser.extractor.LogicalPlanExtractor;

import kpi.model.KpiDefinitionRequest;
import kpi.model.api.IterableAttribute;
import kpi.model.complex.element.ComplexFilterElement;
import kpi.model.complex.table.definition.required.ComplexDefinitionExpression;
import org.apache.spark.sql.catalyst.parser.ParseException;
import org.apache.spark.sql.execution.SparkSqlParser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;

class SqlParserImplTest {
    SqlParserImpl objectUnderTest = new SqlParserImpl(new SparkSqlParser(), new LogicalPlanExtractor());

    @Nested
    class VerifyExpression {

        @TestFactory
        Stream<DynamicTest> verifyPlanParsable() {
            final KpiDefinitionRequest kpiDefinition = loadDefinition();

            final Set<kpi.model.api.table.definition.KpiDefinition> definitions = kpiDefinition.definitions();
            Assertions.assertThat(definitions).isNotEmpty();

            return definitions.stream().map(kpi.model.api.table.definition.KpiDefinition::expression).map(expression -> {
                final String displayName = String.format("Verifying syntactically valid '%s'", expression.value());
                return dynamicTest(displayName, () -> {
                    Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.parsePlan(expression));
                });
            });
        }

        @TestFactory
        Stream<DynamicTest> verifyPlanIsNotParsable() {
            final Map<String, String> invalidSQLs = Map.of(
                    "Missing closing bracket", "SELECT * FROM artist WHERE first_name = 'Vincent' and (last_name = 'Monet' or last_name = 'Da Vinci'",
                    "Invalid statement order", "SELECT name FROM dish WHERE name = 'Prawn Salad' GROUP BY name ORDER BY name HAVING count(*) = 1"
            );
            return invalidSQLs.entrySet().stream().map(entry -> {
                final String displayName = String.format("%s --> %s", entry.getKey(), entry.getValue());
                return dynamicTest(displayName, () -> {
                    final ComplexDefinitionExpression expression = ComplexDefinitionExpression.of(entry.getValue());
                    Assertions.assertThatThrownBy(() -> objectUnderTest.parsePlan(expression))
                            .isInstanceOf(InvalidSqlSyntaxException.class)
                            .hasRootCauseInstanceOf(ParseException.class);
                });
            });

        }

        @TestFactory
        Stream<DynamicTest> verifyPlanIsNotParsableBecauseOfJoinConditions() {
            final Map<String, String> invalidSQLs = Map.of(
                    "Missing 'ON' condition after 'INNER JOIN'", "SELECT * FROM artist INNER JOIN dish WHERE artists.col1 IS NULL",
                    "Missing 'ON' condition after 'LEFT JOIN'", "SELECT * FROM artist LEFT JOIN dish WHERE artists.col1 IS NULL",
                    "Missing 'ON' condition after 'RIGHT JOIN'", "SELECT * FROM artist RIGHT JOIN dish WHERE artists.col1 IS NULL"
            );

            return invalidSQLs.entrySet().stream().map(entry -> {
                final String displayName = String.format("%s --> %s", entry.getKey(), entry.getValue());
                return dynamicTest(displayName, () -> {
                    final ComplexDefinitionExpression expression = ComplexDefinitionExpression.of(entry.getValue());
                    Assertions.assertThatThrownBy(() -> objectUnderTest.parsePlan(expression))
                            .isInstanceOf(InvalidSqlSyntaxException.class).hasMessage(
                                    "In expression %s 'JOIN' with the following tables: %s, %s does not contain 'ON' condition",
                                    entry.getValue(), "artist", "dish"
                            );
                });
            });
        }

        @TestFactory
        Stream<DynamicTest> verifyPlanIsNotParsableWithoutDatasource() {
            final Map<String, String> invalidSQLs = Map.of(
                    "Missing datasource after 'INNER JOIN'", "FIRST(kpi_cell_guid_simple_1440.sum_integer_arrayindex_1440_simple / cell_configuration_test_2.integer_property) FROM kpi_db://kpi_cell_guid_simple_1440 INNER JOIN cell_configuration_test_2 ON kpi_cell_guid_simple_1440.nodeFDN = cell_configuration_test_2.nodeFDN",
                    "Missing datasource after 'LEFT JOIN'", "FIRST(kpi_cell_guid_simple_1440.sum_integer_arrayindex_1440_simple / cell_configuration_test_2.integer_property) FROM kpi_db://kpi_cell_guid_simple_1440 LEFT JOIN cell_configuration_test_2 ON kpi_cell_guid_simple_1440.nodeFDN = cell_configuration_test_2.nodeFDN",
                    "Missing datasource after 'RIGHT JOIN'", "FIRST(kpi_cell_guid_simple_1440.sum_integer_arrayindex_1440_simple / cell_configuration_test_2.integer_property) FROM kpi_db://kpi_cell_guid_simple_1440 RIGHT JOIN cell_configuration_test_2 ON kpi_cell_guid_simple_1440.nodeFDN = cell_configuration_test_2.nodeFDN"
            );

            return invalidSQLs.entrySet().stream().map(entry -> {
                final String displayName = String.format("%s --> %s", entry.getKey(), entry.getValue());
                return dynamicTest(displayName, () -> {
                    final ComplexDefinitionExpression expression = ComplexDefinitionExpression.of(entry.getValue());
                    Assertions.assertThatThrownBy(() -> objectUnderTest.parsePlan(expression))
                            .isInstanceOf(InvalidSqlSyntaxException.class).hasMessage(
                                    "In expression %s there is missing datasource for the following table(s): %s",
                                    entry.getValue(), "cell_configuration_test_2"
                            );
                });
            });
        }

        @TestFactory
        Stream<DynamicTest> verifyPlanIsNotParsableWithTwoMissingDatasources() {
            final Map<String, String> invalidSQLs = Map.of(
                    "Missing datasource after 2 'INNER JOIN'", "FIRST(kpi_cell_guid_simple_1440.sum_integer_arrayindex_1440_simple / cell_configuration_test_2.integer_property) FROM kpi_db://kpi_cell_guid_simple_1440 INNER JOIN cell_configuration_test_2 ON kpi_cell_guid_simple_1440.nodeFDN = cell_configuration_test_2.nodeFDN INNER JOIN cell_configuration_test_3 ON kpi_cell_guid_simple_1440.nodeFDN = cell_configuration_test_3.nodeFDN",
                    "Missing datasource after 2 'LEFT JOIN'", "FIRST(kpi_cell_guid_simple_1440.sum_integer_arrayindex_1440_simple / cell_configuration_test_2.integer_property) FROM kpi_db://kpi_cell_guid_simple_1440 LEFT JOIN cell_configuration_test_2 ON kpi_cell_guid_simple_1440.nodeFDN = cell_configuration_test_2.nodeFDN LEFT JOIN cell_configuration_test_3 ON kpi_cell_guid_simple_1440.nodeFDN = cell_configuration_test_3.nodeFDN",
                    "Missing datasource after 2 'RIGHT JOIN'", "FIRST(kpi_cell_guid_simple_1440.sum_integer_arrayindex_1440_simple / cell_configuration_test_2.integer_property) FROM kpi_db://kpi_cell_guid_simple_1440 RIGHT JOIN cell_configuration_test_2 ON kpi_cell_guid_simple_1440.nodeFDN = cell_configuration_test_2.nodeFDN RIGHT JOIN cell_configuration_test_3 ON kpi_cell_guid_simple_1440.nodeFDN = cell_configuration_test_3.nodeFDN",
                    "Missing datasource after mixed 'JOIN'", "FIRST(kpi_cell_guid_simple_1440.sum_integer_arrayindex_1440_simple / cell_configuration_test_2.integer_property) FROM kpi_db://kpi_cell_guid_simple_1440 LEFT JOIN cell_configuration_test_2 ON kpi_cell_guid_simple_1440.nodeFDN = cell_configuration_test_2.nodeFDN RIGHT JOIN cell_configuration_test_3 ON kpi_cell_guid_simple_1440.nodeFDN = cell_configuration_test_3.nodeFDN"
            );

            return invalidSQLs.entrySet().stream().map(entry -> {
                final String displayName = String.format("%s --> %s", entry.getKey(), entry.getValue());
                return dynamicTest(displayName, () -> {
                    final ComplexDefinitionExpression expression = ComplexDefinitionExpression.of(entry.getValue());
                    Assertions.assertThatThrownBy(() -> objectUnderTest.parsePlan(expression))
                            .isInstanceOf(InvalidSqlSyntaxException.class).hasMessage(
                                    "In expression %s there is missing datasource for the following table(s): %s",
                                    entry.getValue(), "cell_configuration_test_3, cell_configuration_test_2"
                            );
                });
            });
        }

        @TestFactory
        Stream<DynamicTest> verifyPlanIsNotParsableWithoutDatasourceWithMultipleColumnConditions() {
            final Map<String, String> invalidSQLs = Map.of(
                    "Missing datasource after 'INNER JOIN' on multiple condition", "FIRST(kpi_cell_guid_simple_1440.sum_integer_arrayindex_1440_simple / cell_configuration_test_2.integer_property) FROM kpi_db://kpi_cell_guid_simple_1440 INNER JOIN cell_configuration_test_2 ON kpi_cell_guid_simple_1440.nodeFDN = cell_configuration_test_2.nodeFDN AND kpi_cell_guid_simple_1440.column_2 = cell_configuration_test_2.column_2",
                    "Missing datasource after 'LEFT JOIN' on multiple condition", "FIRST(kpi_cell_guid_simple_1440.sum_integer_arrayindex_1440_simple / cell_configuration_test_2.integer_property) FROM kpi_db://kpi_cell_guid_simple_1440 LEFT JOIN cell_configuration_test_2 ON kpi_cell_guid_simple_1440.nodeFDN = cell_configuration_test_2.nodeFDN AND kpi_cell_guid_simple_1440.column_2 = cell_configuration_test_2.column_2",
                    "Missing datasource after 'RIGHT JOIN' on multiple condition", "FIRST(kpi_cell_guid_simple_1440.sum_integer_arrayindex_1440_simple / cell_configuration_test_2.integer_property) FROM kpi_db://kpi_cell_guid_simple_1440 RIGHT JOIN cell_configuration_test_2 ON kpi_cell_guid_simple_1440.nodeFDN = cell_configuration_test_2.nodeFDN AND kpi_cell_guid_simple_1440.column_2 = cell_configuration_test_2.column_2"
            );

            return invalidSQLs.entrySet().stream().map(entry -> {
                final String displayName = String.format("%s --> %s", entry.getKey(), entry.getValue());
                return dynamicTest(displayName, () -> {
                    final ComplexDefinitionExpression expression = ComplexDefinitionExpression.of(entry.getValue());
                    Assertions.assertThatThrownBy(() -> objectUnderTest.parsePlan(expression))
                            .isInstanceOf(InvalidSqlSyntaxException.class).hasMessage(
                                    "In expression %s there is missing datasource for the following table(s): %s",
                                    entry.getValue(), "cell_configuration_test_2"
                            );
                });
            });
        }
    }

    @Nested
    class VerifyFilterElementVisit {

        @TestFactory
        Stream<DynamicTest> verifyFilterElementParsable() {
            final KpiDefinitionRequest kpiDefinition = loadDefinition();

            final Set<kpi.model.api.table.definition.KpiDefinition> definitions = kpiDefinition.definitions();
            Assertions.assertThat(definitions).isNotEmpty();

            return definitions.stream()
                    .map(kpi.model.api.table.definition.KpiDefinition::filters)
                    .flatMap(IterableAttribute::stream)
                    .map(filterElement -> {
                        final String displayName = String.format("Verifying syntactically valid '%s'", filterElement.value());
                        return dynamicTest(displayName, () -> {
                            Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.parseExpression(filterElement));
                        });
                    });
        }

        @TestFactory
        Stream<DynamicTest> verifyFilterElementIsNotParsable() {
            final Map<String, String> invalidSQLs = Map.of(
                    "Missing right hand value", "<table>.<column> <"
            );

            return invalidSQLs.entrySet().stream().map(entry -> {
                final String displayName = String.format("%s --> %s", entry.getKey(), entry.getValue());
                return dynamicTest(displayName, () -> {
                    final ComplexFilterElement complexFilterElement = ComplexFilterElement.of(entry.getValue());
                    Assertions.assertThatThrownBy(() -> objectUnderTest.parseExpression(complexFilterElement))
                            .isInstanceOf(InvalidSqlSyntaxException.class)
                            .hasRootCauseInstanceOf(ParseException.class);
                });
            });
        }
    }

    @Nested
    class VerifyAggregationElementVisit {

        @TestFactory
        Stream<DynamicTest> verifyAggregationElementParsable() {
            final KpiDefinitionRequest kpiDefinition = loadDefinition();

            final Set<kpi.model.api.table.definition.KpiDefinition> definitions = kpiDefinition.definitions();
            Assertions.assertThat(definitions).isNotEmpty();

            return definitions.stream()
                    .map(kpi.model.api.table.definition.KpiDefinition::aggregationElements)
                    .flatMap(IterableAttribute::stream)
                    .map(aggregationElement -> {
                        final String displayName = String.format("Verifying syntactically valid '%s'", aggregationElement.value());
                        return dynamicTest(displayName, () -> {
                            Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.parseExpression(aggregationElement));
                        });
                    });
        }
    }

    static KpiDefinitionRequest loadDefinition() {
        final String jsonContent = JsonLoaders.load("NewRequiredKpis.json");
        return Serialization.deserialize(jsonContent, KpiDefinitionRequest.class);
    }
}