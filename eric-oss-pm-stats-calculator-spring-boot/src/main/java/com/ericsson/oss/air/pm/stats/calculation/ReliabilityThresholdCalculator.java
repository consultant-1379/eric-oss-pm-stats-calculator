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

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.registry.Registry;

import org.springframework.plugin.core.Plugin;

public interface ReliabilityThresholdCalculator extends Registry<KpiType>, Plugin<KpiType> {
    Map<String, LocalDateTime> calculateReliabilityThreshold(UUID kpiCalculationId);
}
