/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.scheduler;

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.EXECUTION_GROUP_ON_DEMAND_CALCULATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculation.api.KpiCalculator;
import com.ericsson.oss.air.pm.stats.calculation.readiness.ReadinessWindowCollector;
import com.ericsson.oss.air.pm.stats.calculation.readiness.window.ReadinessWindowPrinter;
import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiPersistenceException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation.KpiCalculationJob;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation.KpiCalculationJob.KpiCalculationJobBuilder;
import com.ericsson.oss.air.pm.stats.scheduler.finalize.HangingCalculationFinalizer;
import com.ericsson.oss.air.pm.stats.service.api.CalculationService;
import com.ericsson.oss.air.pm.stats.service.metric.KpiMetric;
import com.ericsson.oss.air.pm.stats.service.metric.KpiSchedulerMetricRegistry;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KpiCalculationExecutionControllerTest {

    static final String STARTING_PORT = "10010";
    static final String STARTING_PORT_PLUS_ONE = "10011";
    static final String ON_DEMAND_CALCULATION = EXECUTION_GROUP_ON_DEMAND_CALCULATION;

    static final String COMPLEX_TEST_EXECUTION_GROUP = "complex_test_execgroup1";
    static final String EXECUTION_GROUP = "group1";
    final UUID uuid1 = UUID.fromString("1cd0b8d0-1921-4079-b0de-0c7758061bba");
    final UUID uuid2 = UUID.fromString("5f940054-91d3-406e-8d18-47b8291a3d8b");
    final UUID uuid3 = UUID.fromString("bc6c241c-7902-41f5-9946-05df6c63522b");

    final Timestamp testTime = Timestamp.valueOf("2022-05-05 10:10:10");

    @Mock
    KpiCalculator kpiCalculatorMock;
    @Mock
    CalculationService calculationServiceMock;
    @Mock
    ReadinessWindowCollector readinessWindowCollectorMock;
    @Mock
    ReadinessWindowPrinter readinessWindowPrinterMock;
    @Mock
    HangingCalculationFinalizer hangingCalculationFinalizerMock;
    @Spy
    KpiSchedulerMetricRegistry kpiSchedulerMetricRegistry;

    KpiCalculationExecutionController objectUnderTest;

    @BeforeEach
    void init() {
        //TODO fix this after full migration is done and Spring values can be inserted properly
        objectUnderTest = new KpiCalculationExecutionController(null, 0, 2,
                1, 2, 10010, null,
                kpiSchedulerMetricRegistry, kpiCalculatorMock, calculationServiceMock, readinessWindowCollectorMock,
                readinessWindowPrinterMock, hangingCalculationFinalizerMock);
        objectUnderTest.init();
    }

    @Test
    void whenCalculationsAreAddedAndRemovedFromController_thenMetricsAreCorrectlyUpdated() {
        kpiSchedulerMetricRegistry.getMetricRegistry().remove(KpiMetric.CURRENT_ON_DEMAND_CALCULATION.toString());

        objectUnderTest.scheduleCalculation(KpiCalculationJob.builder()
                .withCalculationId(uuid1)
                .withTimeCreated(testTime)
                .withExecutionGroup(ON_DEMAND_CALCULATION)
                .withJobType(KpiType.ON_DEMAND)
                .build());
        Assertions.assertThat(kpiSchedulerMetricRegistry.getMetricRegistry().counter(KpiMetric.CURRENT_ON_DEMAND_CALCULATION.toString()).getCount())
                .isEqualTo(1);

        objectUnderTest.scheduleCalculation(KpiCalculationJob.builder()
                .withCalculationId(uuid2)
                .withTimeCreated(testTime)
                .withExecutionGroup(ON_DEMAND_CALCULATION)
                .withJobType(KpiType.ON_DEMAND)
                .build());
        Assertions.assertThat(kpiSchedulerMetricRegistry.getMetricRegistry().counter(KpiMetric.CURRENT_ON_DEMAND_CALCULATION.toString()).getCount())
                .isEqualTo(2);

        objectUnderTest.scheduleCalculation(KpiCalculationJob.builder()
                .withCalculationId(uuid3)
                .withTimeCreated(testTime)
                .withExecutionGroup(EXECUTION_GROUP)
                .withJobType(KpiType.ON_DEMAND)
                .build());
        Assertions.assertThat(kpiSchedulerMetricRegistry.getMetricRegistry().counter(KpiMetric.CURRENT_ON_DEMAND_CALCULATION.toString()).getCount())
                .isEqualTo(2);

        objectUnderTest.removeRunningCalculationAndStartNext(uuid1);
        Assertions.assertThat(kpiSchedulerMetricRegistry.getMetricRegistry().counter(KpiMetric.CURRENT_ON_DEMAND_CALCULATION.toString()).getCount())
                .isEqualTo(1);

        objectUnderTest.removeRunningCalculationAndStartNext(uuid3);
        Assertions.assertThat(kpiSchedulerMetricRegistry.getMetricRegistry().counter(KpiMetric.CURRENT_ON_DEMAND_CALCULATION.toString()).getCount())
                .isEqualTo(1);
    }

    @Test
    void whenCalculationServiceThrowsSqlExceptionAndComplexCalculationIsScheduled_ThenExecutionShouldThrowUncheckedSqlException() throws Exception {
        final KpiCalculationJob job = calculationJob(uuid1, testTime, COMPLEX_TEST_EXECUTION_GROUP, KpiType.SCHEDULED_COMPLEX);

        doThrow(new SQLException()).when(calculationServiceMock).save(any());

        objectUnderTest.scheduleCalculation(job);

        Assertions.assertThatThrownBy(() -> objectUnderTest.executeComplexCalculation())
                .hasRootCauseInstanceOf(SQLException.class)
                .isExactlyInstanceOf(KpiPersistenceException.class)
                .hasMessage("Unable to persist state job '%s'", job);

        verify(kpiCalculatorMock, never()).calculateKpis(job);
    }

    @Test
    void whenMaximumTwoConcurrentCalculationsAreAllowed_AndThreeCalculationsAreSubmitted_thenOnlyTwoCalculationsAreStartedAndThirdOneIsNotStarted() {
        final KpiCalculationJob kpiCalculationJob1 =
                KpiCalculationJob.builder().withCalculationId(uuid1).withTimeCreated(testTime).withExecutionGroup(EXECUTION_GROUP).withJobType(KpiType.ON_DEMAND).build();
        final KpiCalculationJob kpiCalculationJob2 =
                KpiCalculationJob.builder().withCalculationId(uuid2).withTimeCreated(testTime).withExecutionGroup(EXECUTION_GROUP).withJobType(KpiType.ON_DEMAND).build();
        final KpiCalculationJob kpiCalculationJob3 =
                KpiCalculationJob.builder().withCalculationId(uuid3).withTimeCreated(testTime).withExecutionGroup(EXECUTION_GROUP).withJobType(KpiType.ON_DEMAND).build();

        objectUnderTest.scheduleCalculation(kpiCalculationJob1);
        objectUnderTest.scheduleCalculation(kpiCalculationJob2);
        objectUnderTest.scheduleCalculation(kpiCalculationJob3);

        verify(kpiCalculatorMock).calculateKpis(kpiCalculationJob1);
        verify(kpiCalculatorMock).calculateKpis(kpiCalculationJob2);
        verify(kpiCalculatorMock, never()).calculateKpis(kpiCalculationJob3);
    }

    @Test
    void whenOneCalculationIsFinished_AndThereAreCalculationsInQueue_thenNextCalculationIsStarted() {
        final KpiCalculationJob kpiCalculationJob1 =
                KpiCalculationJob.builder().withCalculationId(uuid1).withTimeCreated(testTime).withExecutionGroup(EXECUTION_GROUP).withJobType(KpiType.ON_DEMAND).build();
        final KpiCalculationJob kpiCalculationJob2 =
                KpiCalculationJob.builder().withCalculationId(uuid2).withTimeCreated(testTime).withExecutionGroup(EXECUTION_GROUP).withJobType(KpiType.ON_DEMAND).build();
        final KpiCalculationJob kpiCalculationJob3 =
                KpiCalculationJob.builder().withCalculationId(uuid3).withTimeCreated(testTime).withExecutionGroup(EXECUTION_GROUP).withJobType(KpiType.ON_DEMAND).build();

        objectUnderTest.scheduleCalculation(kpiCalculationJob1);
        objectUnderTest.scheduleCalculation(kpiCalculationJob2);
        objectUnderTest.scheduleCalculation(kpiCalculationJob3);

        verify(kpiCalculatorMock).calculateKpis(kpiCalculationJob1);
        verify(kpiCalculatorMock).calculateKpis(kpiCalculationJob2);
        verify(kpiCalculatorMock, never()).calculateKpis(kpiCalculationJob3);
        objectUnderTest.removeRunningCalculationAndStartNext(uuid1);
        verify(kpiCalculatorMock).calculateKpis(kpiCalculationJob3);
    }

    @Test
    void whenOneCalculationIsFinished_AndThereAreNoCalculationsInQueue_thenNothingBreaks() {
        final KpiCalculationJob kpiCalculationJob1 =
                KpiCalculationJob.builder().withCalculationId(uuid1).withTimeCreated(testTime).withExecutionGroup(EXECUTION_GROUP).withJobType(KpiType.ON_DEMAND).build();
        final KpiCalculationJob kpiCalculationJob2 =
                KpiCalculationJob.builder().withCalculationId(uuid2).withTimeCreated(testTime).withExecutionGroup(EXECUTION_GROUP).withJobType(KpiType.ON_DEMAND).build();

        objectUnderTest.scheduleCalculation(kpiCalculationJob1);
        objectUnderTest.scheduleCalculation(kpiCalculationJob2);

        verify(kpiCalculatorMock).calculateKpis(kpiCalculationJob1);
        verify(kpiCalculatorMock).calculateKpis(kpiCalculationJob2);
        objectUnderTest.removeRunningCalculationAndStartNext(uuid1);
    }

    @Test
    void whenOnDemandCalculationIsScheduled_thenOnDemandCalculationsCounterIsIncrementedBy1() {
        objectUnderTest.scheduleCalculation(KpiCalculationJob.builder()
                .withCalculationId(uuid1)
                .withTimeCreated(testTime)
                .withExecutionGroup(ON_DEMAND_CALCULATION)
                .withJobType(KpiType.ON_DEMAND)
                .build());
        Assertions.assertThat(objectUnderTest.getCurrentOnDemandCalculationCount()).isEqualTo(1);
    }

    @Test
    void whenOnDemandCalculationIsCompleted_thenOnDemandCalculationsCounterIsDecrementedBy1() {
        objectUnderTest.scheduleCalculation(KpiCalculationJob.builder()
                .withCalculationId(uuid1)
                .withTimeCreated(testTime)
                .withExecutionGroup(ON_DEMAND_CALCULATION)
                .withJobType(KpiType.ON_DEMAND)
                .build());
        Assertions.assertThat(objectUnderTest.getCurrentOnDemandCalculationCount()).isEqualTo(1);
        objectUnderTest.removeRunningCalculationAndStartNext(uuid1);
        Assertions.assertThat(objectUnderTest.getCurrentOnDemandCalculationCount()).isEqualTo(0);
    }

    @Test
    void whenNewCalculationsAreScheduled_andCalculationsAreExecuted_thenCorrectJMXPortIsAssignedToCalculation() {
        KpiCalculationJob job = KpiCalculationJob.builder()
                .withCalculationId(uuid1)
                .withTimeCreated(testTime)
                .withExecutionGroup(ON_DEMAND_CALCULATION)
                .withJobType(KpiType.ON_DEMAND)
                .build();
        objectUnderTest.scheduleCalculation(job);
        assertThat(job.getJmxPort()).isEqualTo(STARTING_PORT);

        job = KpiCalculationJob.builder()
                .withCalculationId(uuid2)
                .withTimeCreated(testTime)
                .withExecutionGroup(ON_DEMAND_CALCULATION)
                .withJobType(KpiType.ON_DEMAND)
                .build();
        objectUnderTest.scheduleCalculation(job);
        assertThat(job.getJmxPort()).isEqualTo(STARTING_PORT_PLUS_ONE);
    }

    @Test
    void whenNewCalculationsAreScheduled_andCalculationsAreNotExecutedDueToMaxConcurrentExecutionLimit_thenNoJMXPortIsAssignedToCalculation() {
        KpiCalculationJob job = KpiCalculationJob.builder()
                .withCalculationId(uuid1)
                .withTimeCreated(testTime)
                .withExecutionGroup(ON_DEMAND_CALCULATION)
                .withJobType(KpiType.ON_DEMAND)
                .build();
        objectUnderTest.scheduleCalculation(job);
        assertThat(job.getJmxPort()).isEqualTo(STARTING_PORT);

        job = KpiCalculationJob.builder()
                .withCalculationId(uuid2)
                .withTimeCreated(testTime)
                .withExecutionGroup(ON_DEMAND_CALCULATION)
                .withJobType(KpiType.ON_DEMAND)
                .build();
        objectUnderTest.scheduleCalculation(job);
        assertThat(job.getJmxPort()).isEqualTo(STARTING_PORT_PLUS_ONE);

        job = KpiCalculationJob.builder()
                .withCalculationId(uuid3)
                .withTimeCreated(testTime)
                .withExecutionGroup(ON_DEMAND_CALCULATION)
                .withJobType(KpiType.ON_DEMAND)
                .build();
        objectUnderTest.scheduleCalculation(job);
        assertThat(job.getJmxPort()).isNull();
    }

    @Test
    void whenCalculationIsRemoved_andNewCalculationIsScheduled_thenNextJMXPortIsAssignedToCalculation() {
        objectUnderTest.scheduleCalculation(KpiCalculationJob.builder()
                .withCalculationId(uuid1)
                .withTimeCreated(testTime)
                .withExecutionGroup(ON_DEMAND_CALCULATION)
                .withJobType(KpiType.ON_DEMAND)
                .build());
        objectUnderTest.removeRunningCalculationAndStartNext(uuid1);

        final KpiCalculationJob job = KpiCalculationJob.builder()
                .withCalculationId(uuid2)
                .withTimeCreated(testTime)
                .withExecutionGroup(ON_DEMAND_CALCULATION)
                .withJobType(KpiType.ON_DEMAND)
                .build();
        objectUnderTest.scheduleCalculation(job);
        assertThat(job.getJmxPort()).isEqualTo(STARTING_PORT_PLUS_ONE);
    }

    @Test
    void whenCalculationIsRemoved_andThereAreCalculationsInQueue_thenFirstJMXPortIsAssignedToCalculation() {
        objectUnderTest.scheduleCalculation(KpiCalculationJob.builder()
                .withCalculationId(uuid1)
                .withTimeCreated(testTime)
                .withExecutionGroup(ON_DEMAND_CALCULATION)
                .withJobType(KpiType.ON_DEMAND)
                .build());
        objectUnderTest.scheduleCalculation(KpiCalculationJob.builder()
                .withCalculationId(uuid2)
                .withTimeCreated(testTime)
                .withExecutionGroup(ON_DEMAND_CALCULATION)
                .withJobType(KpiType.ON_DEMAND)
                .build());

        final KpiCalculationJob job = KpiCalculationJob.builder()
                .withCalculationId(uuid3)
                .withTimeCreated(testTime)
                .withExecutionGroup(ON_DEMAND_CALCULATION)
                .withJobType(KpiType.ON_DEMAND)
                .build();
        objectUnderTest.scheduleCalculation(job);
        assertThat(job.getJmxPort()).isNull();

        objectUnderTest.removeRunningCalculationAndStartNext(uuid1);

        assertThat(job.getJmxPort()).isEqualTo(STARTING_PORT);
    }

    @Test
    void whenComplexCalculationIsFinished_AndThereAreNoCalculationsInQueue_thenNothingBreaks() {
        KpiCalculationJob job = KpiCalculationJob.builder()
                .withCalculationId(uuid1)
                .withTimeCreated(testTime)
                .withExecutionGroup(COMPLEX_TEST_EXECUTION_GROUP)
                .withJobType(KpiType.SCHEDULED_COMPLEX)
                .build();

        when(readinessWindowCollectorMock.collect(anyString(), anyCollection())).thenReturn(Collections.emptyMap());

        objectUnderTest.scheduleCalculation(job);
        objectUnderTest.executeComplexCalculation();

        verify(kpiCalculatorMock).calculateKpis(job);
        verify(readinessWindowCollectorMock).collect(anyString(), anyCollection());
        verify(readinessWindowPrinterMock).logReadinessWindows(anyString(), anyMap());

        objectUnderTest.removeRunningCalculationAndStartNext(uuid1);
    }

    @MethodSource("provideScheduleData")
    @ParameterizedTest(name = "[{index}] execution group: ''{0}'', can be found: ''{1}''")
    void shouldDecideIfAlreadyPresentInQueueOrCurrentCalculation(final String executionGroup, final boolean expected) {
        //running calculation
        objectUnderTest.scheduleCalculation(KpiCalculationJob.builder()
                .withCalculationId(uuid1)
                .withTimeCreated(testTime)
                .withExecutionGroup("simple1")
                .withJobType(KpiType.SCHEDULED_SIMPLE)
                .build());
        //running calculation
        objectUnderTest.scheduleCalculation(KpiCalculationJob.builder()
                .withCalculationId(uuid2)
                .withTimeCreated(testTime)
                .withExecutionGroup("simple2")
                .withJobType(KpiType.SCHEDULED_SIMPLE)
                .build());
        //queued calculation
        objectUnderTest.scheduleCalculation(KpiCalculationJob.builder()
                .withCalculationId(uuid3)
                .withTimeCreated(testTime)
                .withExecutionGroup("simple3")
                .withJobType(KpiType.SCHEDULED_SIMPLE)
                .build());


        final boolean actual = objectUnderTest.isSimpleQueuedOrRunning(executionGroup);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldCheckForHangingCalculation() {
        when(hangingCalculationFinalizerMock.finalizeHangingCalculationsAfter(Duration.ofHours(1))).thenReturn(List.of());

        objectUnderTest.checkHangingCalculations();

        verify(hangingCalculationFinalizerMock).finalizeHangingCalculationsAfter(Duration.ofHours(1));
    }

    static Stream<Arguments> provideScheduleData() {
        return Stream.of(
                Arguments.of("simple1", true),  // in progress
                Arguments.of("simple3", true),  // in queue
                Arguments.of("simple4", false)  // nowhere
        );
    }


    static KpiCalculationJob calculationJob(final UUID id, final Timestamp created, final String execGroup, final KpiType type) {
        final KpiCalculationJobBuilder builder = KpiCalculationJob.builder();
        builder.withCalculationId(id);
        builder.withTimeCreated(created);
        builder.withExecutionGroup(execGroup);
        builder.withJobType(type);
        return builder.build();
    }

}