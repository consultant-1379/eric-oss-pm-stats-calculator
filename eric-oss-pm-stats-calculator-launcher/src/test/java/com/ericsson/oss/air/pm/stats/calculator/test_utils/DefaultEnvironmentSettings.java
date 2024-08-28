/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.test_utils;

import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_ADAPTIVE_SQL;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_DRIVER;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_DRIVER_MEMORY;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_EXECUTOR_CORES;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_EXECUTOR_MEMORY;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_EXPRESSION_TAG;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_JDBC_URL;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_LOG_LEVEL;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_MASTER_URL;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_MAX_CORES;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_MAX_PARALLELISM;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_MAX_REGISTERED_RESOURCES_WAITING_TIME;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_MIN_REGISTERED_RESOURCES_RATIO;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_ON_DEMAND_EXECUTOR_CORES;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_ON_DEMAND_EXECUTOR_MEMORY;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_OVERRIDE_EXECUTOR_JMX_PORT;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_PASSWORD;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_SAVE_EVENT_LOG;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_SPARK_BLOCKMANAGER_PORT;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_SPARK_DRIVER_BINDADDRESS;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_SPARK_DRIVER_HOST;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_SPARK_DRIVER_PORT;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_SUFFLE_PARTITION;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_TYPE;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_USER;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.ENVIRONMENT_KPI_SERVICE_DATA_SOURCE_TYPE;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.ENVIRONMENT_KPI_SERVICE_DB_DRIVER;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.ENVIRONMENT_KPI_SERVICE_DB_EXPRESSION_TAG;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.ENVIRONMENT_KPI_SERVICE_DB_JDBC_CONNECTION;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.ENVIRONMENT_KPI_SERVICE_DB_PASSWORD;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.ENVIRONMENT_KPI_SERVICE_DB_USER;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.ENVIRONMENT_ON_DEMAND_EXECUTOR_CORES;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.ENVIRONMENT_ON_DEMAND_EXECUTOR_MEMORY;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.ENVIRONMENT_OVERRIDE_EXECUTOR_JMX_PORT;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.ENVIRONMENT_SAVE_EVENT_LOGS;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.ENVIRONMENT_SPARK_ADAPTIVE_SQL;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.ENVIRONMENT_SPARK_BLOCKMANAGER_PORT;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.ENVIRONMENT_SPARK_DRIVER_BINDADDRESS;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.ENVIRONMENT_SPARK_DRIVER_HOST;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.ENVIRONMENT_SPARK_DRIVER_MEMORY;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.ENVIRONMENT_SPARK_DRIVER_PORT;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.ENVIRONMENT_SPARK_EXECUTOR_CORES;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.ENVIRONMENT_SPARK_EXECUTOR_MEMORY;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.ENVIRONMENT_SPARK_LOG_LEVEL;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.ENVIRONMENT_SPARK_MASTER_URL;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.ENVIRONMENT_SPARK_MAX_CORES;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.ENVIRONMENT_SPARK_MAX_REGISTERED_RESOURCES_WAITING_TIME;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.ENVIRONMENT_SPARK_MIN_REGISTERED_RESOURCES_RATIO;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.ENVIRONMENT_SPARK_PARALLELISM;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.ENVIRONMENT_SPARK_SHUFFLE_PARTITIONS;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junitpioneer.jupiter.SetEnvironmentVariable;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SetEnvironmentVariable(key = ENVIRONMENT_OVERRIDE_EXECUTOR_JMX_PORT, value = DEFAULT_OVERRIDE_EXECUTOR_JMX_PORT)
@SetEnvironmentVariable(key = ENVIRONMENT_SPARK_MASTER_URL, value = DEFAULT_MASTER_URL)
@SetEnvironmentVariable(key = ENVIRONMENT_SPARK_LOG_LEVEL, value = DEFAULT_LOG_LEVEL)
@SetEnvironmentVariable(key = ENVIRONMENT_SPARK_DRIVER_MEMORY, value = DEFAULT_DRIVER_MEMORY)
@SetEnvironmentVariable(key = ENVIRONMENT_SPARK_EXECUTOR_MEMORY, value = DEFAULT_EXECUTOR_MEMORY)
@SetEnvironmentVariable(key = ENVIRONMENT_SPARK_EXECUTOR_CORES, value = DEFAULT_EXECUTOR_CORES)
@SetEnvironmentVariable(key = ENVIRONMENT_SPARK_MAX_CORES, value = DEFAULT_MAX_CORES)
@SetEnvironmentVariable(key = ENVIRONMENT_SPARK_PARALLELISM, value = DEFAULT_MAX_PARALLELISM)
@SetEnvironmentVariable(key = ENVIRONMENT_SPARK_SHUFFLE_PARTITIONS, value = DEFAULT_SUFFLE_PARTITION)
@SetEnvironmentVariable(key = ENVIRONMENT_SPARK_ADAPTIVE_SQL, value = DEFAULT_ADAPTIVE_SQL)
@SetEnvironmentVariable(key = ENVIRONMENT_SPARK_DRIVER_HOST, value = DEFAULT_SPARK_DRIVER_HOST)
@SetEnvironmentVariable(key = ENVIRONMENT_SPARK_DRIVER_BINDADDRESS, value = DEFAULT_SPARK_DRIVER_BINDADDRESS)
@SetEnvironmentVariable(key = ENVIRONMENT_SPARK_DRIVER_PORT, value = DEFAULT_SPARK_DRIVER_PORT)
@SetEnvironmentVariable(key = ENVIRONMENT_SPARK_BLOCKMANAGER_PORT, value = DEFAULT_SPARK_BLOCKMANAGER_PORT)
@SetEnvironmentVariable(key = ENVIRONMENT_SPARK_MIN_REGISTERED_RESOURCES_RATIO, value = DEFAULT_MIN_REGISTERED_RESOURCES_RATIO)
@SetEnvironmentVariable(key = ENVIRONMENT_SPARK_MAX_REGISTERED_RESOURCES_WAITING_TIME, value = DEFAULT_MAX_REGISTERED_RESOURCES_WAITING_TIME)
@SetEnvironmentVariable(key = ENVIRONMENT_KPI_SERVICE_DB_USER, value = DEFAULT_USER)
@SetEnvironmentVariable(key = ENVIRONMENT_KPI_SERVICE_DB_PASSWORD, value = DEFAULT_PASSWORD)
@SetEnvironmentVariable(key = ENVIRONMENT_KPI_SERVICE_DB_DRIVER, value = DEFAULT_DRIVER)
@SetEnvironmentVariable(key = ENVIRONMENT_KPI_SERVICE_DATA_SOURCE_TYPE, value = DEFAULT_TYPE)
@SetEnvironmentVariable(key = ENVIRONMENT_KPI_SERVICE_DB_EXPRESSION_TAG, value = DEFAULT_EXPRESSION_TAG)
@SetEnvironmentVariable(key = ENVIRONMENT_KPI_SERVICE_DB_JDBC_CONNECTION, value = DEFAULT_JDBC_URL)
@SetEnvironmentVariable(key = ENVIRONMENT_SAVE_EVENT_LOGS, value = DEFAULT_SAVE_EVENT_LOG)
@SetEnvironmentVariable(key = ENVIRONMENT_ON_DEMAND_EXECUTOR_MEMORY, value = DEFAULT_ON_DEMAND_EXECUTOR_MEMORY)
@SetEnvironmentVariable(key = ENVIRONMENT_ON_DEMAND_EXECUTOR_CORES, value = DEFAULT_ON_DEMAND_EXECUTOR_CORES)
public @interface DefaultEnvironmentSettings {
    String ENVIRONMENT_OVERRIDE_EXECUTOR_JMX_PORT = "OVERRIDE_EXECUTOR_JMX_PORT";
    String ENVIRONMENT_SPARK_MASTER_URL = "SPARK_MASTER_URL";
    String ENVIRONMENT_SPARK_LOG_LEVEL = "SPARK_LOG_LEVEL";
    String ENVIRONMENT_SPARK_DRIVER_MEMORY = "SPARK_DRIVER_MEMORY";
    String ENVIRONMENT_SPARK_EXECUTOR_MEMORY = "SPARK_EXECUTOR_MEMORY";
    String ENVIRONMENT_SPARK_EXECUTOR_CORES = "SPARK_EXECUTOR_CORES";
    String ENVIRONMENT_SPARK_MAX_CORES = "SPARK_MAX_CORES";
    String ENVIRONMENT_SPARK_PARALLELISM = "SPARK_PARALLELISM";
    String ENVIRONMENT_SPARK_SHUFFLE_PARTITIONS = "SPARK_SHUFFLE_PARTITIONS";
    String ENVIRONMENT_SPARK_ADAPTIVE_SQL = "SPARK_ADAPTIVE_SQL";
    String ENVIRONMENT_SPARK_DRIVER_HOST = "SPARK_DRIVER_HOST";
    String ENVIRONMENT_SPARK_DRIVER_BINDADDRESS = "SPARK_DRIVER_BINDADDRESS";
    String ENVIRONMENT_SPARK_DRIVER_PORT = "SPARK_DRIVER_PORT";
    String ENVIRONMENT_SPARK_BLOCKMANAGER_PORT = "SPARK_BLOCKMANAGER_PORT";
    String ENVIRONMENT_SPARK_MIN_REGISTERED_RESOURCES_RATIO = "SPARK_MIN_REGISTERED_RESOURCES_RATIO";
    String ENVIRONMENT_SPARK_MAX_REGISTERED_RESOURCES_WAITING_TIME = "SPARK_MAX_REGISTERED_RESOURCES_WAITING_TIME";
    String ENVIRONMENT_KPI_SERVICE_DB_USER = "KPI_SERVICE_DB_USER";
    String ENVIRONMENT_KPI_SERVICE_DB_PASSWORD = "KPI_SERVICE_DB_PASSWORD";
    String ENVIRONMENT_KPI_SERVICE_DB_DRIVER = "KPI_SERVICE_DB_DRIVER";
    String ENVIRONMENT_KPI_SERVICE_DATA_SOURCE_TYPE = "KPI_SERVICE_DB_TYPE";
    String ENVIRONMENT_KPI_SERVICE_DB_EXPRESSION_TAG = "KPI_SERVICE_DB_EXPRESSION_TAG";
    String ENVIRONMENT_KPI_SERVICE_DB_JDBC_CONNECTION = "KPI_SERVICE_DB_JDBC_CONNECTION";
    String ENVIRONMENT_SAVE_EVENT_LOGS = "SAVE_EVENT_LOGS";
    String ENVIRONMENT_ON_DEMAND_EXECUTOR_MEMORY = "ON_DEMAND_EXECUTOR_MEMORY";
    String ENVIRONMENT_ON_DEMAND_EXECUTOR_CORES = "ON_DEMAND_EXECUTOR_CORES";

    String DEFAULT_OVERRIDE_EXECUTOR_JMX_PORT = "false";
    String DEFAULT_MASTER_URL = "master";
    String DEFAULT_LOG_LEVEL = "INFO";
    String DEFAULT_DRIVER_MEMORY = "driverMemory";
    String DEFAULT_EXECUTOR_CORES = "executorCores";
    String DEFAULT_EXECUTOR_MEMORY = "environmentMemory";
    String DEFAULT_MAX_CORES = "maxCore";
    String DEFAULT_MAX_PARALLELISM = "15";
    String DEFAULT_SUFFLE_PARTITION = "15";
    String DEFAULT_ADAPTIVE_SQL = "true";
    String DEFAULT_SPARK_DRIVER_HOST = "driverHost";
    String DEFAULT_SPARK_DRIVER_BINDADDRESS = "192.168.1.1";
    String DEFAULT_SPARK_DRIVER_PORT = "44444";
    String DEFAULT_SPARK_BLOCKMANAGER_PORT = "55555";
    String DEFAULT_MIN_REGISTERED_RESOURCES_RATIO = "minRegisteredResourcesRatio";
    String DEFAULT_MAX_REGISTERED_RESOURCES_WAITING_TIME = "15s";
    String DEFAULT_USER = "user";
    String DEFAULT_PASSWORD = "password";
    String DEFAULT_DRIVER = "driver";
    String DEFAULT_TYPE = "FACT";
    String DEFAULT_JDBC_URL = "jdbcUrl";
    String DEFAULT_EXPRESSION_TAG = "kpi_db";
    String DEFAULT_SAVE_EVENT_LOG = "true";
    String DEFAULT_ON_DEMAND_EXECUTOR_MEMORY = "defaultOnDemandExecutorMemory";
    String DEFAULT_ON_DEMAND_EXECUTOR_CORES = "defaultOnDemandExecutorCores";
}
