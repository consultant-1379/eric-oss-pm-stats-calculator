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

import java.util.Collections;
import java.util.Map;

import com.ericsson.oss.air.pm.stats.common.scheduler.Activity;
import com.ericsson.oss.air.pm.stats.common.scheduler.ActivitySchedulerException;
import com.ericsson.oss.air.pm.stats.common.scheduler.CronSchedule;

public final class ActivityTestUtils {

    private static final String WEEKDAYS_AT_TWO_AM_SCHEDULE = "0 0 */6 ? * MON,TUE,WED,THU,FRI *";
    private static final Map<String, Object> DEFAULT_ACTIVITY_DATA = Collections.emptyMap();

    private ActivityTestUtils() {
    }

    public static Activity getActivityWithName(final String name) {
        return new DefaultActivity(name);
    }

    public static DefaultInterruptableActivity getInterruptableActivityWithName(final String name) {
        return new DefaultInterruptableActivity(name);
    }

    public static CronSchedule getCronScheduleWithName(final String name) throws ActivitySchedulerException {
        return new CronSchedule(name, DEFAULT_ACTIVITY_DATA, WEEKDAYS_AT_TWO_AM_SCHEDULE);
    }
}
