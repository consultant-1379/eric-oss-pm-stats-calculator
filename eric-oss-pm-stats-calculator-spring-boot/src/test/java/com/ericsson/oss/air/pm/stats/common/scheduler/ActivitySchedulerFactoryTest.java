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

import static org.mockito.Mockito.mockStatic;

import java.io.IOException;

import com.ericsson.oss.air.pm.stats.common.resources.utils.ResourceLoaderUtils;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.Verification;
import org.quartz.Scheduler;

class ActivitySchedulerFactoryTest {
    @Test
    void shouldCreateAndStartScheduler() throws Exception {
        final Scheduler actual = ActivitySchedulerFactory.createAndStart();
        Assertions.assertThat(actual.isStarted()).isTrue();
    }

    @Test
    void shouldThrowActivitySchedulerException_whenCreateAndStart_andPropertiesAreNotFound() {
        try (final MockedStatic<ResourceLoaderUtils> resourceLoaderUtilsMockedStatic = mockStatic(ResourceLoaderUtils.class)) {
            final Verification verification = () -> ResourceLoaderUtils.getClasspathResourceAsProperties("scheduler.properties");
            resourceLoaderUtilsMockedStatic.when(verification).thenThrow(new IOException());

            Assertions.assertThatThrownBy(ActivitySchedulerFactory::createAndStart)
                      .hasCauseInstanceOf(IOException.class)
                      .isInstanceOf(ActivitySchedulerException.class)
                      .hasMessage("Unable to start activity scheduler, scheduled activities will not execute");

            resourceLoaderUtilsMockedStatic.verify(verification);
        }
    }
}