/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.configuration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.data.datasource.DatasourceRegistry;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class SparkConfigurationProviderTest {

    @Test
    void shouldGetSparkConfForDataSources() {
        try (final MockedStatic<DatasourceRegistry> datasourceRegistryMockedStatic = mockStatic(DatasourceRegistry.class)) {
            final DatasourceRegistry datasourceRegistryMock = mock(DatasourceRegistry.class);

            datasourceRegistryMockedStatic.when(DatasourceRegistry::getInstance).thenReturn(datasourceRegistryMock);

            final Map<Datasource, JdbcDatasource> stringJdbcDatasourceHashMap = new HashMap<>(2);

            final Properties jdbcProperties1 = new Properties();
            jdbcProperties1.setProperty("prop11", "val11");
            jdbcProperties1.setProperty("prop12", "val12");

            final Properties jdbcProperties2 = new Properties();
            jdbcProperties2.setProperty("prop21", "val21");

            stringJdbcDatasourceHashMap.put(Datasource.of("key_1"), new JdbcDatasource("connection1", jdbcProperties1));
            stringJdbcDatasourceHashMap.put(Datasource.of("key2"), new JdbcDatasource("connection2", jdbcProperties2));

            when(datasourceRegistryMock.getAllDatasourceRegistry()).thenReturn(stringJdbcDatasourceHashMap);

            final Map<String, String> actual = SparkConfigurationProvider.getSparkConfForDataSources();

            datasourceRegistryMockedStatic.verify(DatasourceRegistry::getInstance);
            verify(datasourceRegistryMock).getAllDatasourceRegistry();

            Assertions.assertThat(actual.entrySet()).satisfiesExactlyInAnyOrder(configurationEntry -> {
                Assertions.assertThat(configurationEntry.getKey()).isEqualTo(getConfigurationKey("key1", "prop11"));
                Assertions.assertThat(configurationEntry.getValue()).isEqualTo("val11");
            }, configurationEntry -> {
                Assertions.assertThat(configurationEntry.getKey()).isEqualTo(getConfigurationKey("key1", "prop12"));
                Assertions.assertThat(configurationEntry.getValue()).isEqualTo("val12");
            }, configurationEntry -> {
                Assertions.assertThat(configurationEntry.getKey()).isEqualTo(getConfigurationKey("key2", "prop21"));
                Assertions.assertThat(configurationEntry.getValue()).isEqualTo("val21");
            });
        }
    }

    private String getConfigurationKey(final String key, final String propertyName) {
        return String.format("spark.jdbc.ext.%s.%s", key, propertyName);
    }
}