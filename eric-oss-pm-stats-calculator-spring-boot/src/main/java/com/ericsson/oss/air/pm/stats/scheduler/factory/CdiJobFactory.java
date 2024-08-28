/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.scheduler.factory;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CdiJobFactory implements JobFactory {

    private final ApplicationContext context;

    @Override
    public Job newJob(@NonNull final TriggerFiredBundle bundle, final Scheduler scheduler) throws SchedulerException {
        final Class<? extends Job> jobClass = bundle.getJobDetail().getJobClass();
        try {
            return context.getBean(jobClass);
        } catch (final BeansException e) {
            log.error(e.getMessage(), e);
            throw new SchedulerException(String.format("No job bean of type %s found.", jobClass));
        }
    }
}
