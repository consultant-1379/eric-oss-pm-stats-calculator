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

import static org.mockito.Mockito.verify;

import com.ericsson.oss.air.pm.stats.common.metrics.ApiCounter;
import com.ericsson.oss.air.pm.stats.common.metrics.ApiGauge;
import com.ericsson.oss.air.pm.stats.common.metrics.ApiMetricRegistry;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TabularParamMetricsTest {
    @Spy ApiMetricRegistry apiMetricRegistrySpy;

    @InjectMocks TabularParamMetrics objectUnderTest;

    @Test
    void setMetricPostCompressedPayloadSizeInBytes() {
        objectUnderTest.setMetricCompressedPayloadSizeInBytes(31457280);
        verify(apiMetricRegistrySpy).defaultSettableGauge(ApiGauge.CALCULATION_POST_COMPRESSED_PAYLOAD_SIZE_IN_BYTES);
    }

    @Test
    void setMetricDeCompressedPayloadSizeInBytes() {
        objectUnderTest.setMetricDeCompressedPayloadSizeInBytes(31457280);
        verify(apiMetricRegistrySpy).defaultSettableGauge(ApiGauge.CALCULATION_POST_DECOMPRESSED_PAYLOAD_SIZE_IN_BYTES);
    }

    @Test
    void increaseMetricTabularParamJsonFormatCounter() {
        objectUnderTest.increaseMetricTabularParamJsonFormat();
        verify(apiMetricRegistrySpy).counter(ApiCounter.CALCULATION_POST_TABULAR_PARAM_FORMAT_JSON);
    }

    @Test
    void increaseMetricTabularParamCsvFormatCounter() {
        objectUnderTest.increaseMetricTabularParamCsvFormat();
        verify(apiMetricRegistrySpy).counter(ApiCounter.CALCULATION_POST_TABULAR_PARAM_FORMAT_CSV);
    }

    @Test
    void increaseMetricCalculationRequestWithTabularParamCounter() {
        objectUnderTest.increaseMetricCalculationPostWithTabularParam();
        verify(apiMetricRegistrySpy).counter(ApiCounter.CALCULATION_POST_WITH_TABULAR_PARAM);
    }
}