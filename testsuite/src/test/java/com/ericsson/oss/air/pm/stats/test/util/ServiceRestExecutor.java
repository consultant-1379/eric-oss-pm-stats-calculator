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
import static com.ericsson.oss.air.pm.stats.common.env.Environment.getEnvironmentValue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import com.ericsson.oss.air.pm.stats.common.rest.RestExecutor;
import com.ericsson.oss.air.pm.stats.common.rest.RestResponse;
import com.ericsson.oss.air.pm.stats.common.rest.SupplierExecutor;
import com.ericsson.oss.air.pm.stats.common.rest.builder.EnmJsonHttpPostRequestCreator;
import com.ericsson.oss.air.pm.stats.common.rest.builder.EnmJsonHttpPutRequestCreator;
import com.ericsson.oss.air.pm.stats.common.rest.exception.RestExecutionException;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;

/**
 * Utility class to access <code>kpi-service</code> through its REST endpoints.
 */

@Slf4j
public final class ServiceRestExecutor {

    private static final String CONTENT_TYPE_VALUE = "application/json; charset=UTF-8";
    private static final String KPI_CALCULATION = "calc/v1/calculations";
    private static final String KPI_CALCULATION_STATE = "calc/v1/calculations/";
    private static final String KPIS = "model/v1/definitions/";

    private final RestExecutor restExecutor;
    private final SupplierExecutor supplierExecutor;
    private final String kpiServiceBaseUrl;

    public ServiceRestExecutor(final RestExecutor restExecutor, final Retry retry, final CircuitBreaker circuitBreaker) {
        final String kpiServiceHostName = getEnvironmentValue("KPI_SERVICE_HOSTNAME");
        final String kpiServicePort = getEnvironmentValue("KPI_SERVICE_PORT");

        Objects.requireNonNull(kpiServiceHostName, "The 'KPI_SERVICE_HOSTNAME' environment variable must be set");
        Objects.requireNonNull(kpiServicePort, "The 'KPI_SERVICE_PORT' environment variable must be set");
        this.restExecutor = restExecutor;
        kpiServiceBaseUrl = String.format("http://%s:%s/kpi-handling/", kpiServiceHostName, kpiServicePort);
        supplierExecutor = new SupplierExecutor(circuitBreaker, retry);
    }

    /**
     * Sends kpis to <code>kpi-service</code> for it to be validated against the internal KPI schema.
     *
     * @param kpiPayload
     *            JSON representation of the KPIs to be parsed
     * @return the response message from the REST call
     */
    public RestResponse<String> putKpis(final String kpiPayload) {
        return supplierExecutor.execute(() -> validatePut(kpiPayload, KPIS));
    }

    private RestResponse<String> validatePut(final String kpiPayload, final String endpoint) {
        try {
            final StringEntity payload = new StringEntity(kpiPayload, StandardCharsets.UTF_8);
            final HttpPut httpPut = EnmJsonHttpPutRequestCreator.createPutRequest(kpiServiceBaseUrl + endpoint, payload);
            return restExecutor.sendPutRequest(httpPut);
        } catch (final IOException e) {
            log.warn("Cannot validate {} model", endpoint, e.getClass());
            throw new RestExecutionException(e);
        }
    }

    /**
     * Sends KPI calculation requests to <code>kpi-service</code> for validation and calculation.
     *
     * @param kpiCalculationRequestPayload
     *            JSON representation of the KPI calculation request
     * @return the response message from the REST call
     */
    public RestResponse<String> postCalculationRequest(final String kpiCalculationRequestPayload) {
        return supplierExecutor.execute(() -> validatePost(kpiCalculationRequestPayload, KPI_CALCULATION, ""));
    }

    /**
     * Sends new KPI definitions to the POST endpoint of <code>kpi-service</code>.
     *
     * @param kpiPayload
     *            JSON representation of the new KPI definitions
     * @return the response message from the REST call
     */
    public RestResponse<String> postKpis(final String kpiPayload) {
        return postKpis(kpiPayload, "");
    }

    public RestResponse<String> postKpis(final String kpiPayload, final String kpiCollectionId) {
        return supplierExecutor.execute(() -> validatePost(kpiPayload, KPIS, kpiCollectionId));
    }


    private RestResponse<String> validatePost(final String kpiPayload, final String endpoint, String collectionId) {
        try {
            final StringEntity payload = new StringEntity(kpiPayload, StandardCharsets.UTF_8);
            final HttpPost httpPost = EnmJsonHttpPostRequestCreator.createPostRequest(kpiServiceBaseUrl + endpoint + collectionId, payload);
            return restExecutor.sendPostRequest(httpPost);
        } catch (final IOException e) {
            log.warn("Cannot validate {} model", endpoint);
            throw new RestExecutionException(e);
        }
    }

    public RestResponse<String> getCalculationState(final String calculationId) {
        return supplierExecutor.execute(() -> validateGet(calculationId, KPI_CALCULATION_STATE));
    }

    private RestResponse<String> validateGet(final String calculationId, final String endpoint) {
        final String paramSuppliedEndpoint = String.format("%s%s%s", kpiServiceBaseUrl, endpoint , calculationId);
        try {
            final HttpGet httpGet = new HttpGet(paramSuppliedEndpoint);
            httpGet.addHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_VALUE);
            return restExecutor.sendGetRequest(httpGet);
        } catch (final IOException e) {
            log.warn("Cannot get state for calculation ID - '{}'", calculationId, e.getClass());
            throw new RestExecutionException(e);
        }
    }

    /**
     * Builder class to generate instances of {@link ServiceRestExecutor}.
     */
    public static class Builder {

        private CircuitBreaker circuitBreaker = SupplierExecutor.DEFAULT_CIRCUIT_BREAKER;
        private RestExecutor restExecutor = new RestExecutor();
        private Retry retry = SupplierExecutor.DEFAULT_RETRY;

        public Builder withCircuitBreaker(final CircuitBreaker circuitBreaker) {
            this.circuitBreaker = circuitBreaker;
            return this;
        }

        public Builder withRestExecutor(final RestExecutor restExecutor) {
            this.restExecutor = restExecutor;
            return this;
        }

        public Builder withRetry(final Retry retry) {
            this.retry = retry;
            return this;
        }

        public ServiceRestExecutor build() {
            return new ServiceRestExecutor(restExecutor, retry, circuitBreaker);
        }
    }
}
