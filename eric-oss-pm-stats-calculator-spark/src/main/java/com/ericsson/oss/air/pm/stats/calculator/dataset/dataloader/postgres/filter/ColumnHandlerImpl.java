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

import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.TableColumns;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.data.datasource.DatasourceRegistry;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ColumnHandlerImpl {
    private final DatasourceRegistry datasourceRegistry;

    public Set<Column> readColumns(@NonNull final JdbcDatasource datasource, final TableColumns tableColumns, final Table table) {
        return datasourceRegistry.isDimTable(datasource.getDatasource(), table)
                ? tableColumns.dimColumns(table)
                : tableColumns.factColumns(table);
    }
}
