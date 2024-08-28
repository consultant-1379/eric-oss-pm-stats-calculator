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

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.ericsson.oss.air.pm.stats.common.scheduler.ActivityScheduler;
import com.ericsson.oss.air.pm.stats.scheduler.factory.CdiJobFactory;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SchedulerProducerTest {
    @Mock
    CdiJobFactory cdiJobFactoryMock;

    @InjectMocks
    SchedulerProducer objectUnderTest;

    @Test
    void shouldProvideScheduler(@Mock final ActivityScheduler activitySchedulerMock) throws Exception {
        try (final MockedStatic<ActivityScheduler> activitySchedulerMockedStatic = mockStatic(ActivityScheduler.class)) {
            activitySchedulerMockedStatic.when(ActivityScheduler::getInstance).thenReturn(activitySchedulerMock);

            final ActivityScheduler actual = objectUnderTest.provideScheduler();

            activitySchedulerMockedStatic.verify(ActivityScheduler::getInstance);
            verify(activitySchedulerMock).setJobFactory(cdiJobFactoryMock);

            Assertions.assertThat(actual).isEqualTo(activitySchedulerMock);
        }
    }
}