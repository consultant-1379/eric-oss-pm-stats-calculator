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

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DOT;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants;
import com.ericsson.oss.air.pm.stats.data.datasource.DatasourceRegistry;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SparkConfigurationProvider {
    private static final String SPARK_JDBC_PREFIX = "spark.jdbc.ext.";

    /**
     * Retrieve datasource properties suitable for passing on spark configuration.
     *
     * @return the {@link Map} of datasource properties
     */
    public static Map<String, String> getSparkConfForDataSources() {
        final Map<String, String> confForSpark = new HashMap<>();
        for (final Map.Entry<Datasource, JdbcDatasource> dbProperties : DatasourceRegistry.getInstance().getAllDatasourceRegistry().entrySet()) {
            final String serviceName = dbProperties.getKey().getName().replace(KpiCalculatorConstants.UNDERSCORE, "");
            final String prefix = SPARK_JDBC_PREFIX + serviceName + DOT;
            confForSpark.putAll(dbProperties.getValue().getJdbcProperties().entrySet().stream().collect(
                    Collectors.toMap(objectObjectEntry -> prefix + objectObjectEntry.getKey(),
                            objectObjectEntry -> String.valueOf(objectObjectEntry.getValue()))));
        }
        return confForSpark;
    }
}
