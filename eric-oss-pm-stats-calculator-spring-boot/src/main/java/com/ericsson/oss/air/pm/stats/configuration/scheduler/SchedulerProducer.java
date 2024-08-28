/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.configuration.scheduler;

import static lombok.AccessLevel.PUBLIC;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Qualifier;

import com.ericsson.oss.air.pm.stats.common.scheduler.ActivityScheduler;
import com.ericsson.oss.air.pm.stats.scheduler.factory.CdiJobFactory;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class SchedulerProducer {
    @Inject
    private CdiJobFactory cdiJobFactory;

    @Produces
    @Scheduler
    public ActivityScheduler provideScheduler() throws SchedulerException {
        final ActivityScheduler activityScheduler = ActivityScheduler.getInstance();

        activityScheduler.setJobFactory(cdiJobFactory);

        return activityScheduler;
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD})
    public @interface Scheduler {
    }
}
