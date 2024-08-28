/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.metric;

import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.FINALIZING;

import java.time.Instant;
import java.util.UUID;
import java.util.function.BiConsumer;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import lombok.NonNull;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class SparkMeterServiceTest {
    SparkMeterService objectUnderTest = new SparkMeterService(900L);

    @Test
    void shouldMeterCalculationStart() {
        final UUID calculationId = UUID.fromString("c95abd16-24a0-4ef7-bc8d-b2b9a9861835");
        final String executionGroup = "execution_group";

        objectUnderTest.meterCalculationStart(executionGroup, calculationId);

        assertGauge("calculation_start_time", (metricName, gauge) -> {
            final Instant now = Instant.now();
            Assertions.assertThat(metricName).isEqualTo("execution_group.c95abd16-24a0-4ef7-bc8d-b2b9a9861835.calculation_start_time");
            Assertions.assertThat(epochMilli(gauge)).isBetween(now.minusSeconds(1), now);
        });
    }

    @Test
    void shouldMeterCalculationEnd() {
        final UUID calculationId = UUID.fromString("c95abd16-24a0-4ef7-bc8d-b2b9a9861835");
        final String executionGroup = "execution_group";

        objectUnderTest.meterCalculationEnd(executionGroup, calculationId, FINALIZING);

        assertGauge("calculation_end_time", (metricName, gauge) -> {
            final Instant now = Instant.now();
            Assertions.assertThat(metricName).isEqualTo("execution_group.c95abd16-24a0-4ef7-bc8d-b2b9a9861835.FINALIZING.calculation_end_time");
            Assertions.assertThat(epochMilli(gauge)).isBetween(now.minusSeconds(1), now);
        });
    }

    @Test
    void shouldMeterCalculationDuration() {
        final Instant now = Instant.now();
        final UUID calculationId = UUID.fromString("c95abd16-24a0-4ef7-bc8d-b2b9a9861835");
        final String executionGroup = "execution_group";

        objectUnderTest.meterCalculationDuration(now.toEpochMilli(), executionGroup, calculationId, FINALIZING);

        assertGauge("calculation_end_time", (metricName, gauge) -> {
            Assertions.assertThat(metricName).isEqualTo("execution_group.c95abd16-24a0-4ef7-bc8d-b2b9a9861835.FINALIZING.calculation_end_time");
        });

        assertGauge("calculation_duration", (metricName, gauge) -> {
            Assertions.assertThat(metricName).isEqualTo("execution_group.c95abd16-24a0-4ef7-bc8d-b2b9a9861835.FINALIZING.calculation_duration");
        });
    }

    @SuppressWarnings("rawtypes")
    void assertGauge(final String suffix, final BiConsumer<String, Gauge> biConsumer) {
        final MetricRegistry metricRegistry = objectUnderTest.getMetricRegistry();
        Assertions.assertThat(metricRegistry.getGauges((name, metric) -> name.endsWith(suffix)).entrySet()).first().satisfies(entry -> {
            biConsumer.accept(entry.getKey(), entry.getValue());
        });
    }

    @SuppressWarnings("rawtypes")
    static Instant epochMilli(@NonNull final Gauge gauge) {
        return Instant.ofEpochMilli((long) gauge.getValue());
    }
}