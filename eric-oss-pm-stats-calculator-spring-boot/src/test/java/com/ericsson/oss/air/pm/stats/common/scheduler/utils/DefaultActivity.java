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
public class DefaultActivity extends Activity {

    private final boolean executed;

    public DefaultActivity() {
        super();
        executed = false;
    }

    public DefaultActivity(final String name) {
        super(name, new HashMap<>());
        executed = false;
    }

    @Override
    public void run(final JobDataMap activityContext) {
        log.info("Activity Running");
        try {
            TimeUnit.MILLISECONDS.sleep(150);
        } catch (final InterruptedException e) {
            log.debug("Error sleeping", e);
        }
    }

    @Override
    public String toString() {
        return String.format("%s:: {name: '%s', context: '%s', executed: '%s'}", getClass().getSimpleName(), getName(), getContext(), executed);
    }
}
