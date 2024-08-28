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

import static com.ericsson.oss.air.pm.stats.calculator.util.FunctionUtils.newCollection;
import static com.ericsson.oss.air.pm.stats.common.model.constants.DatasourceConstants.PROPERTY_CONNECTION_URL;
import static com.ericsson.oss.air.pm.stats.common.model.constants.DatasourceConstants.PROPERTY_EXPRESSION_TAG;
import static com.ericsson.oss.air.pm.stats.common.model.constants.DatasourceConstants.PROPERTY_TYPE;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatabaseProperties;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.common.model.DatasourceType;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.data.datasource.DatasourceRegistry;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatasourceRegistryHandlerImp {
    private final SparkService sparkService;
    private final DatasourceRegistry datasourceRegistry;
    private final KpiDefinitionHelperImpl kpiDefinitionHelper;

    @PostConstruct
    public void populateDatasourceRegistry() {
        final DatabaseProperties databaseProperties = sparkService.getDatabaseProperties();

        databaseProperties.forEach((database, properties) -> {
            final String expressionTag = properties.getProperty(PROPERTY_EXPRESSION_TAG);
            final String jdbcUrl = properties.getProperty(PROPERTY_CONNECTION_URL);

            final JdbcDatasource jdbcDatasource = new JdbcDatasource(jdbcUrl, properties);

            datasourceRegistry.addDatasource(Datasource.of(expressionTag), jdbcDatasource);
        });

        //  Enrich Registry with KPI DB
        datasourceRegistry.addDatasource(
                Datasource.KPI_DB,
                new JdbcDatasource(
                        sparkService.getKpiJdbcConnection(),
                        sparkService.getKpiJdbcProperties()));
    }

    public boolean isNoDataSourceMissing(final Set<? extends KpiDefinition> kpiDefinitions) {
        if (CollectionUtils.isEmpty(kpiDefinitions)) {
            log.warn("The provided KPI Definitions are empty");
            return false;
        }

        final Set<Datasource> requiredDataSources = kpiDefinitionHelper.extractNonInMemoryDataSources(kpiDefinitions);
        final Set<Datasource> registeredDataSources = datasourceRegistry.getDataSources();

        if (registeredDataSources.containsAll(requiredDataSources)) {
            return true;
        }

        log.warn("The following data sources are required but have not been registered '{}'",
                 CollectionUtils.subtract(requiredDataSources, registeredDataSources)
                                .stream()
                                .map(Datasource::getName)
                                .collect(Collectors.joining(",")));

        return false;
    }

    public Map<DatasourceType, List<Datasource>> groupDatabaseExpressionTagsByType() {
        final Map<DatasourceType, List<Datasource>> result = new EnumMap<>(DatasourceType.class);

        sparkService.getDatabaseProperties().forEach((database, properties) -> {
            final String type = Objects.requireNonNull(properties.getProperty(PROPERTY_TYPE).toUpperCase(Locale.ROOT), PROPERTY_TYPE);
            final String expressionTag = Objects.requireNonNull(properties.getProperty(PROPERTY_EXPRESSION_TAG), PROPERTY_EXPRESSION_TAG);

            result.computeIfAbsent(DatasourceType.valueOf(type), newCollection(ArrayList::new)).add(Datasource.of(expressionTag));
        });

        //  Enrich result
        result.computeIfAbsent(DatasourceType.FACT, newCollection(ArrayList::new)).add(Datasource.KPI_DB);

        return result;
    }
}
