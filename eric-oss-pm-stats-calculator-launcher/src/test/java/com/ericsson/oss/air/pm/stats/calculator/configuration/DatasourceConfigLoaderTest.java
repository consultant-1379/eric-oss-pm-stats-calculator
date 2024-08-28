/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.configuration;

import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_DRIVER;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_JDBC_URL;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_PASSWORD;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_USER;
import static com.ericsson.oss.air.pm.stats.common.model.constants.DatasourceConstants.PROPERTY_CONNECTION_URL;
import static com.ericsson.oss.air.pm.stats.common.model.constants.DatasourceConstants.PROPERTY_DRIVER;
import static com.ericsson.oss.air.pm.stats.common.model.constants.DatasourceConstants.PROPERTY_EXPRESSION_TAG;
import static com.ericsson.oss.air.pm.stats.common.model.constants.DatasourceConstants.PROPERTY_PASSWORD;
import static com.ericsson.oss.air.pm.stats.common.model.constants.DatasourceConstants.PROPERTY_TYPE;
import static com.ericsson.oss.air.pm.stats.common.model.constants.DatasourceConstants.PROPERTY_USER;

import java.util.Map;

import com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings;
import com.ericsson.oss.air.pm.stats.common.model.DatasourceType;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.data.datasource.DatasourceRegistry;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@DefaultEnvironmentSettings
@ExtendWith(MockitoExtension.class)
class DatasourceConfigLoaderTest {
    DatasourceConfigLoader objectUnderTest = new DatasourceConfigLoader();

    @Test
    void shouldPopulate() {

        objectUnderTest.populateDatasourceRegistry();

        final Map<Datasource, JdbcDatasource> allDatasourceRegistry = DatasourceRegistry.getInstance().getAllDatasourceRegistry();
        Assertions.assertThat(allDatasourceRegistry).hasSize(3);
        assertStaticDataSources(allDatasourceRegistry);
    }

    private void assertStaticDataSources(final Map<Datasource, JdbcDatasource> allDatasourceRegistry) {
        final JdbcDatasource kpiDBJdbcDatasource = allDatasourceRegistry.get(Datasource.KPI_DB);
        Assertions.assertThat(kpiDBJdbcDatasource.getJbdcConnection()).isEqualTo(DEFAULT_JDBC_URL);
        assertDataSource(kpiDBJdbcDatasource, DEFAULT_USER, DEFAULT_PASSWORD, DEFAULT_DRIVER, DEFAULT_JDBC_URL,
                         DatasourceType.FACT, "kpi_db");

        final JdbcDatasource kpiInMemoryJdbcDatasource = allDatasourceRegistry.get(Datasource.KPI_IN_MEMORY);
        Assertions.assertThat(kpiInMemoryJdbcDatasource.getJbdcConnection()).isEqualTo(null);
        assertDataSource(kpiInMemoryJdbcDatasource, null, null, null, null, DatasourceType.FACT, "kpi_inmemory");

        final JdbcDatasource tabularParameterJdbcDatasource = allDatasourceRegistry.get(Datasource.TABULAR_PARAMETERS);
        assertDataSource(tabularParameterJdbcDatasource, DEFAULT_USER, DEFAULT_PASSWORD, DEFAULT_DRIVER, DEFAULT_JDBC_URL,
                         DatasourceType.DIM, "tabular_parameters");
    }

    private void assertDataSource(final JdbcDatasource jdbcDatasource, final String user, final String password, final String driver,
                                  final String jdbcConnection, final DatasourceType datasourceType, final String expressionTag) {
        Assertions.assertThat(jdbcDatasource.getJdbcProperties().getProperty(PROPERTY_USER)).isEqualTo(user);
        Assertions.assertThat(jdbcDatasource.getJdbcProperties().getProperty(PROPERTY_PASSWORD)).isEqualTo(password);
        Assertions.assertThat(jdbcDatasource.getJdbcProperties().getProperty(PROPERTY_DRIVER)).isEqualTo(driver);
        Assertions.assertThat(jdbcDatasource.getJdbcProperties().getProperty(PROPERTY_CONNECTION_URL)).isEqualTo(jdbcConnection);
        Assertions.assertThat(jdbcDatasource.getJdbcProperties().getProperty(PROPERTY_TYPE)).isEqualTo(datasourceType.toString());
        Assertions.assertThat(jdbcDatasource.getJdbcProperties().getProperty(PROPERTY_EXPRESSION_TAG)).isEqualTo(expressionTag);
    }
}