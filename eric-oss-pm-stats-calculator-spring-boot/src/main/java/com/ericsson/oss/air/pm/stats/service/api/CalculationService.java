/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.api;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import javax.ejb.Local;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.model.entity.Calculation;
import com.ericsson.oss.air.pm.stats.model.exception.EntityNotFoundException;

import io.github.resilience4j.retry.Retry;

@Local
public interface CalculationService {
    /**
     * Finds {@link  KpiCalculationState} by calculation ID.
     *
     * @param calculationId The id of the calculation
     * @return The {@link  KpiCalculationState} associated with the provided calculation id
     * @throws EntityNotFoundException if {@link Calculation} is not found by id
     */
    KpiCalculationState forceFindByCalculationId(UUID calculationId);

    /**
     * Finds calculation to be exported.
     *
     * @param calculationId {@link UUID} id of the {@link Calculation} to find.
     * @return {@link Calculation} object to be exported to the <strong>PM Stats Exporter</strong>.
     */
    Calculation findCalculationReadyToBeExported(UUID calculationId);

    /**
     * Finds all {@link Calculation}s to be exported.
     * <p>
     * {@link Calculation}s with {@link KpiCalculationState#FINALIZING} state.
     *
     * @return {@link List} of {@link Calculation}s to be exported.
     */
    List<Calculation> findCalculationReadyToBeExported();

    /**
     * Finds {@link Calculation}s created within the provided elapsed time.
     * The elapsed time is subtracted from <strong>now</strong> to obtain the target time to find {@link Calculation}s after.
     * <pre>
     *   target    now
     * ====|========|===
     *     \_______/
     *      elapsed
     * </pre>
     *
     * @param elapsedTime {@link Duration} of the elapsed time
     * @return {@link Calculation}s created within the elapsed time.
     */
    List<Calculation> findCalculationsCreatedWithin(Duration elapsedTime);

    /**
     * Finds execution group by calculation ID.
     * <br>
     * If the execution group is not found by the calculation ID, then throws {@link EntityNotFoundException}.
     *
     * @param calculationId {@link UUID} id of the calculation to find by.
     * @return the execution group
     */
    String forceFetchExecutionGroupByCalculationId(UUID calculationId);

    /**
     * Finds kpi type by calculation ID.
     * <br>
     * If the kpi type is not found by the calculation ID, then throws {@link EntityNotFoundException}.
     *
     * @param calculationId {@link UUID} id of the calculation to find by.
     * @return the kpi type
     */
    KpiType forceFetchKpiTypeByCalculationId(UUID calculationId);

    /**
     * Updates the completion state of the calculation.
     *
     * @param calculationId       Calculation to update
     * @param kpiCalculationState {@link  KpiCalculationState} to set for completion
     */
    void updateCompletionState(UUID calculationId, final KpiCalculationState kpiCalculationState);

    /**
     * Updates the completion state of the calculation.
     *
     * @param calculationId       Calculation to update
     * @param kpiCalculationState {@link  KpiCalculationState} to set for completion
     * @param retry               {@link  Retry} object to help with database retries
     */
    void updateCompletionStateWithRetry(UUID calculationId, final KpiCalculationState kpiCalculationState, final Retry retry);

    /**
     * Updates {@link KpiCalculationState#STARTED} and {@link KpiCalculationState#IN_PROGRESS} {@link KpiCalculationState}s to
     * {@link KpiCalculationState#LOST}.
     *
     * @param retry {@link Retry} configuration to use when attempting the database updates
     */
    void updateRunningCalculationsToLost(Retry retry);

    /**
     * Updates {@link KpiCalculationState#STARTED} and {@link KpiCalculationState#IN_PROGRESS} {@link KpiCalculationState}s to
     * {@link KpiCalculationState#LOST} and {@link KpiCalculationState#FINALIZING} {@link KpiCalculationState} to
     * {@link KpiCalculationState#FINISHED}.
     *
     * @param retry {@link Retry} configuration to use when attempting the database updates
     */
    void updateStatesAfterRestore(Retry retry);

    /**
     * Saves {@link Calculation} entity.
     *
     * @param calculation {@link Calculation} to save
     * @throws SQLException If there was any problem executing the query
     */
    void save(Calculation calculation) throws SQLException;

    /**
     * Checks if there is any {@link  KpiCalculationState#STARTED} or {@link  KpiCalculationState#IN_PROGRESS} calculations for the execution group.
     *
     * @param executionGroup The execution group to check
     * @return true if any calculations are running for the excution group else false
     */
    boolean isAnyCalculationRunning(String executionGroup);

    /**
     * Checks if any <strong>ON_DEMAND</strong> calculation is in running state
     *
     * @return true if any <strong>ON_DEMAND</strong> calculation is in running state, otherwise false
     */
    boolean isAnyOnDemandCalculationRunning();

    /**
     * Checks if there is any {@link  KpiCalculationState#STARTED} or {@link  KpiCalculationState#IN_PROGRESS} calculations for the execution groups.
     *
     * @param executionGroups The execution groups to check
     * @return true if any calculations are running for the execution groups else false
     */
    boolean isAnyCalculationRunning(Collection<String> executionGroups);

    /**
     * Set any KPI calculations that are in state {@link  KpiCalculationState#STARTED} for provided elapsed time to {@link  KpiCalculationState#LOST}
     * <br>
     * <strong>NOTE:</strong> <strong>ON_DEMAND</strong> calculations are not candidates of finalization
     *
     * @param elapsedTime elapsed time to change state after
     * @return {@link List} of the finalized calculations UUIDs
     * @throws SQLException If any error occurs during update of KPI calculation state
     */
    List<UUID> finalizeHangingStartedCalculations(Duration elapsedTime) throws SQLException;

    /**
     * Deletes records based on the <strong>time_created</strong> column less than the provided {@link LocalDateTime}
     *
     * @param localDateTime {@link LocalDateTime} to delete records by
     * @return Number of deleted rows
     */
    long deleteByTimeCreatedLessThen(LocalDateTime localDateTime);
}
