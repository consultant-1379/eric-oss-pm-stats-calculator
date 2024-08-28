/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.api;

import javax.ejb.Local;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation.KpiCalculationJob;

/**
 * Interface used to calculate KPIs.
 */
@Local
public interface KpiCalculator {

    /**
     * Calculates the KPIs based on the passed {@link KpiCalculationJob}.
     *
     * @param kpiCalculationJob {@link KpiCalculationJob} containing calculated related information.
     */
    void calculateKpis(KpiCalculationJob kpiCalculationJob);
}
