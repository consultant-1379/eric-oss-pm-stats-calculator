/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.api;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.ejb.Local;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.model.entity.Calculation;
import com.ericsson.oss.air.pm.stats.model.exception.EntityNotFoundException;

@Local
public interface CalculationRepository {
    /**
     * Saves {@link Calculation} entity.
     *
     * @param connection  {@link Connection} to the database.
     * @param calculation {@link Calculation} to save
     * @throws SQLException If there was any problem executing the query
     */
    void save(Connection connection, Calculation calculation) throws SQLException;

    /**
     * Updates calculation's timeCompleted and state columns by calculationId.
     *
     * @param connection          {@link Connection} to the database.
     * @param timeCompleted       {@link LocalDateTime} when the calculation was completed
     * @param kpiCalculationState {@link KpiCalculationState} of the calculation
     * @param calculationId       The id of the calculation to update
     * @throws SQLException If there was any problem executing the query
     */
    void updateTimeCompletedAndStateByCalculationId(Connection connection,
                                                    LocalDateTime timeCompleted,
                                                    KpiCalculationState kpiCalculationState,
                                                    UUID calculationId) throws SQLException;

    /**
     * Updates calculation's state column by calculationId.
     *
     * @param connection          {@link Connection} to the database.
     * @param kpiCalculationState {@link KpiCalculationState} of the calculation
     * @param calculationId       The id of the calculation to update
     * @throws SQLException If there was any problem executing the query
     */
    void updateStateByCalculationId(Connection connection, KpiCalculationState kpiCalculationState, UUID calculationId) throws SQLException;

    /**
     * Updates all the statesToModify to the newState.
     *
     * @param connection     {@link Connection} to the database.
     * @param newState       {@link KpiCalculationState} to set
     * @param statesToModify {@link Collection} of {@link KpiCalculationState}s to change
     * @throws SQLException If there was any problem executing the query
     */
    void updateStateByStates(Connection connection, KpiCalculationState newState, Collection<KpiCalculationState> statesToModify) throws SQLException;

    /**
     * Updates oldState to newState based on the execution group and the time created.
     * <br>
     * <strong>NOTE:</strong> <strong>ON_DEMAND</strong> calculations are not candidates of finalization
     *
     * @param connection  {@link Connection} to the database.
     * @param newState    {@link KpiCalculationState} new state
     * @param oldState    {@link KpiCalculationState} old state
     * @param timeCreated {@link LocalDateTime} calculations which created prior this date
     * @return {@link List} of the updated calculations UUIDs
     * @throws SQLException If there was any problem executing the query
     */
    List<UUID> updateStateByStateAndTimeCreated(Connection connection,
                                                KpiCalculationState newState,
                                                KpiCalculationState oldState,
                                                LocalDateTime timeCreated) throws SQLException;

    /**
     * Counts rows based on execution group and states.
     *
     * @param executionGroup       Execution group to filter by
     * @param kpiCalculationStates {@link Collection} of {@link KpiCalculationState} to filter by
     * @return Amount of rows matching the executionGroup and states criteria
     */
    long countByExecutionGroupAndStates(String executionGroup, Collection<KpiCalculationState> kpiCalculationStates);

    /**
     * Counts rows based on execution groups and states.
     *
     * @param executionGroups      Execution groups to filter by
     * @param kpiCalculationStates {@link Collection} of {@link KpiCalculationState} to filter by
     * @return Amount of rows matching the executionGroups and states criteria
     */
    long countByExecutionGroupsAndStates(Collection<String> executionGroups, Collection<KpiCalculationState> kpiCalculationStates);

    /**
     * Counts rows for all states.
     *
     * @return EnumMap containing the number of rows for each calculation state.
     */
    Map<KpiCalculationState, Long> countByStates();

    /**
     * Counts rows based on states.
     *
     * @param kpiCalculationStates {@link Collection} of {@link KpiCalculationState} to filter by
     * @return EnumMap containing the number of rows for each calculation state.
     */
    Map<KpiCalculationState, Long> countByStates(Collection<KpiCalculationState> kpiCalculationStates);

    /**
     * Finds {@link  KpiCalculationState} by calculation ID.
     *
     * @param calculationId The id of the calculation
     * @return {@link Optional} containing the {@link  KpiCalculationState}, or an {@link Optional#empty()} if {@link  KpiCalculationState} was not
     * found by calculation ID
     */
    Optional<KpiCalculationState> findByCalculationId(UUID calculationId);

    /**
     * Finds all {@link Calculation}s by the provided {@link KpiCalculationState}.
     *
     * @param kpiCalculationState {@link KpiCalculationState} to filter {@link Calculation}s by.
     * @return {@link List} of {@link Calculation}s with the provided state.
     */
    List<Calculation> findAllByState(KpiCalculationState kpiCalculationState);

    /**
     * Finds {@link Calculation} that is ready to be exported by the provided calculation ID.
     * <br>
     * The calculation has to have {@link KpiCalculationState#FINALIZING} state to be exportable.
     *
     * @param calculationId {@link UUID} id to find the {@link Calculation} by.
     * @return {@link Optional} containing the {@link Calculation} or {@link Optional#empty()} if such {@link Calculation} does not exist.
     */
    Optional<Calculation> findCalculationToSendToExporter(UUID calculationId);

    /**
     * Finds {@link Calculation}s with <strong>time_created</strong> column is after the provided target.
     *
     * @param target inclusive lower limit for the <strong>time_started</strong> column
     * @return {@link List} of {@link Calculation}s
     */
    List<Calculation> findCalculationsByTimeCreatedIsAfter(LocalDateTime target);

    /**
     * Deletes records based on the <strong>time_created</strong> column less than the provided {@link LocalDateTime}
     *
     * @param connection    {@link Connection} to the database
     * @param localDateTime {@link LocalDateTime} to delete records by
     * @return Number of deleted rows
     * @throws SQLException If there was any problem executing the query
     */
    long deleteByTimeCreatedLessThen(Connection connection, LocalDateTime localDateTime) throws SQLException;

    /**
     * Finds execution group by Calculation ID.
     *
     * @param calculationId {@link UUID} id of the calculation to find by.
     * @return {@link Optional} of execution group, otherwise {@link Optional#empty()}.
     */
    Optional<String> findExecutionGroupByCalculationId(UUID calculationId);

    /**
     * Finds execution group by calculation ID.
     * <br>
     * If the execution group is not found by the calculation ID, then throws {@link EntityNotFoundException}.
     *
     * @param calculationId {@link UUID} id of the calculation to find by.
     * @return the execution group
     */
    default String forceFetchExecutionGroupByCalculationId(final UUID calculationId) {
        return findExecutionGroupByCalculationId(calculationId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Kpi calculation with calculation id '%s' not found.", calculationId))
        );
    }

    /**
     * Get the last reliability threshold of given complex execution group that was successful
     *
     * @param complexExecutionGroup Name of the complex execution group
     * @return The reliability threshold in {@link LocalDateTime} or Zero timestamp if there is no completed successful complex group by given name.
     */
    LocalDateTime getLastComplexCalculationReliability(String complexExecutionGroup);

    /**
     * Finds kpi type by Calculation ID.
     *
     * @param calculationId {@link UUID} id of the calculation to find by.
     * @return {@link Optional} of kpi type, otherwise {@link Optional#empty()}.
     */
    Optional<KpiType> findKpiTypeByCalculationId(UUID calculationId);

    /**
     * Finds kpi type by calculation ID.
     * <br>
     * If the kpi type is not found by the calculation ID, then throws {@link EntityNotFoundException}.
     *
     * @param calculationId {@link UUID} id of the calculation to find by.
     * @return the kpi type
     */
    default KpiType forceFetchKpiTypeByCalculationId(final UUID calculationId) {
        return findKpiTypeByCalculationId(calculationId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Kpi calculation with calculation id '%s' not found.", calculationId))
        );
    }
}
