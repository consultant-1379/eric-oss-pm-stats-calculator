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

import java.util.Arrays;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatabaseProperties;
import com.ericsson.oss.air.pm.stats.calculator.service.util.api.DatabasePropertiesProvider;
import com.ericsson.oss.air.pm.stats.common.model.collection.Database;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.SparkConf;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import scala.Tuple2;

@Slf4j
@Component
@CacheConfig(cacheNames = "database-properties")
public class DatabasePropertiesProviderImpl implements DatabasePropertiesProvider {
    private static final String SPARK_CONF_JDBC_PREFIX = "spark.jdbc.ext.";

    @Override
    @Cacheable
    public DatabaseProperties readDatabaseProperties(@NonNull final SparkConf sparkConf) {
        final DatabaseProperties databaseProperties = DatabaseProperties.newInstance();

        Arrays.stream(sparkConf.getAll())
              .filter(DatabasePropertiesProviderImpl::isExternalDatabaseConfiguration)
              .forEach(property -> {
                  final String[] tokens = getKey(property).substring(SPARK_CONF_JDBC_PREFIX.length()).split("\\.", 2);
                  if (tokens.length == 2) {
                      final Database database = new Database(tokens[0]);
                      final String propertyKey = tokens[1];
                      databaseProperties.computeIfAbsent(database).setProperty(propertyKey, getValue(property));
                  }
              });

        return databaseProperties;
    }

    private static boolean isExternalDatabaseConfiguration(@NonNull final Tuple2<String, String> property) {
        return getKey(property).startsWith(SPARK_CONF_JDBC_PREFIX);
    }

    private String getValue(@NonNull final Tuple2<String, String> property) {
        return property._2;
    }

    private static String getKey(@NonNull final Tuple2<String, String> property) {
        return property._1;
    }
}
