/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres;

import static java.util.Collections.singletonList;
import static org.assertj.core.util.Sets.newLinkedHashSet;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.model.SingleTableColumns;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.data.datasource.DatasourceRegistry;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TableColumnsDefinerImplTest {
    @Mock KpiDefinitionHelperImpl kpiDefinitionHelperMock;
    @Mock DatasourceRegistry datasourceRegistryMock;
    @Mock SparkService sparkServiceMock;

    @InjectMocks
    TableColumnsDefinerImpl objectUnderTest;

    @Nested
    @DisplayName("Testing external table columns")
    class DefineSingleTableColumns {
        @Mock Collection<KpiDefinition> kpiDefinitionsMock;
        @Mock Datasource kpiDatasourceMock;

        @Test
        void shouldDefineExternalFACTTable() {
            final Column kpiDefinitionNameColumn = Column.of("kpiDefinitionNameColumn");
            final String alias = "alias";

            when(kpiDefinitionHelperMock.extractKpiDefinitionNameColumns(kpiDefinitionsMock, alias, 60)).thenReturn(newLinkedHashSet(kpiDefinitionNameColumn));
            when(sparkServiceMock.getKpiDatabaseDatasource()).thenReturn(kpiDatasourceMock);
            when(datasourceRegistryMock.isDimTable(kpiDatasourceMock, Table.of("kpi_alias_60"))).thenReturn(false);
            when(kpiDefinitionHelperMock.extractAggregationElementColumns(
                    alias,
                    kpiDefinitionsMock
            )).thenReturn(singletonList(Column.of("aggregationElementColumn")));

            final SingleTableColumns actual = objectUnderTest.defineTableColumns(kpiDefinitionsMock, 60, alias);

            verify(kpiDefinitionHelperMock).extractKpiDefinitionNameColumns(kpiDefinitionsMock, alias, 60);
            verify(sparkServiceMock).getKpiDatabaseDatasource();
            verify(datasourceRegistryMock).isDimTable(kpiDatasourceMock, Table.of("kpi_alias_60"));
            verify(kpiDefinitionHelperMock).extractAggregationElementColumns(alias, kpiDefinitionsMock);

            Assertions.assertThat(actual.getTable()).isEqualTo(Table.of("kpi_alias_60"));
            Assertions.assertThat(actual.getColumns()).containsExactlyInAnyOrder(
                    kpiDefinitionNameColumn,
                    Column.AGGREGATION_BEGIN_TIME,
                    Column.of("aggregationElementColumn")
            );
        }

        @Test
        void shouldDefineExternalDIMTable() {
            final Column kpiDefinitionNameColumn = Column.of("kpiDefinitionNameColumn");
            final String alias = "alias";

            when(kpiDefinitionHelperMock.extractKpiDefinitionNameColumns(kpiDefinitionsMock, alias, 60)).thenReturn(newLinkedHashSet(kpiDefinitionNameColumn));
            when(sparkServiceMock.getKpiDatabaseDatasource()).thenReturn(kpiDatasourceMock);
            when(datasourceRegistryMock.isDimTable(kpiDatasourceMock, Table.of("kpi_alias_60"))).thenReturn(true);

            final SingleTableColumns actual = objectUnderTest.defineTableColumns(kpiDefinitionsMock, 60, alias);

            verify(kpiDefinitionHelperMock).extractKpiDefinitionNameColumns(kpiDefinitionsMock, alias, 60);
            verify(sparkServiceMock).getKpiDatabaseDatasource();
            verify(datasourceRegistryMock).isDimTable(kpiDatasourceMock, Table.of("kpi_alias_60"));

            Assertions.assertThat(actual.getTable()).isEqualTo(Table.of("kpi_alias_60"));
            Assertions.assertThat(actual.getColumns()).containsExactlyInAnyOrder(kpiDefinitionNameColumn);
        }
    }
}