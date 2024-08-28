/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation;

import static lombok.AccessLevel.PUBLIC;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.service.api.CalculationReliabilityService;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Class used to give back Reliability Threshold for On demand KPIs.
 */
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class CalculationReliabilityThreshold implements ReliabilityThresholdCalculator {

    @Inject
    private CalculationReliabilityService calculationReliabilityService;

    @Override
    public Map<String, LocalDateTime> calculateReliabilityThreshold(final UUID kpiCalculationId) {
        return calculationReliabilityService.findReliabilityThresholdByCalculationId(kpiCalculationId);
    }

    @Override
    public boolean doesSupport(final KpiType kpiType) {
        return kpiType == KpiType.ON_DEMAND || kpiType == KpiType.SCHEDULED_COMPLEX;
    }

    @Override
    public boolean supports(final KpiType kpiType) {
        return kpiType == KpiType.ON_DEMAND || kpiType == KpiType.SCHEDULED_COMPLEX;
    }
}