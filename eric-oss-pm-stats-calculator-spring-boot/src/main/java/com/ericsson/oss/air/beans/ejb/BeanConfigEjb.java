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

import java.time.Clock;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

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
import com.ericsson.oss.air.pm.stats.repository.api.MetricRepository;
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
import com.ericsson.oss.air.pm.stats.scheduler.activity.KpiMetricUpdaterActivity;
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
import com.ericsson.oss.air.pm.stats.service.startup.KpiMetricExposureStartupService;
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

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Range;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.spark.sql.execution.SparkSqlParser;
import org.quartz.SchedulerException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BeanConfigEjb {
    private final CalculatorProperties calculatorProperties;

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                         .findAndAddModules()
                         .configure(MapperFeature.ALLOW_COERCION_OF_SCALARS, false)
                         .build();
    }

    @Bean
    public FlywayIntegration flywayIntegration() {
        log.info("Flyway integration");
        return new FlywayIntegration();
    }

    @Bean
    public EnvironmentValue<Integer> maxHeartbeatToWaitToRecalculateSimples() {
        final Integer maxHeartbeatToWaitToRecalculateSimples = calculatorProperties.getMaxHeartbeatToWaitToRecalculateSimples();
        if (maxHeartbeatToWaitToRecalculateSimples < 0) {
            throw new InvalidValueException(
                    String.format(
                            "'%s' has to be positive or zero, but was '%d'",
                            "MAX_HEARTBEAT_TO_WAIT_TO_RECALCULATE_SIMPLES",
                            maxHeartbeatToWaitToRecalculateSimples)
            );
        }

        return EnvironmentValue.of(maxHeartbeatToWaitToRecalculateSimples);
    }

    @Bean
    public EnvironmentValue<Duration> retentionPeriodDays(final EnvironmentValue<Duration> retentionConfiguredMax) {
        final long retentionPeriodDays = calculatorProperties.getRetentionPeriodDays();

        final Range<Long> retentionRange = Range.between(1L, retentionConfiguredMax.value().toDays());

        if (retentionRange.contains(retentionPeriodDays)) {
            log.info("'RETENTION_PERIOD_DAYS' is set to '{}', it is between the required validation range of {}", retentionPeriodDays, retentionRange);

            return EnvironmentValue.of(Duration.ofDays(retentionPeriodDays));
        }

        throw new InvalidValueException(String.format("'RETENTION_PERIOD_DAYS' has to have value between %s, but was '%d'", retentionRange, retentionPeriodDays));
    }

    @Bean
    public EnvironmentValue<Duration> retentionPeriodConfiguredMax() {
        final int configuredMax = calculatorProperties.getRetentionConfiguredMax();

        if (configuredMax <= 0) {
            throw new InvalidValueException(String.format("'RETENTION_PERIOD_CONFIGURED_MAX' has to have value greater than zero, but was '%d'", configuredMax));
        }

        return EnvironmentValue.of(Duration.ofDays(configuredMax));
    }

    @Bean
    public EnvironmentValue<Integer> queueWeightOnDemandCalculation() {
        return EnvironmentValue.of(calculatorProperties.getQueueWeightOnDemandCalculation());
    }

    @Bean
    public EnvironmentValue<Integer> queueWeightScheduledCalculation() {
        return EnvironmentValue.of(calculatorProperties.getQueueWeightScheduledCalculation());
    }

    @Bean
    public EnvironmentValue<String> cronRetentionPeriodCheck() {
        return EnvironmentValue.of(calculatorProperties.getCronRetentionPeriodCheck());
    }

    @Bean
    public EnvironmentValue<Integer> maximumConcurrentCalculations() {
        return EnvironmentValue.of(calculatorProperties.getMaximumConcurrentCalculations());
    }

    @Bean
    public EnvironmentValue<Integer> sparkExecutorStartingPort() {
        return EnvironmentValue.of(calculatorProperties.getSparkExecutorStartingPort());
    }

    @Bean
    public Retry updateRunningCalculationsToLostRetry() {
        return Retry.of("handleLostStatesRetry",
                        RetryConfig.custom()
                                   .retryExceptions(Throwable.class)
                                   .maxAttempts(10)
                                   .waitDuration(Duration.ofSeconds(6))
                                   .build());
    }

    @Bean
    public Retry updateCalculationStateRetry() {
        return Retry.of("handleCalculationStateRetry",
                        RetryConfig.custom()
                                   .maxAttempts(6)
                                   .waitDuration(Duration.ofSeconds(5))
                                   .build());
    }

    @Bean
    public ActivityRunner provideActivityRunner() {
        return new ActivityRunner(ActivityScheduler.getInstance());
    }

    @Bean
    @SchedulerProducer.Scheduler
    public ActivityScheduler provideScheduler(final CdiJobFactory cdiJobFactory) throws SchedulerException {
        final ActivityScheduler activityScheduler = ActivityScheduler.getInstance();

        activityScheduler.setJobFactory(cdiJobFactory);

        return activityScheduler;
    }

    @Bean
    public SparkSqlParser provideSqlParser() {
        final SparkSqlParser sparkSqlParser = new SparkSqlParser();
        log.info("'{}' loaded successfully", SparkSqlParser.class.getSimpleName());
        return sparkSqlParser;
    }

    @Bean
    public RestExecutor restExecutor() {
        return new RestExecutor();
    }

    @Bean
    public SchemaRegistryClient schemaRegistryClient() {
        return new CachedSchemaRegistryClient(calculatorProperties.getSchemaRegistryUrl(), 1);
    }

    @Bean
    public CalculatorDatabaseBackupImpl calculatorDatabaseBackup(final BackupHelper backupHelper, final BackupAndRestoreProcessHandler backupAndRestoreProcessHandler) {
        return new CalculatorDatabaseBackupImpl(backupHelper, backupAndRestoreProcessHandler);
    }

    @Bean
    public CalculatorDatabaseRestoreImpl calculatorDatabaseRestore(
        final BackupAndRestoreQueryExecutor backupAndRestoreQueryExecutor,
        final BackupAndRestoreProcessHandler backupAndRestoreProcessHandler,
        final KpiExposureService kpiExposureService,
        final CalculationService calculationService,
        final PartitionRetentionFacade partitionRetentionFacade,
        final RestoreStatus restoreStatus,
        final Retry updateCalculationStateRetry) {
        return new CalculatorDatabaseRestoreImpl(backupAndRestoreQueryExecutor, backupAndRestoreProcessHandler, kpiExposureService,
                                                 calculationService, partitionRetentionFacade, restoreStatus, updateCalculationStateRetry);
    }

    @Bean
    public KpiDefinitionAdapter kpiDefinitionAdapter(final SchemaDetailCache schemaDetailCache) {
        return new KpiDefinitionAdapter(schemaDetailCache);
    }

    @Bean
    public BackupAndRestoreQueryExecutor backupAndRestoreQueryExecutor(final BackupAndRestoreProcessHandler backupAndRestoreProcessHandler) {
        return new BackupAndRestoreQueryExecutor(backupAndRestoreProcessHandler);
    }

    @Bean
    public BackupAndRestoreAgentBehavior backupAndRestoreAgentBehavior(final CalculatorDatabaseBackup calculatorDatabaseBackup, final CalculatorDatabaseRestore calculatorDatabaseRestore) {
        return new BackupAndRestoreAgentBehavior(calculatorDatabaseBackup, calculatorDatabaseRestore);
    }

    @Bean
    public BackupAndRestoreProcessHandler backupAndRestoreProcessHandler() {
        return new BackupAndRestoreProcessHandler();
    }

    @Bean
    public BackupHelper backupHelper(final BackupAndRestoreProcessHandler backupAndRestoreProcessHandler) {
        return new BackupHelper(backupAndRestoreProcessHandler);
    }

    @Bean
    public KpiMetricExposureStartupService kpiMetricExposureStartupService(final ApiMetricRegistry apiMetricRegistry,
                                                                           final KpiSchedulerMetricRegistry kpiSchedulerMetricRegistry,
                                                                           final ActivityScheduler activityScheduler) {
        return new KpiMetricExposureStartupService(apiMetricRegistry, kpiSchedulerMetricRegistry, activityScheduler);
    }

    @Bean
    public KpiMetricUpdaterActivity kpiMetricUpdaterActivity(final CalculationRepository calculationRepository,
                                                             final KpiSchedulerMetricRegistry kpiSchedulerMetricRegistry,
                                                             final MetricRepository metricRepository) {
        return new KpiMetricUpdaterActivity(calculationRepository, kpiSchedulerMetricRegistry, metricRepository);
    }

    @Bean
    public SchemaDetailCache schemaDetailCache() {
        return new SchemaDetailCache();
    }

    @Bean
    public CalculationReliabilityThreshold calculationReliabilityThreshold(final CalculationReliabilityService calculationReliabilityService) {
        return new CalculationReliabilityThreshold(calculationReliabilityService);
    }

    @Bean
    public ReadinessLogRepositoryImpl readinessLogRepositoryImpl() {
        return new ReadinessLogRepositoryImpl();
    }

    @Bean
    public SimpleReliabilityThresholdCalculator simpleReliabilityThresholdCalculator(
            final ReadinessLogRepository readinessLogRepository,
            final CalculationRepository calculationRepository,
            final KpiDefinitionRepository kpiDefinitionRepository,
            final ReliabilityThresholdTemporalAdjuster reliabilityThresholdTemporalAdjuster) {
        return new SimpleReliabilityThresholdCalculator(readinessLogRepository, calculationRepository, kpiDefinitionRepository, reliabilityThresholdTemporalAdjuster);
    }

    @Bean
    public TabularParameterFacade tabularParameterFacade(final DatabaseService databaseService, final DimensionTablesService dimensionTablesService) {
        return new TabularParameterFacade(databaseService, dimensionTablesService);
    }

    @Bean
    public CalculationLimitCalculator calculationLimitCalculator(
            final KpiDefinitionService kpiDefinitionService,
            final ReadinessBoundCollector readinessBoundCollector,
            final CalculationRepository calculationRepository) {
        return new CalculationLimitCalculator(kpiDefinitionService, readinessBoundCollector, calculationRepository);
    }

    @Bean
    public ReadinessBoundCalculator readinessBoundCalculator(
            final AggregationPeriodSupporter aggregationPeriodSupporter,
            final KpiDefinitionService kpiDefinitionService,
            final ReadinessBoundCollector readinessBoundCollector,
            final CalculationReliabilityService calculationReliabilityService) {
        return new ReadinessBoundCalculator(aggregationPeriodSupporter, kpiDefinitionService, readinessBoundCollector, calculationReliabilityService);
    }

    @Bean
    public ReadinessBoundCollector readinessBoundCollector(final ReadinessWindowCollector readinessWindowCollector) {
        return new ReadinessBoundCollector(readinessWindowCollector);
    }

    @Bean
    public AggregationPeriodSupporter aggregationPeriodSupporter(final AggregationPeriodCreatorRegistrySpring aggregationPeriodCreatorRegistrySpring) {
        return new AggregationPeriodSupporter(aggregationPeriodCreatorRegistrySpring);
    }

    @Bean
    public DailyAggregationPeriodCreator dailyAggregationPeriodCreator() {
        return new DailyAggregationPeriodCreator();
    }

    @Bean
    public FifteenAggregationPeriodCreator fifteenAggregationPeriodCreator() {
        return new FifteenAggregationPeriodCreator();
    }

    @Bean
    public HourlyAggregationPeriodCreator hourlyAggregationPeriodCreator() {
        return new HourlyAggregationPeriodCreator();
    }

    @Bean
    public ReadinessWindowCollector readinessWindowCollector(
            final ReadinessWindowCalculator readinessWindowCalculator,
            final ReadinessLogCollector readinessLogCollector,
            final SimpleKpiDependencyCache simpleKpiDependencyCache) {
        return new ReadinessWindowCollector(readinessWindowCalculator, readinessLogCollector, simpleKpiDependencyCache);
    }

    @Bean
    public ReadinessLogCollector readinessLogCollector(final ReadinessLogService readinessLogService) {
        return new ReadinessLogCollector(readinessLogService);
    }

    @Bean
    public ReadinessWindowCalculator readinessWindowCalculator() {
        return new ReadinessWindowCalculator();
    }

    @Bean
    public ReadinessWindowPrinter readinessWindowPrinter() {
        return new ReadinessWindowPrinter();
    }

    @Bean
    public ReliabilityThresholdTemporalAdjuster reliabilityThresholdTemporalAdjuster() {
        return new ReliabilityThresholdTemporalAdjuster();
    }

    @Bean
    public CalculationStartTimeCalculator calculationStartTimeCalculator(final CalculationReliabilityService calculationReliabilityService) {
        return new CalculationStartTimeCalculator(calculationReliabilityService);
    }

    @Bean
    public SimpleStartTimeCalculator simpleStartTimeCalculator(
            final ReadinessLogRepository readinessLogRepository,
            final CalculationRepository calculationRepository,
            final KpiDefinitionRepository kpiDefinitionRepository,
            final StartTimeTemporalAdjuster startTimeTemporalAdjuster) {
        return new SimpleStartTimeCalculator(readinessLogRepository, calculationRepository, kpiDefinitionRepository, startTimeTemporalAdjuster);
    }

    @Bean
    public StartTimeTemporalAdjuster startTimeTemporalAdjuster() {
        return new StartTimeTemporalAdjuster();
    }

    @Bean
    public DependencyFinder dependencyFinder(final KpiDependencyHelper kpiDependencyHelper, final KpiDefinitionService kpiDefinitionService) {
        return new DependencyFinder(kpiDependencyHelper, kpiDefinitionService);
    }

    @Bean
    public GraphCycleDetector graphCycleDetector() {
        return new GraphCycleDetector();
    }

    @Bean
    public KpiDependencyHelper kpiDependencyHelper() {
        return new KpiDependencyHelper();
    }

    @Bean
    public KpiGroupLoopDetector kpiGroupLoopDetector(final GraphCycleDetector graphCycleDetector, final RouteHelper routeHelper) {
        return new KpiGroupLoopDetector(graphCycleDetector, routeHelper);
    }

    @Bean
    public ExecutionGroupGraphHelper executionGroupGraphHelper(final KpiDependencyHelper kpiDependencyHelper) {
        return new ExecutionGroupGraphHelper(kpiDependencyHelper);
    }

    @Bean
    public RouteHelper routeHelper() {
        return new RouteHelper();
    }

    @Bean
    public ComplexReadinessLogRepositoryImpl complexReadinessLogRepository() {
        return new ComplexReadinessLogRepositoryImpl();
    }

    @Bean
    public PartitionNameEncoder partitionNameEncoder() {
        return new PartitionNameEncoder();
    }

    @Bean
    public SqlAppenderImpl sqlAppenderImpl() {
        return new SqlAppenderImpl();
    }

    @Bean
    public SqlTableGeneratorImpl sqlTableGenerator(final SqlAppender sqlAppender) {
        return new SqlTableGeneratorImpl(sqlAppender);
    }

    @Bean
    public SqlTableModifierImpl sqlTableModifier() {
        return new SqlTableModifierImpl();
    }

    @Bean
    public KpiCalculationJobScheduler kpiCalculationJobScheduler(
            final CalculationLimitCalculator calculationLimitCalculator,
            final ComplexExecutionGroupOrderFacade complexExecutionGroupOrderFacade,
            final KpiCalculationExecutionController kpiCalculationExecutionController,
            final KpiDefinitionService kpiDefinitionService,
            final HeartbeatManager heartbeatManager,
            final KafkaOffsetCheckerFacade kafkaOffsetCheckerFacade,
            final EnvironmentValue<Integer> maxHeartbeatToWaitToRecalculateSimples) {
        return new KpiCalculationJobScheduler(calculationLimitCalculator, complexExecutionGroupOrderFacade, kpiCalculationExecutionController,
                                              kpiDefinitionService, heartbeatManager, kafkaOffsetCheckerFacade, maxHeartbeatToWaitToRecalculateSimples);
    }

    @Bean
    public KpiCalculatorActivity kpiCalculatorActivity(final KpiCalculationJobScheduler calculationJobScheduler, final CalculationPriorityRanker calculationPriorityRanker) {
        return new KpiCalculatorActivity(calculationJobScheduler, calculationPriorityRanker);
    }

    @Bean
    public KpiCalculatorRetentionActivity kpiCalculatorRetentionActivity(
            final PartitionRetentionFacade partitionRetentionFacade,
            final CalculationService calculationService,
            final PartitionRetentionManager partitionRetentionManager) {
        return new KpiCalculatorRetentionActivity(partitionRetentionFacade, calculationService, partitionRetentionManager);
    }

    @Bean
    public KpiComplexSchedulerActivity kpiComplexSchedulerActivity(
            final KpiCalculationExecutionController kpiCalculationExecutionController,
            final RunningCalculationDetectorFacade runningCalculationDetector) {
        return new KpiComplexSchedulerActivity(kpiCalculationExecutionController, runningCalculationDetector);
    }

    @Bean
    public ComplexExecutionGroupOrderFacade complexExecutionGroupOrderFacade(
            final ComplexExecutionOrderDeterminer complexExecutionOrderDeterminer,
            final ComplexExecutionOrderFilterer complexExecutionOrderFilterer,
            final ExecutionGroupGraphHelper executionGroupGraphHelper,
            final KpiDefinitionService kpiDefinitionService) {
        return new ComplexExecutionGroupOrderFacade(complexExecutionOrderDeterminer, complexExecutionOrderFilterer,
                                                    executionGroupGraphHelper, kpiDefinitionService);
    }

    @Bean
    public ComplexExecutionOrderDeterminer complexExecutionOrderDeterminer() {
        return new ComplexExecutionOrderDeterminer();
    }

    @Bean
    public ComplexExecutionOrderFilterer complexExecutionOrderFilterer(final ReadinessBoundCalculator readinessBoundCalculator) {
        return new ComplexExecutionOrderFilterer(readinessBoundCalculator);
    }

    @Bean
    public HangingCalculationFinalizer hangingCalculationFinalizer(final CalculationService calculationService) {
        return new HangingCalculationFinalizer(calculationService);
    }

    @Bean
    public HeartbeatManager heartbeatManager(final EnvironmentValue<Integer> maxHeartbeatToWaitToRecalculateSimples) {
        return new HeartbeatManager(maxHeartbeatToWaitToRecalculateSimples, new AtomicInteger());
    }

    @Bean
    public CalculationReliabilityServiceImpl calculationReliabilityService(final CalculationReliabilityRepository calculationReliabilityRepository) {
        return new CalculationReliabilityServiceImpl(calculationReliabilityRepository);
    }

    @Bean
    public ExecutionReportBuilderFacade executionReportBuilderFacade(
            final CalculationService calculationService,
            final KpiDefinitionService kpiDefinitionService,
            final DatabaseService databaseService,
            final ReliabilityThresholdCalculatorRegistrySpring reliabilityThresholdCalculatorRegistrySpring,
            final StartTimeCalculatorRegistrySpring startTimeCalculatorRegistrySpring) {
        return new ExecutionReportBuilderFacade(calculationService, kpiDefinitionService,
                                                databaseService, reliabilityThresholdCalculatorRegistrySpring, startTimeCalculatorRegistrySpring);
    }

    @Bean
    public ComplexReliabilityThresholdFacade complexReliabilityThresholdFacade(
            final CalculationReliabilityService calculationReliabilityService,
            final ReadinessBoundCollector readinessBoundCollector,
            final KpiDefinitionService kpiDefinitionService,
            final CalculationService calculationService,
            final AggregationPeriodCreatorRegistrySpring aggregationPeriodCreatorRegistrySpring) {
        return new ComplexReliabilityThresholdFacade(calculationReliabilityService, readinessBoundCollector,
                                                     kpiDefinitionService, calculationService, aggregationPeriodCreatorRegistrySpring);
    }

    @Bean
    public KafkaOffsetCheckerFacade kafkaOffsetCheckerFacade(final KafkaReader kafkaReader, final LatestProcessedOffsetsRepository latestProcessedOffsetsRepository) {
        return new KafkaOffsetCheckerFacade(kafkaReader, latestProcessedOffsetsRepository);
    }

    @Bean
    public KpiDefinitionDeletionFacade kpiDefinitionDeletionFacade(
            final KpiDefinitionService kpiDefinitionService,
            final DatabaseServiceImpl databaseService,
            final KpiExposureService kpiExposureService,
            final RetentionPeriodService retentionPeriodService) {
        return new KpiDefinitionDeletionFacade(kpiDefinitionService, databaseService,
                                               kpiExposureService, retentionPeriodService);
    }

    @Bean
    public PartitionRetentionManager partitionRetentionManager(
            final PartitionService partitionService,
            final PartitionNameEncoder partitionNameEncoder,
            final Clock clock) {
        return new PartitionRetentionManager(partitionService, partitionNameEncoder, clock);
    }

    @Bean
    public ReadinessLogManagerFacade readinessLogManagerFacade(
            final ComplexReadinessLogService complexReadinessLogService,
            final KpiDefinitionService kpiDefinitionService,
            final SimpleKpiDependencyCache simpleKpiDependencyCache) {
        return new ReadinessLogManagerFacade(complexReadinessLogService, kpiDefinitionService, simpleKpiDependencyCache);
    }

    @Bean
    public RunningCalculationDetectorFacade runningCalculationDetectorFacade(final KpiDefinitionService kpiDefinitionService, final CalculationService calculationService) {
        return new RunningCalculationDetectorFacade(kpiDefinitionService, calculationService);
    }

    @Bean
    public SchemaRegistryFacade schemaRegistryFacade(final SchemaRegistryClient schemaRegistryClient, final SchemaSubjectHelper schemaSubjectHelper) {
        return new SchemaRegistryFacade(schemaRegistryClient, schemaSubjectHelper);
    }

    @Bean
    public CalculationLauncher calculationLauncher() {
        return new CalculationLauncher();
    }

    @Bean
    public KafkaReader kafkaReader(final KafkaConsumer<String, String> kafkaConsumer) {
        return new KafkaReader(kafkaConsumer);
    }

    @Bean
    public SparkMeterService sparkMeterService() {
        Long metricRetentionInSeconds = calculatorProperties.getSparkMetricRetentionInSeconds();
        return new SparkMeterService(metricRetentionInSeconds);
    }

    @Bean
    public OutputTableCreatorImpl outputTableCreator(
            final DatabaseService databaseService,
            final PartitionService partitionService,
            final KpiExposureService kpiExposureService,
            final EnvironmentValue<Duration> retentionPeriodDays) {
        return new OutputTableCreatorImpl(databaseService, partitionService, kpiExposureService, retentionPeriodDays);
    }

    @Bean
    public OutputTableUpdaterImpl outputTableUpdater(final DatabaseService databaseService) {
        return new OutputTableUpdaterImpl(databaseService);
    }

    @Bean
    public DefinitionMapper definitionMapper() {
        return new DefinitionMapper();
    }

    @Bean
    public SchemaSubjectHelper schemaSubjectHelper(final SchemaDetailCache schemaDetailCache) {
        return new SchemaSubjectHelper(schemaDetailCache);
    }

    @Bean
    public AliasValidator aliasValidator() {
        return new AliasValidator();
    }

    @Bean
    public DataIdentifierValidator dataIdentifierValidator(final DataCatalogReader dataCatalogReader, final SchemaDetailCache schemaDetailCache) {
        return new DataIdentifierValidator(dataCatalogReader, schemaDetailCache);
    }

    @Bean
    public DependencyValidator dependencyValidator(final DependencyFinder dependencyFinder) {
        return new DependencyValidator(dependencyFinder);
    }

    @Bean
    public ExpressionValidator expressionValidator(final SqlRelationExtractor sqlRelationExtractor) {
        return new ExpressionValidator(sqlRelationExtractor);
    }

    @Bean
    public KpiDefinitionRequestValidator kpiDefinitionRequestValidator() {
        return new KpiDefinitionRequestValidator();
    }

    @Bean
    public LoopValidator loopValidator(
            final DependencyFinder dependencyFinder,
            final KpiGroupLoopDetector kpiGroupLoopDetector,
            final KpiDefinitionService kpiDefinitionService) {
        return new LoopValidator(dependencyFinder, kpiGroupLoopDetector, kpiDefinitionService);
    }

    @Bean
    public OnDemandParameterValidator onDemandParameterValidator(final ParameterService parameterService, final ParameterDefinitionHelper parameterDefinitionHelper) {
        return new OnDemandParameterValidator(parameterService, parameterDefinitionHelper);
    }

    @Bean
    public ParameterValidator parameterValidator(final ParameterValidatorHelper parameterValidatorHelper) {
        return new ParameterValidator(parameterValidatorHelper);
    }

    @Bean
    public PostgresDataTypeChangeValidator postgresDataTypeChangeValidator() {
        return new PostgresDataTypeChangeValidator();
    }

    @Bean
    public SchemaExistenceValidator schemaExistenceValidator(final SchemaRegistryFacade schemaRegistryFacade) {
        return new SchemaExistenceValidator(schemaRegistryFacade);
    }

    @Bean
    public TabularParameterValidator tabularParameterValidator(final TabularParameterHelper tabularParameterHelper, final TabularParameterService tabularParameterService) {
        return new TabularParameterValidator(tabularParameterHelper, tabularParameterService);
    }

    @Bean
    public ParameterDefinitionHelper parameterDefinitionHelper() {
        return new ParameterDefinitionHelper();
    }

    @Bean
    public ParameterValidatorHelper parameterValidatorHelper(final ParameterService parameterService) {
        return new ParameterValidatorHelper(parameterService);
    }

    @Bean
    public TabularParameterHelper tabularParameterHelper(
            final KpiDefinitionService kpiDefinitionService,
            final SqlRelationExtractor sqlRelationExtractor,
            final SqlExtractorService sqlExtractorService) {
        return new TabularParameterHelper(kpiDefinitionService, sqlRelationExtractor, sqlExtractorService);
    }

    @Bean
    public RetentionPeriodManager retentionPeriodManager(
            final TableRetentionRepository tableRetentionRepository,
            final CollectionRetentionRepository collectionRetentionRepository,
            final EnvironmentValue<Duration> retentionPeriodDays) {
        return new RetentionPeriodManager(tableRetentionRepository, collectionRetentionRepository, retentionPeriodDays);
    }

    @Bean
    public RetentionPeriodValidator retentionPeriodValidator(final EnvironmentValue<Duration> retentionPeriodConfiguredMax, final RetentionPeriodService retentionPeriodService) {
        return new RetentionPeriodValidator(retentionPeriodConfiguredMax, retentionPeriodService);
    }

    @Bean
    public SchemaFieldValidator schemaFieldValidator(final SqlExtractorService sqlExtractorService, final SchemaRegistryFacade schemaRegistryFacade) {
        return new SchemaFieldValidator(sqlExtractorService, schemaRegistryFacade);
    }

    @Bean
    public SqlDatasourceValidator sqlDatasourceValidator(final KpiDefinitionService kpiDefinitionService) {
        return new SqlDatasourceValidator(kpiDefinitionService);
    }

    @Bean
    public LogicalPlanExtractor logicalPlanExtractor() {
        return new LogicalPlanExtractor();
    }

    @Bean
    public LeafCollector leafCollector() {
        return new LeafCollector();
    }

    @Bean
    public ExpressionCollector expressionCollector(final LeafCollector leafCollector) {
        return new ExpressionCollector(leafCollector);
    }

    @Bean
    public SqlParserImpl sqlParser(@NonNull final SparkSqlParser sparkSqlParser, final LogicalPlanExtractor logicalPlanExtractor) {
        return new SqlParserImpl(sparkSqlParser, logicalPlanExtractor);
    }

    @Bean
    public SqlProcessorService sqlProcessorService(final SqlParserImpl sqlParser, final ExpressionCollector expressionCollector) {
        return new SqlProcessorService(sqlParser, expressionCollector);
    }

    @Bean
    public SqlExtractorService sqlExtractorService(final SqlProcessorService sqlProcessorService) {
        return new SqlExtractorService(sqlProcessorService);
    }

    @Bean
    public SqlReferenceValidator sqlReferenceValidator(final VirtualDatabaseService virtualDatabaseService, final VirtualDatabaseResolver virtualDatabaseResolver) {
        return new SqlReferenceValidator(virtualDatabaseService, virtualDatabaseResolver);
    }

    @Bean
    public SqlRelationExtractor sqlRelationExtractor(final SqlParserImpl sqlParser) {
        return new SqlRelationExtractor(sqlParser);
    }

    @Bean
    public SqlWhitelistValidator sqlWhitelistValidator(final SparkSqlParser sparkSqlParser, final SqlValidatorImpl sqlValidator) {
        return new SqlWhitelistValidator(sparkSqlParser, sqlValidator);
    }

    @Bean
    public VirtualDatabaseResolver virtualDatabaseResolver(final SqlExtractorService extractorService, final SqlRelationExtractor relationExtractor) {
        return new VirtualDatabaseResolver(extractorService, relationExtractor);
    }

    @Bean
    public VirtualDatabaseService virtualDatabaseService(
            final SqlRelationExtractor relationExtractor,
            final ParameterRepository parameterRepository,
            final SqlProcessorService sqlProcessorService,
            final DatabaseService databaseService) {
        return new VirtualDatabaseService(relationExtractor, parameterRepository, sqlProcessorService, databaseService);
    }

    @Bean
    public SparkSqlParserProvider sparkSqlParserProvider() {
        return new SparkSqlParserProvider();
    }

    @Bean
    public SqlValidatorImpl sqlValidatorImpl() {
        return new SqlValidatorImpl();
    }

    @Bean
    public CalculationReliabilityRepositoryImpl calculationReliabilityRepository() {
        return new CalculationReliabilityRepositoryImpl();
    }

    @Bean
    public CalculationRepositoryImpl calculationRepository() {
        return new CalculationRepositoryImpl();
    }

    @Bean
    public CalculationServiceImpl calculationService(final CalculationRepository calculationRepository) {
        return new CalculationServiceImpl(calculationRepository);
    }

    @Bean
    public ClockProvider clockProvider() {
        return new ClockProvider();
    }

    @Bean
    public CollectionRetentionRepositoryImpl collectionRetentionRepository() {
        return new CollectionRetentionRepositoryImpl();
    }

    @Bean
    public ComplexReadinessLogServiceImpl complexReadinessLogService(final ComplexReadinessLogRepository complexReadinessLogRepository) {
        return new ComplexReadinessLogServiceImpl(complexReadinessLogRepository);
    }

    @Bean
    public DimensionTablesRepositoryImpl dimensionTablesRepository() {
        return new DimensionTablesRepositoryImpl();
    }

    @Bean
    public DimensionTablesServiceImpl dimensionTablesService(final DimensionTablesRepository dimensionTablesRepository) {
        return new DimensionTablesServiceImpl(dimensionTablesRepository);
    }

    @Bean
    public ExecutionGroupRepositoryImpl executionGroupRepository(final ExecutionGroupGenerator executionGroupGenerator) {
        return new ExecutionGroupRepositoryImpl(executionGroupGenerator);
    }

    @Bean
    public HealthCheckMonitor healthCheckMonitor() {
        return new HealthCheckMonitor();
    }

    @Bean
    public KpiCalculationMediatorImpl kpiCalculationMediator(final KpiCalculationExecutionController kpiCalculationExecutionController) {
        return new KpiCalculationMediatorImpl(kpiCalculationExecutionController);
    }

    @Bean
    public KpiDefinitionRepositoryImpl kpiDefinitionRepository(
            final ExecutionGroupRepository executionGroupRepository,
            final SchemaDetailRepository schemaDetailRepository) {
        return new KpiDefinitionRepositoryImpl(executionGroupRepository, schemaDetailRepository);
    }

    @Bean
    public KpiDefinitionServiceImpl kpiDefinitionServiceImpl(
            final KpiDefinitionRepository kpiDefinitionRepository,
            final SimpleKpiDependencyCache simpleKpiDependencyCache,
            final KpiDefinitionAdapter kpiDefinitionAdapter,
            final DatabaseService databaseService,
            final ApiMetricRegistry apiMetricRegistry) {
        return new KpiDefinitionServiceImpl(kpiDefinitionRepository, simpleKpiDependencyCache, kpiDefinitionAdapter,
                                            databaseService, apiMetricRegistry);
    }

    @Bean
    public LatestProcessedOffsetsRepositoryImpl latestProcessedOffsetsRepositoryImpl() {
        return new LatestProcessedOffsetsRepositoryImpl();
    }

    @Bean
    public ParameterRepositoryImpl parameterRepositoryImpl() {
        return new ParameterRepositoryImpl();
    }

    @Bean
    public ParameterServiceImpl parameterServiceImpl(final ParameterRepository parameterRepository) {
        return new ParameterServiceImpl(parameterRepository);
    }

    @Bean
    public PartitionRepositoryImpl partitionRepositoryImpl(final SqlTableModifier sqlTableModifier) {
        return new PartitionRepositoryImpl(sqlTableModifier);
    }

    @Bean
    public PartitionRetentionFacade partitionRetentionFacade(
            final KpiDefinitionService kpiDefinitionService,
            final KpiDefinitionDeletionFacade definitionDeletionFacade,
            final RetentionPeriodManager retentionPeriodManager,
            final PartitionRetentionManager partitionRetentionManager) {
        return new PartitionRetentionFacade(kpiDefinitionService, definitionDeletionFacade, retentionPeriodManager, partitionRetentionManager);
    }

    @Bean
    public PartitionServiceImpl partitionServiceImpl(final PartitionRepository partitionRepository) {
        return new PartitionServiceImpl(partitionRepository);
    }

    @Bean
    public ReadinessLogServiceImpl readinessLogServiceImpl(final ReadinessLogRepository readinessLogRepository) {
        return new ReadinessLogServiceImpl(readinessLogRepository);
    }

    @Bean
    public RestExecutorProducer restExecutorProducer() {
        return new RestExecutorProducer();
    }

    @Bean
    public RetentionPeriodServiceImpl retentionPeriodServiceImpl(
            final TableRetentionRepository tableRetentionRepository,
            final CollectionRetentionRepository collectionRetentionRepository) {
        return new RetentionPeriodServiceImpl(tableRetentionRepository, collectionRetentionRepository);
    }

    @Bean
    public RetryProducer retryProducer() {
        return new RetryProducer();
    }

    @Bean
    public SchedulerProducer schedulerProducer(final CdiJobFactory cdiJobFactory) {
        return new SchedulerProducer(cdiJobFactory);
    }

    @Bean
    public SchedulerRunnerProducer schedulerRunnerProducer(final ActivityScheduler activityScheduler) {
        return new SchedulerRunnerProducer(activityScheduler);
    }

    @Bean
    public SchemaDetailRepositoryImpl schemaDetailRepositoryImpl() {
        return new SchemaDetailRepositoryImpl();
    }

    @Bean
    @DependsOn("flywayIntegration")
    public SimpleKpiDependencyCache simpleKpiDependencyCache(
            final KpiDefinitionRepository kpiDefinitionRepository,
            final KpiDependencyHelper kpiDependencyHelper) {
        return new SimpleKpiDependencyCache(kpiDefinitionRepository, kpiDependencyHelper);
    }

    @Bean
    public TableRetentionRepositoryImpl tableRetentionRepositoryImpl() {
        return new TableRetentionRepositoryImpl();
    }

    @Bean
    public TabularParameterRepositoryImpl tabularParameterRepositoryImpl(final ParameterRepository parameterRepository) {
        return new TabularParameterRepositoryImpl(parameterRepository);
    }

    @Bean
    public TabularParameterServiceImpl tabularParameterServiceImpl(final TabularParameterRepository tabularParameterRepository) {
        return new TabularParameterServiceImpl(tabularParameterRepository);
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public KpiCalculatorBean kpiCalculator(final CalculationLauncher calculationLauncher,
                                           final CalculationService calculationService,
                                           final KpiCalculationMediator kpiCalculationMediator,
                                           final ReadinessLogManagerFacade readinessLogManagerFacade,
                                           final ComplexReliabilityThresholdFacade complexReliabilityThresholdFacade,
                                           final TabularParameterFacade tabularParameterFacade,
                                           final SparkMeterService sparkMeterService) {
        return new KpiCalculatorBean(calculationLauncher, calculationService, kpiCalculationMediator, readinessLogManagerFacade,
                                     complexReliabilityThresholdFacade, tabularParameterFacade, sparkMeterService, null);
    }

    @Bean
    public KpiSchedulerMetricRegistry kpiSchedulerMetricRegistry() {
        return new KpiSchedulerMetricRegistry();
    }

    @Bean
    public KpiCalculatorSchedulerBean kpiCalculatorSchedulerBean(final ActivityRunner activityRunner, final ActivityScheduler activityScheduler) {
        return new KpiCalculatorSchedulerBean(activityScheduler, activityRunner);
    }

    @Bean
    public KpiRetentionStartupService kpiRetentionStartupService(final DatabaseService databaseService,
                                                                 final ActivityScheduler activityScheduler,
                                                                 final EnvironmentValue<Duration> retentionPeriodDays,
                                                                 final EnvironmentValue<String> cronRetentionPeriodCheck) {
        return new KpiRetentionStartupService(databaseService, activityScheduler, retentionPeriodDays, cronRetentionPeriodCheck);
    }

    @Bean
    public KpiComplexSchedulerStartupService kpiComplexSchedulerStartupService(final ActivityRunner activityRunner) {
        return new KpiComplexSchedulerStartupService(activityRunner);
    }

    @Bean
    public BackupAndRestoreAgent backupAndRestoreAgent(final BackupAndRestoreAgentBehavior backupAndRestoreAgentBehavior) {
        return new BackupAndRestoreAgent(backupAndRestoreAgentBehavior, Optional.empty());
    }

    @Bean
    public ApiMetricRegistry apiMetricRegistry() {
        return new ApiMetricRegistry();
    }
}
