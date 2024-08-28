/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.scheduler.finalize;

import static lombok.AccessLevel.PUBLIC;

import java.sql.SQLException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.service.api.CalculationService;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class HangingCalculationFinalizer {
    @Inject
    private CalculationService calculationService;

    /**
     * Finalizes - changes state to {@link KpiCalculationState#LOST} - for calculations started before the current time minus the elapsed time.
     * <br>
     * <strong>NOTE:</strong> <strong>ON_DEMAND</strong> calculations are not candidates of finalization
     *
     * @param elapsedTime elapsed time to change state after
     */
    public List<UUID> finalizeHangingCalculationsAfter(final Duration elapsedTime) {
        log.info("Finalising KPI calculations that are in state 'STARTED' for over {}", elapsedTime);
        try {
            return calculationService.finalizeHangingStartedCalculations(elapsedTime);
        } catch (final SQLException e) {
            log.error("Unable to finalise KPI calculations that are in state 'STARTED'", e);
        }
        return Collections.emptyList();
    }
}
