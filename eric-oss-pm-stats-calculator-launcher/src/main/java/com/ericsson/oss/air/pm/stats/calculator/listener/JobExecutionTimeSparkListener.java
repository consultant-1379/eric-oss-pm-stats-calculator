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

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.SPARK_ERICSSON_JOB_DESCRIPTION;
import static lombok.AccessLevel.PACKAGE;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.KpiCalculatorSparkLauncher;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.scheduler.SparkListener;
import org.apache.spark.scheduler.SparkListenerJobEnd;
import org.apache.spark.scheduler.SparkListenerJobStart;
import org.apache.spark.scheduler.SparkListenerTaskEnd;
import org.apache.spark.scheduler.TaskInfo;

/**
 * Extend the calculation performed by Spark with additional functions:
 * <ul>
 *     <li>calculates, logs and pushes into a metric the execution time of the Spark job</li>
 *     <li>logs number of rows written by a task</li>
 * </ul>
 * <strong>NOTE:</strong> Class is being used as <strong>spark.extraListeners</strong> configuration in {@link KpiCalculatorSparkLauncher}.
 */
@Slf4j
@Getter(PACKAGE)
public class JobExecutionTimeSparkListener extends SparkListener {

    private final Map<Integer, SparkListenerJobStart> jobStartEventByJobId = new HashMap<>();
    private UUID calculationId;

    @Override
    public void onJobStart(final SparkListenerJobStart jobStart) {
        final Properties properties = jobStart.properties();

        jobStartEventByJobId.put(jobStart.jobId(), jobStart);
        calculationId = UUID.fromString(properties.getProperty("spark.calculationId"));
    }

    @Override
    public void onJobEnd(final SparkListenerJobEnd jobEnd) {
        final SparkListenerJobStart sparkListenerJobStart = jobStartEventByJobId.get(jobEnd.jobId());
        final long executionTimeInMillis = jobEnd.time() - sparkListenerJobStart.time();
        final String jobDescription = sparkListenerJobStart.properties().getProperty(SPARK_ERICSSON_JOB_DESCRIPTION);
        if (jobDescription == null) {
            return;
        }
        String executionGroup = sparkListenerJobStart.properties().getProperty("spark.executionGroup");

        MetricHelper.pushSparkJobExecutionTimeMetric(jobDescription, executionGroup, executionTimeInMillis);
        log.debug("Job '{}' (id-{}) executed in {}ms", jobDescription, jobEnd.jobId(), executionTimeInMillis);
    }

    @Override
    public void onTaskEnd(final SparkListenerTaskEnd taskEnd) {
        //  TODO: Here we can create metrics to follow the load distributed across Executors
        //        The idea here would be to create a counter <executor_id>-<written_rows>
        //        For now I leave this here just as a remainder - can UDF/UDAF provide metrics via TaskContext?
        final TaskInfo taskInfo = taskEnd.taskInfo();
        log.info("Task id '{}' executed via executor '{}' for calculationId: '{}' wrote '{}' records.",
                taskInfo.taskId(),
                taskInfo.executorId(),
                calculationId,
                Optional.ofNullable(taskEnd.taskMetrics()).map(t -> t.outputMetrics().recordsWritten()).map(String::valueOf).orElse("??"));
    }
}
