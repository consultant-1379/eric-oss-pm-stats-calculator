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

import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.withSettings;

import com.ericsson.oss.air.pm.stats.common.scheduler.ActivityRunner;
import com.ericsson.oss.air.pm.stats.common.scheduler.ActivityScheduler;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SchedulerRunnerProducerTest {
    @Mock
    ActivityScheduler activitySchedulerMock;

    @InjectMocks
    SchedulerRunnerProducer objectUnderTest;

    @Test
    void shouldProduceActivityRunner() {
        try (final MockedConstruction<ActivityRunner> activityRunnerMockedConstruction = mockConstruction(ActivityRunner.class, context -> {
            Assertions.assertThat(context.arguments()).first().isEqualTo(activitySchedulerMock);

            return withSettings();
        })) {
            objectUnderTest.provideActivityRunner();
        }
    }
}