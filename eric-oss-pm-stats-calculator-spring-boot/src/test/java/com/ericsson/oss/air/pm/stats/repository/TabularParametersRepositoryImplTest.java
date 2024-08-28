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

import com.ericsson.oss.air.pm.stats._util.AssertionHelpers;
import com.ericsson.oss.air.pm.stats.model.entity.TabularParameter;
import com.ericsson.oss.air.pm.stats.model.entity.TabularParameter.TabularParameterBuilder;
import com.ericsson.oss.air.pm.stats.repository._util.RepositoryHelpers;
import com.ericsson.oss.air.pm.stats.repository.api.ParameterRepository;

import kpi.model.ondemand.OnDemandParameter;
import kpi.model.ondemand.OnDemandParameter.OnDemandParameterBuilder;
import kpi.model.ondemand.OnDemandTabularParameter;
import kpi.model.ondemand.OnDemandTabularParameter.OnDemandTabularParameterBuilder;
import kpi.model.ondemand.ParameterType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;

@ExtendWith(MockitoExtension.class)
class TabularParametersRepositoryImplTest {

    @Mock
    ParameterRepository parameterRepositoryMock;
    @InjectMocks
    TabularParameterRepositoryImpl objectUnderTest;

    @Nested
    @DisplayName("Given an invalid JDBC URL")
    class GivenAnInvalidJdbcUrl {

        @Test
        void shouldThrowUncheckedSqlException_onFindAllParameters() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findAllTabularParameters());
        }
    }

    @Nested
    @DisplayName("Given an available database")
    class GivenAnAvailableDatabase {

        final OnDemandParameter kpiParams1 = onDemandParam("start_date_time", ParameterType.STRING);
        final OnDemandParameter kpiParams2 = onDemandParam("execution_id", ParameterType.LONG);
        EmbeddedDatabase embeddedDatabase;

        @BeforeEach
        void setUp() throws SQLException {
            embeddedDatabase = RepositoryHelpers.database("sql/initialize_tabular_parameters.sql", "sql/initialize_parameters.sql");
            final OnDemandTabularParameter kpiTabularParams1 = onDemandTabularParam("cell_config", List.of(kpiParams1, kpiParams2));

            objectUnderTest.saveTabularParameter(embeddedDatabase.getConnection(), kpiTabularParams1, DEFAULT_COLLECTION_ID);
        }

        @AfterEach
        void tearDown() {
            embeddedDatabase.shutdown();
        }

        @Test
        void shouldFindAllTabularParameters() {
            RepositoryHelpers.prepare(embeddedDatabase, () -> {
                final List<TabularParameter> actual = objectUnderTest.findAllTabularParameters();
                assertThat(actual).containsExactly(
                        tabularParameter("cell_params"),
                        tabularParameter("cell_configuration"),
                        tabularParameter("cell_config")
                );
            });
        }
    }

    static OnDemandTabularParameter onDemandTabularParam(final String name, final List<OnDemandParameter> params) {
        final OnDemandTabularParameterBuilder builder = OnDemandTabularParameter.builder();
        builder.name(name);
        builder.columns(params);
        return builder.build();
    }

    static OnDemandParameter onDemandParam(final String name, final ParameterType type) {
        final OnDemandParameterBuilder builder = OnDemandParameter.builder();
        builder.name(name);
        builder.type(type);
        return builder.build();
    }

    static TabularParameter tabularParameter(final String name) {
        final TabularParameterBuilder builder = TabularParameter.builder();
        builder.withName(name);
        return builder.build();
    }
}
