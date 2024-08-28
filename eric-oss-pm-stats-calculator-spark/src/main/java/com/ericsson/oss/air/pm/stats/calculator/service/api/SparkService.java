/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.api;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatabaseProperties;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDataset;
import com.ericsson.oss.air.pm.stats.calculator.service.util.api.DatabasePropertiesProvider;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.AggregationPeriodWindow;
import com.ericsson.oss.air.pm.stats.common.model.collection.Database;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;

import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SparkSession;

/**
 * Utility interface to wrap {@link SparkSession}.
 */
public interface SparkService {
    /**
     * Returns the calculation ID from the {@link SparkSession} - <strong>spark.calculationId</strong>
     *
     * @return the calculation ID.
     */
    UUID getCalculationId();

    /**
     * Returns {@link List} of KPI Definition names to be calculated from the {@link SparkSession} - <strong>spark.kpisToCalculate</strong>
     *
     * @return {@link List} of KPI Definition names to be calculated.
     */
    List<String> getKpisToCalculate();

    /**
     * Returns the execution group to be calculated from the {@link SparkSession} - <strong>spark.executionGroup</strong>
     *
     * @return The execution group to be calculated.
     */
    String getExecutionGroup();

    /**
     * Returns the KPI JDBC connection URL from the {@link SparkSession} - <strong>spark.jdbc.kpi.jdbcUrl</strong>
     *
     * @return The KPI JDBC connection URL,
     */
    String getKpiJdbcConnection();

    /**
     * Returns the KPI JDBC {@link Properties} from the {@link SparkSession}.
     * <br>
     * <ul>
     *     <li><strong>spark.jdbc.kpi.user</strong></li>
     *     <li><strong>spark.jdbc.kpi.password</strong></li>
     *     <li><strong>spark.jdbc.kpi.driverClass</strong></li>
     * </ul>
     * <p>
     * Adds KPI Database enrichment to the returned {@link Properties}.
     *
     * @return The KPI JDBC {@link Properties}.
     */
    Properties getKpiJdbcProperties();

    /**
     * Returns the KPI Database {@link Datasource}.
     *
     * @return the KPI Database {@link Datasource}.
     * @see Datasource#KPI_DB
     */
    Datasource getKpiDatabaseDatasource();

    /**
     * Returns the KPI Database {@link JdbcDatasource}.
     *
     * @return the KPI Database {@link JdbcDatasource}.
     */
    JdbcDatasource getKpiJdbcDatasource();

    /**
     * Reads KPI Definition parameters.
     *
     * @return {@link Map} containing KPI Definition parameter keys and values.
     */
    Map<String, String> getKpiDefinitionParameters();

    /**
     * Returns the application ID from the {@link SparkSession}.
     *
     * @return The application ID.
     */
    String getApplicationId();

    /**
     * Returns calculation end time of <strong>COMPLEX KPI</strong> from the {@link SparkSession}.
     *
     * @return calculation end time.
     */
    LocalDateTime getCalculationEndTime();

    /**
     * Returns calculation start time of <strong>COMPLEX KPI</strong> from the {@link SparkSession}
     *
     * @return calculation start time.
     */
    LocalDateTime getCalculationStartTime();

    /**
     * Combines {@link #getCalculationStartTime()} and {@link #getCalculationEndTime()} parameters in a parameter
     * {@link AggregationPeriodWindow} for <strong>COMPLEX</strong> KPI calculations needed for Postgres offset handling.
     *
     * @return {@link AggregationPeriodWindow} used for <strong>COMPLEX</strong> KPI calculations.
     */
    AggregationPeriodWindow getComplexAggregationPeriodWindow();

    /**
     * Reads database properties from the {@link SparkSession}.
     *
     * @return {@link DatabaseProperties} read form the {@link SparkSession}.
     * @implNote {@link DatabasePropertiesProvider#readDatabaseProperties}
     */
    DatabaseProperties getDatabaseProperties();

    /**
     * Returns a {@link Connection} to the provided {@link Database}
     *
     * @param database
     *         {@link Database} to connect to
     * @return {@link Connection} to the provided {@link Database}
     * @throws SQLException
     *         if connection cannot be established
     */
    Connection connectionTo(Database database) throws SQLException;

    /**
     * Adds job descriptions to the {@link SQLContext}.
     * <br>
     * Description added to:
     * <ul>
     *     <li>{@link KpiCalculatorConstants#SPARK_ERICSSON_JOB_DESCRIPTION}</li>
     *     <li>{@link KpiCalculatorConstants#SPARK_JOB_DESCRIPTION}</li>
     * </ul>
     *
     * @param description
     *         The description to add.
     */
    void registerJobDescription(String description);

    /**
     * Removes a job description from the {@link SQLContext}.
     * <br>
     * Description deleted from:
     * <ul>
     *     <li>{@link KpiCalculatorConstants#SPARK_ERICSSON_JOB_DESCRIPTION}</li>
     *     <li>{@link KpiCalculatorConstants#SPARK_JOB_DESCRIPTION}</li>
     * </ul>
     */
    void unregisterJobDescription();

    /**
     * Caches the provided {@link Dataset} for the view name.
     *
     * @param viewName
     *         the name of the cached {@link Dataset}.
     * @param sourceDataset
     *         {@link Dataset} to cache.
     * @return {@link TableDataset} containing the cached viewName and sourceDataset.
     */
    TableDataset cacheView(String viewName, Dataset<Row> sourceDataset);

    /**
     * Initializes {@link DataFrameReader} to read from <strong>Kafka</strong>.
     *
     * @return {@link DataFrameReader} targeting <strong>Kafka</strong> source.
     */
    DataFrameReader fromKafka();

    /**
     * Checks if the calculation is on demand or not.
     *
     * @return true if the calculation is on demand.
     */
    boolean isOnDemand();

    /**
     * Removes all cached tables from the in-memory cache.
     */
    void clearCache();

    /**
     * Checks if the calculation is schedule simple or not.
     *
     * @return true if the calculation is scheduled simple.
     */
    boolean isScheduledSimple();

    /**
     * Checks if the calculation is schedule complex or not.
     *
     * @return true if the calculation is scheduled complex.
     */
    boolean isScheduledComplex();
}
