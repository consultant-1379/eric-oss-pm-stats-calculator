/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service;

import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.getRunningKpiCalculationStates;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.getSuccessfulDoneKpiCalculationStates;
import static com.ericsson.oss.air.pm.stats.common.env.DatabaseProperties.getKpiServiceJdbcConnection;
import static com.ericsson.oss.air.pm.stats.common.env.DatabaseProperties.getKpiServiceJdbcProperties;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.EXECUTION_GROUP_ON_DEMAND_CALCULATION;
import static lombok.AccessLevel.PUBLIC;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiCalculatorErrorCode;
import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiCalculatorException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.model.entity.Calculation;
import com.ericsson.oss.air.pm.stats.model.exception.CalculationStateCorrectionException;
import com.ericsson.oss.air.pm.stats.model.exception.EntityNotFoundException;
import com.ericsson.oss.air.pm.stats.model.exception.StartupException;
import com.ericsson.oss.air.pm.stats.model.exception.UncheckedSqlException;
import com.ericsson.oss.air.pm.stats.repository.api.CalculationRepository;
import com.ericsson.oss.air.pm.stats.service.api.CalculationService;

import io.github.resilience4j.retry.Retry;
import io.vavr.CheckedRunnable;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Stateless
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class CalculationServiceImpl implements CalculationService {
    @Inject
    private CalculationRepository calculationRepository;

    @Override
    public KpiCalculationState forceFindByCalculationId(final UUID calculationId) {
        return calculationRepository.findByCalculationId(calculationId).orElseThrow(() -> new EntityNotFoundException(String.format(
                "%s state with id '%s' is not found", Calculation.class.getSimpleName(), calculationId
        )));
    }

    @Override
    public Calculation findCalculationReadyToBeExported(final UUID calculationId) {
        return calculationRepository.findCalculationToSendToExporter(calculationId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Calculation is not found by the provided calculation id: '%s'", calculationId))
        );
    }

    @Override
    public List<Calculation> findCalculationReadyToBeExported() {
        return calculationRepository.findAllByState(KpiCalculationState.FINALIZING);
    }

    @Override
    public List<Calculation> findCalculationsCreatedWithin(final Duration elapsedTime) {
        final LocalDateTime target = LocalDateTime.now().minus(elapsedTime);
        return calculationRepository.findCalculationsByTimeCreatedIsAfter(target);
    }

    @Override
    public String forceFetchExecutionGroupByCalculationId(final UUID calculationId) {
        return calculationRepository.forceFetchExecutionGroupByCalculationId(calculationId);
    }

    @Override
    public KpiType forceFetchKpiTypeByCalculationId(final UUID calculationId) {
        return calculationRepository.forceFetchKpiTypeByCalculationId(calculationId);
    }

    @Override
    public void updateCompletionState(final UUID calculationId, final KpiCalculationState kpiCalculationState) {
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties())) {
            final LocalDateTime currentTime = LocalDateTime.now();
            log.info("KPI calculation with ID '{}' concluded at '{}' with state '{}'", calculationId, currentTime, kpiCalculationState.name());
            calculationRepository.updateTimeCompletedAndStateByCalculationId(connection, currentTime, kpiCalculationState, calculationId);
        } catch (final SQLException e) {
            throw new KpiCalculatorException(KpiCalculatorErrorCode.KPI_CALCULATION_STATE_PERSISTENCE_ERROR, e);
        }
    }

    @Override
    public void updateCompletionStateWithRetry(final UUID calculationId, final KpiCalculationState kpiCalculationState, final Retry retry) {
        try (Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties())) {
            Try.run(Retry.decorateCheckedRunnable(retry, () ->
                            calculationRepository.updateStateByCalculationId(connection, kpiCalculationState, calculationId)))
                    .getOrElseThrow(throwable -> new KpiCalculatorException(KpiCalculatorErrorCode.KPI_CALCULATION_STATE_PERSISTENCE_ERROR, throwable));
        } catch (final SQLException e) {
            throw new KpiCalculatorException(KpiCalculatorErrorCode.KPI_CALCULATION_STATE_PERSISTENCE_ERROR, e);
        }
    }

    @Override
    public void updateRunningCalculationsToLost(final Retry retry) {
        final CheckedRunnable updateRunningCalculationsToLost = () -> {
            try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties())) {
                final List<KpiCalculationState> runningKpiCalculationStates = KpiCalculationState.getRunningKpiCalculationStates();
                calculationRepository.updateStateByStates(connection, KpiCalculationState.LOST, runningKpiCalculationStates);
            }
        };

        Try.run(Retry.decorateCheckedRunnable(retry, updateRunningCalculationsToLost))
                .getOrElseThrow(throwable -> new StartupException("Unable to update the calculation state of running calculation jobs", throwable));
    }

    @Override
    public void updateStatesAfterRestore(final Retry retry) {
        final CheckedRunnable updateCalculations = () -> {
            try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties())) {
                calculationRepository.updateStateByStates(connection, KpiCalculationState.LOST, getRunningKpiCalculationStates());
                calculationRepository.updateStateByStates(connection, KpiCalculationState.FINISHED, getSuccessfulDoneKpiCalculationStates());
            }
        };

        Try.run(Retry.decorateCheckedRunnable(retry, updateCalculations))
                .getOrElseThrow(throwable -> new CalculationStateCorrectionException("Unable to update the calculation state after restore", throwable));
    }

    @Override
    public void save(final Calculation calculation) throws SQLException {
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties())) {
            calculationRepository.save(connection, calculation);
        }
    }

    @Override
    public boolean isAnyCalculationRunning(final String executionGroup) {
        final List<KpiCalculationState> runningKpiCalculationStates = getRunningKpiCalculationStates();
        return calculationRepository.countByExecutionGroupAndStates(executionGroup, runningKpiCalculationStates) != 0;
    }

    @Override
    public boolean isAnyOnDemandCalculationRunning() {
        return isAnyCalculationRunning(EXECUTION_GROUP_ON_DEMAND_CALCULATION);
    }

    @Override
    public boolean isAnyCalculationRunning(final Collection<String> executionGroups) {
        final List<KpiCalculationState> runningKpiCalculationStates = getRunningKpiCalculationStates();
        return calculationRepository.countByExecutionGroupsAndStates(executionGroups, runningKpiCalculationStates) != 0;
    }

    @Override
    public List<UUID> finalizeHangingStartedCalculations(final Duration elapsedTime) throws SQLException {
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties())) {
            final LocalDateTime timeCreated = LocalDateTime.now().minus(elapsedTime);
            return calculationRepository.updateStateByStateAndTimeCreated(connection, KpiCalculationState.LOST, KpiCalculationState.STARTED, timeCreated);
        }
    }

    @Override
    public long deleteByTimeCreatedLessThen(final LocalDateTime localDateTime) {
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties())) {
            return calculationRepository.deleteByTimeCreatedLessThen(connection, localDateTime);
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }
}
