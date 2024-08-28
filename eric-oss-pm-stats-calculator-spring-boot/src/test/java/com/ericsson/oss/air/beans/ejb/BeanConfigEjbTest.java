/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.beans.ejb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;

import com.ericsson.oss.air.pm.stats.adapter.KpiDefinitionAdapter;
import com.ericsson.oss.air.pm.stats.bragent.BackupAndRestoreAgent;
import com.ericsson.oss.air.pm.stats.bragent.CalculatorDatabaseBackupImpl;
import com.ericsson.oss.air.pm.stats.bragent.CalculatorDatabaseRestoreImpl;
import com.ericsson.oss.air.pm.stats.bragent.agent.BackupAndRestoreAgentBehavior;
import com.ericsson.oss.air.pm.stats.bragent.agent.BackupAndRestoreQueryExecutor;
import com.ericsson.oss.air.pm.stats.bragent.api.CalculatorDatabaseBackup;
import com.ericsson.oss.air.pm.stats.bragent.api.CalculatorDatabaseRestore;
import com.ericsson.oss.air.pm.stats.bragent.model.RestoreStatus;
import com.ericsson.oss.air.pm.stats.bragent.utils.BackupAndRestoreProcessHandler;
import com.ericsson.oss.air.pm.stats.bragent.utils.BackupHelper;
import com.ericsson.oss.air.pm.stats.cache.SchemaDetailCache;
import com.ericsson.oss.air.pm.stats.calculation.CalculationReliabilityThreshold;
import com.ericsson.oss.air.pm.stats.calculation.KpiCalculatorBean;
import com.ericsson.oss.air.pm.stats.calculation.SimpleReliabilityThresholdCalculator;
import com.ericsson.oss.air.pm.stats.calculation.TabularParameterFacade;
import com.ericsson.oss.air.pm.stats.calculation.limits.CalculationLimitCalculator;
import com.ericsson.oss.air.pm.stats.calculation.limits.ReadinessBoundCalculator;
import com.ericsson.oss.air.pm.stats.calculation.limits.ReadinessBoundCollector;
import com.ericsson.oss.air.pm.stats.calculation.limits.period.AggregationPeriodSupporter;
import com.ericsson.oss.air.pm.stats.calculation.limits.period.creator.DailyAggregationPeriodCreator;
import com.ericsson.oss.air.pm.stats.calculation.limits.period.creator.FifteenAggregationPeriodCreator;
import com.ericsson.oss.air.pm.stats.calculation.limits.period.creator.HourlyAggregationPeriodCreator;
import com.ericsson.oss.air.pm.stats.calculation.readiness.ReadinessWindowCollector;
import com.ericsson.oss.air.pm.stats.calculation.readiness.log.ReadinessLogCollector;
import com.ericsson.oss.air.pm.stats.calculation.readiness.window.ReadinessWindowCalculator;
import com.ericsson.oss.air.pm.stats.calculation.readiness.window.ReadinessWindowPrinter;
import com.ericsson.oss.air.pm.stats.calculation.reliability.threshold.ReliabilityThresholdTemporalAdjuster;
import com.ericsson.oss.air.pm.stats.calculation.start.time.CalculationStartTimeCalculator;
import com.ericsson.oss.air.pm.stats.calculation.start.time.SimpleStartTimeCalculator;
import com.ericsson.oss.air.pm.stats.calculation.start.time.adjuster.StartTimeTemporalAdjuster;
import com.ericsson.oss.air.pm.stats.calculator.configuration.CalculatorProperties;
import com.ericsson.oss.air.pm.stats.calculator.registry.AggregationPeriodCreatorRegistrySpring;
import com.ericsson.oss.air.pm.stats.calculator.registry.ReliabilityThresholdCalculatorRegistrySpring;
import com.ericsson.oss.air.pm.stats.calculator.registry.StartTimeCalculatorRegistrySpring;
import com.ericsson.oss.air.pm.stats.common.metrics.ApiMetricRegistry;
import com.ericsson.oss.air.pm.stats.common.rest.RestExecutor;
import com.ericsson.oss.air.pm.stats.common.scheduler.ActivityRunner;
import com.ericsson.oss.air.pm.stats.common.scheduler.ActivityScheduler;
import com.ericsson.oss.air.pm.stats.common.sqlparser.SqlParserImpl;
import com.ericsson.oss.air.pm.stats.common.sqlparser.SqlProcessorService;
import com.ericsson.oss.air.pm.stats.common.sqlparser.extractor.LogicalPlanExtractor;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.ExpressionCollector;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.LeafCollector;
import com.ericsson.oss.air.pm.stats.configuration.clock.ClockProvider;
import com.ericsson.oss.air.pm.stats.configuration.environment.exception.InvalidValueException;
import com.ericsson.oss.air.pm.stats.configuration.environment.model.EnvironmentValue;
import com.ericsson.oss.air.pm.stats.configuration.rest.RestExecutorProducer;
import com.ericsson.oss.air.pm.stats.configuration.retry.RetryProducer;
import com.ericsson.oss.air.pm.stats.configuration.scheduler.SchedulerProducer;
import com.ericsson.oss.air.pm.stats.configuration.scheduler.SchedulerRunnerProducer;
import com.ericsson.oss.air.pm.stats.flyway.FlywayIntegration;
import com.ericsson.oss.air.pm.stats.graph.DependencyFinder;
import com.ericsson.oss.air.pm.stats.graph.GraphCycleDetector;
import com.ericsson.oss.air.pm.stats.graph.KpiDependencyHelper;
import com.ericsson.oss.air.pm.stats.graph.KpiGroupLoopDetector;
import com.ericsson.oss.air.pm.stats.graph.helper.ExecutionGroupGraphHelper;
import com.ericsson.oss.air.pm.stats.graph.route.RouteHelper;
import com.ericsson.oss.air.pm.stats.repository.CalculationReliabilityRepositoryImpl;
import com.ericsson.oss.air.pm.stats.repository.CalculationRepositoryImpl;
import com.ericsson.oss.air.pm.stats.repository.CollectionRetentionRepositoryImpl;
import com.ericsson.oss.air.pm.stats.repository.ComplexReadinessLogRepositoryImpl;
import com.ericsson.oss.air.pm.stats.repository.DimensionTablesRepositoryImpl;
import com.ericsson.oss.air.pm.stats.repository.ExecutionGroupGenerator;
import com.ericsson.oss.air.pm.stats.repository.ExecutionGroupRepositoryImpl;
import com.ericsson.oss.air.pm.stats.repository.KpiDefinitionRepositoryImpl;
import com.ericsson.oss.air.pm.stats.repository.LatestProcessedOffsetsRepositoryImpl;
import com.ericsson.oss.air.pm.stats.repository.ParameterRepositoryImpl;
import com.ericsson.oss.air.pm.stats.repository.PartitionRepositoryImpl;
import com.ericsson.oss.air.pm.stats.repository.ReadinessLogRepositoryImpl;
import com.ericsson.oss.air.pm.stats.repository.SchemaDetailRepositoryImpl;
import com.ericsson.oss.air.pm.stats.repository.SimpleKpiDependencyCache;
import com.ericsson.oss.air.pm.stats.repository.TableRetentionRepositoryImpl;
import com.ericsson.oss.air.pm.stats.repository.TabularParameterRepositoryImpl;
import com.ericsson.oss.air.pm.stats.repository.api.CalculationReliabilityRepository;
import com.ericsson.oss.air.pm.stats.repository.api.CalculationRepository;
import com.ericsson.oss.air.pm.stats.repository.api.CollectionRetentionRepository;
import com.ericsson.oss.air.pm.stats.repository.api.ComplexReadinessLogRepository;
import com.ericsson.oss.air.pm.stats.repository.api.DimensionTablesRepository;
import com.ericsson.oss.air.pm.stats.repository.api.ExecutionGroupRepository;
import com.ericsson.oss.air.pm.stats.repository.api.KpiDefinitionRepository;
import com.ericsson.oss.air.pm.stats.repository.api.LatestProcessedOffsetsRepository;
import com.ericsson.oss.air.pm.stats.repository.api.ParameterRepository;
import com.ericsson.oss.air.pm.stats.repository.api.PartitionRepository;
import com.ericsson.oss.air.pm.stats.repository.api.ReadinessLogRepository;
import com.ericsson.oss.air.pm.stats.repository.api.SchemaDetailRepository;
import com.ericsson.oss.air.pm.stats.repository.api.TableRetentionRepository;
import com.ericsson.oss.air.pm.stats.repository.api.TabularParameterRepository;
import com.ericsson.oss.air.pm.stats.repository.util.partition.PartitionNameEncoder;
import com.ericsson.oss.air.pm.stats.repository.util.table.SqlAppenderImpl;
import com.ericsson.oss.air.pm.stats.repository.util.table.SqlTableGeneratorImpl;
import com.ericsson.oss.air.pm.stats.repository.util.table.SqlTableModifierImpl;
import com.ericsson.oss.air.pm.stats.repository.util.table.api.SqlAppender;
import com.ericsson.oss.air.pm.stats.repository.util.table.api.SqlTableModifier;
import com.ericsson.oss.air.pm.stats.scheduler.KpiCalculationExecutionController;
import com.ericsson.oss.air.pm.stats.scheduler.KpiCalculationJobScheduler;
import com.ericsson.oss.air.pm.stats.scheduler.KpiCalculationMediatorImpl;
import com.ericsson.oss.air.pm.stats.scheduler.KpiCalculatorSchedulerBean;
import com.ericsson.oss.air.pm.stats.scheduler.activity.KpiCalculatorActivity;
import com.ericsson.oss.air.pm.stats.scheduler.activity.KpiCalculatorRetentionActivity;
import com.ericsson.oss.air.pm.stats.scheduler.activity.KpiComplexSchedulerActivity;
import com.ericsson.oss.air.pm.stats.scheduler.api.KpiCalculationMediator;
import com.ericsson.oss.air.pm.stats.scheduler.execution.order.ComplexExecutionGroupOrderFacade;
import com.ericsson.oss.air.pm.stats.scheduler.execution.order.compute.ComplexExecutionOrderDeterminer;
import com.ericsson.oss.air.pm.stats.scheduler.execution.order.compute.ComplexExecutionOrderFilterer;
import com.ericsson.oss.air.pm.stats.scheduler.factory.CdiJobFactory;
import com.ericsson.oss.air.pm.stats.scheduler.finalize.HangingCalculationFinalizer;
import com.ericsson.oss.air.pm.stats.scheduler.heartbeatmanager.HeartbeatManager;
import com.ericsson.oss.air.pm.stats.scheduler.priority.CalculationPriorityRanker;
import com.ericsson.oss.air.pm.stats.service.CalculationReliabilityServiceImpl;
import com.ericsson.oss.air.pm.stats.service.CalculationServiceImpl;
import com.ericsson.oss.air.pm.stats.service.ComplexReadinessLogServiceImpl;
import com.ericsson.oss.air.pm.stats.service.DatabaseServiceImpl;
import com.ericsson.oss.air.pm.stats.service.DimensionTablesServiceImpl;
import com.ericsson.oss.air.pm.stats.service.KpiDefinitionServiceImpl;
import com.ericsson.oss.air.pm.stats.service.ParameterServiceImpl;
import com.ericsson.oss.air.pm.stats.service.PartitionServiceImpl;
import com.ericsson.oss.air.pm.stats.service.ReadinessLogServiceImpl;
import com.ericsson.oss.air.pm.stats.service.RetentionPeriodServiceImpl;
import com.ericsson.oss.air.pm.stats.service.TabularParameterServiceImpl;
import com.ericsson.oss.air.pm.stats.service.api.CalculationReliabilityService;
import com.ericsson.oss.air.pm.stats.service.api.CalculationService;
import com.ericsson.oss.air.pm.stats.service.api.ComplexReadinessLogService;
import com.ericsson.oss.air.pm.stats.service.api.DatabaseService;
import com.ericsson.oss.air.pm.stats.service.api.DimensionTablesService;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.service.api.KpiExposureService;
import com.ericsson.oss.air.pm.stats.service.api.ParameterService;
import com.ericsson.oss.air.pm.stats.service.api.PartitionService;
import com.ericsson.oss.air.pm.stats.service.api.ReadinessLogService;
import com.ericsson.oss.air.pm.stats.service.api.RetentionPeriodService;
import com.ericsson.oss.air.pm.stats.service.api.TabularParameterService;
import com.ericsson.oss.air.pm.stats.service.exporter.builder.ExecutionReportBuilderFacade;
import com.ericsson.oss.air.pm.stats.service.facade.ComplexReliabilityThresholdFacade;
import com.ericsson.oss.air.pm.stats.service.facade.KafkaOffsetCheckerFacade;
import com.ericsson.oss.air.pm.stats.service.facade.KpiDefinitionDeletionFacade;
import com.ericsson.oss.air.pm.stats.service.facade.PartitionRetentionFacade;
import com.ericsson.oss.air.pm.stats.service.facade.PartitionRetentionManager;
import com.ericsson.oss.air.pm.stats.service.facade.ReadinessLogManagerFacade;
import com.ericsson.oss.air.pm.stats.service.facade.RunningCalculationDetectorFacade;
import com.ericsson.oss.air.pm.stats.service.facade.SchemaRegistryFacade;
import com.ericsson.oss.air.pm.stats.service.helper.CalculationLauncher;
import com.ericsson.oss.air.pm.stats.service.helper.DataCatalogReader;
import com.ericsson.oss.air.pm.stats.service.helper.KafkaReader;
import com.ericsson.oss.air.pm.stats.service.metric.KpiSchedulerMetricRegistry;
import com.ericsson.oss.air.pm.stats.service.metric.SparkMeterService;
import com.ericsson.oss.air.pm.stats.service.startup.KpiComplexSchedulerStartupService;
import com.ericsson.oss.air.pm.stats.service.startup.KpiRetentionStartupService;
import com.ericsson.oss.air.pm.stats.service.startup.health.HealthCheckMonitor;
import com.ericsson.oss.air.pm.stats.service.table.output.OutputTableCreatorImpl;
import com.ericsson.oss.air.pm.stats.service.table.output.OutputTableUpdaterImpl;
import com.ericsson.oss.air.pm.stats.service.util.DefinitionMapper;
import com.ericsson.oss.air.pm.stats.service.util.SchemaSubjectHelper;
import com.ericsson.oss.air.pm.stats.service.validator.AliasValidator;
import com.ericsson.oss.air.pm.stats.service.validator.DataIdentifierValidator;
import com.ericsson.oss.air.pm.stats.service.validator.DependencyValidator;
import com.ericsson.oss.air.pm.stats.service.validator.ExpressionValidator;
import com.ericsson.oss.air.pm.stats.service.validator.KpiDefinitionRequestValidator;
import com.ericsson.oss.air.pm.stats.service.validator.LoopValidator;
import com.ericsson.oss.air.pm.stats.service.validator.OnDemandParameterValidator;
import com.ericsson.oss.air.pm.stats.service.validator.ParameterValidator;
import com.ericsson.oss.air.pm.stats.service.validator.PostgresDataTypeChangeValidator;
import com.ericsson.oss.air.pm.stats.service.validator.SchemaExistenceValidator;
import com.ericsson.oss.air.pm.stats.service.validator.TabularParameterValidator;
import com.ericsson.oss.air.pm.stats.service.validator.helper.ParameterDefinitionHelper;
import com.ericsson.oss.air.pm.stats.service.validator.helper.ParameterValidatorHelper;
import com.ericsson.oss.air.pm.stats.service.validator.helper.TabularParameterHelper;
import com.ericsson.oss.air.pm.stats.service.validator.retention.RetentionPeriodManager;
import com.ericsson.oss.air.pm.stats.service.validator.retention.RetentionPeriodValidator;
import com.ericsson.oss.air.pm.stats.service.validator.schema.SchemaFieldValidator;
import com.ericsson.oss.air.pm.stats.service.validator.sql.SqlDatasourceValidator;
import com.ericsson.oss.air.pm.stats.service.validator.sql.SqlExtractorService;
import com.ericsson.oss.air.pm.stats.service.validator.sql.SqlReferenceValidator;
import com.ericsson.oss.air.pm.stats.service.validator.sql.SqlRelationExtractor;
import com.ericsson.oss.air.pm.stats.service.validator.sql.SqlWhitelistValidator;
import com.ericsson.oss.air.pm.stats.service.validator.sql.database.VirtualDatabaseResolver;
import com.ericsson.oss.air.pm.stats.service.validator.sql.database.VirtualDatabaseService;
import com.ericsson.oss.air.pm.stats.service.validator.sql.provider.SparkSqlParserProvider;
import com.ericsson.oss.air.pm.stats.service.validator.sql.whitelist.SqlValidatorImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.github.resilience4j.retry.Retry;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.spark.sql.execution.SparkSqlParser;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.simpl.PropertySettingJobFactory;

@ExtendWith(MockitoExtension.class)
class BeanConfigEjbTest {
    @Mock CalculatorProperties calculatorPropertiesMock;
    @InjectMocks BeanConfigEjb objectUnderTest;
    @Spy EnvironmentValue<Duration> retentionConfiguredMax = EnvironmentValue.of(Duration.ofDays(15));

    @Test
    void shouldVerifyRegisteredModules() {
        final ObjectMapper objectMapper = objectUnderTest.objectMapper();

        Assertions.assertThat(objectMapper.getRegisteredModuleIds()).contains(
                "jackson-datatype-jsr310"
        );
    }

    @Test
    void getFlywayIntegration() {
        FlywayIntegration actual = objectUnderTest.flywayIntegration();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void maxHeartbeatToWaitToRecalculateSimples() {
        when(calculatorPropertiesMock.getMaxHeartbeatToWaitToRecalculateSimples()).thenReturn(3);

        final EnvironmentValue<Integer> actual = objectUnderTest.maxHeartbeatToWaitToRecalculateSimples();

        Assertions.assertThat(actual.value()).isEqualTo(3);
    }

    @Test
    void maxHeartbeatToWaitToRecalculateSimples_whenHeartBeatIsIncorrect() {
        when(calculatorPropertiesMock.getMaxHeartbeatToWaitToRecalculateSimples()).thenReturn(-1);

        Assertions.assertThatThrownBy(() -> objectUnderTest.maxHeartbeatToWaitToRecalculateSimples())
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("'MAX_HEARTBEAT_TO_WAIT_TO_RECALCULATE_SIMPLES' has to be positive or zero, but was '-1'");
    }

    @Test
    void retentionPeriodDays_valid() {
        when(calculatorPropertiesMock.getRetentionPeriodDays()).thenReturn(1);

        final EnvironmentValue<Duration> actual = objectUnderTest.retentionPeriodDays(retentionConfiguredMax);

        Assertions.assertThat(actual.value()).hasDays(1);
    }

    @Test
    void retentionPeriodDays_invalid() {
        when(calculatorPropertiesMock.getRetentionPeriodDays()).thenReturn(31);

        Assertions.assertThatThrownBy(() -> objectUnderTest.retentionPeriodDays(retentionConfiguredMax))
                .isInstanceOf(InvalidValueException.class).hasMessage(
                        "'RETENTION_PERIOD_DAYS' has to have value between [1..%d], but was '31'",
                        retentionConfiguredMax.value().toDays()
                );
    }

    @Test
    void retentionPeriodConfiguredMax_valid() {
        when(calculatorPropertiesMock.getRetentionConfiguredMax()).thenReturn(10);

        final EnvironmentValue<Duration> actual = objectUnderTest.retentionPeriodConfiguredMax();

        Assertions.assertThat(actual.value()).hasDays(10);
    }

    @Test
    void retentionPeriodConfiguredMax_invalid() {
        when(calculatorPropertiesMock.getRetentionConfiguredMax()).thenReturn(0);

        Assertions.assertThatThrownBy(() -> objectUnderTest.retentionPeriodConfiguredMax())
                .isInstanceOf(InvalidValueException.class).hasMessage(
                        "'RETENTION_PERIOD_CONFIGURED_MAX' has to have value greater than zero, but was '0'",
                        retentionConfiguredMax.value().toDays()
                );
    }

    @Test
    void queueWeightOnDemandCalculation() {
        when(calculatorPropertiesMock.getQueueWeightOnDemandCalculation()).thenReturn(3);

        final EnvironmentValue<Integer> actual = objectUnderTest.queueWeightOnDemandCalculation();

        Assertions.assertThat(actual.value()).isEqualTo(3);
    }

    @Test
    void queueWeightScheduledCalculation() {
        when(calculatorPropertiesMock.getQueueWeightScheduledCalculation()).thenReturn(1);

        final EnvironmentValue<Integer> actual = objectUnderTest.queueWeightScheduledCalculation();

        Assertions.assertThat(actual.value()).isEqualTo(1);
    }

    @Test
    void cronRetentionPeriodCheck() {
        when(calculatorPropertiesMock.getCronRetentionPeriodCheck()).thenReturn("0 1 0 1/1 * ? *");

        final EnvironmentValue<String> actual = objectUnderTest.cronRetentionPeriodCheck();

        Assertions.assertThat(actual.value()).isEqualTo("0 1 0 1/1 * ? *");
    }

    @Test
    void maximumConcurrentCalculations() {
        when(calculatorPropertiesMock.getMaximumConcurrentCalculations()).thenReturn(2);

        final EnvironmentValue<Integer> actual = objectUnderTest.maximumConcurrentCalculations();

        Assertions.assertThat(actual.value()).isEqualTo(2);
    }

    @Test
    void sparkExecutorStartingPort() {
        when(calculatorPropertiesMock.getSparkExecutorStartingPort()).thenReturn(10010);

        final EnvironmentValue<Integer> actual = objectUnderTest.sparkExecutorStartingPort();

        Assertions.assertThat(actual.value()).isEqualTo(10010);
    }

    @Test
    void shouldReturnRetryForUpdateRunningCalculationsToLostRetry() {
        final Retry actual = objectUnderTest.updateRunningCalculationsToLostRetry();

        Assertions.assertThat(actual.getName()).isEqualTo("handleLostStatesRetry");
        Assertions.assertThat(actual.getRetryConfig().getMaxAttempts()).isEqualTo(10);
        Assertions.assertThat(actual.getRetryConfig().getIntervalBiFunction().apply(0, null)).isEqualTo(Duration.ofSeconds(6).toMillis());
        Assertions.assertThat(actual.getRetryConfig())
                .extracting("retryExceptions", InstanceOfAssertFactories.array(Class[].class))
                .containsExactly(Throwable.class);
    }

    @Test
    void shouldReturnRetryForUpdateCalculationsStateRetry() {
        final Retry actual = objectUnderTest.updateCalculationStateRetry();

        Assertions.assertThat(actual.getName()).isEqualTo("handleCalculationStateRetry");
        Assertions.assertThat(actual.getRetryConfig().getMaxAttempts()).isEqualTo(6);
        Assertions.assertThat(actual.getRetryConfig().getIntervalBiFunction().apply(0, null)).isEqualTo(Duration.ofSeconds(5).toMillis());
    }


    @Test
    void shouldProduceActivityRunner() {
        final ActivityRunner actual = objectUnderTest.provideActivityRunner();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void shouldProvideScheduler() throws Exception {
        final ActivityScheduler actual = objectUnderTest.provideScheduler(mock(CdiJobFactory.class));

        // TODO: That was the end of the original test
        Assertions.assertThat(actual).isNotNull();
        // TODO: Because it's a singleton, keeping a mock ruins other tests as a side effect.
        actual.setJobFactory(new PropertySettingJobFactory());
    }

    @Test
    void shouldProvideSqlParser() {
        final SparkSqlParser actual = objectUnderTest.provideSqlParser();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getRestExecutor() {
        final RestExecutor actual = objectUnderTest.restExecutor();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getSchemaRegistryClient() {
        try (MockedConstruction<CachedSchemaRegistryClient> schemaRegistryClientMock = mockConstruction(CachedSchemaRegistryClient.class)) {
            SchemaRegistryClient actual = objectUnderTest.schemaRegistryClient();

            Assertions.assertThat(schemaRegistryClientMock.constructed()).first().satisfies(constructed -> assertThat(constructed).isEqualTo(actual));
        }
    }

    @Test
    void getCalculatorDatabaseBackupImpl(@Mock BackupHelper backupHelper, @Mock BackupAndRestoreProcessHandler backupAndRestoreProcessHandler) {
        CalculatorDatabaseBackupImpl actual = objectUnderTest.calculatorDatabaseBackup(backupHelper, backupAndRestoreProcessHandler);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getCalculatorDatabaseRestoreImpl(
            @Mock BackupAndRestoreQueryExecutor backupAndRestoreQueryExecutor,
            @Mock BackupAndRestoreProcessHandler backupAndRestoreProcessHandler,
            @Mock KpiExposureService kpiExposureService,
            @Mock CalculationService calculationService,
            @Mock PartitionRetentionFacade partitionRetentionFacade,
            @Mock RestoreStatus restoreStatus,
            @Mock Retry updateCalculationStateRetry) {
        CalculatorDatabaseRestoreImpl actual = objectUnderTest.calculatorDatabaseRestore(
                backupAndRestoreQueryExecutor, backupAndRestoreProcessHandler, kpiExposureService,
                calculationService, partitionRetentionFacade, restoreStatus ,updateCalculationStateRetry);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getKpiDefinitionAdapter(@Mock SchemaDetailCache schemaDetailCache) {
        KpiDefinitionAdapter actual = objectUnderTest.kpiDefinitionAdapter(schemaDetailCache);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getBackupAndRestoreQueryExecutor(@Mock BackupAndRestoreProcessHandler backupAndRestoreProcessHandler) {
        BackupAndRestoreQueryExecutor actual = objectUnderTest.backupAndRestoreQueryExecutor(backupAndRestoreProcessHandler);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getBackupAndRestoreAgentBehavior(@Mock CalculatorDatabaseBackup calculatorDatabaseBackup, @Mock CalculatorDatabaseRestore calculatorDatabaseRestore) {
        BackupAndRestoreAgentBehavior actual = objectUnderTest.backupAndRestoreAgentBehavior(calculatorDatabaseBackup, calculatorDatabaseRestore);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getBackupAndRestoreProcessHandler() {
        BackupAndRestoreProcessHandler actual = objectUnderTest.backupAndRestoreProcessHandler();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getBackupHelper(@Mock BackupAndRestoreProcessHandler backupAndRestoreProcessHandler) {
        BackupHelper actual = objectUnderTest.backupHelper(backupAndRestoreProcessHandler);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getSchemaDetailCache() {
        SchemaDetailCache actual = objectUnderTest.schemaDetailCache();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getCalculationReliabilityThreshold(@Mock CalculationReliabilityService calculationReliabilityService) {
        CalculationReliabilityThreshold actual = objectUnderTest.calculationReliabilityThreshold(calculationReliabilityService);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getReadinessLogRepositoryImpl() {
        ReadinessLogRepositoryImpl actual = objectUnderTest.readinessLogRepositoryImpl();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getSimpleReliabilityThresholdCalculator(
            @Mock ReadinessLogRepository readinessLogRepository,
            @Mock CalculationRepository calculationRepository,
            @Mock KpiDefinitionRepository kpiDefinitionRepository,
            @Mock ReliabilityThresholdTemporalAdjuster reliabilityThresholdTemporalAdjuster) {
        SimpleReliabilityThresholdCalculator actual = objectUnderTest.simpleReliabilityThresholdCalculator(
                readinessLogRepository, calculationRepository, kpiDefinitionRepository, reliabilityThresholdTemporalAdjuster);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getTabularParameterFacade(@Mock DatabaseService databaseService, @Mock DimensionTablesService dimensionTablesService) {
        TabularParameterFacade actual = objectUnderTest.tabularParameterFacade(databaseService, dimensionTablesService);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getCalculationLimitCalculator(
            @Mock KpiDefinitionService kpiDefinitionService,
            @Mock ReadinessBoundCollector readinessBoundCollector,
            @Mock CalculationRepository calculationRepository) {
        CalculationLimitCalculator actual = objectUnderTest.calculationLimitCalculator(kpiDefinitionService, readinessBoundCollector, calculationRepository);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getReadinessBoundCalculator(
            @Mock AggregationPeriodSupporter aggregationPeriodSupporter,
            @Mock KpiDefinitionService kpiDefinitionService,
            @Mock ReadinessBoundCollector readinessBoundCollector,
            @Mock CalculationReliabilityService calculationReliabilityService) {
        ReadinessBoundCalculator actual = objectUnderTest.readinessBoundCalculator(
                aggregationPeriodSupporter, kpiDefinitionService, readinessBoundCollector, calculationReliabilityService);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getReadinessBoundCollector(@Mock ReadinessWindowCollector readinessWindowCollector) {
        ReadinessBoundCollector actual = objectUnderTest.readinessBoundCollector(readinessWindowCollector);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getAggregationPeriodSupporter(@Mock AggregationPeriodCreatorRegistrySpring aggregationPeriodCreatorRegistrySpring) {
        AggregationPeriodSupporter actual = objectUnderTest.aggregationPeriodSupporter(aggregationPeriodCreatorRegistrySpring);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getDailyAggregationPeriodCreator() {
        DailyAggregationPeriodCreator actual = objectUnderTest.dailyAggregationPeriodCreator();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getFifteenAggregationPeriodCreator() {
        FifteenAggregationPeriodCreator actual = objectUnderTest.fifteenAggregationPeriodCreator();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getHourlyAggregationPeriodCreator() {
        HourlyAggregationPeriodCreator actual = objectUnderTest.hourlyAggregationPeriodCreator();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getReadinessWindowCollector(
            @Mock ReadinessWindowCalculator readinessWindowCalculator,
            @Mock ReadinessLogCollector readinessLogCollector,
            @Mock SimpleKpiDependencyCache simpleKpiDependencyCache) {
        ReadinessWindowCollector actual = objectUnderTest.readinessWindowCollector(readinessWindowCalculator, readinessLogCollector, simpleKpiDependencyCache);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getReadinessLogCollector(@Mock ReadinessLogService readinessLogService) {
        ReadinessLogCollector actual = objectUnderTest.readinessLogCollector(readinessLogService);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getReadinessWindowCalculator() {
        ReadinessWindowCalculator actual = objectUnderTest.readinessWindowCalculator();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getReadinessWindowPrinter() {
        ReadinessWindowPrinter actual = objectUnderTest.readinessWindowPrinter();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getReliabilityThresholdTemporalAdjuster() {
        ReliabilityThresholdTemporalAdjuster actual = objectUnderTest.reliabilityThresholdTemporalAdjuster();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getCalculationStartTimeCalculator(@Mock CalculationReliabilityService calculationReliabilityService) {
        CalculationStartTimeCalculator actual = objectUnderTest.calculationStartTimeCalculator(calculationReliabilityService);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getSimpleStartTimeCalculator(
            @Mock ReadinessLogRepository readinessLogRepository,
            @Mock CalculationRepository calculationRepository,
            @Mock KpiDefinitionRepository kpiDefinitionRepository,
            @Mock StartTimeTemporalAdjuster startTimeTemporalAdjuster) {
        SimpleStartTimeCalculator actual = objectUnderTest.simpleStartTimeCalculator(
                readinessLogRepository, calculationRepository, kpiDefinitionRepository, startTimeTemporalAdjuster);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getStartTimeTemporalAdjuster() {
        StartTimeTemporalAdjuster actual = objectUnderTest.startTimeTemporalAdjuster();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getDependencyFinder(@Mock KpiDependencyHelper kpiDependencyHelper, @Mock KpiDefinitionService kpiDefinitionService) {
        DependencyFinder actual = objectUnderTest.dependencyFinder(kpiDependencyHelper, kpiDefinitionService);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getGraphCycleDetector() {
        GraphCycleDetector actual = objectUnderTest.graphCycleDetector();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getKpiDependencyHelper() {
        KpiDependencyHelper actual = objectUnderTest.kpiDependencyHelper();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getKpiGroupLoopDetector(@Mock GraphCycleDetector graphCycleDetector, @Mock RouteHelper routeHelper) {
        KpiGroupLoopDetector actual = objectUnderTest.kpiGroupLoopDetector(graphCycleDetector, routeHelper);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getExecutionGroupGraphHelper(@Mock KpiDependencyHelper kpiDependencyHelper) {
        ExecutionGroupGraphHelper actual = objectUnderTest.executionGroupGraphHelper(kpiDependencyHelper);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getRouteHelper() {
        RouteHelper actual = objectUnderTest.routeHelper();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getComplexReadinessLogRepositoryImpl() {
        ComplexReadinessLogRepositoryImpl actual = objectUnderTest.complexReadinessLogRepository();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getPartitionNameEncoder() {
        PartitionNameEncoder actual = objectUnderTest.partitionNameEncoder();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getSqlAppenderImpl() {
        SqlAppenderImpl actual = objectUnderTest.sqlAppenderImpl();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getSqlTableGeneratorImpl(@Mock SqlAppender sqlAppender) {
        SqlTableGeneratorImpl actual = objectUnderTest.sqlTableGenerator(sqlAppender);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getSqlTableModifierImpl() {
        SqlTableModifierImpl actual = objectUnderTest.sqlTableModifier();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getKpiCalculationJobScheduler(
            @Mock CalculationLimitCalculator calculationLimitCalculator,
            @Mock ComplexExecutionGroupOrderFacade complexExecutionGroupOrderFacade,
            @Mock KpiCalculationExecutionController kpiCalculationExecutionController,
            @Mock KpiDefinitionService kpiDefinitionService,
            @Mock HeartbeatManager heartbeatManager,
            @Mock KafkaOffsetCheckerFacade kafkaOffsetCheckerFacade,
            @Mock EnvironmentValue<Integer> maxHeartbeatToWaitToRecalculateSimples) {
        KpiCalculationJobScheduler actual = objectUnderTest.kpiCalculationJobScheduler(
                calculationLimitCalculator, complexExecutionGroupOrderFacade, kpiCalculationExecutionController,
                kpiDefinitionService, heartbeatManager, kafkaOffsetCheckerFacade, maxHeartbeatToWaitToRecalculateSimples);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getKpiCalculatorActivity(@Mock KpiCalculationJobScheduler calculationJobScheduler, @Mock CalculationPriorityRanker calculationPriorityRanker) {
        KpiCalculatorActivity actual = objectUnderTest.kpiCalculatorActivity(calculationJobScheduler, calculationPriorityRanker);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getKpiCalculatorRetentionActivity(
            @Mock PartitionRetentionFacade partitionRetentionFacade,
            @Mock CalculationService calculationService,
            @Mock PartitionRetentionManager partitionRetentionManager) {
        KpiCalculatorRetentionActivity actual = objectUnderTest.kpiCalculatorRetentionActivity(partitionRetentionFacade, calculationService, partitionRetentionManager);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getKpiComplexSchedulerActivity(
            @Mock KpiCalculationExecutionController kpiCalculationExecutionController,
            @Mock RunningCalculationDetectorFacade runningCalculationDetector) {
        KpiComplexSchedulerActivity actual = objectUnderTest.kpiComplexSchedulerActivity(kpiCalculationExecutionController, runningCalculationDetector);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getComplexExecutionGroupOrderFacade(
            @Mock ComplexExecutionOrderDeterminer complexExecutionOrderDeterminer,
            @Mock ComplexExecutionOrderFilterer complexExecutionOrderFilterer,
            @Mock ExecutionGroupGraphHelper executionGroupGraphHelper,
            @Mock KpiDefinitionService kpiDefinitionService) {
        ComplexExecutionGroupOrderFacade actual = objectUnderTest.complexExecutionGroupOrderFacade(
                complexExecutionOrderDeterminer, complexExecutionOrderFilterer, executionGroupGraphHelper, kpiDefinitionService);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getComplexExecutionOrderDeterminer() {
        ComplexExecutionOrderDeterminer actual = objectUnderTest.complexExecutionOrderDeterminer();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getComplexExecutionOrderFilterer(@Mock ReadinessBoundCalculator readinessBoundCalculator) {
        ComplexExecutionOrderFilterer actual = objectUnderTest.complexExecutionOrderFilterer(readinessBoundCalculator);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getHangingCalculationFinalizer(@Mock CalculationService calculationService) {
        HangingCalculationFinalizer actual = objectUnderTest.hangingCalculationFinalizer(calculationService);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getHeartbeatManager(@Mock EnvironmentValue<Integer> maxHeartbeatToWaitToRecalculateSimples) {
        HeartbeatManager actual = objectUnderTest.heartbeatManager(maxHeartbeatToWaitToRecalculateSimples);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getCalculationReliabilityServiceImpl(@Mock CalculationReliabilityRepository calculationReliabilityRepository) {
        CalculationReliabilityServiceImpl actual = objectUnderTest.calculationReliabilityService(calculationReliabilityRepository);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getExecutionReportBuilderFacade(
            @Mock CalculationService calculationService,
            @Mock KpiDefinitionService kpiDefinitionService,
            @Mock DatabaseService databaseService,
            @Mock ReliabilityThresholdCalculatorRegistrySpring reliabilityThresholdCalculatorRegistrySpring,
            @Mock StartTimeCalculatorRegistrySpring startTimeCalculatorRegistrySpring) {
        ExecutionReportBuilderFacade actual = objectUnderTest.executionReportBuilderFacade(
                calculationService, kpiDefinitionService, databaseService, reliabilityThresholdCalculatorRegistrySpring, startTimeCalculatorRegistrySpring);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getComplexReliabilityThresholdFacade(
            @Mock CalculationReliabilityService calculationReliabilityService,
            @Mock ReadinessBoundCollector readinessBoundCollector,
            @Mock KpiDefinitionService kpiDefinitionService,
            @Mock CalculationService calculationService,
            @Mock AggregationPeriodCreatorRegistrySpring aggregationPeriodCreatorRegistrySpring) {
        ComplexReliabilityThresholdFacade actual = objectUnderTest.complexReliabilityThresholdFacade(
                calculationReliabilityService, readinessBoundCollector, kpiDefinitionService, calculationService, aggregationPeriodCreatorRegistrySpring);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getKafkaOffsetCheckerFacade(@Mock KafkaReader kafkaReader, @Mock LatestProcessedOffsetsRepository latestProcessedOffsetsRepository) {
        KafkaOffsetCheckerFacade actual = objectUnderTest.kafkaOffsetCheckerFacade(kafkaReader, latestProcessedOffsetsRepository);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getKpiDefinitionDeletionFacade(
            @Mock KpiDefinitionService kpiDefinitionService,
            @Mock DatabaseServiceImpl databaseService,
            @Mock KpiExposureService kpiExposureService,
            @Mock RetentionPeriodService retentionPeriodService) {
        KpiDefinitionDeletionFacade actual = objectUnderTest.kpiDefinitionDeletionFacade(kpiDefinitionService, databaseService, kpiExposureService, retentionPeriodService);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getPartitionRetentionManager(
            @Mock PartitionService partitionService,
            @Mock PartitionNameEncoder partitionNameEncoder,
            @Mock Clock clock) {
        PartitionRetentionManager actual = objectUnderTest.partitionRetentionManager(partitionService, partitionNameEncoder, clock);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getReadinessLogManagerFacade(
            @Mock ComplexReadinessLogService complexReadinessLogService,
            @Mock KpiDefinitionService kpiDefinitionService,
            @Mock SimpleKpiDependencyCache simpleKpiDependencyCache) {
        ReadinessLogManagerFacade actual = objectUnderTest.readinessLogManagerFacade(complexReadinessLogService, kpiDefinitionService, simpleKpiDependencyCache);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getRunningCalculationDetectorFacade(@Mock KpiDefinitionService kpiDefinitionService, @Mock CalculationService calculationService) {
        RunningCalculationDetectorFacade actual = objectUnderTest.runningCalculationDetectorFacade(kpiDefinitionService, calculationService);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getSchemaRegistryFacade(@Mock SchemaRegistryClient schemaRegistryClient, @Mock SchemaSubjectHelper schemaSubjectHelper) {
        SchemaRegistryFacade actual = objectUnderTest.schemaRegistryFacade(schemaRegistryClient, schemaSubjectHelper);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getCalculationLauncher() {
        CalculationLauncher actual = objectUnderTest.calculationLauncher();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getKafkaReader(@Mock KafkaConsumer<String, String> kafkaConsumer) {
        KafkaReader actual = objectUnderTest.kafkaReader(kafkaConsumer);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getSparkMeterService() {
        SparkMeterService actual = objectUnderTest.sparkMeterService();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getOutputTableCreatorImpl(
            @Mock DatabaseService databaseService,
            @Mock PartitionService partitionService,
            @Mock KpiExposureService kpiExposureService,
            @Mock EnvironmentValue<Duration> retentionPeriodDays) {
        OutputTableCreatorImpl actual = objectUnderTest.outputTableCreator(databaseService, partitionService, kpiExposureService, retentionPeriodDays);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getOutputTableUpdaterImpl(@Mock DatabaseService databaseService) {
        OutputTableUpdaterImpl actual = objectUnderTest.outputTableUpdater(databaseService);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getDefinitionMapper() {
        DefinitionMapper actual = objectUnderTest.definitionMapper();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getSchemaSubjectHelper(@Mock SchemaDetailCache schemaDetailCache) {
        SchemaSubjectHelper actual = objectUnderTest.schemaSubjectHelper(schemaDetailCache);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getAliasValidator() {
        AliasValidator actual = objectUnderTest.aliasValidator();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getDataIdentifierValidator(@Mock DataCatalogReader dataCatalogReader, @Mock SchemaDetailCache schemaDetailCache) {
        DataIdentifierValidator actual = objectUnderTest.dataIdentifierValidator(dataCatalogReader, schemaDetailCache);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getDependencyValidator(@Mock DependencyFinder dependencyFinder) {
        DependencyValidator actual = objectUnderTest.dependencyValidator(dependencyFinder);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getExpressionValidator(@Mock SqlRelationExtractor sqlRelationExtractor) {
        ExpressionValidator actual = objectUnderTest.expressionValidator(sqlRelationExtractor);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getKpiDefinitionRequestValidator() {
        KpiDefinitionRequestValidator actual = objectUnderTest.kpiDefinitionRequestValidator();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getLoopValidator(
            @Mock DependencyFinder dependencyFinder,
            @Mock KpiGroupLoopDetector kpiGroupLoopDetector,
            @Mock KpiDefinitionService kpiDefinitionService) {
        LoopValidator actual = objectUnderTest.loopValidator(dependencyFinder, kpiGroupLoopDetector, kpiDefinitionService);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getOnDemandParameterValidator(@Mock ParameterService parameterService, @Mock ParameterDefinitionHelper parameterDefinitionHelper) {
        OnDemandParameterValidator actual = objectUnderTest.onDemandParameterValidator(parameterService, parameterDefinitionHelper);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getParameterValidator(@Mock ParameterValidatorHelper parameterValidatorHelper) {
        ParameterValidator actual = objectUnderTest.parameterValidator(parameterValidatorHelper);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getPostgresDataTypeChangeValidator() {
        PostgresDataTypeChangeValidator actual = objectUnderTest.postgresDataTypeChangeValidator();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getSchemaExistenceValidator(@Mock SchemaRegistryFacade schemaRegistryFacade) {
        SchemaExistenceValidator actual = objectUnderTest.schemaExistenceValidator(schemaRegistryFacade);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getTabularParameterValidator(@Mock TabularParameterHelper tabularParameterHelper, @Mock TabularParameterService tabularParameterService) {
        TabularParameterValidator actual = objectUnderTest.tabularParameterValidator(tabularParameterHelper, tabularParameterService);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getParameterDefinitionHelper() {
        ParameterDefinitionHelper actual = objectUnderTest.parameterDefinitionHelper();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getParameterValidatorHelper(@Mock ParameterService parameterService) {
        ParameterValidatorHelper actual = objectUnderTest.parameterValidatorHelper(parameterService);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getTabularParameterHelper(
            @Mock KpiDefinitionService kpiDefinitionService,
            @Mock SqlRelationExtractor sqlRelationExtractor,
            @Mock SqlExtractorService sqlExtractorService) {
        TabularParameterHelper actual = objectUnderTest.tabularParameterHelper(kpiDefinitionService, sqlRelationExtractor, sqlExtractorService);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getRetentionPeriodManager(
            @Mock TableRetentionRepository tableRetentionRepository,
            @Mock CollectionRetentionRepository collectionRetentionRepository,
            @Mock EnvironmentValue<Duration> retentionPeriodDays) {
        RetentionPeriodManager actual = objectUnderTest.retentionPeriodManager(tableRetentionRepository, collectionRetentionRepository, retentionPeriodDays);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getRetentionPeriodValidator(@Mock EnvironmentValue<Duration> retentionPeriodConfiguredMax, @Mock RetentionPeriodService retentionPeriodService) {
        RetentionPeriodValidator actual = objectUnderTest.retentionPeriodValidator(retentionPeriodConfiguredMax, retentionPeriodService);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getSchemaFieldValidator(@Mock SqlExtractorService sqlExtractorService, @Mock SchemaRegistryFacade schemaRegistryFacade) {
        SchemaFieldValidator actual = objectUnderTest.schemaFieldValidator(sqlExtractorService, schemaRegistryFacade);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getSqlDatasourceValidator(@Mock KpiDefinitionService kpiDefinitionService) {
        SqlDatasourceValidator actual = objectUnderTest.sqlDatasourceValidator(kpiDefinitionService);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getLogicalPlanExtractor() {
        LogicalPlanExtractor actual = objectUnderTest.logicalPlanExtractor();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getLeafCollector() {
        LeafCollector actual = objectUnderTest.leafCollector();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getExpressionCollector(@Mock LeafCollector leafCollector) {
        ExpressionCollector actual = objectUnderTest.expressionCollector(leafCollector);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getSqlParserImpl(@Mock SparkSqlParser sparkSqlParser, @Mock LogicalPlanExtractor logicalPlanExtractor) {
        SqlParserImpl actual = objectUnderTest.sqlParser(sparkSqlParser, logicalPlanExtractor);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getSqlProcessorService(@Mock SqlParserImpl sqlParser, @Mock ExpressionCollector expressionCollector) {
        SqlProcessorService actual = objectUnderTest.sqlProcessorService(sqlParser, expressionCollector);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getSqlExtractorService(@Mock SqlProcessorService sqlProcessorService) {
        SqlExtractorService actual = objectUnderTest.sqlExtractorService(sqlProcessorService);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getSqlReferenceValidator(@Mock VirtualDatabaseService virtualDatabaseService, @Mock VirtualDatabaseResolver virtualDatabaseResolver) {
        SqlReferenceValidator actual = objectUnderTest.sqlReferenceValidator(virtualDatabaseService, virtualDatabaseResolver);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getSqlRelationExtractor(@Mock SqlParserImpl sqlParser) {
        SqlRelationExtractor actual = objectUnderTest.sqlRelationExtractor(sqlParser);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getSqlWhitelistValidator(@Mock SparkSqlParser sparkSqlParser, @Mock SqlValidatorImpl sqlValidator) {
        SqlWhitelistValidator actual = objectUnderTest.sqlWhitelistValidator(sparkSqlParser, sqlValidator);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getVirtualDatabaseResolver(@Mock SqlExtractorService extractorService, @Mock SqlRelationExtractor relationExtractor) {
        VirtualDatabaseResolver actual = objectUnderTest.virtualDatabaseResolver(extractorService, relationExtractor);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getVirtualDatabaseService(
            @Mock SqlRelationExtractor relationExtractor,
            @Mock ParameterRepository parameterRepository,
            @Mock SqlProcessorService sqlProcessorService,
            @Mock DatabaseService databaseService) {
        VirtualDatabaseService actual = objectUnderTest.virtualDatabaseService(
                relationExtractor, parameterRepository, sqlProcessorService, databaseService);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getSparkSqlParserProvider() {
        SparkSqlParserProvider actual = objectUnderTest.sparkSqlParserProvider();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getSqlValidatorImpl() {
        SqlValidatorImpl actual = objectUnderTest.sqlValidatorImpl();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getCalculationReliabilityRepositoryImpl() {
        CalculationReliabilityRepositoryImpl actual = objectUnderTest.calculationReliabilityRepository();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getCalculationRepositoryImpl() {
        CalculationRepositoryImpl actual = objectUnderTest.calculationRepository();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getCalculationServiceImpl(@Mock CalculationRepository calculationRepository) {
        CalculationServiceImpl actual = objectUnderTest.calculationService(calculationRepository);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getClockProvider() {
        ClockProvider actual = objectUnderTest.clockProvider();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getCollectionRetentionRepositoryImpl() {
        CollectionRetentionRepositoryImpl actual = objectUnderTest.collectionRetentionRepository();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getComplexReadinessLogServiceImpl(@Mock ComplexReadinessLogRepository complexReadinessLogRepository) {
        ComplexReadinessLogServiceImpl actual = objectUnderTest.complexReadinessLogService(complexReadinessLogRepository);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getDimensionTablesRepositoryImpl() {
        DimensionTablesRepositoryImpl actual = objectUnderTest.dimensionTablesRepository();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getDimensionTablesServiceImpl(@Mock DimensionTablesRepository dimensionTablesRepository) {
        DimensionTablesServiceImpl actual = objectUnderTest.dimensionTablesService(dimensionTablesRepository);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getExecutionGroupRepositoryImpl(@Mock ExecutionGroupGenerator executionGroupGenerator) {
        ExecutionGroupRepositoryImpl actual = objectUnderTest.executionGroupRepository(executionGroupGenerator);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getHealthCheckMonitor() {
        HealthCheckMonitor actual = objectUnderTest.healthCheckMonitor();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getKpiCalculationMediatorImpl(@Mock KpiCalculationExecutionController kpiCalculationExecutionController) {
        KpiCalculationMediatorImpl actual = objectUnderTest.kpiCalculationMediator(kpiCalculationExecutionController);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getKpiDefinitionRepositoryImpl(
            @Mock ExecutionGroupRepository executionGroupRepository,
            @Mock SchemaDetailRepository schemaDetailRepository) {
        KpiDefinitionRepositoryImpl actual = objectUnderTest.kpiDefinitionRepository(executionGroupRepository, schemaDetailRepository);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getKpiDefinitionServiceImpl(
            @Mock KpiDefinitionRepository kpiDefinitionRepository,
            @Mock SimpleKpiDependencyCache simpleKpiDependencyCache,
            @Mock KpiDefinitionAdapter kpiDefinitionAdapter,
            @Mock DatabaseService databaseService,
            @Mock ApiMetricRegistry apiMetricRegistry) {
        KpiDefinitionServiceImpl actual = objectUnderTest.kpiDefinitionServiceImpl(
                kpiDefinitionRepository, simpleKpiDependencyCache, kpiDefinitionAdapter, databaseService, apiMetricRegistry);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getLatestProcessedOffsetsRepositoryImpl() {
        LatestProcessedOffsetsRepositoryImpl actual = objectUnderTest.latestProcessedOffsetsRepositoryImpl();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getParameterRepositoryImpl() {
        ParameterRepositoryImpl actual = objectUnderTest.parameterRepositoryImpl();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getParameterServiceImpl(@Mock ParameterRepository parameterRepository) {
        ParameterServiceImpl actual = objectUnderTest.parameterServiceImpl(parameterRepository);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getPartitionRepositoryImpl(@Mock SqlTableModifier sqlTableModifier) {
        PartitionRepositoryImpl actual = objectUnderTest.partitionRepositoryImpl(sqlTableModifier);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getPartitionRetentionFacade(
            @Mock KpiDefinitionService kpiDefinitionService,
            @Mock KpiDefinitionDeletionFacade definitionDeletionFacade,
            @Mock RetentionPeriodManager retentionPeriodManager,
            @Mock PartitionRetentionManager partitionRetentionManager) {
        PartitionRetentionFacade actual = objectUnderTest.partitionRetentionFacade(
                kpiDefinitionService, definitionDeletionFacade, retentionPeriodManager, partitionRetentionManager);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getPartitionServiceImpl(@Mock PartitionRepository partitionRepository) {
        PartitionServiceImpl actual = objectUnderTest.partitionServiceImpl(partitionRepository);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getReadinessLogServiceImpl(@Mock ReadinessLogRepository readinessLogRepository) {
        ReadinessLogServiceImpl actual = objectUnderTest.readinessLogServiceImpl(readinessLogRepository);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getRestExecutorProducer() {
        RestExecutorProducer actual = objectUnderTest.restExecutorProducer();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getRetentionPeriodServiceImpl(
            @Mock TableRetentionRepository tableRetentionRepository,
            @Mock CollectionRetentionRepository collectionRetentionRepository) {
        RetentionPeriodServiceImpl actual = objectUnderTest.retentionPeriodServiceImpl(tableRetentionRepository, collectionRetentionRepository);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getRetryProducer() {
        RetryProducer actual = objectUnderTest.retryProducer();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getSchedulerProducer(@Mock CdiJobFactory cdiJobFactory) {
        SchedulerProducer actual = objectUnderTest.schedulerProducer(cdiJobFactory);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getSchedulerRunnerProducer(@Mock ActivityScheduler activityScheduler) {
        SchedulerRunnerProducer actual = objectUnderTest.schedulerRunnerProducer(activityScheduler);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getSchemaDetailRepositoryImpl() {
        SchemaDetailRepositoryImpl actual = objectUnderTest.schemaDetailRepositoryImpl();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getSimpleKpiDependencyCache(
            @Mock KpiDefinitionRepository kpiDefinitionRepository,
            @Mock KpiDependencyHelper kpiDependencyHelper) {
        SimpleKpiDependencyCache actual = objectUnderTest.simpleKpiDependencyCache(kpiDefinitionRepository, kpiDependencyHelper);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getTableRetentionRepositoryImpl() {
        TableRetentionRepositoryImpl actual = objectUnderTest.tableRetentionRepositoryImpl();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getTabularParameterRepositoryImpl(@Mock ParameterRepository parameterRepository) {
        TabularParameterRepositoryImpl actual = objectUnderTest.tabularParameterRepositoryImpl(parameterRepository);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getTabularParameterServiceImpl(@Mock TabularParameterRepository tabularParameterRepository) {
        TabularParameterServiceImpl actual = objectUnderTest.tabularParameterServiceImpl(tabularParameterRepository);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getClock() {
        Clock actual = objectUnderTest.clock();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getKpiCalculatorBean(
            @Mock CalculationLauncher calculationLauncher,
            @Mock CalculationService calculationService,
            @Mock KpiCalculationMediator kpiCalculationMediator,
            @Mock ReadinessLogManagerFacade readinessLogManagerFacade,
            @Mock ComplexReliabilityThresholdFacade complexReliabilityThresholdFacade,
            @Mock TabularParameterFacade tabularParameterFacade,
            @Mock SparkMeterService sparkMeterService) {
        KpiCalculatorBean actual = objectUnderTest.kpiCalculator(
                calculationLauncher, calculationService, kpiCalculationMediator, readinessLogManagerFacade, complexReliabilityThresholdFacade, tabularParameterFacade, sparkMeterService);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getKpiSchedulerMetricRegistry() {
        KpiSchedulerMetricRegistry actual = objectUnderTest.kpiSchedulerMetricRegistry();
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getKpiCalculatorSchedulerBean(@Mock ActivityRunner activityRunner, @Mock ActivityScheduler activityScheduler) {
        KpiCalculatorSchedulerBean actual = objectUnderTest.kpiCalculatorSchedulerBean(activityRunner, activityScheduler);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getKpiRetentionStartupService(
            @Mock DatabaseService databaseService,
            @Mock ActivityScheduler activityScheduler,
            @Mock EnvironmentValue<Duration> retentionPeriodDays,
            @Mock EnvironmentValue<String> cronRetentionPeriodCheck) {
        KpiRetentionStartupService actual = objectUnderTest.kpiRetentionStartupService(databaseService, activityScheduler, retentionPeriodDays, cronRetentionPeriodCheck);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getKpiComplexSchedulerStartupService(@Mock ActivityRunner activityRunner) {
        KpiComplexSchedulerStartupService actual = objectUnderTest.kpiComplexSchedulerStartupService(activityRunner);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getBackupAndRestoreAgent(@Mock BackupAndRestoreAgentBehavior backupAndRestoreAgentBehavior) {
        BackupAndRestoreAgent actual = objectUnderTest.backupAndRestoreAgent(backupAndRestoreAgentBehavior);
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void getApiMetricRegistry() {
        ApiMetricRegistry actual = objectUnderTest.apiMetricRegistry();
        Assertions.assertThat(actual).isNotNull();
    }
}