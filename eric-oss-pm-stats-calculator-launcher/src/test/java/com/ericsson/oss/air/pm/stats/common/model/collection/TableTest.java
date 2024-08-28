/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.model.collection;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.ericsson.oss.air.pm.stats.common.model.collection.Table.DEFAULT_SCHEMA;

class TableTest {

    @Test
    void shouldConstructTableWithTable() {
        final Table actual = Table.of("table");
        Assertions.assertThat(actual.getSchema()).isEqualTo(DEFAULT_SCHEMA);
        Assertions.assertThat(actual.getName()).isEqualTo("table");
    }

    @Test
    void shouldConstructTableWithSchema_andTable() {
        final Table actual = Table.of("schema", "table");
        Assertions.assertThat(actual.getSchema()).isEqualTo("schema");
        Assertions.assertThat(actual.getName()).isEqualTo("table");
    }

    @MethodSource("provideTableNameContainsNumberData")
    @ParameterizedTest(name = "[{index}] Table with name ''{0}'' contains number => ''{1}''")
    void verifyTableNameContainsNumber(final Table table, final boolean expected) {
        final boolean actual = table.doesContainNumber();

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @MethodSource("provideTableNameDoesNotContainNumberData")
    @ParameterizedTest(name = "[{index}] Table with name ''{0}'' does not contain number => ''{1}''")
    void verifyTableNameDoesNotContainNumber(final Table table, final boolean expected) {
        final boolean actual = table.doesNotContainNumber();

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest(name = "[{index}] TableTest: {2} table: {0} anotherTable: {1}")
    @ArgumentsSource(ProvideDataToIsNameEqualsAnotherTableName.class)
    void isNameEqualsAnotherTableNameTest(Table table, Table anotherTable, boolean expected) {
        boolean result = table.isNameEqualsAnotherTableName(anotherTable);
        Assertions.assertThat(result).isEqualTo(expected);
    }

    @MethodSource("provideTableNamesForGetAggregationPeriod")
    @ParameterizedTest(name = "[{index}] Table with name ''{0}'' has number => ''{1}''")
    void getNumberTest(final Table table, final int expected) {
        final int actual = table.getAggregationPeriod();

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @MethodSource("provideTableNamesForGetAlias")
    @ParameterizedTest(name = "[{index}] Table with name ''{0}'' has alias => ''{1}''")
    void getAliasTest(final Table table, final String expected) {
        final String actual = table.getAlias();

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> provideTableNameContainsNumberData() {
        return Stream.of(
                Arguments.of(Table.of("kpi_sector_"), false),
                Arguments.of(Table.of("kpi_sector_60"), true),
                Arguments.of(Table.of("kpi_sector_1_440"), true)
        );
    }

    static Stream<Arguments> provideTableNamesForGetAggregationPeriod() {
        return Stream.of(
                Arguments.of(Table.of("kpi_sector_"), -1),
                Arguments.of(Table.of("kpi_sector_60"), 60),
                Arguments.of(Table.of("kpi_sector_1440"), 1440)
        );
    }

    static Stream<Arguments> provideTableNameDoesNotContainNumberData() {
        return Stream.of(
                Arguments.of(Table.of("kpi_sector_"), true),
                Arguments.of(Table.of("kpi_sector_60"), false),
                Arguments.of(Table.of("kpi_sector_1_440"), false)
        );
    }

    static Stream<Arguments> provideTableNamesForGetAlias() {
        return Stream.of(
                Arguments.of(Table.of("kpi_cell_sector_"), "cell_sector"),
                Arguments.of(Table.of("kpi_kpi_cell_sector_"), "kpi_cell_sector"),
                Arguments.of(Table.of("kpi_cell_sector_60"), "cell_sector"),
                Arguments.of(Table.of("kpi_cell_sector_45_1440"), "cell_sector_45"),
                Arguments.of(Table.of("kpi_cell_sector_60_1440"), "cell_sector_60"),
                Arguments.of(Table.of("kpi_kpi_cell_sector_60_1440"), "kpi_cell_sector_60")
        );
    }

    private static final class ProvideDataToIsNameEqualsAnotherTableName implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) {
            return Stream.of(
                    Arguments.of(Table.of("table"), Table.of("table"), true),
                    Arguments.of(Table.of("table"), Table.of("table1"), false)
            );
        }
    }
}