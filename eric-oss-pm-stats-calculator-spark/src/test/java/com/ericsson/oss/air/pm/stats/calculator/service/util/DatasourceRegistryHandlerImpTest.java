/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatabaseProperties;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.common.model.DatasourceType;
import com.ericsson.oss.air.pm.stats.common.model.collection.Database;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.data.datasource.DatasourceRegistry;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatasourceRegistryHandlerImpTest {
    @Captor ArgumentCaptor<JdbcDatasource> jdbcDatasourceArgumentCaptor;

    @Mock SparkService sparkServiceMock;
    @Mock DatasourceRegistry datasourceRegistryMock;
    @Mock KpiDefinitionHelperImpl kpiDefinitionHelperMock;

    @InjectMocks DatasourceRegistryHandlerImp objectUnderTest;

    @Test
    void shouldPopulateDatasourceRegistry() {
        final DatabaseProperties databaseProperties = DatabaseProperties.newInstance();

        final Properties kpiDbProperties = new Properties();
        final Properties properties = new Properties();

        properties.setProperty("expressionTag", "value_expressionTag");
        properties.setProperty("jdbcUrl", "value_jdbcUrl");

        databaseProperties.put(new Database("database"), properties);

        when(sparkServiceMock.getDatabaseProperties()).thenReturn(databaseProperties);
        when(sparkServiceMock.getKpiJdbcConnection()).thenReturn("kpiDbJdbcConnection");
        when(sparkServiceMock.getKpiJdbcProperties()).thenReturn(kpiDbProperties);

        objectUnderTest.populateDatasourceRegistry();

        verify(sparkServiceMock).getDatabaseProperties();

        verify(datasourceRegistryMock).addDatasource(eq(Datasource.of("value_expressionTag")), jdbcDatasourceArgumentCaptor.capture());
        Assertions.assertThat(jdbcDatasourceArgumentCaptor.getValue()).satisfies(jdbcDatasource -> {
            Assertions.assertThat(jdbcDatasource.getJbdcConnection()).isEqualTo("value_jdbcUrl");
            Assertions.assertThat(jdbcDatasource.getJdbcProperties()).isEqualTo(properties);
        });

        verify(datasourceRegistryMock).addDatasource(eq(Datasource.of("kpi_db")), jdbcDatasourceArgumentCaptor.capture());
        Assertions.assertThat(jdbcDatasourceArgumentCaptor.getValue()).satisfies(jdbcDatasource -> {
            Assertions.assertThat(jdbcDatasource.getJbdcConnection()).isEqualTo("kpiDbJdbcConnection");
            Assertions.assertThat(jdbcDatasource.getJdbcProperties()).isEqualTo(kpiDbProperties);
        });
    }

    @Test
    void shouldGroupDatabaseExpressionTagsByType() {
        final DatabaseProperties databaseProperties = DatabaseProperties.newInstance();

        databaseProperties.put(new Database("database1"), createPropertiesForGrouping("FACT", "expressionTag_1"));
        databaseProperties.put(new Database("database2"), createPropertiesForGrouping("FACT", "expressionTag_2"));
        databaseProperties.put(new Database("database3"), createPropertiesForGrouping("DIM", "expressionTag_3"));

        when(sparkServiceMock.getDatabaseProperties()).thenReturn(databaseProperties);

        final Map<DatasourceType, List<Datasource>> actual = objectUnderTest.groupDatabaseExpressionTagsByType();

        verify(sparkServiceMock).getDatabaseProperties();

        final Map<DatasourceType, List<Datasource>> expected = new HashMap<>(2);

        expected.put(DatasourceType.FACT, Arrays.asList(Datasource.of("expressionTag_1"), Datasource.of("expressionTag_2"), Datasource.of("kpi_db")));
        expected.put(DatasourceType.DIM, Collections.singletonList(Datasource.of("expressionTag_3")));

        Assertions.assertThat(actual).containsExactlyInAnyOrderEntriesOf(expected);
    }

    @Nested
    @DisplayName("verify noDataSourceMissing")
    class VerifyNoDataSourceMissing {
        @Test
        void shouldReturnFalse_whenTheProvidedKpiDefinitionsAreEmpty() {
            final boolean actual = objectUnderTest.isNoDataSourceMissing(Collections.emptySet());

            Assertions.assertThat(actual).isFalse();
        }

        @Test
        void shouldReturnFalse_whenADataSourceIsMissing() {
            final Set<KpiDefinition> kpiDefinitions = Sets.newLinkedHashSet(KpiDefinition.builder().build());

            final Datasource dataSource1 = Datasource.of("dataSource1");
            final Datasource dataSource2 = Datasource.of("dataSource2");

            when(kpiDefinitionHelperMock.extractNonInMemoryDataSources(kpiDefinitions)).thenReturn(Sets.newLinkedHashSet(dataSource1, dataSource2));
            when(datasourceRegistryMock.getDataSources()).thenReturn(Sets.newLinkedHashSet(dataSource1));

            final boolean actual = objectUnderTest.isNoDataSourceMissing(kpiDefinitions);

            verify(kpiDefinitionHelperMock).extractNonInMemoryDataSources(kpiDefinitions);
            verify(datasourceRegistryMock).getDataSources();

            Assertions.assertThat(actual).isFalse();
        }

        @Test
        void shouldReturnTrue_whenNoDataSourceIsMissing() {
            final Set<KpiDefinition> kpiDefinitions = Sets.newLinkedHashSet(KpiDefinition.builder().build());

            final Datasource dataSource1 = Datasource.of("dataSource1");
            final Datasource dataSource2 = Datasource.of("dataSource2");

            when(kpiDefinitionHelperMock.extractNonInMemoryDataSources(kpiDefinitions)).thenReturn(Sets.newLinkedHashSet(dataSource1, dataSource2));
            when(datasourceRegistryMock.getDataSources()).thenReturn(Sets.newLinkedHashSet(dataSource1, dataSource2));

            final boolean actual = objectUnderTest.isNoDataSourceMissing(kpiDefinitions);

            verify(kpiDefinitionHelperMock).extractNonInMemoryDataSources(kpiDefinitions);
            verify(datasourceRegistryMock).getDataSources();

            Assertions.assertThat(actual).isTrue();
        }
    }

    private static Properties createPropertiesForGrouping(final String type, final String expressionTag) {
        final Properties properties = new Properties();

        properties.setProperty("type", type);
        properties.setProperty("expressionTag", expressionTag);

        return properties;
    }
}