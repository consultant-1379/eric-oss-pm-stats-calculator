/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.listener;

import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.spark.scheduler.SparkListenerJobEnd;
import org.apache.spark.scheduler.SparkListenerJobStart;
import org.apache.spark.scheduler.SparkListenerTaskEnd;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class JobExecutionTimeSparkListenerTest {
    static final int JOB_ID = 1;

    static final String JOB_DESCRIPTION_SPARK_ERICSSON = "spark.ericsson.job.description";
    static final String SPARK_EXECUTION_GROUP = "spark.executionGroup";
    static final String JOB_DESCRIPTION = "jobDescription";

    JobExecutionTimeSparkListener objectUnderTest = new JobExecutionTimeSparkListener();

    @Test
    void shouldVerifyOnJobStart_theRequiredFieldsAreStored() {
        final UUID calculationId = UUID.fromString("5e4bfd5b-c66d-48d9-8c9d-cf40620d47be");
        final SparkListenerJobStart sparkListenerJobStartMock = mock(SparkListenerJobStart.class, RETURNS_DEEP_STUBS);

        when(sparkListenerJobStartMock.jobId()).thenReturn(JOB_ID);
        when(sparkListenerJobStartMock.properties().getProperty("spark.calculationId")).thenReturn(calculationId.toString());

        objectUnderTest.onJobStart(sparkListenerJobStartMock);

        verify(sparkListenerJobStartMock).jobId();
        verify(sparkListenerJobStartMock.properties()).getProperty("spark.calculationId");

        Assertions.assertThat(objectUnderTest.getJobStartEventByJobId()).containsExactly(entry(JOB_ID, sparkListenerJobStartMock));
        Assertions.assertThat(objectUnderTest.getCalculationId()).isEqualTo(calculationId);
    }

    @Test
    void whenTaskEnds_thenTaskInfoIsRetrieved() {
        final SparkListenerTaskEnd sparkListenerTaskEndMock = mock(SparkListenerTaskEnd.class, RETURNS_DEEP_STUBS);

        when(sparkListenerTaskEndMock.taskInfo().taskId()).thenReturn(10L);
        when(sparkListenerTaskEndMock.taskInfo().executorId()).thenReturn("executor-id");
        when(sparkListenerTaskEndMock.taskMetrics().outputMetrics().recordsWritten()).thenReturn(100L);

        objectUnderTest.onTaskEnd(sparkListenerTaskEndMock);

        verify(sparkListenerTaskEndMock.taskInfo()).taskId();
        verify(sparkListenerTaskEndMock.taskInfo()).executorId();
        verify(sparkListenerTaskEndMock.taskMetrics().outputMetrics()).recordsWritten();
    }

    @Test
    void shouldVerifyOnJobEnd_theMetricsArePushed() throws IllegalAccessException {
        try (final MockedStatic<MetricHelper> metricHelperMockedStatic = mockStatic(MetricHelper.class)) {
            final SparkListenerJobStart sparkListenerJobStartMock = mock(SparkListenerJobStart.class);
            final SparkListenerJobEnd sparkListenerJobEndMock = mock(SparkListenerJobEnd.class);
            final Properties propertiesMock = mock(Properties.class);

            FieldUtils.writeDeclaredField(objectUnderTest, "jobStartEventByJobId", Collections.singletonMap(JOB_ID, sparkListenerJobStartMock), true);

            final long endTime = 10L;
            final long startTime = 5L;

            when(sparkListenerJobEndMock.jobId()).thenReturn(JOB_ID);
            when(sparkListenerJobEndMock.time()).thenReturn(endTime);
            when(sparkListenerJobStartMock.time()).thenReturn(startTime);
            when(sparkListenerJobStartMock.properties()).thenReturn(propertiesMock, propertiesMock);
            when(propertiesMock.getProperty(JOB_DESCRIPTION_SPARK_ERICSSON)).thenReturn(JOB_DESCRIPTION);
            when(propertiesMock.getProperty(SPARK_EXECUTION_GROUP)).thenReturn("executionGroup");

            final long executionTimeInMillis = endTime - startTime;
            when(sparkListenerJobEndMock.jobId()).thenReturn(JOB_ID);

            objectUnderTest.onJobEnd(sparkListenerJobEndMock);

            verify(sparkListenerJobEndMock, times(2)).jobId();
            verify(sparkListenerJobEndMock).time();
            verify(sparkListenerJobStartMock).time();
            verify(sparkListenerJobStartMock, times(2)).properties();
            verify(propertiesMock).getProperty(JOB_DESCRIPTION_SPARK_ERICSSON);
            verify(propertiesMock).getProperty(SPARK_EXECUTION_GROUP);
            metricHelperMockedStatic.verify(() -> MetricHelper.pushSparkJobExecutionTimeMetric(JOB_DESCRIPTION, "executionGroup", executionTimeInMillis));
        }
    }
}