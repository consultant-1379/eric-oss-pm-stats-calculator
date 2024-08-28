/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.configuration.retry;

import java.time.Duration;

import com.ericsson.oss.air.pm.stats.configuration.retry.RetryProducer.RetryConfigurations;

import io.github.resilience4j.retry.Retry;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RetryProducerTest {
    private static final String RETRY_EXCEPTIONS = "retryExceptions";

    private RetryProducer objectUnderTest;

    @BeforeEach
    void setUp() {
        objectUnderTest = new RetryProducer();
    }

    @Test
    void shouldReturnRetryForUpdateRunningCalculationsToLostRetry() {
        final Retry actual = objectUnderTest.produceUpdateRunningCalculationsToLostRetry();

        Assertions.assertThat(actual.getName()).isEqualTo("handleLostStatesRetry");
        Assertions.assertThat(actual.getRetryConfig().getMaxAttempts()).isEqualTo(10);
        Assertions.assertThat(actual.getRetryConfig().getIntervalBiFunction().apply(0, null)).isEqualTo(Duration.ofSeconds(6).toMillis());
        Assertions.assertThat(actual.getRetryConfig())
                .extracting(RETRY_EXCEPTIONS, InstanceOfAssertFactories.array(Class[].class))
                .containsExactly(Throwable.class);
    }

    @Test
    void shouldReturnRetryForUpdateCalculationsStateRetry() {
        final Retry actual = objectUnderTest.produceUpdateCalculationsStateRetry();

        Assertions.assertThat(actual.getName()).isEqualTo("handleCalculationStateRetry");
        Assertions.assertThat(actual.getRetryConfig().getMaxAttempts()).isEqualTo(6);
        Assertions.assertThat(actual.getRetryConfig().getIntervalBiFunction().apply(0, null)).isEqualTo(Duration.ofSeconds(5).toMillis());
    }

    @Test
    void shouldReturnRetryForUpdateRenamedStateRetry() {
        final Retry actual = objectUnderTest.produceUpdateRenamedStateRetry();

        Assertions.assertThat(actual.getName()).isEqualTo("handleRenamedStateRetry");
        Assertions.assertThat(actual.getRetryConfig().getMaxAttempts()).isEqualTo(5);
        Assertions.assertThat(actual.getRetryConfig().getIntervalBiFunction().apply(0, null)).isEqualTo(Duration.ofSeconds(5).toMillis());
        Assertions.assertThat(actual.getRetryConfig())
                .extracting(RETRY_EXCEPTIONS, InstanceOfAssertFactories.array(Class[].class))
                .containsExactly(Throwable.class);
    }

    @Test
    void shouldVerifyRetryConfigurationEnum() {
        Assertions.assertThat(RetryConfigurations.values())
                .containsExactlyInAnyOrder(RetryConfigurations.UPDATE_RUNNING_CALCULATIONS_TO_LOST_RETRY,
                        RetryConfigurations.UPDATE_CALCULATION_STATE_RETRY,
                        RetryConfigurations.UPDATE_RENAMED_STATE_RETRY);
    }
}