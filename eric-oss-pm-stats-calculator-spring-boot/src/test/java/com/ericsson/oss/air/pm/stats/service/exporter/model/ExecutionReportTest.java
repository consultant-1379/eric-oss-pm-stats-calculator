/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.exporter.model;

import static java.time.Month.SEPTEMBER;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.model.entity.Calculation;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ExecutionReportTest {

    @Test
    void shouldCalculationToExecutionReportMappingBeCorrectScheduled() {
        final UUID uuid = UUID.fromString("63515664-6a90-4aab-a033-053e8b4a659c");
        final Calculation calculation = Calculation.builder()
                .withCalculationId(uuid)
                .withExecutionGroup("eric-data-message-bus-kf:9092|topic0|fact_table_0")
                .withKpiCalculationState(KpiCalculationState.FINALIZING)
                .withTimeCreated(LocalDateTime.of(2022, SEPTEMBER, 1, 0, 0, 0))
                .withTimeCompleted(LocalDateTime.of(2022, SEPTEMBER, 1, 1, 0, 0))
                .build();
        final ExecutionReport report = new ExecutionReport(calculation);
        Assertions.assertThat(report.getExecutionId()).isEqualTo(uuid);
        Assertions.assertThat(report.isScheduled()).isTrue();
        Assertions.assertThat(report.getExecutionStart()).isEqualTo(LocalDateTime.of(2022, SEPTEMBER, 1, 0, 0, 0).toEpochSecond(ZoneOffset.UTC));
        Assertions.assertThat(report.getExecutionEnd()).isEqualTo(LocalDateTime.of(2022, SEPTEMBER, 1, 1, 0, 0).toEpochSecond(ZoneOffset.UTC));
        Assertions.assertThat(report.getExecutionGroup()).isEqualTo("eric-data-message-bus-kf:9092|topic0|fact_table_0");
    }

    @Test
    void shouldCalculationToExecutionReportMappingBeCorrectOnDemand() {
        final UUID uuid = UUID.fromString("63515664-6a90-4aab-a033-053e8b4a659c");
        final Calculation calculation = Calculation.builder()
                .withCalculationId(uuid)
                .withExecutionGroup("ON_DEMAND")
                .withKpiCalculationState(KpiCalculationState.FINALIZING)
                .withTimeCreated(LocalDateTime.of(2022, SEPTEMBER, 1, 0, 0, 0))
                .withTimeCompleted(LocalDateTime.of(2022, SEPTEMBER, 1, 1, 0, 0))
                .build();
        final ExecutionReport report = new ExecutionReport(calculation);
        Assertions.assertThat(report.getExecutionId()).isEqualTo(uuid);
        Assertions.assertThat(report.isScheduled()).isFalse();
        Assertions.assertThat(report.getExecutionStart()).isEqualTo(LocalDateTime.of(2022, SEPTEMBER, 1, 0, 0, 0).toEpochSecond(ZoneOffset.UTC));
        Assertions.assertThat(report.getExecutionEnd()).isEqualTo(LocalDateTime.of(2022, SEPTEMBER, 1, 1, 0, 0).toEpochSecond(ZoneOffset.UTC));
        Assertions.assertThat(report.getExecutionGroup()).isEqualTo("ON_DEMAND");
    }
}
