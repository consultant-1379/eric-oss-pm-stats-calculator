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

import java.io.IOException;
import java.util.Properties;

import com.ericsson.oss.air.pm.stats.common.resources.utils.ResourceLoaderUtils;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Factory class to create an instance of a {@link Scheduler}.
 */
final class ActivitySchedulerFactory {

    private ActivitySchedulerFactory() {

    }

    /**
     * Creates a {@link Scheduler} using default properties, and starts it.
     *
     * @return the {@link Scheduler}
     * @throws ActivitySchedulerException
     *             thrown if there is an issue when instantiating the scheduler
     */
    static Scheduler createAndStart() throws ActivitySchedulerException {
        try {
            final Properties schedulerProperties = getDefaultProperties();
            final Scheduler scheduler = new StdSchedulerFactory(schedulerProperties).getScheduler();
            scheduler.start();
            return scheduler;
        } catch (final SchedulerException | IOException e) {
            throw new ActivitySchedulerException("Unable to start activity scheduler, scheduled activities will not execute", e);
        }
    }

    private static Properties getDefaultProperties() throws IOException {
        return ResourceLoaderUtils.getClasspathResourceAsProperties("scheduler.properties");
    }
}
