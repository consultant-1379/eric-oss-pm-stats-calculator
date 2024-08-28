/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.start.time;

import static lombok.AccessLevel.PUBLIC;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculation.start.time.api.StartTimeCalculator;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.service.api.CalculationReliabilityService;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class CalculationStartTimeCalculator implements StartTimeCalculator {

    @Inject
    private CalculationReliabilityService calculationReliabilityService;

    @Override
    public Map<String, LocalDateTime> calculateStartTime(final UUID kpiCalculationId) {
        return calculationReliabilityService.findStartTimeByCalculationId(kpiCalculationId);
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
