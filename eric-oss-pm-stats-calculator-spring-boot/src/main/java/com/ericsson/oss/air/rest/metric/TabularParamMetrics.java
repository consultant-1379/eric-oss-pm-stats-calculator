/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.metric;

import com.ericsson.oss.air.pm.stats.common.metrics.ApiCounter;
import com.ericsson.oss.air.pm.stats.common.metrics.ApiGauge;
import com.ericsson.oss.air.pm.stats.common.metrics.ApiMetricRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class TabularParamMetrics {

    private final ApiMetricRegistry apiMetricRegistry;

    public void setMetricCompressedPayloadSizeInBytes(final int size) {
        apiMetricRegistry.defaultSettableGauge(ApiGauge.CALCULATION_POST_COMPRESSED_PAYLOAD_SIZE_IN_BYTES).setValue(size);
        log.info("Metric {} is set to value {}.", ApiGauge.CALCULATION_POST_COMPRESSED_PAYLOAD_SIZE_IN_BYTES.getName(), size);
    }

    public void setMetricDeCompressedPayloadSizeInBytes(final int size) {
        apiMetricRegistry.defaultSettableGauge(ApiGauge.CALCULATION_POST_DECOMPRESSED_PAYLOAD_SIZE_IN_BYTES).setValue(size);
        log.info("Metric {} is set to value {}.", ApiGauge.CALCULATION_POST_COMPRESSED_PAYLOAD_SIZE_IN_BYTES.getName(), size);
    }

    public void increaseMetricTabularParamJsonFormat() {
        apiMetricRegistry.counter(ApiCounter.CALCULATION_POST_TABULAR_PARAM_FORMAT_JSON).inc();
        log.info("Value of metric {} is increased.", ApiCounter.CALCULATION_POST_TABULAR_PARAM_FORMAT_JSON.getName());
    }

    public void increaseMetricTabularParamCsvFormat() {
        apiMetricRegistry.counter(ApiCounter.CALCULATION_POST_TABULAR_PARAM_FORMAT_CSV).inc();
        log.info("Value of metric {} is increased.", ApiCounter.CALCULATION_POST_TABULAR_PARAM_FORMAT_CSV.getName());
    }

    public void increaseMetricCalculationPostWithTabularParam() {
        apiMetricRegistry.counter(ApiCounter.CALCULATION_POST_WITH_TABULAR_PARAM).inc();
        log.info("Value of metric {} is increased.", ApiCounter.CALCULATION_POST_WITH_TABULAR_PARAM.getName());
    }
}
