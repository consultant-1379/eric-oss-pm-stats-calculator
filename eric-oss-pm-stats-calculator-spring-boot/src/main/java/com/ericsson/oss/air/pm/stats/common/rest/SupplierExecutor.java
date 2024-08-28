/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.rest;

import java.util.function.Supplier;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.vavr.control.Try;

/**
 * Utility class used to execute a {@link Supplier} Lambda function with <code>resilience4j</code> decorators.
 */
public class SupplierExecutor {

    public static final CircuitBreaker DEFAULT_CIRCUIT_BREAKER = CircuitBreaker.of("never",
            CircuitBreakerConfig.custom().ignoreExceptions(Throwable.class).build());
    public static final Retry DEFAULT_RETRY = Retry.of("never", RetryConfig.custom().maxAttempts(1).ignoreExceptions(Throwable.class).build());

    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public SupplierExecutor(final Retry retry) {
        circuitBreaker = DEFAULT_CIRCUIT_BREAKER;
        this.retry = retry;
    }

    public SupplierExecutor(final CircuitBreaker circuitBreaker, final Retry retry) {
        this.circuitBreaker = circuitBreaker;
        this.retry = retry;
    }

    /**
     * Executes the provided {@link Supplier} after decorating with:
     * <ol>
     * <li>{@link CircuitBreaker}</li>
     * <li>{@link Retry}</li>
     * </ol>
     *
     * @param supplier
     *            the supplier to be decorated and executed
     * @param <T>
     *            the return type of the {@link Supplier}
     * @return the result of the {@link Supplier}
     */
    public <T> T execute(final Supplier<T> supplier) {
        final Supplier<T> supplierWithCircuitBreaker = CircuitBreaker.decorateSupplier(circuitBreaker, supplier);
        final Supplier<T> supplierWithRetry = Retry.decorateSupplier(retry, supplierWithCircuitBreaker);
        return Try.ofSupplier(supplierWithRetry).get();
    }

}
