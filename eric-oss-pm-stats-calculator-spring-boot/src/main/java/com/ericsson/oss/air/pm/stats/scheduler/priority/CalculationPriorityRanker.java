/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.scheduler.priority;

import com.ericsson.oss.air.pm.stats.calculator.configuration.CalculatorProperties;
import com.ericsson.oss.air.pm.stats.service.api.CalculationService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class CalculationPriorityRanker {
    private CalculatorProperties calculatorProperties;
    private CalculationService calculationService;

    public boolean isOnDemandPrioritized() {
        return calculatorProperties.getBlockScheduledWhenHandlingOnDemand() && calculationService.isAnyOnDemandCalculationRunning();
    }
}
