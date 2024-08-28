/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.util;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiCalculatorErrorCode;
import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiCalculatorException;
import com.ericsson.oss.air.pm.stats.common.rest.RestExecutor;
import com.ericsson.oss.air.pm.stats.common.rest.RestResponse;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;

@Slf4j
public class CalculationRequestHandler {
    private static final Set<Integer> RECOVERABLE_STATUS_CODES = new HashSet<>(2);
    private static final int TOO_MANY_REQUESTS = 429;
    private static final String KPI_CALCULATION = "kpiCalculation";

    private final ServiceRestExecutor serviceRestExecutor;
    private final int maxRetryAttempts;
    private final int retryWaitDuration;

    public CalculationRequestHandler(final int maxRetryAttempts, final int retryWaitDuration) {
        this.maxRetryAttempts = maxRetryAttempts;
        this.retryWaitDuration = retryWaitDuration;
        RECOVERABLE_STATUS_CODES.add(TOO_MANY_REQUESTS);
        RECOVERABLE_STATUS_CODES.add(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        serviceRestExecutor = new ServiceRestExecutor.Builder()
            .withRestExecutor(new RestExecutor())
            .withCircuitBreaker(getKpiCalculationRequestCircuitBreaker())
            .withRetry(getKpiCalculationRequestRetry())
            .build();
    }

    /**
     * Sends a request to the <code>eric-oss-pm-stats-calculator</code> to calculate on demand KPI.
     *
     * @param calculationRequestAsJson
     *            the file path to the JSON file which represents the calculation request
     * @return the REST response received from <code>eric-oss-pm-stats-calculator</code>
     * @throws KpiCalculatorException
     *             if the KPI calculation is not completed successfully
     */
    public RestResponse<String> sendKpiCalculationRequest(final String calculationRequestAsJson) {
        final RestResponse<String> kpiCalculationRequestResponse = serviceRestExecutor.postCalculationRequest(calculationRequestAsJson);
        if (HttpStatus.SC_CREATED != kpiCalculationRequestResponse.getStatus()) {
            throw new KpiCalculatorException(KpiCalculatorErrorCode.KPI_CALCULATION_ERROR,
                                             "Failed to perform KPI calculation: " + kpiCalculationRequestResponse);
        }
        return kpiCalculationRequestResponse;
    }

    private Retry getKpiCalculationRequestRetry() {
        final RetryConfig retryConfig = RetryConfig.custom()
                                                   .maxAttempts(maxRetryAttempts)
                                                   .waitDuration(Duration.ofSeconds(retryWaitDuration))
                                                   .retryOnResult(CalculationRequestHandler::retryResponse)
                                                   .retryOnException(throwable -> {
                                                       log.warn("Failed to calculate KPI (an exception occurred), retrying", throwable);
                                                       return true;
                                                   })
                                                   .build();
        return Retry.of(KPI_CALCULATION, retryConfig);
    }

    private static boolean retryResponse(final Object r) {
        final RestResponse response = (RestResponse) r;
        final int statusCode = response.getStatus();
        if (log.isDebugEnabled()) {
            log.debug("KPI calculation request returned status code {}", statusCode);
        }
        return responseIsRecoverable(statusCode);
    }

    private static boolean responseIsRecoverable(final int statusCode) {
        if (RECOVERABLE_STATUS_CODES.contains(statusCode)) {
            log.warn("Failed to calculate KPI (status code: {}), retrying", statusCode);
            return true;
        }
        return false;
    }

    private static CircuitBreaker getKpiCalculationRequestCircuitBreaker() {
        final CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                                                                              .failureRateThreshold(80)
                                                                              .slidingWindowSize(10)
                                                                              .minimumNumberOfCalls(10)
                                                                              .permittedNumberOfCallsInHalfOpenState(3)
                                                                              .enableAutomaticTransitionFromOpenToHalfOpen()
                                                                              .waitDurationInOpenState(Duration.ofMinutes(1))
                                                                              .build();
        return CircuitBreaker.of(KPI_CALCULATION, circuitBreakerConfig);
    }

}
