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

import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_IN_MEMORY;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_POST_AGGREGATION;

import java.util.List;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.MethodSource;

class DatasourceTest {

    @Test
    void shouldInstantiateDatasource() {
        final Datasource datasource = Datasource.datasource("name");
        Assertions.assertThat(datasource.getName()).isEqualTo("name");
    }

    @Test
    void shouldReturnInMemoryDatasources() {
        final List<Datasource> actual = Datasource.inMemoryDatasources();
        Assertions.assertThat(actual).containsExactlyInAnyOrder(KPI_IN_MEMORY, KPI_POST_AGGREGATION);
    }

    @ParameterizedTest(name = "[{index}] {0} isKpiInDb: {1}")
    @ArgumentsSource(ProvideDatasourceKpiIsInDbArgument.class)
    void whenKpiIsInDb(final Datasource datasource, final boolean expected) {
        final boolean actual = Datasource.isKpiDb(datasource);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest(name = "[{index}] {0} isKpiInMemory: {1}")
    @ArgumentsSource(ProvideDatasourceKpiIsInMemoryArgument.class)
    void whenKpiIsInMemory(final Datasource datasource, final boolean expected) {
        final boolean actual = Datasource.isInMemory(datasource);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest(name = "[{index}] {0} isInMemory: {1}")
    @ArgumentsSource(ProvideDatasourceIsInmemoryArgument.class)
    void whenDatasourceIsInMemory(final Datasource datasource, final boolean expected) {
        final boolean actual = datasource.isInMemory();
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest(name = "[{index}] {0} isNonInMemory: {1}")
    @ArgumentsSource(ProvideDatasourceIsInmemoryArgument.class)
    void whenDatasourceIsNonInMemory(final Datasource datasource, final boolean expected) {
        final boolean actual = datasource.isNonInMemory();
        Assertions.assertThat(actual).isNotEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("provideToDatabaseData")
    void shouldVerifyToDatabase(final Datasource datasource, final Database expected) {
        final Database actual = datasource.toDatabase();
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("provideIsInternalData")
    void shouldDecideIfInternal(final Datasource datasource, final boolean expected) {
        final boolean actual = datasource.isInternal();
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("provideIsUnknownData")
    void shouldDecideIfExternal(final Datasource datasource, final boolean expected) {
        final boolean actual = datasource.isUnknown();
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> provideToDatabaseData() {
        return Stream.of(
                Arguments.of(Datasource.of("kpi_db"), new Database("kpidb")),
                Arguments.of(Datasource.of("datasource"), new Database("datasource")),
                Arguments.of(Datasource.of("tabular_parameter"), new Database("tabularparameter"))

        );
    }

    static Stream<Arguments> provideIsUnknownData() {
        return Stream.of(
                Arguments.of(Datasource.KPI_DB, false),
                Arguments.of(KPI_IN_MEMORY, false),
                Arguments.of(KPI_POST_AGGREGATION, false),
                Arguments.of(Datasource.of("datasource"), true),
                Arguments.of(Datasource.TABULAR_PARAMETERS, false)
        );
    }

    static Stream<Arguments> provideIsInternalData() {
        return Stream.of(
                Arguments.of(Datasource.KPI_DB, true),
                Arguments.of(KPI_IN_MEMORY, true),
                Arguments.of(KPI_POST_AGGREGATION, true),
                Arguments.of(Datasource.of("datasource"), false),
                Arguments.of(Datasource.TABULAR_PARAMETERS, true)
        );
    }

    private static final class ProvideDatasourceKpiIsInDbArgument implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) {
            return Stream.of(
                    Arguments.of(null, false),
                    Arguments.of(Datasource.KPI_DB, true),
                    Arguments.of(KPI_IN_MEMORY, false),
                    Arguments.of(KPI_POST_AGGREGATION, false),
                    Arguments.of(Datasource.of("datasource"), false),
                    Arguments.of(Datasource.TABULAR_PARAMETERS, false)
            );
        }
    }

    private static final class ProvideDatasourceKpiIsInMemoryArgument implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) {
            return Stream.of(
                    Arguments.of(null, false),
                    Arguments.of(KPI_IN_MEMORY, true),
                    Arguments.of(Datasource.KPI_DB, false),
                    Arguments.of(KPI_POST_AGGREGATION, false),
                    Arguments.of(Datasource.of("datasource"), false),
                    Arguments.of(Datasource.TABULAR_PARAMETERS, false)
            );
        }
    }

    private static final class ProvideDatasourceIsInmemoryArgument implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) {
            return Stream.of(
                    Arguments.of(KPI_IN_MEMORY, true),
                    Arguments.of(KPI_POST_AGGREGATION, true),
                    Arguments.of(Datasource.KPI_DB, false),
                    Arguments.of(Datasource.of("datasource"), false),
                    Arguments.of(Datasource.TABULAR_PARAMETERS, false)
            );
        }
    }

}
