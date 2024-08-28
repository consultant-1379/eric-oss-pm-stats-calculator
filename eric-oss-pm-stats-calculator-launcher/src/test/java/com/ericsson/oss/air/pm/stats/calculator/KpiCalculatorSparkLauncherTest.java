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

import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_ADAPTIVE_SQL;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_DRIVER;
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
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_PASSWORD;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_SPARK_BLOCKMANAGER_PORT;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_SPARK_DRIVER_BINDADDRESS;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_SPARK_DRIVER_HOST;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_SPARK_DRIVER_PORT;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_SUFFLE_PARTITION;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_TYPE;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.DEFAULT_USER;
import static com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings.ENVIRONMENT_OVERRIDE_EXECUTOR_JMX_PORT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation.CalculationLimit;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation.KpiCalculationJob;
import com.ericsson.oss.air.pm.stats.calculator.configuration.SparkConfigurationProvider;
import com.ericsson.oss.air.pm.stats.calculator.test_utils.DefaultEnvironmentSettings;
import com.ericsson.oss.air.pm.stats.exception.KpiCalculatorSparkStateException;

import io.github.resilience4j.retry.Retry;
import lombok.SneakyThrows;
import org.apache.spark.launcher.SparkAppHandle.State;
import org.apache.spark.launcher.SparkLauncher;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.mockito.InOrder;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KpiCalculatorSparkLauncherTest {
    static final UUID CALCULATION_ID = UUID.fromString("4ff3d5de-0133-4118-ad10-b425e83de942");
    static final Timestamp TEST_TIME = Timestamp.valueOf(LocalDateTime.of(LocalDate.of(2_022, Month.APRIL, 12), LocalTime.NOON));
    static final String EXECUTION_GROUP_SCHEDULED = "executionGroupScheduled";
    static final String ON_DEMAND = "ON_DEMAND";
    static final String JMX_PORT = "jmxPort";
    static final List<String> KPI_DEFINITION_NAMES = Arrays.asList("def1", "def2");
    static final String ZERO = "0";

    KpiCalculatorSparkLauncher objectUnderTest;

    @BeforeEach
    void setUp() {
        objectUnderTest = getMotherObject(EXECUTION_GROUP_SCHEDULED, KpiType.ON_DEMAND);
    }

    @Test
    void shouldGetRetry() {
        final Retry actual = objectUnderTest.getRetry();

        Assertions.assertThat(actual.getName()).isEqualTo("KpiCalculatorSparkApplicationRetry");
        Assertions.assertThat(actual.getRetryConfig().getMaxAttempts()).isEqualTo(2);

        final Predicate<Throwable> exceptionPredicate = actual.getRetryConfig().getExceptionPredicate();
        Assertions.assertThat(exceptionPredicate).accepts(new Throwable());

        final Predicate<Object> resultPredicate = actual.getRetryConfig().getResultPredicate();
        Assertions.assertThat(resultPredicate)
                  .as("retries").accepts(State.FAILED,
                                         State.KILLED,
                                         State.LOST)
                  .as("does not retry").rejects(State.UNKNOWN,
                                                State.CONNECTED,
                                                State.SUBMITTED,
                                                State.RUNNING,
                                                State.FINISHED);

        final Function<Integer, Long> intervalFunction = Objects.requireNonNull(actual.getRetryConfig().getIntervalFunction());
        Assertions.assertThat(intervalFunction.apply(1)).isEqualTo(60_000);
        Assertions.assertThat(intervalFunction.apply(2)).isEqualTo(90_000L);
        Assertions.assertThat(intervalFunction.apply(3)).isEqualTo(135_000L);
        Assertions.assertThat(intervalFunction.apply(4)).isEqualTo(202_500L);
    }

    @Nested
    @DefaultEnvironmentSettings
    @DisplayName("when get Spark launcher")
    class WhenGetSparkLauncher {
        static final String MAIN_CLASS = "com.ericsson.oss.air.pm.stats.calculator.Application";
        final String JOB_EXECUTION_TIME_SPARK_LISTENER = "com.ericsson.oss.air.pm.stats.calculator.listener.JobExecutionTimeSparkListener";
        static final String APP_RESOURCE_POSTFIX = "eric-oss-pm-stats-calculator-launcher";

        static final String CONFIG_MAP_KEY = "configMapKey";
        static final String CONFIG_MAP_VALUE = "configMapValue";

        @BeforeEach
        void setUp() {
            populateSparkConfigMapField();
        }

        @Nested
        @DisplayName("with set OVERRIDE_EXECUTOR_JMX_PORT")
        class WithSetExecutorJmxPortEnvironmentVariable {
            @Test
            @SetEnvironmentVariable(key = ENVIRONMENT_OVERRIDE_EXECUTOR_JMX_PORT, value = "true")
            void shouldGetSparkLauncher() {
                try (final MockedConstruction<SparkLauncher> sparkLauncherMockedConstruction = getSparkLauncherMockedConstruction()) {
                    Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.getSparkLauncher());

                    Assertions.assertThat(sparkLauncherMockedConstruction.constructed()).first().satisfies(sparkLauncherMock -> {
                        verify(
                                sparkLauncherMock,
                                "0",
                                EXECUTION_GROUP_SCHEDULED,
                                DEFAULT_EXECUTOR_MEMORY,
                                DEFAULT_EXECUTOR_CORES,
                                KpiType.SCHEDULED_SIMPLE
                        );
                    });
                }
            }
        }

        @Nested
        @DisplayName("with failing parameters file creation")
        class WithFailingParametersFileCreation {
            @Test
            void shouldGetSparkLauncher() {
                try (final MockedConstruction<SparkLauncher> sparkLauncherMockedConstruction = getSparkLauncherMockedConstruction()) {
                    Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.getSparkLauncher());

                    Assertions.assertThat(sparkLauncherMockedConstruction.constructed()).first().satisfies(sparkLauncherMock -> {
                        verify(
                                sparkLauncherMock,
                                JMX_PORT,
                                EXECUTION_GROUP_SCHEDULED,
                                DEFAULT_EXECUTOR_MEMORY,
                                DEFAULT_EXECUTOR_CORES,
                                KpiType.SCHEDULED_SIMPLE
                        );
                    });
                }
            }
        }

        @Nested
        @DisplayName("for SCHEDULED calculation")
        class ForScheduledCalculation {

            @BeforeEach
            void setUp() {
                objectUnderTest = getMotherObject(EXECUTION_GROUP_SCHEDULED, KpiType.SCHEDULED_COMPLEX);
                populateSparkConfigMapField();
            }

            @Test
            void shouldGetSparkLauncher() {
                try (final MockedConstruction<SparkLauncher> sparkLauncherMockedConstruction = getSparkLauncherMockedConstruction()) {
                    Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.getSparkLauncher());

                    Assertions.assertThat(sparkLauncherMockedConstruction.constructed()).first().satisfies(sparkLauncherMock -> {
                        verify(
                                sparkLauncherMock,
                                JMX_PORT,
                                EXECUTION_GROUP_SCHEDULED,
                                DEFAULT_EXECUTOR_MEMORY,
                                DEFAULT_EXECUTOR_CORES,
                                KpiType.SCHEDULED_COMPLEX
                        );
                    });
                }
            }
        }

        @Nested
        @DisplayName("for ON_DEMAND calculation")
        class ForOnDemandCalculation {
            @BeforeEach
            void setUp() {
                objectUnderTest = getMotherObject(ON_DEMAND, KpiType.ON_DEMAND);
                populateSparkConfigMapField();
            }

            @Test
            void shouldGetSparkLauncher() {
                try (final MockedConstruction<SparkLauncher> sparkLauncherMockedConstruction = getSparkLauncherMockedConstruction()) {
                    objectUnderTest.getSparkLauncher();

                    Assertions.assertThat(sparkLauncherMockedConstruction.constructed()).first().satisfies(sparkLauncherMock -> {
                        verify(
                                sparkLauncherMock,
                               JMX_PORT,
                               ON_DEMAND,
                               DEFAULT_ON_DEMAND_EXECUTOR_MEMORY,
                               DEFAULT_ON_DEMAND_EXECUTOR_CORES,
                               KpiType.ON_DEMAND
                        );
                    });
                }
            }
        }

        MockedConstruction<SparkLauncher> getSparkLauncherMockedConstruction() {
            return mockConstruction(SparkLauncher.class, (sparkLauncherMock, invocation) -> {
                when(sparkLauncherMock.setAppResource(any())).thenReturn(sparkLauncherMock);
                when(sparkLauncherMock.setMainClass(any())).thenReturn(sparkLauncherMock);
                when(sparkLauncherMock.setMaster(any())).thenReturn(sparkLauncherMock);
                when(sparkLauncherMock.setConf(anyString(), anyString())).thenReturn(sparkLauncherMock);
                when(sparkLauncherMock.addFile(anyString())).thenReturn(sparkLauncherMock);
            });
        }

        void populateSparkConfigMapField() {
            final Map<String, String> sparkConfigMap = readSparkConfigMap();
            sparkConfigMap.put(CONFIG_MAP_KEY, CONFIG_MAP_VALUE);
        }

        @SneakyThrows
        Map<String, String> readSparkConfigMap() {
            final Field sparkConfigMapField = objectUnderTest.getClass().getDeclaredField("sparkConfigMap");
            sparkConfigMapField.setAccessible(true);

            final Map<String, String> sparkConfigMap = (Map<String, String>) sparkConfigMapField.get(objectUnderTest);
            return sparkConfigMap;
        }

        void verify(final SparkLauncher sparkLauncherMock,
                    final String jmxPort,
                    final String executionGroupScheduled,
                    final String defaultExecutorMemory,
                    final String defaultExecutorCores,
                    final KpiType kpiType) {
            final String driverExtraJavaOptions = getDriverExtraJavaOptions();
            final String executorJmxOptions = getExecutorJmxOptions(jmxPort);
            final String executorExtraJavaOptions = getExecutorExtraJavaOptions(executorJmxOptions);

            final InOrder inOrder = inOrder(sparkLauncherMock);
            inOrder.verify(sparkLauncherMock).setAppResource(contains(APP_RESOURCE_POSTFIX));
            inOrder.verify(sparkLauncherMock).setMainClass(MAIN_CLASS);
            inOrder.verify(sparkLauncherMock).setMaster(DEFAULT_MASTER_URL);
            inOrder.verify(sparkLauncherMock).setConf(SparkLauncher.DRIVER_EXTRA_JAVA_OPTIONS, driverExtraJavaOptions);
            inOrder.verify(sparkLauncherMock).setConf(SparkLauncher.EXECUTOR_EXTRA_JAVA_OPTIONS, executorExtraJavaOptions);
            inOrder.verify(sparkLauncherMock).setConf("spark.cores.max", DEFAULT_MAX_CORES);
            inOrder.verify(sparkLauncherMock).setConf("spark.default.parallelism", DEFAULT_MAX_PARALLELISM);
            inOrder.verify(sparkLauncherMock).setConf("spark.sql.shuffle.partitions", DEFAULT_SUFFLE_PARTITION);
            inOrder.verify(sparkLauncherMock).setConf("spark.sql.adaptive.enabled", DEFAULT_ADAPTIVE_SQL);
            inOrder.verify(sparkLauncherMock).setConf("spark.sql.ui.retainedExecutions", ZERO);
            inOrder.verify(sparkLauncherMock).setConf("spark.ui.enabled", "false");
            inOrder.verify(sparkLauncherMock).setConf("spark.driver.host", DEFAULT_SPARK_DRIVER_HOST);
            inOrder.verify(sparkLauncherMock).setConf("spark.driver.bindAddress", DEFAULT_SPARK_DRIVER_BINDADDRESS);
            inOrder.verify(sparkLauncherMock).setConf("spark.driver.port", DEFAULT_SPARK_DRIVER_PORT);
            inOrder.verify(sparkLauncherMock).setConf("spark.blockManager.port", DEFAULT_SPARK_BLOCKMANAGER_PORT);
            inOrder.verify(sparkLauncherMock).setConf("spark.metrics.namespace", "eric-oss-pm-stats-calculator");
            inOrder.verify(sparkLauncherMock).setConf("spark.scheduler.minRegisteredResourcesRatio", DEFAULT_MIN_REGISTERED_RESOURCES_RATIO);
            inOrder.verify(sparkLauncherMock).setConf("spark.scheduler.maxRegisteredResourcesWaitingTime", DEFAULT_MAX_REGISTERED_RESOURCES_WAITING_TIME);
            inOrder.verify(sparkLauncherMock).setConf("spark.jdbc.kpi.user", DEFAULT_USER);
            inOrder.verify(sparkLauncherMock).setConf("spark.jdbc.kpi.password", DEFAULT_PASSWORD);
            inOrder.verify(sparkLauncherMock).setConf("spark.jdbc.kpi.driverClass", DEFAULT_DRIVER);
            inOrder.verify(sparkLauncherMock).setConf("spark.jdbc.kpi.type", DEFAULT_TYPE);
            inOrder.verify(sparkLauncherMock).setConf("spark.jdbc.kpi.expressionTag", DEFAULT_EXPRESSION_TAG);
            inOrder.verify(sparkLauncherMock).setConf("spark.jdbc.kpi.jdbcUrl", DEFAULT_JDBC_URL);
            inOrder.verify(sparkLauncherMock).setConf("spark.executionGroup", executionGroupScheduled);
            inOrder.verify(sparkLauncherMock).setConf("spark.calculationId", CALCULATION_ID.toString());
            inOrder.verify(sparkLauncherMock).setConf("spark.kpisToCalculate", String.join(",", KPI_DEFINITION_NAMES));
            inOrder.verify(sparkLauncherMock).setConf("spark.eventLog.enabled", "true");
            inOrder.verify(sparkLauncherMock).setConf("spark.extraListeners", JOB_EXECUTION_TIME_SPARK_LISTENER);
            inOrder.verify(sparkLauncherMock).setConf("spark.ui.prometheus.enabled", "true");
            inOrder.verify(sparkLauncherMock).setConf(SparkLauncher.EXECUTOR_MEMORY, defaultExecutorMemory);
            inOrder.verify(sparkLauncherMock).setConf(SparkLauncher.EXECUTOR_CORES, defaultExecutorCores);

            if (kpiType == KpiType.SCHEDULED_COMPLEX) {
                inOrder.verify(sparkLauncherMock).setConf("spark.calculationStartTime", String.valueOf(LocalDateTime.MIN));
                inOrder.verify(sparkLauncherMock).setConf("spark.calculationEndTime", String.valueOf(LocalDateTime.MIN));
            }
            inOrder.verify(sparkLauncherMock).setConf(CONFIG_MAP_KEY, CONFIG_MAP_VALUE);
        }

        private String getExecutorExtraJavaOptions(final String executorJmxOptions) {
            return String.format(" -XX:+UseCompressedOops " +
                                 "-Dlogback.configurationFile=file:/usr/local/spark/conf/logback.xml " +
                                 "-Dlog.level=%s%s",
                                 DEFAULT_LOG_LEVEL,
                                 executorJmxOptions);
        }

        private String getExecutorJmxOptions(final String jmxPort) {
            return String.format(" -Dcom.sun.management.jmxremote " +
                                 "-Dcom.sun.management.jmxremote.port=%1$s " +
                                 "-Dcom.sun.management.jmxremote.rmi.port=%1$s " +
                                 "-Dcom.sun.management.jmxremote.ssl=false " +
                                 "-Dcom.sun.management.jmxremote.authenticate=false",
                                 jmxPort);
        }

        private String getDriverExtraJavaOptions() {
            return String.format(" -XX:+UseCompressedOops " +
                                 "-Dlogback.configurationFile=file:/usr/local/spark/conf/logback.xml " +
                                 "-Dlog.level=%s",
                                 DEFAULT_LOG_LEVEL);
        }
    }

    @Nested
    @DisplayName("when launch")
    class WhenLaunch {
        private static final String APPLICATION_ID = "PM Stats Calculator";
        private static final String APPLICATION_HOME_DIRECTORY = "/ericsson/eric-oss-pm-stats-calculator/kpi-spark/";

        @Spy
        private KpiCalculatorSparkLauncher kpiCalculatorSparkLauncherSpy = getMotherObject(EXECUTION_GROUP_SCHEDULED, KpiType.ON_DEMAND);

        private MockedStatic<SparkConfigurationProvider> sparkConfigurationProviderMockedStatic;

        @BeforeEach
        void setUp() {
            sparkConfigurationProviderMockedStatic = mockStatic(SparkConfigurationProvider.class);
            sparkConfigurationProviderMockedStatic.when(SparkConfigurationProvider::getSparkConfForDataSources).thenReturn(Collections.emptyMap());
            doNothing().when(kpiCalculatorSparkLauncherSpy).setApplicationHomeDirectory(anyString());
        }

        @AfterEach
        void tearDown() {
            sparkConfigurationProviderMockedStatic.close();
        }

        @Test
        void thenCalculationFinishes() throws KpiCalculatorSparkStateException {
            doReturn(State.FINISHED).when(kpiCalculatorSparkLauncherSpy).startSparkLauncher(anyString());

            kpiCalculatorSparkLauncherSpy.launch();

            sparkConfigurationProviderMockedStatic.verify(SparkConfigurationProvider::getSparkConfForDataSources);
            verify(kpiCalculatorSparkLauncherSpy).setApplicationHomeDirectory(contains(APPLICATION_HOME_DIRECTORY));
            verify(kpiCalculatorSparkLauncherSpy).startSparkLauncher(getApplicationId());
        }

        private String getApplicationId() {
            return String.format("%s for execution group : %s", APPLICATION_ID, EXECUTION_GROUP_SCHEDULED);
        }

        @Test
        void andCalculationFails_thenKpiCalculatorSparkStateExceptionIsThrown() {
            doReturn(State.FAILED).when(kpiCalculatorSparkLauncherSpy).startSparkLauncher(anyString());

            Assertions.assertThatThrownBy(() -> kpiCalculatorSparkLauncherSpy.launch())
                      .isInstanceOf(KpiCalculatorSparkStateException.class)
                      .hasMessage("KPI calculation was unsuccessful and is in state '%s'", State.FAILED.toString());

            sparkConfigurationProviderMockedStatic.verify(SparkConfigurationProvider::getSparkConfForDataSources);
            verify(kpiCalculatorSparkLauncherSpy).setApplicationHomeDirectory(contains(APPLICATION_HOME_DIRECTORY));
            verify(kpiCalculatorSparkLauncherSpy).startSparkLauncher(getApplicationId());

        }
    }

    static KpiCalculatorSparkLauncher getMotherObject(final String executionGroup, final KpiType jobType) {
        final KpiCalculationJob kpiCalculationJob = KpiCalculationJob.builder()
                .withCalculationId(CALCULATION_ID)
                .withTimeCreated(TEST_TIME)
                .withExecutionGroup(executionGroup)
                .withJobType(jobType)
                .withKpiDefinitionNames(KPI_DEFINITION_NAMES)
                .withJmxPort(JMX_PORT)
                .withCalculationLimit(CalculationLimit.DEFAULT)
                .build();

        return new KpiCalculatorSparkLauncher(kpiCalculationJob);
    }
}