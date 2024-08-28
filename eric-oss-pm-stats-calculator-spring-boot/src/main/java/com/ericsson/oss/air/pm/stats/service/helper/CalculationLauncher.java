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

import javax.enterprise.context.ApplicationScoped;

import com.ericsson.oss.air.pm.stats.calculator.KpiCalculatorSparkLauncher;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation.KpiCalculationJob;
import com.ericsson.oss.air.pm.stats.exception.KpiCalculatorSparkStateException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class CalculationLauncher {
    public void launchCalculation(final KpiCalculationJob kpiCalculationJob) throws KpiCalculatorSparkStateException {
        final KpiCalculatorSparkLauncher kpiCalculatorSparkLauncher = new KpiCalculatorSparkLauncher(kpiCalculationJob);
        kpiCalculatorSparkLauncher.launch();
    }
}
