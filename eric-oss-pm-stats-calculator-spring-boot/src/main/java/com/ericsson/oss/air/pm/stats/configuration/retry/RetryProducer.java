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

import static com.ericsson.oss.air.pm.stats.configuration.retry.RetryProducer.RetryConfigurations.UPDATE_CALCULATION_STATE_RETRY;
import static com.ericsson.oss.air.pm.stats.configuration.retry.RetryProducer.RetryConfigurations.UPDATE_RENAMED_STATE_RETRY;
import static com.ericsson.oss.air.pm.stats.configuration.retry.RetryProducer.RetryConfigurations.UPDATE_RUNNING_CALCULATIONS_TO_LOST_RETRY;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Duration;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Qualifier;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

@ApplicationScoped
public class RetryProducer {

    @Produces
    @RetryConfiguration(UPDATE_RUNNING_CALCULATIONS_TO_LOST_RETRY)
    public Retry produceUpdateRunningCalculationsToLostRetry() {
        return Retry.of("handleLostStatesRetry",
                RetryConfig.custom()
                        .retryExceptions(Throwable.class)
                        .maxAttempts(10)
                        .waitDuration(Duration.ofSeconds(6))
                        .build());
    }

    @Produces
    @RetryConfiguration(UPDATE_CALCULATION_STATE_RETRY)
    public Retry produceUpdateCalculationsStateRetry() {
        return Retry.of("handleCalculationStateRetry",
                RetryConfig.custom()
                        .maxAttempts(6)
                        .waitDuration(Duration.ofSeconds(5))
                        .build());
    }

    @Produces
    @RetryConfiguration(UPDATE_RENAMED_STATE_RETRY)
    public Retry produceUpdateRenamedStateRetry() {
        return Retry.of("handleRenamedStateRetry",
                RetryConfig.custom()
                        .retryExceptions(Throwable.class)
                        .maxAttempts(5)
                        .waitDuration(Duration.ofSeconds(5))
                        .build());
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD})
    public @interface RetryConfiguration {
        RetryConfigurations value();
    }

    public enum RetryConfigurations {
        UPDATE_RUNNING_CALCULATIONS_TO_LOST_RETRY,
        UPDATE_CALCULATION_STATE_RETRY,
        UPDATE_RENAMED_STATE_RETRY
    }
}
