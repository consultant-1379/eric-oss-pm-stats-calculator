/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator;

import static com.ericsson.oss.air.pm.stats.calculator.KpiCalculatorSparkLauncherConfiguration.MAX_SPARK_RETRY_ATTEMPTS;
import static com.ericsson.oss.air.pm.stats.calculator.KpiCalculatorSparkLauncherConfiguration.SPARK_RETRY_DURATION_VALUE_MS;
import static com.ericsson.oss.air.pm.stats.calculator.KpiCalculatorSparkLauncherConfiguration.SPARK_RETRY_MULTIPLIER;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.KPI_SERVICE_DB_DATASOURCE_TYPE;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.KPI_SERVICE_DB_DRIVER;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.KPI_SERVICE_DB_EXPRESSION_TAG;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.KPI_SERVICE_DB_JDBC_CONNECTION;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.KPI_SERVICE_DB_PASSWORD;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.KPI_SERVICE_DB_USER;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.ON_DEMAND_EXECUTOR_CORES;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.ON_DEMAND_EXECUTOR_MEMORY;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.OVERRIDE_EXECUTOR_JMX_PORT;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.SAVE_EVENT_LOGS;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.SPARK_ADAPTIVE_SQL;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.SPARK_BLOCKMANAGER_PORT;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.SPARK_DRIVER_BINDADDRESS;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.SPARK_DRIVER_HOST;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.SPARK_DRIVER_MEMORY;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.SPARK_DRIVER_PORT;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.SPARK_EXECUTOR_CORES;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.SPARK_EXECUTOR_MEMORY;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.SPARK_MASTER_URL;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.SPARK_MAX_CORES;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.SPARK_MAX_REGISTERED_RESOURCES_WAITING_TIME;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.SPARK_MIN_REGISTERED_RESOURCES_RATIO;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.SPARK_PARALLELISM;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.SPARK_SHUFFLE_PARTITIONS;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValueReader.readAsBooleanEnvironmentFor;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValueReader.readEnvironmentFor;
import static com.ericsson.oss.air.pm.stats.common.env.Environment.getEnvironmentValue;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation.CalculationLimit;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation.KpiCalculationJob;
import com.ericsson.oss.air.pm.stats.calculator.configuration.SparkConfigurationProvider;
import com.ericsson.oss.air.pm.stats.common.spark.launcher.AbstractSparkLauncher;
import com.ericsson.oss.air.pm.stats.exception.KpiCalculatorSparkStateException;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkLauncher;

/**
 * Class used to launch a Spark job for calculating KPIs.
 * <p>
 * Uses the class <code>com.ericsson.oss.air.pm.stats.calculator.KpiCalculatorSparkHandler</code> in JAR
 * <code>eric-oss-pm-stats-calculator-spark</code>.
 */
@Slf4j
public class KpiCalculatorSparkLauncher extends AbstractSparkLauncher {

    private static final String KPI_CALCULATOR_JARS_DIRECTORY_PATH = System.getProperty("user.home") + "/ericsson/eric-oss-pm-stats-calculator/kpi-spark/";
    private static final String APPLICATION_ID = "PM Stats Calculator";
    private static final String CALCULATOR_MAIN_CLASS = "com.ericsson.oss.air.pm.stats.calculator.Application";
    private static final String COMMA_SEPARATOR = ",";
    private static final String ZERO = "0";
    private final Map<String, String> sparkConfigMap = new HashMap<>();
    private final KpiCalculationJob kpiCalculationJob;
    private static final AtomicInteger calculationCounter = new AtomicInteger(0);

    public KpiCalculatorSparkLauncher(@NonNull final KpiCalculationJob kpiCalculationJob) {
        this.kpiCalculationJob = kpiCalculationJob;
    }

    /**
     * Launches a Spark job to calculate KPIs for execution group.
     *
     * @throws KpiCalculatorSparkStateException
     *             If the final state of the Spark Application is other that {@link SparkAppHandle.State} FINISHED
     */
    public void launch() throws KpiCalculatorSparkStateException {
        sparkConfigMap.putAll(SparkConfigurationProvider.getSparkConfForDataSources());
        setApplicationHomeDirectory(KPI_CALCULATOR_JARS_DIRECTORY_PATH);
        final SparkAppHandle.State state = startSparkLauncher(APPLICATION_ID + " for execution group : " + kpiCalculationJob.getExecutionGroup());
        if (!SparkAppHandle.State.FINISHED.equals(state)) {
            throw new KpiCalculatorSparkStateException(String.format("KPI calculation was unsuccessful and is in state '%s'", state.toString()));
        }
    }

    @Override
    protected SparkLauncher getSparkLauncher() {
        log.info("Launching KPI Calculation configuration tasks for execution group : {}", kpiCalculationJob.getExecutionGroup());

        final String jmxPort = readAsBooleanEnvironmentFor(OVERRIDE_EXECUTOR_JMX_PORT)
                ? ZERO
                : kpiCalculationJob.getJmxPort();

        log.info("JMX Port for Calculation ID '{}' is '{}' ", kpiCalculationJob.getCalculationId(), jmxPort);

        final String sparkLogLevel = getEnvironmentValue("SPARK_LOG_LEVEL");

        final String driverExtraJavaOptions = String.format(
                " -XX:+UseCompressedOops "
                        + "-Dlogback.configurationFile=file:/usr/local/spark/conf/logback.xml "
                        + "-Dlog.level=%s",
                sparkLogLevel);
        final String executorJmxOptions = String.format(
                " -Dcom.sun.management.jmxremote "
                        + "-Dcom.sun.management.jmxremote.port=%1$s "
                        + "-Dcom.sun.management.jmxremote.rmi.port=%1$s "
                        + "-Dcom.sun.management.jmxremote.ssl=false "
                        + "-Dcom.sun.management.jmxremote.authenticate=false",
                jmxPort);
        final String executorExtraJavaOptions = String.format(
                " -XX:+UseCompressedOops "
                        + "-Dlogback.configurationFile=file:/usr/local/spark/conf/logback.xml -Dlog.level=%s%s",
                sparkLogLevel,
                executorJmxOptions);

        final String requestedKpiNames = String.join(COMMA_SEPARATOR, kpiCalculationJob.getKpiDefinitionNames());

        final String sparkExecutorMemory = readEnvironmentFor(SPARK_EXECUTOR_MEMORY);
        final String sparkExecutorCores = readEnvironmentFor(SPARK_EXECUTOR_CORES);

        final SparkLauncher sparkLauncher = new SparkLauncher()
                .setAppResource(KPI_CALCULATOR_JARS_DIRECTORY_PATH + getAppSource())
                .setMainClass(CALCULATOR_MAIN_CLASS)
                .setMaster(readEnvironmentFor(SPARK_MASTER_URL))
                .setConf(SparkLauncher.DRIVER_EXTRA_JAVA_OPTIONS, driverExtraJavaOptions)
                .setConf(SparkLauncher.EXECUTOR_EXTRA_JAVA_OPTIONS, executorExtraJavaOptions)
                .setConf(SparkLauncher.DRIVER_MEMORY, readEnvironmentFor(SPARK_DRIVER_MEMORY))
                .setConf("spark.cores.max", readEnvironmentFor(SPARK_MAX_CORES))
                .setConf("spark.default.parallelism", readEnvironmentFor(SPARK_PARALLELISM))
                .setConf("spark.sql.shuffle.partitions", readEnvironmentFor(SPARK_SHUFFLE_PARTITIONS))
                .setConf("spark.sql.adaptive.enabled", readEnvironmentFor(SPARK_ADAPTIVE_SQL))
                .setConf("spark.sql.ui.retainedExecutions", ZERO)
                .setConf("spark.ui.enabled", "false")
                .setConf("spark.sql.codegen.aggregate.map.twolevel.enabled", "false")
                .setConf("spark.driver.host", readEnvironmentFor(SPARK_DRIVER_HOST))
                .setConf("spark.driver.bindAddress", readEnvironmentFor(SPARK_DRIVER_BINDADDRESS))
                .setConf("spark.driver.port", readEnvironmentFor(SPARK_DRIVER_PORT))
                .setConf("spark.blockManager.port", readEnvironmentFor(SPARK_BLOCKMANAGER_PORT))
                .setConf("spark.metrics.namespace", "eric-oss-pm-stats-calculator")
                .setConf("spark.scheduler.minRegisteredResourcesRatio", readEnvironmentFor(SPARK_MIN_REGISTERED_RESOURCES_RATIO))
                .setConf("spark.scheduler.maxRegisteredResourcesWaitingTime", readEnvironmentFor(SPARK_MAX_REGISTERED_RESOURCES_WAITING_TIME))
                // KPI service DB JDBC configuration
                .setConf("spark.jdbc.kpi.user", readEnvironmentFor(KPI_SERVICE_DB_USER))
                .setConf("spark.jdbc.kpi.password", readEnvironmentFor(KPI_SERVICE_DB_PASSWORD))
                .setConf("spark.jdbc.kpi.driverClass", readEnvironmentFor(KPI_SERVICE_DB_DRIVER))
                .setConf("spark.jdbc.kpi.type", readEnvironmentFor(KPI_SERVICE_DB_DATASOURCE_TYPE))
                .setConf("spark.jdbc.kpi.expressionTag", readEnvironmentFor(KPI_SERVICE_DB_EXPRESSION_TAG))
                .setConf("spark.jdbc.kpi.jdbcUrl", readEnvironmentFor(KPI_SERVICE_DB_JDBC_CONNECTION))
                // Application Specific Parameters
                .setConf("spark.executionGroup", kpiCalculationJob.getExecutionGroup())
                .setConf("spark.calculationId", String.valueOf(kpiCalculationJob.getCalculationId()))
                .setConf("spark.kpisToCalculate", requestedKpiNames)
                .setConf("spark.eventLog.enabled", readEnvironmentFor(SAVE_EVENT_LOGS))
                .setConf("spark.extraListeners", "com.ericsson.oss.air.pm.stats.calculator.listener.JobExecutionTimeSparkListener")
                .setConf("spark.calculation.counter", String.valueOf(calculationCounter.incrementAndGet()))
                // Native Prometheus setup
                .setConf("spark.ui.prometheus.enabled", "true")
                .setConf("spark.driver.extraClassPath", "/kpiuser/ericsson/eric-oss-pm-stats-calculator/kpi-spark/*")
                .setConf("spark.executor.extraClassPath", "/kpiuser/ericsson/eric-oss-pm-stats-calculator/kpi-spark/*");

        if (kpiCalculationJob.isOnDemand()) {
            sparkLauncher.setConf(SparkLauncher.EXECUTOR_MEMORY, readEnvironmentFor(ON_DEMAND_EXECUTOR_MEMORY));
            sparkLauncher.setConf(SparkLauncher.EXECUTOR_CORES, readEnvironmentFor(ON_DEMAND_EXECUTOR_CORES));
        } else {
            sparkLauncher.setConf(SparkLauncher.EXECUTOR_MEMORY, sparkExecutorMemory);
            sparkLauncher.setConf(SparkLauncher.EXECUTOR_CORES, sparkExecutorCores);
        }

        /* TODO: Split KpiCalculationJob to avoid type checking */
        if (kpiCalculationJob.isComplex()) {
            final CalculationLimit calculationLimit = kpiCalculationJob.getCalculationLimit();
            sparkLauncher.setConf("spark.calculationStartTime", String.valueOf(calculationLimit.calculationStartTime()));
            sparkLauncher.setConf("spark.calculationEndTime", String.valueOf(calculationLimit.calculationEndTime()));
        }

        for (final Map.Entry<String, String> sparkConf : sparkConfigMap.entrySet()) {
            sparkLauncher.setConf(sparkConf.getKey(), sparkConf.getValue());
        }

        return sparkLauncher;
    }

    private String getAppSource() {
        final String jarVersion = getClass().getPackage().getImplementationVersion();
        log.info("Maven version of the JAR files: {}", jarVersion);
        return "eric-oss-pm-stats-calculator-launcher-" + jarVersion + ".jar";
    }

    @Override
    protected Retry getRetry() {
        final RetryConfig config = RetryConfig.<SparkAppHandle.State>custom()
                .retryOnResult(SPARK_JOB_FAILED_STATES::contains)
                .maxAttempts(MAX_SPARK_RETRY_ATTEMPTS)
                .intervalFunction(IntervalFunction
                        .ofExponentialBackoff(Duration.ofMillis(SPARK_RETRY_DURATION_VALUE_MS), SPARK_RETRY_MULTIPLIER))
                .retryExceptions(Throwable.class)
                .build();
        return Retry.of("KpiCalculatorSparkApplicationRetry", config);
    }
}