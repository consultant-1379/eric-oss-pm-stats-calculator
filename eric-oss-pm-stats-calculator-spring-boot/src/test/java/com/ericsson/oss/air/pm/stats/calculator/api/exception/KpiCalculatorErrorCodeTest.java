/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.exception;

import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

class KpiCalculatorErrorCodeTest {
    @ParameterizedTest(name = "[{index}] {0} has errorCode: {1}, errorMessage: {2}")
    @ArgumentsSource(KpiCalculatorErrorCodeArgumentProvider.class)
    void shouldVerifyKpiCalculationErrorCodeMetadataRemainsTheSame(final KpiCalculatorErrorCode kpiCalculatorErrorCode,
                                                                   final int errorCode,
                                                                   final String errorMessage) {
        Assertions.assertThat(kpiCalculatorErrorCode.getErrorCode()).isEqualTo(errorCode);
        Assertions.assertThat(kpiCalculatorErrorCode.getErrorMessage()).isEqualTo(errorMessage);
    }

    @Test
    void shouldTestAllEnums() {
        Assertions.assertThat(KpiCalculatorErrorCode.values())
                .containsExactlyInAnyOrder(KpiCalculatorErrorCode.KPI_CALCULATION_ERROR,
                        KpiCalculatorErrorCode.KPI_DEFINITION_ERROR,
                        KpiCalculatorErrorCode.KPI_EXTERNAL_DATASOURCE_ERROR,
                        KpiCalculatorErrorCode.KPI_CALCULATION_STATE_PERSISTENCE_ERROR,
                        KpiCalculatorErrorCode.KPI_SENDING_EXECUTION_REPORT_ERROR);
    }

    private static final class KpiCalculatorErrorCodeArgumentProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) {
            return Stream.of(Arguments.of(KpiCalculatorErrorCode.KPI_CALCULATION_ERROR, 1_000, "Error calculating KPIs"),
                    Arguments.of(KpiCalculatorErrorCode.KPI_DEFINITION_ERROR, 2_000, "Error retrieving KPI Definitions from database table"),
                    Arguments.of(KpiCalculatorErrorCode.KPI_EXTERNAL_DATASOURCE_ERROR, 3_000, "Error retrieving External DataSource details"),
                    Arguments.of(KpiCalculatorErrorCode.KPI_CALCULATION_STATE_PERSISTENCE_ERROR, 4_000, "Error persisting state for calculation ID"),
                    Arguments.of(KpiCalculatorErrorCode.KPI_SENDING_EXECUTION_REPORT_ERROR, 5_000, "Error sending execution report to Kafka"));
        }
    }
}