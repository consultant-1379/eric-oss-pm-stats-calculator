/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.helper;

import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

import com.ericsson.oss.air.pm.stats.calculator.KpiCalculatorSparkLauncher;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation.KpiCalculationJob;
import com.ericsson.oss.air.pm.stats.exception.KpiCalculatorSparkStateException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

class CalculationLauncherTest {
    CalculationLauncher objectUnderTest = new CalculationLauncher();

    @Test
    void shouldLaunchCalculation() throws KpiCalculatorSparkStateException {
        try (final MockedConstruction<KpiCalculatorSparkLauncher> sparkLauncherMock = mockConstruction(KpiCalculatorSparkLauncher.class)) {
            final KpiCalculationJob kpiCalculationJobMock = Mockito.mock(KpiCalculationJob.class);

            objectUnderTest.launchCalculation(kpiCalculationJobMock);

            Assertions.assertThat(sparkLauncherMock.constructed()).first().satisfies(kpiCalculatorSparkLauncherMock -> {
                verify(kpiCalculatorSparkLauncherMock).launch();
            });
        }
    }
}