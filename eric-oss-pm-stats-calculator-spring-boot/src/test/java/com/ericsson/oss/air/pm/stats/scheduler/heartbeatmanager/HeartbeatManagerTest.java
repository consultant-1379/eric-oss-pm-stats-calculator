/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.scheduler.heartbeatmanager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import com.ericsson.oss.air.pm.stats.configuration.environment.model.EnvironmentValue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HeartbeatManagerTest {

    @Mock(name = "maxHeartbeatToWaitToRecalculateSimples")
    EnvironmentValue<Integer> maxHeartbeatToWaitToRecalculateSimplesMock;

    @InjectMocks
    HeartbeatManager objectUnderTest;

    @Test
    @Disabled("Disabled while migrating to Spring Boot, because EnvironmentalValue injection does not work with @AllArgsConstructor.")
    void shouldIncrementHeartbeatCounter() {
        assertThat(objectUnderTest.getHeartbeatCounter().get()).isZero();

        objectUnderTest.incrementHeartbeatCounter();

        assertThat(objectUnderTest.getHeartbeatCounter().get()).isOne();
    }

    @Test
    @Disabled("Disabled while migrating to Spring Boot, because EnvironmentalValue injection does not work with @AllArgsConstructor.")
    void shouldResetHeartbeatCounter() {
        objectUnderTest.incrementHeartbeatCounter();

        assertThat(objectUnderTest.getHeartbeatCounter().get()).isOne();

        objectUnderTest.resetHeartBeatCounter();

        assertThat(objectUnderTest.getHeartbeatCounter().get()).isZero();
    }

    @ValueSource(ints = {0, 1, 2, 3, 4})
    @ParameterizedTest(name = "Test #{index} with argument: {arguments}")
    @Disabled("Disabled while migrating to Spring Boot, because EnvironmentalValue injection does not work with @AllArgsConstructor.")
    void whenIncrementedEqualTimesAsConfig_thenSimplesCanWait(int numberOfIncrements) {
        doReturn(5).when(maxHeartbeatToWaitToRecalculateSimplesMock).value();

        for (int i = 1; i <= numberOfIncrements; i++) {
            objectUnderTest.incrementHeartbeatCounter();
        }

        assertThat(objectUnderTest.canSimplesWait()).isTrue();
    }

    @Test
    @Disabled("Disabled while migrating to Spring Boot, because EnvironmentalValue injection does not work with @AllArgsConstructor.")
    void whenIncrementedMoreTimeAsConfig_thenSimplesCanNotWait() {
        doReturn(5).when(maxHeartbeatToWaitToRecalculateSimplesMock).value();

        for (int i = 1; i <= 5; i++) {
            objectUnderTest.incrementHeartbeatCounter();
        }

        assertThat(objectUnderTest.canSimplesWait()).isFalse();
    }
}

