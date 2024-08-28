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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation.KpiCalculationJob;
import com.ericsson.oss.air.pm.stats.service.metric.KpiMetric;
import com.ericsson.oss.air.pm.stats.service.metric.KpiSchedulerMetricRegistry;

import com.codahale.metrics.MetricRegistry;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class KpiCalculationQueueTest {
    final KpiSchedulerMetricRegistry kpiSchedulerMetricRegistry = new KpiSchedulerMetricRegistry(new MetricRegistry());
    static final String SIMPLE_TEST_EXECUTION_GROUP = "simple_test_execgroup1";
    static final String COMPLEX_TEST_EXECUTION_GROUP = "complex_test_execgroup1";
    static final String ON_DEMAND_TEST_EXECUTION_GROUP = EXECUTION_GROUP_ON_DEMAND_CALCULATION;

    final UUID uuid1 = UUID.fromString("3e5bf5bb-4a93-40e8-a557-68c7cf024659");
    final UUID uuid2 = UUID.fromString("2a76b5e1-47bd-4b5c-a482-a2b18233640f");
    final UUID uuid3 = UUID.fromString("06e1a0b0-5e51-40b1-835c-3c122d26de9a");
    final UUID uuid4 = UUID.fromString("8279c247-4794-45cf-b649-5a3aac81fc51");
    final UUID uuid5 = UUID.fromString("ce68cd8d-16f6-4996-b831-2f77febac646");
    final UUID uuidComplex1 = UUID.fromString("2b0f57cb-e0e9-4e47-bc42-7531dba3f1d5");

    final Timestamp testTime = Timestamp.valueOf("2022-05-05 10:10:10");

    @Test
    void whenComplexJobIsAdded_thenComplexCanBePolled() {
        final KpiCalculationQueue objectUnderTest = new KpiCalculationQueue(3, 1, kpiSchedulerMetricRegistry);
        objectUnderTest.add(KpiCalculationJob.builder()
                .withCalculationId(uuidComplex1)
                .withTimeCreated(testTime)
                .withExecutionGroup(COMPLEX_TEST_EXECUTION_GROUP)
                .withJobType(KpiType.SCHEDULED_COMPLEX)
                .build());
        assertThat(objectUnderTest.getScheduledComplexCalculationJob().getCalculationId()).isEqualTo(uuidComplex1);
    }

    @Test
    void whenWeightsAre1And2_AndQueueIsEmpty_thenNullIsReturnedOnPolling() {
        final KpiCalculationQueue objectUnderTest = new KpiCalculationQueue(2, 1, kpiSchedulerMetricRegistry);
        assertThat(objectUnderTest.poll()).isNull();
    }

    @Test
    void whenJobsAreAddedAndPolledFromQueue_thenMetricsAreCorrectlyUpdated() {
        kpiSchedulerMetricRegistry.getMetricRegistry().remove(KpiMetric.ON_DEMAND_CALCULATION_QUEUE_REMAINING_WEIGHT.toString());
        kpiSchedulerMetricRegistry.getMetricRegistry().remove(KpiMetric.SCHEDULED_SIMPLE_CALCULATION_QUEUE_REMAINING_WEIGHT.toString());
        kpiSchedulerMetricRegistry.getMetricRegistry().remove(KpiMetric.ON_DEMAND_CALCULATION_QUEUE.toString());
        kpiSchedulerMetricRegistry.getMetricRegistry().remove(KpiMetric.SCHEDULED_SIMPLE_CALCULATION_QUEUE.toString());
        kpiSchedulerMetricRegistry.getMetricRegistry().remove(KpiMetric.SCHEDULED_COMPLEX_CALCULATION_QUEUE.toString());
        final StringBuilder processFlow = new StringBuilder(
                "Metric expected due to following flow of operations INIT:ON_DEMAND_WEIGHT-2:SCHEDULED_WEIGHT:1");
        final KpiCalculationQueue objectUnderTest = new KpiCalculationQueue(2, 1, kpiSchedulerMetricRegistry);
        verifyOnDemandJobMetrics(2, 0, processFlow);
        verifyScheduledJobMetrics(1, 0, processFlow);

        objectUnderTest.add(KpiCalculationJob.builder()
                .withCalculationId(uuid1)
                .withTimeCreated(testTime)
                .withExecutionGroup(ON_DEMAND_TEST_EXECUTION_GROUP)
                .build());
        processFlow.append(" -> ADD:ONDEMAND");
        verifyOnDemandJobMetrics(2, 1, processFlow);
        verifyScheduledJobMetrics(1, 0, processFlow);

        objectUnderTest.add(KpiCalculationJob.builder()
                .withCalculationId(uuid2)
                .withTimeCreated(testTime)
                .withExecutionGroup(SIMPLE_TEST_EXECUTION_GROUP)
                .build());
        processFlow.append(" -> ADD:SCHEDULED");
        verifyOnDemandJobMetrics(2, 1, processFlow);
        verifyScheduledJobMetrics(1, 1, processFlow);

        objectUnderTest.poll();
        processFlow.append(" -> POLL");
        verifyOnDemandJobMetrics(1, 0, processFlow);
        verifyScheduledJobMetrics(1, 1, processFlow);

        objectUnderTest.add(KpiCalculationJob.builder()
                .withCalculationId(uuid3)
                .withTimeCreated(testTime)
                .withExecutionGroup(ON_DEMAND_TEST_EXECUTION_GROUP)
                .build());
        processFlow.append(" -> ADD:ONDEMAND");
        verifyOnDemandJobMetrics(1, 1, processFlow);
        verifyScheduledJobMetrics(1, 1, processFlow);

        objectUnderTest.poll();
        processFlow.append(" -> POLL");
        verifyOnDemandJobMetrics(2, 0, processFlow);
        verifyScheduledJobMetrics(1, 1, processFlow);

        objectUnderTest.poll();
        processFlow.append(" -> POLL");
        verifyOnDemandJobMetrics(2, 0, processFlow);
        verifyScheduledJobMetrics(1, 0, processFlow);

        assertThat(kpiSchedulerMetricRegistry.getMetricRegistry().counter(KpiMetric.SCHEDULED_COMPLEX_CALCULATION_QUEUE.toString()).getCount()).isZero();
        objectUnderTest.add(KpiCalculationJob.builder()
                .withCalculationId(uuidComplex1)
                .withTimeCreated(testTime)
                .withExecutionGroup(COMPLEX_TEST_EXECUTION_GROUP)
                .withJobType(KpiType.SCHEDULED_COMPLEX)
                .build());
        assertThat(kpiSchedulerMetricRegistry.getMetricRegistry().counter(KpiMetric.SCHEDULED_COMPLEX_CALCULATION_QUEUE.toString()).getCount()).isOne();
        objectUnderTest.getScheduledComplexCalculationJob();
        assertThat(kpiSchedulerMetricRegistry.getMetricRegistry().counter(KpiMetric.SCHEDULED_COMPLEX_CALCULATION_QUEUE.toString()).getCount()).isZero();
    }

    @Test
    void whenWeightsAre1And2_And1SimpleAnd2OnDemandJobsInQueue_thenPollingReturns2OnDemandJobsThen1SimpleJob() {

        final List<TestCalculationJob> testCalculationJobs = new ArrayList<>();
        testCalculationJobs.add(new TestCalculationJob(uuid1, SIMPLE_TEST_EXECUTION_GROUP));
        testCalculationJobs.add(new TestCalculationJob(uuid2, ON_DEMAND_TEST_EXECUTION_GROUP));
        testCalculationJobs.add(new TestCalculationJob(uuid3, ON_DEMAND_TEST_EXECUTION_GROUP));
        KpiCalculationQueue testQueue = createTestQueueWithTasks(1, 2, testCalculationJobs);

        final List<UUID> expectedCalculationIds = List.of(uuid2, uuid3, uuid1);
        assertPoll(testQueue, expectedCalculationIds);
    }

    @Test
    void whenWeightsAre1And2_AndSimpleAndOnDemandJobsInQueue_thenPollingReturnsSimpleJobsAndTheseScheduledJobsAreCountedForGivenWeight() {

        final List<TestCalculationJob> testCalculationJobs = new ArrayList<>();
        testCalculationJobs.add(new TestCalculationJob(uuid1, SIMPLE_TEST_EXECUTION_GROUP));
        testCalculationJobs.add(new TestCalculationJob(uuid2, ON_DEMAND_TEST_EXECUTION_GROUP));
        testCalculationJobs.add(new TestCalculationJob(uuid3, ON_DEMAND_TEST_EXECUTION_GROUP));
        testCalculationJobs.add(new TestCalculationJob(uuid4, ON_DEMAND_TEST_EXECUTION_GROUP));
        testCalculationJobs.add(new TestCalculationJob(uuid5, SIMPLE_TEST_EXECUTION_GROUP));
        KpiCalculationQueue testQueue = createTestQueueWithTasks(1, 2, testCalculationJobs);

        final List<UUID> expectedCalculationIds = List.of(uuid2, uuid3, uuid1, uuid4, uuid5);
        assertPoll(testQueue, expectedCalculationIds);
    }

    @Test
    void whenWeightsAre1And2_AndNotSufficientOnDemandJobsInQueue_thenPollingReturnsSimpleJobs() {

        final List<TestCalculationJob> testCalculationJobs = new ArrayList<>();
        testCalculationJobs.add(new TestCalculationJob(uuid1, SIMPLE_TEST_EXECUTION_GROUP));
        testCalculationJobs.add(new TestCalculationJob(uuid2, SIMPLE_TEST_EXECUTION_GROUP));
        testCalculationJobs.add(new TestCalculationJob(uuid3, ON_DEMAND_TEST_EXECUTION_GROUP));
        KpiCalculationQueue testQueue = createTestQueueWithTasks(1, 2, testCalculationJobs);

        final List<UUID> expectedCalculationIds = List.of(uuid3, uuid1, uuid2);
        assertPoll(testQueue, expectedCalculationIds);
    }

    @Test
    void whenWeightsAre1And2_AndNoOnDemandJobsInQueue_thenPollingReturnsSimpleJob() {

        final List<TestCalculationJob> testCalculationJobs = new ArrayList<>();
        testCalculationJobs.add(new TestCalculationJob(uuid1, SIMPLE_TEST_EXECUTION_GROUP));
        KpiCalculationQueue testQueue = createTestQueueWithTasks(1, 2, testCalculationJobs);

        final List<UUID> expectedCalculationIds = List.of(uuid1);
        assertPoll(testQueue, expectedCalculationIds);
    }

    @Test
    void whenWeightsAre1And1_And2SimpleAnd2OnDemandJobsInQueue_thenPollingReturnsOnDemandAndJobsScheduledJobsAlternatively() {
        final List<TestCalculationJob> testCalculationJobs = new ArrayList<>();

        testCalculationJobs.add(new TestCalculationJob(uuid1, ON_DEMAND_TEST_EXECUTION_GROUP));
        testCalculationJobs.add(new TestCalculationJob(uuid2, SIMPLE_TEST_EXECUTION_GROUP));
        testCalculationJobs.add(new TestCalculationJob(uuid3, SIMPLE_TEST_EXECUTION_GROUP));
        testCalculationJobs.add(new TestCalculationJob(uuid4, ON_DEMAND_TEST_EXECUTION_GROUP));
        KpiCalculationQueue testQueue = createTestQueueWithTasks(1, 1, testCalculationJobs);

        final List<UUID> expectedCalculationIds = List.of(uuid1, uuid2, uuid4, uuid3);
        assertPoll(testQueue, expectedCalculationIds);
    }

    @ParameterizedTest(name = "[{index}] execution group: ''{0}'', can be found in the queue: ''{1}''")
    @MethodSource("provideIsQueuedData")
    void whenSimpleGroupIsCheckedInQueueReturnsCorrectAnswer(final String executionGroup, final boolean expected) {
        final List<TestCalculationJob> testCalculationJobs = new ArrayList<>();

        testCalculationJobs.add(new TestCalculationJob(uuid1, SIMPLE_TEST_EXECUTION_GROUP));
        KpiCalculationQueue objectUnderTest = createTestQueueWithTasks(1, 1, testCalculationJobs);

        final boolean actual = objectUnderTest.isSimpleQueued(executionGroup);

        assertThat(actual).isEqualTo(expected);
    }

    KpiCalculationQueue createTestQueueWithTasks(final Integer simpleCalculationWeight, final Integer onDemandCalculationWeight,
                                                 final List<TestCalculationJob> testCalculationJobs) {
        final KpiCalculationQueue testQueue = new KpiCalculationQueue(onDemandCalculationWeight, simpleCalculationWeight, kpiSchedulerMetricRegistry);

        for (final TestCalculationJob testCalculationJob : testCalculationJobs) {
            testQueue.add(KpiCalculationJob.builder()
                    .withCalculationId(testCalculationJob.getCalculationId())
                    .withTimeCreated(testTime)
                    .withExecutionGroup(testCalculationJob.getExecutionGroup())
                    .build());
        }

        return testQueue;
    }

    void assertPoll(final KpiCalculationQueue testQueue, final List<UUID> expectedCalculationIds) {
        for (UUID uuid : expectedCalculationIds) {
            assertThat(testQueue.poll().getCalculationId()).isEqualTo(uuid);
        }
    }

    void verifyOnDemandJobMetrics(final int expectedOnDemandCalculationQueueRemainingWeight, final int expectedOnDemandCalculationQueueCount,
                                  final StringBuilder processFlow) {
        assertThat(kpiSchedulerMetricRegistry.counter(KpiMetric.ON_DEMAND_CALCULATION_QUEUE_REMAINING_WEIGHT.toString()).getCount())
                .as(processFlow.toString()).isEqualTo(expectedOnDemandCalculationQueueRemainingWeight);
        assertThat(kpiSchedulerMetricRegistry.counter(KpiMetric.ON_DEMAND_CALCULATION_QUEUE.toString()).getCount())
                .as(processFlow.toString()).isEqualTo(expectedOnDemandCalculationQueueCount);
    }

    void verifyScheduledJobMetrics(final int expectedScheduledCalculationQueueRemainingWeight,
                                   final int expectedScheduledCalculationQueueCount, final StringBuilder processFlow) {
        assertThat(kpiSchedulerMetricRegistry.counter(KpiMetric.SCHEDULED_SIMPLE_CALCULATION_QUEUE_REMAINING_WEIGHT.toString()).getCount())
                .as(processFlow.toString()).isEqualTo(expectedScheduledCalculationQueueRemainingWeight);
        assertThat(kpiSchedulerMetricRegistry.counter(KpiMetric.SCHEDULED_SIMPLE_CALCULATION_QUEUE.toString()).getCount())
                .as(processFlow.toString()).isEqualTo(expectedScheduledCalculationQueueCount);
    }

    static Stream<Arguments> provideIsQueuedData() {
        return Stream.of(
                Arguments.of(SIMPLE_TEST_EXECUTION_GROUP, true),
                Arguments.of("unknown", false)
        );
    }

    @Getter
    static class TestCalculationJob {
        final UUID calculationId;
        final String executionGroup;

        TestCalculationJob(UUID calculationId, String executionGroup) {
            this.calculationId = calculationId;
            this.executionGroup = executionGroup;
        }
    }
}