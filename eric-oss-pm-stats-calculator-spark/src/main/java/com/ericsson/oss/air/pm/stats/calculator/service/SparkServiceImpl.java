/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service;

import static com.ericsson.oss.air.pm.stats.common.model.constants.DatasourceConstants.PROPERTY_DRIVER;
import static com.ericsson.oss.air.pm.stats.common.model.constants.DatasourceConstants.PROPERTY_EXPRESSION_TAG;
import static com.ericsson.oss.air.pm.stats.common.model.constants.DatasourceConstants.PROPERTY_PASSWORD;
import static com.ericsson.oss.air.pm.stats.common.model.constants.DatasourceConstants.PROPERTY_TYPE;
import static com.ericsson.oss.air.pm.stats.common.model.constants.DatasourceConstants.PROPERTY_USER;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.EXECUTION_GROUP_ON_DEMAND_CALCULATION;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.SPARK_ERICSSON_JOB_DESCRIPTION;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.SPARK_JOB_DESCRIPTION;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatabaseProperties;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDataset;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.Calculation;
import com.ericsson.oss.air.pm.stats.calculator.model.exception.SparkPropertyMissingException;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.CalculationRepository;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.api.DatabasePropertiesProvider;
import com.ericsson.oss.air.pm.stats.calculator.service.util.api.ParameterParser;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.AggregationPeriodWindow;
import com.ericsson.oss.air.pm.stats.common.model.collection.Database;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.internal.SQLConf;
import org.springframework.stereotype.Service;

/**
 * Utility class to wrap {@link SparkSession}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SparkServiceImpl implements SparkService {
    private static final String COMMA = ",";

    private final CalculationRepository calculationRepository;

    private final ParameterParser parameterParser;
    private final DatabasePropertiesProvider databasePropertiesProvider;

    private final SparkSession sparkSession;

    @Override
    public UUID getCalculationId() {
        return UUID.fromString(configuration().get("spark.calculationId"));
    }

    @Override
    public List<String> getKpisToCalculate() {
        return Arrays.asList(configuration().get("spark.kpisToCalculate").split(COMMA));
    }

    @Override
    public String getExecutionGroup() {
        return configuration().get("spark.executionGroup");
    }

    @Override
    public String getKpiJdbcConnection() {
        return configuration().get("spark.jdbc.kpi.jdbcUrl");
    }

    @Override
    public Properties getKpiJdbcProperties() {
        final SparkConf configuration = configuration();
        final Properties jdbcProperties = new Properties();

        jdbcProperties.setProperty(PROPERTY_USER, configuration.get("spark.jdbc.kpi.user"));
        jdbcProperties.setProperty(PROPERTY_PASSWORD, configuration.get("spark.jdbc.kpi.password"));
        jdbcProperties.setProperty(PROPERTY_DRIVER, configuration.get("spark.jdbc.kpi.driverClass"));

        //  Enrich Properties
        jdbcProperties.setProperty(PROPERTY_TYPE, configuration.get("spark.jdbc.kpi.type"));
        jdbcProperties.setProperty(PROPERTY_EXPRESSION_TAG, configuration.get("spark.jdbc.kpi.expressionTag"));

        return jdbcProperties;
    }

    @Override
    public Datasource getKpiDatabaseDatasource() {
        return Datasource.KPI_DB;
    }

    @Override
    public JdbcDatasource getKpiJdbcDatasource() {
        return JdbcDatasource.of(getKpiJdbcConnection(), getKpiJdbcProperties());
    }

    @Override
    public Map<String, String> getKpiDefinitionParameters() {
        final String parameters = calculationRepository.forceFetchById(getCalculationId()).getParameters();
        return parameterParser.parseParameters(parameters);
    }

    @Override
    public String getApplicationId() {
        return sparkContext().applicationId();
    }

    @Override
    public LocalDateTime getCalculationEndTime() {
        final String calculationEndTime = configuration().get("spark.calculationEndTime");

        if (Objects.isNull(calculationEndTime)) {
            throw new SparkPropertyMissingException("spark.calculationEndTime attribute is missing");
        }

        return LocalDateTime.parse(calculationEndTime);
    }

    @Override
    public LocalDateTime getCalculationStartTime() {
        final String calculationStartTime = configuration().get("spark.calculationStartTime");

        if (Objects.isNull(calculationStartTime)) {
            throw new SparkPropertyMissingException("spark.calculationStartTime attribute is missing");
        }

        return LocalDateTime.parse(calculationStartTime);
    }

    @Override
    public AggregationPeriodWindow getComplexAggregationPeriodWindow() {
        return AggregationPeriodWindow.of(
                Timestamp.valueOf(getCalculationStartTime()),
                Timestamp.valueOf(getCalculationEndTime())
        );
    }

    @Override
    public DatabaseProperties getDatabaseProperties() {
        return databasePropertiesProvider.readDatabaseProperties(configuration());
    }

    @Override
    public Connection connectionTo(final Database database) throws SQLException {
        return getDatabaseProperties().connectionTo(database);
    }

    @Override
    public void registerJobDescription(final String description) {
        final SQLConf sqlConf = sqlContext().conf();

        sqlConf.setConfString(SPARK_ERICSSON_JOB_DESCRIPTION, description);
        sqlConf.setConfString(SPARK_JOB_DESCRIPTION, description);
    }

    @Override
    public void unregisterJobDescription() {
        final SQLConf sqlConf = sqlContext().conf();

        sqlConf.unsetConf(SPARK_ERICSSON_JOB_DESCRIPTION);
        sqlConf.unsetConf(SPARK_JOB_DESCRIPTION);
    }

    @Override
    public TableDataset cacheView(final String viewName, @NonNull final Dataset<Row> sourceDataset) {
        sourceDataset.cache();
        sparkSession.catalog().dropTempView(viewName);

        sourceDataset.createOrReplaceTempView(viewName);

        return TableDataset.of(Table.of(viewName), sourceDataset);
    }

    @Override
    public void clearCache() {
        sqlContext().clearCache();
    }

    @Override
    public boolean isScheduledSimple() {
        final Calculation calculation =  calculationRepository.forceFetchById(getCalculationId());
        return calculation.getKpiType() == KpiType.SCHEDULED_SIMPLE;
    }

    @Override
    public boolean isScheduledComplex() {
        final Calculation calculation =  calculationRepository.forceFetchById(getCalculationId());
        return calculation.getKpiType() == KpiType.SCHEDULED_COMPLEX;
    }

    @Override
    public DataFrameReader fromKafka() {
        return sparkSession.read().format("kafka");
    }

    @Override
    public boolean isOnDemand() {
        return EXECUTION_GROUP_ON_DEMAND_CALCULATION.equals(getExecutionGroup());
    }

    private SparkConf configuration() {
        return sparkContext().getConf();
    }

    private SQLContext sqlContext() {
        return sparkSession.sqlContext();
    }

    private SparkContext sparkContext() {
        return sparkSession.sparkContext();
    }
}
