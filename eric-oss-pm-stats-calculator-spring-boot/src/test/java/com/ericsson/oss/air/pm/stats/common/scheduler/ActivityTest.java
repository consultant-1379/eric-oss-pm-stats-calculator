/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import org.assertj.core.data.MapEntry;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.JobExecutionContextImpl;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.spi.TriggerFiredBundle;

/**
 * Unit tests for {@link Activity}.
 */
class ActivityTest {
    static final MapEntry<String, Object> ACTIVITY_CONTEXT_ENTRY = entry("key", "value");
    static final Map<String, Object> ACTIVITY_CONTEXT = Collections.singletonMap(ACTIVITY_CONTEXT_ENTRY.key, ACTIVITY_CONTEXT_ENTRY.value);

    @Test
    void whenCreatingActivity_andRetrievingContext_thenContextIsImmutable() {
        final TestActivity activity = new TestActivity(ACTIVITY_CONTEXT);
        final Map<String, Object> result = activity.getContext();

        assertThat(result).containsExactly(ACTIVITY_CONTEXT_ENTRY);

        assertThatThrownBy(() -> result.put(ACTIVITY_CONTEXT_ENTRY.key,
                ACTIVITY_CONTEXT_ENTRY.value)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void whenExecuteActivity_thenRunMethodIsInvokedWithContext() throws JobExecutionException {
        final TestActivity activity = new TestActivity(ACTIVITY_CONTEXT);

        final JobDataMap map = new JobDataMap();
        map.putAll(ACTIVITY_CONTEXT);
        final JobDetailImpl jobDetail = new JobDetailImpl();
        jobDetail.setJobDataMap(map);

        final JobExecutionContext jobExecutionContext = new JobExecutionContextImpl(null,
                new TriggerFiredBundle(jobDetail, new CronTriggerImpl(), null, true, null, null, null, null), activity);
        activity.execute(jobExecutionContext);

        assertThat(activity.getValues())
                .hasSize(1)
                .containsExactly(ACTIVITY_CONTEXT_ENTRY.value);
    }

    @Test
    void whenExecuteActivity_andRunMethodThrowsError_thenJobExecutionExceptionIsPropagated() {
        final InvalidActivity activity = new InvalidActivity();
        final JobExecutionContext jobExecutionContext = new JobExecutionContextImpl(null,
                new TriggerFiredBundle(new JobDetailImpl(), new CronTriggerImpl(), null, true, null, null, null, null), activity);

        assertThatThrownBy(() -> activity.execute(jobExecutionContext)).isInstanceOf(JobExecutionException.class);
    }

    @Test
    void whenExecuteActivity_andActivityIsInterrupted_thenExceptionIsThrown() throws Exception {
        final Activity activity = new TestActivity(ACTIVITY_CONTEXT);

        final JobDataMap map = new JobDataMap();
        map.putAll(ACTIVITY_CONTEXT);
        final JobDetailImpl jobDetail = new JobDetailImpl();
        jobDetail.setJobDataMap(map);

        final JobExecutionContext jobExecutionContext = new JobExecutionContextImpl(null,
                new TriggerFiredBundle(jobDetail, new CronTriggerImpl(), null, true, null, null, null, null), activity);
        activity.execute(jobExecutionContext);

        assertThatThrownBy(activity::interrupt).isInstanceOf(UnableToInterruptJobException.class);
    }

    @Test
    void whenExecuteInterruptableActivity_andActivityIsInterrupted_thenJobExecutionDoesNotComplete() throws Exception {
        final InterruptableTestActivity activity = new InterruptableTestActivity(ACTIVITY_CONTEXT);

        final JobDataMap map = new JobDataMap();
        map.putAll(ACTIVITY_CONTEXT);
        final JobDetailImpl jobDetail = new JobDetailImpl();
        jobDetail.setJobDataMap(map);

        final JobExecutionContext jobExecutionContext = new JobExecutionContextImpl(null,
                new TriggerFiredBundle(jobDetail, new CronTriggerImpl(), null, true, null, null, null, null), activity);
        activity.execute(jobExecutionContext);
        activity.interrupt();

        TimeUnit.MILLISECONDS.sleep(10);

        assertThat(activity.interrupted).isTrue();
        assertThat(activity.getValues())
                .isEmpty();
    }

    @Test
    void whenExecuteInterruptableActivity_andActivityIsNotInterrupted_thenJobExecutionDoesComplete() throws Exception {
        final InterruptableTestActivity activity = new InterruptableTestActivity(ACTIVITY_CONTEXT);

        final JobDataMap map = new JobDataMap();
        map.putAll(ACTIVITY_CONTEXT);
        final JobDetailImpl jobDetail = new JobDetailImpl();
        jobDetail.setJobDataMap(map);

        final JobExecutionContext jobExecutionContext = new JobExecutionContextImpl(null,
                new TriggerFiredBundle(jobDetail, new CronTriggerImpl(), null, true, null, null, null, null), activity);
        activity.execute(jobExecutionContext);

        Awaitility.await()
                  .pollDelay(10, TimeUnit.MILLISECONDS)
                  .pollInterval(2, TimeUnit.MILLISECONDS)
                  .untilAsserted(() -> assertThat(activity.getValues()).containsExactly(ACTIVITY_CONTEXT_ENTRY.value));
    }

    @Getter
    static class TestActivity extends Activity {
        final List<Object> values = new ArrayList<>();

        TestActivity(final Map<String, Object> context) {
            super("", context);
        }

        @Override
        public void run(final JobDataMap activityContext) {
            values.addAll(activityContext.values());
        }
    }

    @Getter
    static class InterruptableTestActivity extends Activity {
        final List<Object> values = new ArrayList<>();
        boolean interrupted;

        InterruptableTestActivity(final Map<String, Object> context) {
            super("", context);
        }

        @Override
        public void run(final JobDataMap activityContext) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!interrupted) {
                        values.addAll(activityContext.values());
                    }
                }
            }, TimeUnit.MILLISECONDS.toMillis(5));
        }

        @Override
        public void interrupt() {
            interrupted = true;
        }
    }

    static class InvalidActivity extends Activity {

        @Override
        public void run(final JobDataMap activityContext) throws ActivityException {
            throw new ActivityException("");
        }
    }
}
