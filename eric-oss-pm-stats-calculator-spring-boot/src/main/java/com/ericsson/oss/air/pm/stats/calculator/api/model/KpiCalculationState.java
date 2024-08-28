/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model;

import java.util.List;

/**
 * ENUM representing the potential states for a KPI calculation.
 */
public enum KpiCalculationState {
    STARTED,
    IN_PROGRESS,
    FINALIZING,
    NOTHING_CALCULATED,
    FINISHED,
    FAILED,
    LOST;

    public static List<KpiCalculationState> getSuccessfulDoneKpiCalculationStates() {
        return List.of(FINISHED, FINALIZING);
    }

    public static List<KpiCalculationState> getRunningKpiCalculationStates() {
        return List.of(STARTED, IN_PROGRESS);
    }
}
