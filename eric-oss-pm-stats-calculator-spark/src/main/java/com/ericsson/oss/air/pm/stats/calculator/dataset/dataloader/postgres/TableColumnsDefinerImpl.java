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

import static com.ericsson.oss.air.pm.stats.common.model.collection.Column.AGGREGATION_BEGIN_TIME;

import java.util.Collection;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.model.SingleTableColumns;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.util.KpiNameUtils;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.data.datasource.DatasourceRegistry;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TableColumnsDefinerImpl {
    private final KpiDefinitionHelperImpl kpiDefinitionHelper;
    private final DatasourceRegistry datasourceRegistry;

    private final SparkService sparkService;

    public SingleTableColumns defineTableColumns(@NonNull final Collection<? extends KpiDefinition> kpiDefinitions,
                                                 final Integer aggregationPeriodInMinutes,
                                                 final String alias) {
        final Table table = Table.of(KpiNameUtils.createOutputTableName(alias, aggregationPeriodInMinutes));
        final Set<Column> columns = kpiDefinitionHelper.extractKpiDefinitionNameColumns(kpiDefinitions, alias, aggregationPeriodInMinutes);

        if (!datasourceRegistry.isDimTable(sparkService.getKpiDatabaseDatasource(), table)) {
            columns.add(AGGREGATION_BEGIN_TIME);
            columns.addAll(kpiDefinitionHelper.extractAggregationElementColumns(alias, kpiDefinitions));
        }

        return SingleTableColumns.of(table, columns);
    }

}
