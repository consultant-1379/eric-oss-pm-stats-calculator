/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository;

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_COLLECTION_ID;
import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats._util.AssertionHelpers;
import com.ericsson.oss.air.pm.stats.common.model.collection.CollectionIdProxy;
import com.ericsson.oss.air.pm.stats.model.entity.Parameter;
import com.ericsson.oss.air.pm.stats.model.entity.Parameter.ParameterBuilder;
import com.ericsson.oss.air.pm.stats.model.entity.TabularParameter;
import com.ericsson.oss.air.pm.stats.repository._util.RepositoryHelpers;

import kpi.model.ondemand.OnDemandParameter;
import kpi.model.ondemand.OnDemandParameter.OnDemandParameterBuilder;
import kpi.model.ondemand.ParameterType;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;

class ParameterRepositoryImplTest {

    ParameterRepositoryImpl objectUnderTest = new ParameterRepositoryImpl();

    @Nested
    @DisplayName("Given an invalid JDBC URL")
    class GivenAnInvalidJdbcUrl {

        @Test
        void shouldThrowUncheckedSqlException_onFindAllParameters() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findAllParameters());
        }

        @Test
        void shouldThrowUncheckedSqlException_onFindAllSingleParameters() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findAllSingleParameters());
        }

        @Test
        void shouldThrowUncheckedSqlException_OnFindParametersForTabularParameter() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findParametersForTabularParameter("foo"));
        }

        @Test
        void shouldThrowUncheckedSqlException_OnFindParametersForListOfTabularParameter() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findParametersForListOfTabularParameter(List.of("cell_params")));
        }
    }

    @Nested
    @DisplayName("Given an available database")
    class GivenAnAvailableDatabase {

        static UUID OTHER_COLLECTION_ID = UUID.fromString("6a9666b0-c86d-41dd-b046-c87f2eb339e7");

        EmbeddedDatabase embeddedDatabase;

        final OnDemandParameter kpiParams1 = entity("start_date_time", ParameterType.STRING);
        final OnDemandParameter kpiParams2 = entity("execution_id", ParameterType.LONG);
        final OnDemandParameter kpiParams3 = entity("tabular_parameter_test", ParameterType.INTEGER);
        final Parameter parameter1 = parameter("start_date_time", ParameterType.STRING, null, DEFAULT_COLLECTION_ID);
        final Parameter parameter2 = parameter("execution_id", ParameterType.LONG, null,DEFAULT_COLLECTION_ID);
        final List<Parameter> tabularParameters = List.of(parameter1, parameter2);

        @BeforeEach
        void setUp() throws SQLException {
            embeddedDatabase = RepositoryHelpers.database("sql/initialize_tabular_parameters.sql", "sql/initialize_parameters.sql");
        }

        @AfterEach
        void tearDown() {
            embeddedDatabase.shutdown();
        }

        @Test
        void shouldFindAllParameters() {
            RepositoryHelpers.prepare(embeddedDatabase, () -> {
                objectUnderTest.saveAllParameters(embeddedDatabase.getConnection(), List.of(kpiParams1, kpiParams2), DEFAULT_COLLECTION_ID);
                final List<Parameter> actual = objectUnderTest.findAllParameters();
                assertThat(actual).containsExactlyInAnyOrder(parameter("start_date_time", ParameterType.STRING, null, DEFAULT_COLLECTION_ID), parameter("execution_id", ParameterType.LONG, null, CollectionIdProxy.COLLECTION_ID));
            });
        }

        @Test
        void shouldFindAllParametersWithCollectionId() {
            RepositoryHelpers.prepare(embeddedDatabase, () -> {
                objectUnderTest.saveAllParameters(embeddedDatabase.getConnection(), List.of(kpiParams1, kpiParams2), DEFAULT_COLLECTION_ID);
                objectUnderTest.saveAllParameters(embeddedDatabase.getConnection(), List.of(kpiParams3), OTHER_COLLECTION_ID);
                final List<Parameter> actual = objectUnderTest.findAllParameters(OTHER_COLLECTION_ID);
                assertThat(actual).containsExactlyInAnyOrder(parameter("tabular_parameter_test", ParameterType.INTEGER, null, OTHER_COLLECTION_ID));
            });
        }

        @Test
        void shouldFindSingleParameters() {
            RepositoryHelpers.prepare(embeddedDatabase, () -> {
                objectUnderTest.saveAllParameters(embeddedDatabase.getConnection(), List.of(kpiParams1, kpiParams2), DEFAULT_COLLECTION_ID);
                final List<Parameter> actual = objectUnderTest.findAllSingleParameters();
                assertThat(actual).containsExactlyInAnyOrder(
                        singleParameter("start_date_time", ParameterType.STRING),
                        singleParameter("execution_id", ParameterType.LONG)
                );
            });
        }

        @Test
        @SneakyThrows
        void shouldSaveTabularIdParameters() {
            final TabularParameter tabularParameter = TabularParameter.builder().withId(1).withName("cell_params").build();
            objectUnderTest.saveParametersWithTabularParameterId(embeddedDatabase.getConnection(), List.of(kpiParams1), 1, DEFAULT_COLLECTION_ID);
            RepositoryHelpers.prepare(embeddedDatabase, () -> {
                final List<Parameter> actual = objectUnderTest.findAllParameters();
                assertThat(actual).containsExactlyInAnyOrder(Parameter.builder()
                        .withName("start_date_time")
                        .withType(ParameterType.STRING)
                        .withTabularParameter(tabularParameter)
                        .build());
            });
        }

        @Test
        void shouldFindParametersForTabularParameter() throws SQLException {
            objectUnderTest.saveParametersWithTabularParameterId(embeddedDatabase.getConnection(), List.of(kpiParams1, kpiParams2), 1, DEFAULT_COLLECTION_ID);
            RepositoryHelpers.prepare(embeddedDatabase, () -> {
                final List<Parameter> actual = objectUnderTest.findParametersForTabularParameter("cell_params");
                assertThat(actual).isEqualTo(tabularParameters);
            });
        }

        @Test
        @SneakyThrows
        void shouldFindParametersForListOfTabularParameters() {
            objectUnderTest.saveParametersWithTabularParameterId(embeddedDatabase.getConnection(), List.of(kpiParams1, kpiParams2), 1, DEFAULT_COLLECTION_ID);
            objectUnderTest.saveParametersWithTabularParameterId(embeddedDatabase.getConnection(), List.of(kpiParams3), 2, DEFAULT_COLLECTION_ID);
            RepositoryHelpers.prepare(embeddedDatabase, () -> {
                final List<Parameter> actual = objectUnderTest.findParametersForListOfTabularParameter(List.of("cell_params", "cell_configuration"));
                final TabularParameter tabular_parameter1 = tabularParameter(1, "cell_params");

                final Parameter expected = parameter("start_date_time", ParameterType.STRING, tabular_parameter1, CollectionIdProxy.COLLECTION_ID);
                final Parameter expected2 = parameter("execution_id", ParameterType.LONG, tabular_parameter1, CollectionIdProxy.COLLECTION_ID);
                final Parameter expected3 = parameter("tabular_parameter_test", ParameterType.INTEGER, tabularParameter(2, "cell_configuration"), CollectionIdProxy.COLLECTION_ID);

                assertThat(actual).isEqualTo(List.of(expected, expected2, expected3));
            });
        }

        @Test
        void shouldNotFindParametersForListOfTabularParameters() {
            RepositoryHelpers.prepare(embeddedDatabase, () -> {
                final List<Parameter> actual = objectUnderTest.findParametersForListOfTabularParameter(List.of("dummy"));
                assertThat(actual).isEmpty();
            });
        }
    }

    static OnDemandParameter entity(final String name, final ParameterType type) {
        final OnDemandParameterBuilder builder = OnDemandParameter.builder();
        builder.name(name);
        builder.type(type);
        return builder.build();
    }

    static Parameter parameter(final String name, final ParameterType type, final TabularParameter tabularParameter, UUID collectionId) {
        final ParameterBuilder builder = Parameter.builder();
        builder.withName(name);
        builder.withType(type);
        builder.withTabularParameter(tabularParameter);
        return builder.build();
    }

    static Parameter singleParameter(final String name, final ParameterType type) {
        final ParameterBuilder builder = Parameter.builder();
        builder.withName(name);
        builder.withType(type);
        builder.withTabularParameter(null);
        return builder.build();
    }

    static TabularParameter tabularParameter(final int id, final String name) {
        return TabularParameter.builder()
                .withId(id)
                .withName(name)
                .build();
    }
}
