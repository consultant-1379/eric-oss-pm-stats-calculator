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

import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.KPI_SERVICE_DB_DRIVER;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.KPI_SERVICE_DB_JDBC_CONNECTION;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.KPI_SERVICE_DB_PASSWORD;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.KPI_SERVICE_DB_USER;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValueReader.readEnvironmentFor;
import static com.ericsson.oss.air.pm.stats.common.model.constants.DatasourceConstants.PROPERTY_CONNECTION_URL;
import static com.ericsson.oss.air.pm.stats.common.model.constants.DatasourceConstants.PROPERTY_DRIVER;
import static com.ericsson.oss.air.pm.stats.common.model.constants.DatasourceConstants.PROPERTY_EXPRESSION_TAG;
import static com.ericsson.oss.air.pm.stats.common.model.constants.DatasourceConstants.PROPERTY_PASSWORD;
import static com.ericsson.oss.air.pm.stats.common.model.constants.DatasourceConstants.PROPERTY_TYPE;
import static com.ericsson.oss.air.pm.stats.common.model.constants.DatasourceConstants.PROPERTY_USER;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.ericsson.oss.air.pm.stats.common.model.DatasourceType;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.data.datasource.DatasourceRegistry;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class DatasourceConfigLoader {

    public void populateDatasourceRegistry() {
        final Map<String, Properties> databaseProperties = getDataSourceProperties();

        for (final Map.Entry<String, Properties> entry : databaseProperties.entrySet()) {
            DatasourceRegistry.getInstance().addDatasource(
                    Datasource.of(entry.getKey()),
                    new JdbcDatasource((String) (entry.getValue().get(PROPERTY_CONNECTION_URL)), entry.getValue())
            );
        }
    }

    private Map<String, Properties> getDataSourceProperties() {
        final String kpiServiceDbUser = readEnvironmentFor(KPI_SERVICE_DB_USER);
        final String kpiServiceDbPassword = readEnvironmentFor(KPI_SERVICE_DB_PASSWORD);
        final String kpiServiceDbDriver = readEnvironmentFor(KPI_SERVICE_DB_DRIVER);
        final String kpiServiceDbJdbcConnection = readEnvironmentFor(KPI_SERVICE_DB_JDBC_CONNECTION);


        final Map<String, Properties> databaseProperties = new HashMap<>();

        final Properties kpiDSProperties = new Properties();
        String kpiDbName = Datasource.KPI_DB.getName();
        databaseProperties.put(kpiDbName, kpiDSProperties);

        kpiDSProperties.setProperty(PROPERTY_USER, kpiServiceDbUser);
        kpiDSProperties.setProperty(PROPERTY_PASSWORD, kpiServiceDbPassword);
        kpiDSProperties.setProperty(PROPERTY_DRIVER, kpiServiceDbDriver);
        kpiDSProperties.setProperty(PROPERTY_CONNECTION_URL, kpiServiceDbJdbcConnection);
        kpiDSProperties.setProperty(PROPERTY_TYPE, DatasourceType.FACT.name());
        kpiDSProperties.setProperty(PROPERTY_EXPRESSION_TAG, kpiDbName);

        final Properties kpiInMemoryDSProperties = new Properties();
        String kpiInMemoryName = Datasource.KPI_IN_MEMORY.getName();
        databaseProperties.put(kpiInMemoryName, kpiInMemoryDSProperties);
        kpiInMemoryDSProperties.setProperty(PROPERTY_TYPE, DatasourceType.FACT.name());
        kpiInMemoryDSProperties.setProperty(PROPERTY_EXPRESSION_TAG, kpiInMemoryName);

        final Properties tabularParameterDataSourceProperties = new Properties();
        String tabularParameterDataSourceName = Datasource.TABULAR_PARAMETERS.getName();
        databaseProperties.put(tabularParameterDataSourceName, tabularParameterDataSourceProperties);

        tabularParameterDataSourceProperties.setProperty(PROPERTY_USER, kpiServiceDbUser);
        tabularParameterDataSourceProperties.setProperty(PROPERTY_PASSWORD, kpiServiceDbPassword);
        tabularParameterDataSourceProperties.setProperty(PROPERTY_DRIVER, kpiServiceDbDriver);
        tabularParameterDataSourceProperties.setProperty(PROPERTY_CONNECTION_URL, kpiServiceDbJdbcConnection);
        tabularParameterDataSourceProperties.setProperty(PROPERTY_TYPE, DatasourceType.DIM.name());
        tabularParameterDataSourceProperties.setProperty(PROPERTY_EXPRESSION_TAG, tabularParameterDataSourceName);

        return databaseProperties;
    }
}
