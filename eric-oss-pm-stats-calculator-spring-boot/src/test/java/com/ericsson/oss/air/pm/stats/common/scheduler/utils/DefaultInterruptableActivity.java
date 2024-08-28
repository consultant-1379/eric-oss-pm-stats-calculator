/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.scheduler.utils;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import com.ericsson.oss.air.pm.stats.common.scheduler.Activity;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;

@Slf4j
public class DefaultInterruptableActivity extends Activity {

    private boolean completelyExecuted;
    private boolean interrupted;

    public DefaultInterruptableActivity() {
        super();
    }

    public DefaultInterruptableActivity(final String name) {
        super(name, new HashMap<>());
    }

    @Override
    public void run(final JobDataMap activityContext) {
        log.info("Activity Running");
        while (true) {
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            if (interrupted) {
                completelyExecuted = true;
                break;
            }
        }
    }

    @Override
    public void interrupt() {
        this.interrupted = true;
    }

    public boolean isCompletelyExecuted() {
        return completelyExecuted;
    }

    @Override
    public String toString() {
        return String.format("%s:: {name: '%s', context: '%s', executed: '%s'}", getClass().getSimpleName(), getName(), getContext(),
                completelyExecuted);
    }
}
