/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.environment.reader;

import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValueReader.readEnvironmentFor;

import java.util.Objects;

import com.ericsson.oss.air.pm.stats.common.model.DatasourceType;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;

import org.apache.spark.launcher.SparkLauncher;

/**
 * Enum holding environment variable set by the <strong>Helm</strong> deployment.
 * <br>
 * The {@link Enum} name is matching with the environment variable.
 *
 * @implNote <strong>Do not reformat this file.</strong>
 */
public enum EnvironmentValue {
    //  Mandatory - no default environment variables

    /**
     * Environment variable to set <strong>{@link SparkLauncher#EXECUTOR_MEMORY}</strong>.
     */
    SPARK_EXECUTOR_MEMORY { @Override boolean hasDefaultValue() { return false; } },

    /**
     * Environment variable to set <strong>{@link SparkLauncher#EXECUTOR_CORES}</strong>.
     */
    SPARK_EXECUTOR_CORES { @Override boolean hasDefaultValue() { return false; } },

    /**
     * Environment variable to set <strong>{@link SparkLauncher#setMaster(String)}</strong>.
     */
    SPARK_MASTER_URL { @Override boolean hasDefaultValue() { return false; } },

    /**
     * Environment variable to set <strong>{@link SparkLauncher#DRIVER_MEMORY}</strong>.
     */
    SPARK_DRIVER_MEMORY { @Override boolean hasDefaultValue() { return false; } },

    /**
     * Environment variable to set <strong>spark.cores.max</strong>.
     */
    SPARK_MAX_CORES { @Override boolean hasDefaultValue() { return false; } },

    /**
     * Environment variable to set <strong>spark.driver.host</strong>.
     */
    SPARK_DRIVER_HOST { @Override boolean hasDefaultValue() { return false; } },

    /**
     * Environment variable to set <strong>spark.driver.bindAddress</strong>.
     */
    SPARK_DRIVER_BINDADDRESS { @Override boolean hasDefaultValue() { return false; } },

    /**
     * Environment variable to set <strong>spark.jdbc.kpi.user</strong>.
     */
    KPI_SERVICE_DB_USER { @Override boolean hasDefaultValue() { return false; } },

    /**
     * Environment variable to set <strong>spark.jdbc.kpi.driverClass</strong>.
     */
    KPI_SERVICE_DB_DRIVER { @Override boolean hasDefaultValue() { return false; } },

    /**
     * Environment variable to set <strong>spark.jdbc.kpi.jdbcUrl</strong>.
     */
    KPI_SERVICE_DB_JDBC_CONNECTION { @Override boolean hasDefaultValue() { return false; } },

    //  Optional - environment variables with default value

    /**
     * Environment variable to set <strong>spark.jdbc.kpi.password</strong>.
     */
    KPI_SERVICE_DB_PASSWORD {
        @Override boolean hasDefaultValue() { return true; }
        @Override String getDefaultValue() { return ""; }
    },

    /**
     * Environment variable to set <strong>spark.jdbc.kpi.type</strong>.
     */
    KPI_SERVICE_DB_DATASOURCE_TYPE() {
        @Override boolean hasDefaultValue() { return true; }
        @Override String getDefaultValue() { return DatasourceType.FACT.name(); }
    },

    /**
     * Environment variable to set <strong>spark.jdbc.kpi.expressionTag</strong>.
     */
    KPI_SERVICE_DB_EXPRESSION_TAG() {
        @Override boolean hasDefaultValue() { return true; }
        @Override String getDefaultValue() { return Datasource.KPI_DB.getName(); }
    },

    /**
     * Environment variable to set <strong>spark.default.parallelism</strong>.
     */
    SPARK_PARALLELISM() {
        @Override boolean hasDefaultValue() { return true; }
        @Override String getDefaultValue() { return "12"; }
    },

    /**
     * Environment variable to set <strong>spark.sql.shuffle.partitions</strong>.
     */
    SPARK_SHUFFLE_PARTITIONS() {
        @Override boolean hasDefaultValue() { return true; }
        @Override String getDefaultValue() { return "12"; }
    },

    /**
     * Environment variable to set <strong>spark.sql.adaptive.enabled</strong>.
     */
    SPARK_ADAPTIVE_SQL() {
        @Override boolean hasDefaultValue() { return true; }
        @Override String getDefaultValue() { return "true"; }
    },

    /**
     * Environment variable to set <strong>spark.scheduler.minRegisteredResourcesRatio</strong>.
     */
    SPARK_MIN_REGISTERED_RESOURCES_RATIO() {
        @Override boolean hasDefaultValue() { return true; }
        @Override String getDefaultValue() { return "0.0"; }
    },

    /**
     * Environment variable to set <strong>spark.scheduler.maxRegisteredResourcesWaitingTime</strong>.
     */
    SPARK_MAX_REGISTERED_RESOURCES_WAITING_TIME() {
        @Override boolean hasDefaultValue() { return true; }
        @Override String getDefaultValue() { return "30s"; }
    },

    /**
     * Environment variable to set <strong>spark.eventLog.enabled</strong>.
     */
    SAVE_EVENT_LOGS() {
        @Override boolean hasDefaultValue() { return true; }
        @Override String getDefaultValue() { return "false"; }
    },

    /**
     * Environment variable to set <strong>com.sun.management.jmxremote.port</strong> and <strong>com.sun.management.jmxremote.rmi.port</strong>.
     */
    OVERRIDE_EXECUTOR_JMX_PORT() {
        @Override boolean hasDefaultValue() { return true; }
        @Override String getDefaultValue() { return "false"; }
    },

    /**
     * Environment variable to set <strong>spark.driver.port</strong>.
     */
    SPARK_DRIVER_PORT {
        @Override boolean hasDefaultValue() { return true; }
        @Override String getDefaultValue() { return "34444"; }
    },

    /**
     * Environment variable to set <strong>spark.blockManager.port</strong>.
     */
    SPARK_BLOCKMANAGER_PORT {
        @Override boolean hasDefaultValue() { return true; }
        @Override String getDefaultValue() { return "35555"; }
    },

    //  Optional - environment variables with dependency on other environment variable

    /**
     * Environment variable to set <strong>{@link SparkLauncher#EXECUTOR_MEMORY}</strong>.
     */
    ON_DEMAND_EXECUTOR_MEMORY {
        @Override boolean hasDefaultValue() { return true; }
        @Override String getDefaultValue() { return readOtherDependencyOf(SPARK_EXECUTOR_MEMORY); }
    },

    /**
     * Environment variable to set <strong>{@link SparkLauncher#EXECUTOR_CORES}</strong>.
     */
    ON_DEMAND_EXECUTOR_CORES {
        @Override boolean hasDefaultValue() { return true; }
        @Override String getDefaultValue() { return readOtherDependencyOf(SPARK_EXECUTOR_CORES); }
    };

    abstract boolean hasDefaultValue();

    //  Due to 'environment variables with dependency on other environment variable' we have to read it dynamically otherwise JVM would create ENUM
    //  statically when it loads, not waiting for testing frameworks to set Environment variables. Tests would become flaky, would depend on what we
    //  run first.
    String getDefaultValue() {
        throw new UnsupportedOperationException(String.format("'%s' has no default value.", name()));
    }

    String readOtherDependencyOf(final EnvironmentValue parent) {
        return Objects.requireNonNull(readEnvironmentFor(parent),
                                      () -> String.format("'%1$s' depending on '%2$s', but '%2$s' has no default value.",
                                                          name(),
                                                          parent.name()));
    }
}
