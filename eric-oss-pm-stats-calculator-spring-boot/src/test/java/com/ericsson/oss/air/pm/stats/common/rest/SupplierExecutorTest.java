/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.atomic.AtomicLong;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SupplierExecutor}.
 */
class SupplierExecutorTest {

    static final IllegalArgumentException EXCEPTION_TO_BE_THROWN = new IllegalArgumentException("exception message");
    static final String EXCEPTION_NAME = EXCEPTION_TO_BE_THROWN.getClass().getSimpleName();
    static final IntervalFunction INTERVAL_FUNCTION = IntervalFunction.ofExponentialBackoff(10, 1.0D);
    static final String SUCCESS = "SUCCESS";

    @Test
    void whenExecutingSupplierAndPredicateIncludesLogger_andSupplierReturnsUnwantedResult_thenRetriesAreLogged() {
        final AtomicLong retryCounter = new AtomicLong();
        final int target = 4;
        final RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(target + 1)
                .retryOnResult(integer -> {
                    retryCounter.incrementAndGet();
                    return (Integer) integer < target;
                })
                .intervalFunction(INTERVAL_FUNCTION)
                .build();
        final Retry retry = Retry.of("custom", retryConfig);

        final IterateIntsFrom0TestLambdaFunction testLambdaFunction = new IterateIntsFrom0TestLambdaFunction(retry);
        final int result = testLambdaFunction.executeFunction();

        assertThat(retryCounter).hasValue(target);
        assertThat(result).isEqualTo(target);
        assertThat(testLambdaFunction.getNumberOfExecutions()).isEqualTo(target);
    }

    @Test
    void whenExecutingSupplierAndPredicateIncludesLogger_andSupplyFails_thenRetriesAreLogged() {
        final AtomicLong retryCounter = new AtomicLong();

        final RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(3)
                .retryOnException(throwable -> {
                    retryCounter.incrementAndGet();
                    return throwable instanceof IllegalArgumentException;
                })
                .intervalFunction(INTERVAL_FUNCTION)
                .build();
        final Retry retry = Retry.of("custom", retryConfig);

        final TestLambdaFunction testLambdaFunction = new TestLambdaFunction(retry, SupplierExecutor.DEFAULT_CIRCUIT_BREAKER);
        try {
            testLambdaFunction.executeFunction();
            fail(String.format("Expected %s", EXCEPTION_NAME));
        } catch (final IllegalArgumentException e) {
            assertThat(retryCounter).hasValue(3);
            assertThat(testLambdaFunction.getNumberOfExecutions()).isEqualTo(3);
        }
    }

    @Test
    void whenExecutingSupplierAndPredicateIncludesLogger_andSupplyIsSuccessful_thenRetriesAreLogged() {
        final AtomicLong retryCounter = new AtomicLong();

        final RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(10)
                .retryOnException(throwable -> {
                    retryCounter.incrementAndGet();
                    return throwable instanceof IllegalArgumentException;
                })
                .intervalFunction(INTERVAL_FUNCTION)
                .build();
        final Retry retry = Retry.of("custom", retryConfig);

        final FailTwiceTestLambdaFunction testLambdaFunction = new FailTwiceTestLambdaFunction(retry);

        assertThat(testLambdaFunction.executeFunction()).isEqualTo(SUCCESS);

        assertThat(retryCounter).hasValue(2);
        assertThat(testLambdaFunction.getNumberOfExecutions()).isEqualTo(3);
    }

    @Test
    void whenExecutingSupplier_givenAnExceptionIsThrown_andNoRetryOrCircuitBreakerIsDefined_thenExecutionIsOnlyMadeOnce() {
        final TestLambdaFunction testLambdaFunction = new TestLambdaFunction(SupplierExecutor.DEFAULT_RETRY,
                SupplierExecutor.DEFAULT_CIRCUIT_BREAKER);

        try {
            testLambdaFunction.executeFunction();
            fail(String.format("Expected %s", EXCEPTION_NAME));
        } catch (final IllegalArgumentException e) {
            assertThat(testLambdaFunction.getNumberOfExecutions()).isEqualTo(1);
        }
    }

    @Test
    void whenExecutingSupplier_givenAnExceptionIsThrown_andARetryWith5Attempts_thenExecutionIsMade5Times() {
        final RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(5)
                .intervalFunction(INTERVAL_FUNCTION)
                .retryExceptions(IllegalArgumentException.class)
                .build();
        final Retry retry = Retry.of("custom", retryConfig);

        final TestLambdaFunction testLambdaFunction = new TestLambdaFunction(retry, SupplierExecutor.DEFAULT_CIRCUIT_BREAKER);

        try {
            testLambdaFunction.executeFunction();
            fail(String.format("Expected %s", EXCEPTION_NAME));
        } catch (final IllegalArgumentException e) {
            assertThat(testLambdaFunction.getNumberOfExecutions()).isEqualTo(5);
        }
    }

    @Test
    void whenExecutingSupplier_givenAnExceptionIsThrown_andACircuitBreakerWithOpenAfter3Failures_andAfterThreeAttempts_thenTheFourthPropagatedCircuitBreakerOpenException() {
        final CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .ringBufferSizeInClosedState(3)
                .build();
        final CircuitBreaker circuitBreaker = CircuitBreaker.of("custom", circuitBreakerConfig);

        final TestLambdaFunction testLambdaFunction1 = new TestLambdaFunction(SupplierExecutor.DEFAULT_RETRY, circuitBreaker);

        try {
            testLambdaFunction1.executeFunction();
            fail(String.format("Expected %s", EXCEPTION_NAME));
        } catch (final IllegalArgumentException e) {
            assertThat(testLambdaFunction1.getNumberOfExecutions()).isEqualTo(1);
        }

        final TestLambdaFunction testLambdaFunction2 = new TestLambdaFunction(SupplierExecutor.DEFAULT_RETRY, circuitBreaker);

        try {
            testLambdaFunction2.executeFunction();
            fail(String.format("Expected %s", EXCEPTION_NAME));
        } catch (final IllegalArgumentException e) {
            assertThat(testLambdaFunction2.getNumberOfExecutions()).isEqualTo(1);
        }

        final TestLambdaFunction testLambdaFunction3 = new TestLambdaFunction(SupplierExecutor.DEFAULT_RETRY, circuitBreaker);

        try {
            testLambdaFunction3.executeFunction();
            fail(String.format("Expected %s", EXCEPTION_NAME));
        } catch (final IllegalArgumentException e) {
            assertThat(testLambdaFunction3.getNumberOfExecutions()).isEqualTo(1);
        }

        final TestLambdaFunction testLambdaFunction4 = new TestLambdaFunction(SupplierExecutor.DEFAULT_RETRY, circuitBreaker);

        try {
            testLambdaFunction4.executeFunction();
            fail(String.format("Expected %s", CallNotPermittedException.class.getSimpleName()));
        } catch (final CallNotPermittedException e) {
            assertThat(testLambdaFunction4.getNumberOfExecutions()).isEqualTo(0);
        }
    }

    @Test
    void whenExecutingSupplier_givenAnExceptionIsThrown_andACircuitBreakerWithOpenLoopAfter3Failures_andRetryWith5Attempts_thenCircuitBreakerOpenExceptionIsPropagated() {
        final RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(5)
                .intervalFunction(INTERVAL_FUNCTION)
                .retryExceptions(IllegalArgumentException.class)
                .build();
        final Retry retry = Retry.of("custom", retryConfig);

        final CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .ringBufferSizeInClosedState(3)
                .build();
        final CircuitBreaker circuitBreaker = CircuitBreaker.of("custom", circuitBreakerConfig);

        final TestLambdaFunction testLambdaFunction = new TestLambdaFunction(retry,
                circuitBreaker);

        try {
            testLambdaFunction.executeFunction();
            fail(String.format("Expected %s", CallNotPermittedException.class.getSimpleName()));
        } catch (final CallNotPermittedException e) {
            assertThat(testLambdaFunction.getNumberOfExecutions()).isEqualTo(3);
        }
    }

    /**
     * Test class that contains a Lambda function that throws an exception and counts the number of times it was executed.
     */
    static class TestLambdaFunction {

        final SupplierExecutor supplierExecutor;

        int numberOfExecutions;

        TestLambdaFunction(final Retry retry, final CircuitBreaker circuitBreaker) {
            supplierExecutor = new SupplierExecutor(circuitBreaker, retry);
        }

        void executeFunction() {
            supplierExecutor.execute(() -> {
                numberOfExecutions++;
                throw EXCEPTION_TO_BE_THROWN;
            });
        }

        int getNumberOfExecutions() {
            return numberOfExecutions;
        }
    }

    /**
     * Test class that contains a Lambda function that throws an exception and counts the number of times it was executed.
     */
    static class FailTwiceTestLambdaFunction {
        final SupplierExecutor supplierExecutor;

        int numberOfExecutions;

        FailTwiceTestLambdaFunction(final Retry retry) {
            supplierExecutor = new SupplierExecutor(retry);
        }

        String executeFunction() {
            return supplierExecutor.execute(() -> {
                if (++numberOfExecutions < 3) {
                    throw EXCEPTION_TO_BE_THROWN;
                }
                return SUCCESS;
            });
        }

        int getNumberOfExecutions() {
            return numberOfExecutions;
        }
    }

    /**
     * Test class that contains a Lambda function that returns the number of times it has been executed.
     */
    static class IterateIntsFrom0TestLambdaFunction {
        final SupplierExecutor supplierExecutor;

        int numberOfExecutions;

        IterateIntsFrom0TestLambdaFunction(final Retry retry) {
            supplierExecutor = new SupplierExecutor(retry);
        }

        int executeFunction() {
            return supplierExecutor.execute(() -> ++numberOfExecutions);
        }

        int getNumberOfExecutions() {
            return numberOfExecutions;
        }
    }

}
