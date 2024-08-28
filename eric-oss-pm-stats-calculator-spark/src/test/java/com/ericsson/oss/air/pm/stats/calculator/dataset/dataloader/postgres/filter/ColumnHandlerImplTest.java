/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.TableColumns;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.data.datasource.DatasourceRegistry;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ColumnHandlerImplTest {
    @Mock DatasourceRegistry datasourceRegistryMock;

    @InjectMocks ColumnHandlerImpl objectUnderTest;

    @Nested
    @DisplayName("Testing reading columns")
    class ReadColumns {
        final Table table = Table.of("table");

        @Mock JdbcDatasource jdbcDatasourceMock;
        @Mock Datasource datasourceMock;
        @Mock TableColumns tableColumnsMock;

        @Test
        void shouldReadDimColumns() {
            when(jdbcDatasourceMock.getDatasource()).thenReturn(datasourceMock);
            when(datasourceRegistryMock.isDimTable(datasourceMock, table)).thenReturn(true);

            objectUnderTest.readColumns(jdbcDatasourceMock, tableColumnsMock, table);

            verify(jdbcDatasourceMock).getDatasource();
            verify(datasourceRegistryMock).isDimTable(datasourceMock, table);
            verify(tableColumnsMock).dimColumns(table);
        }

        @Test
        void shouldReadFactColumns() {
            when(jdbcDatasourceMock.getDatasource()).thenReturn(datasourceMock);
            when(datasourceRegistryMock.isDimTable(datasourceMock, table)).thenReturn(false);

            objectUnderTest.readColumns(jdbcDatasourceMock, tableColumnsMock, table);

            verify(jdbcDatasourceMock).getDatasource();
            verify(datasourceRegistryMock).isDimTable(datasourceMock, table);
            verify(tableColumnsMock).factColumns(table);
        }
    }
}