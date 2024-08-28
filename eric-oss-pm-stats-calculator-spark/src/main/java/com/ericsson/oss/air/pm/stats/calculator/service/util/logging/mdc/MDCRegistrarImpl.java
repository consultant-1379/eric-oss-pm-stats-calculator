/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.logging.mdc;

import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MDCRegistrarImpl {
    private final SparkService sparkService;

    /**
     * Registers logging keys to MDC.
     * <br>
     * Registered keys are used in Spark's log4j declaration to defined output log pattern.
     */
    public void registerLoggingKeys() {
        MDC.put("applicationIdLogLabelKey", "Application_ID: ");
        MDC.put("applicationId", String.format("%s ", sparkService.getApplicationId()));
        MDC.put("calculationIdLogLabelKey", "Calculation_ID: ");
        MDC.put("calculationId", String.format("%s ", sparkService.getCalculationId()));
    }
}
