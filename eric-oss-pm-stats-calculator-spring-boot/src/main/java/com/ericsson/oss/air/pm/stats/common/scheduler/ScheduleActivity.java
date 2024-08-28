/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.scheduler;

import java.util.Collections;
import java.util.Map;

import org.quartz.JobDataMap;

/**
 * Class used for scheduling an activity. The logic for the process must be implemented in the run method.
 */
public class ScheduleActivity extends Activity {

    private static final String ACTIVITY_APPENDER = "_activity";

    public ScheduleActivity() {
        super();
    }

    /**
     * Constructor with the params.
     *
     * @param name
     *            {@link String} name of the activity.
     * @param map
     *            {@link Map} of {@link String} and {@link Object} params used inside the overridden method run.
     */
    public ScheduleActivity(final String name, final Map<String, Object> map) {
        super(name, map);
    }

    ScheduleActivity createInstance(final String contextEntry) {
        final Map<String, Object> paramsMap = Collections.singletonMap(contextEntry, contextEntry);
        return new ScheduleActivity(contextEntry + ACTIVITY_APPENDER, paramsMap);
    }

    @Override
    public void run(final JobDataMap activityContext) throws ActivityException {
        //  Implemented by the subclasses
    }
}
