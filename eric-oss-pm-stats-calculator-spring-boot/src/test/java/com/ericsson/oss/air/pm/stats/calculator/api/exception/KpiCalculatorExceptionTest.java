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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class KpiCalculatorExceptionTest {
    private static final String MESSAGE = "Something went wrong";

    @Test
    void shouldVerifyThrowableConstructor() {
        final RuntimeException throwable = new RuntimeException(MESSAGE);
        final KpiCalculatorException actual = new KpiCalculatorException(KpiCalculatorErrorCode.KPI_CALCULATION_ERROR, throwable);

        Assertions.assertThat(actual.getErrorCode()).isEqualTo(KpiCalculatorErrorCode.KPI_CALCULATION_ERROR);
        Assertions.assertThat(actual)
                .hasRootCauseExactlyInstanceOf(RuntimeException.class)
                .hasRootCauseMessage(MESSAGE);
    }

    @Test
    void shouldVerifyErrorMessageConstructor() {
        final KpiCalculatorException actual = new KpiCalculatorException(KpiCalculatorErrorCode.KPI_CALCULATION_ERROR, MESSAGE);

        Assertions.assertThat(actual.getErrorCode()).isEqualTo(KpiCalculatorErrorCode.KPI_CALCULATION_ERROR);
        Assertions.assertThat(actual)
                .hasNoCause()
                .hasMessage(MESSAGE);
    }
}