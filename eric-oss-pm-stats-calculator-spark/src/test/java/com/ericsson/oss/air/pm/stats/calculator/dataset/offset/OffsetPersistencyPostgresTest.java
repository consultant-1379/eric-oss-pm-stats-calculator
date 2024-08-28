/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.offset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatasourceTables;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.data.datasource.DatasourceRegistry;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OffsetPersistencyPostgresTest {
    static final Datasource DATASOURCE_1 = Datasource.of("datasource1");
    private static final String EXECUTION_GROUP = "ExecutionGroup";

    @Mock SparkService sparkServiceMock;
    @Mock KpiDefinitionHelperImpl kpiDefinitionHelperMock;
    @Mock DatasourceRegistry datasourceRegistry;
    @Mock SparkSession sparkSession;
    @InjectMocks
    OffsetPersistencyPostgres objectUnderTest;

    @Test
    void shouldSupportNotSimpleDefinitions() {
        final List<KpiDefinition> complexDefinitions = List.of(KpiDefinition.builder().withInpDataIdentifier(DataIdentifier.of(null)).build());

        final boolean actual = objectUnderTest.supports(complexDefinitions);

        assertThat(actual).isTrue();
    }

    @Test
    void shouldNotSupportSimpleDefinitions() {
        final List<KpiDefinition> simpleDefinitions = List.of(KpiDefinition.builder().withInpDataIdentifier(DataIdentifier.of("identifier_1")).build());

        final boolean actual = objectUnderTest.supports(simpleDefinitions);

        assertThat(actual).isFalse();
    }

    @Test
    void shouldCalculateMaxTimestampsForSourceTables(@Mock KpiDefinition definitionMock, @Mock Table tableMock, @Mock Dataset<Row> datasetMock) {
        when(sparkServiceMock.getExecutionGroup()).thenReturn(EXECUTION_GROUP);
        when(sparkSession.sql(any())).thenReturn(datasetMock);
        when(datasetMock.withColumn(anyString(), any())).thenReturn(datasetMock);

        final Map<Integer, List<KpiDefinition>> kpisByStage = new HashMap<>();
        kpisByStage.put(1, List.of(definitionMock));

        when(kpiDefinitionHelperMock.getTablesFromStagedKpis(kpisByStage)).thenReturn(new HashSet<>(Set.of(tableMock)));

        DatasourceTables datasourceTables = DatasourceTables.newInstance();
        datasourceTables.put(DATASOURCE_1, Set.of(tableMock));
        int aggregationPeriodInMinutes = 60;

        objectUnderTest.calculateMaxOffsetsForSourceTables(kpisByStage, datasourceTables, aggregationPeriodInMinutes);

        verify(sparkServiceMock).getExecutionGroup();
        verify(kpiDefinitionHelperMock).getTablesFromStagedKpis(kpisByStage);
        verify(datasetMock, times(3)).withColumn(anyString(), any());
        verify(sparkSession).sql(any());
    }
}
