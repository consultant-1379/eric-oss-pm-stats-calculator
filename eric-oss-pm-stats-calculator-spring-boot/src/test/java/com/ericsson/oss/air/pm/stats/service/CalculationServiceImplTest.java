/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Stream;

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
import com.ericsson.oss.air.pm.stats.test_utils.DriverManagerMock;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CalculationServiceImplTest {
    static final UUID CALCULATION_ID_1 = UUID.fromString("55119493-5f23-41c5-8ef3-1efe08076dd7");

    static final LocalDateTime TEST_TIME = LocalDateTime.of(LocalDate.of(2_022, Month.APRIL, 19), LocalTime.NOON);

    Properties properties;

    @Mock
    CalculationRepository calculationRepositoryMock;

    @InjectMocks
    CalculationServiceImpl objectUnderTest;

    @BeforeEach
    void setUp() {
        properties = new Properties();
    }

    @Test
    void shouldFindByCalculationId() {
        when(calculationRepositoryMock.findByCalculationId(CALCULATION_ID_1)).thenReturn(Optional.of(KpiCalculationState.IN_PROGRESS));

        final KpiCalculationState actual = objectUnderTest.forceFindByCalculationId(CALCULATION_ID_1);

        verify(calculationRepositoryMock).findByCalculationId(CALCULATION_ID_1);

        Assertions.assertThat(actual).isEqualTo(KpiCalculationState.IN_PROGRESS);
    }

    @Test
    void shouldRaiseException_whenFindByCalculationId_doesNotFind() {
        when(calculationRepositoryMock.findByCalculationId(CALCULATION_ID_1)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> objectUnderTest.forceFindByCalculationId(CALCULATION_ID_1))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Calculation state with id '55119493-5f23-41c5-8ef3-1efe08076dd7' is not found");

        verify(calculationRepositoryMock).findByCalculationId(CALCULATION_ID_1);
    }

    @Test
    void shouldFindCalculationsReadyToBeExported() {
        when(calculationRepositoryMock.findAllByState(KpiCalculationState.FINALIZING)).thenReturn(emptyList());

        final List<Calculation> actual = objectUnderTest.findCalculationReadyToBeExported();

        verify(calculationRepositoryMock).findAllByState(KpiCalculationState.FINALIZING);

        Assertions.assertThat(actual).isEmpty();
    }


    @Test
    void shouldForceFetchKpiTypeByCalculationId() {
        when(calculationRepositoryMock.forceFetchKpiTypeByCalculationId(CALCULATION_ID_1)).thenReturn(KpiType.SCHEDULED_SIMPLE);

        final KpiType actual = objectUnderTest.forceFetchKpiTypeByCalculationId(CALCULATION_ID_1);

        verify(calculationRepositoryMock).forceFetchKpiTypeByCalculationId(CALCULATION_ID_1);

        Assertions.assertThat(actual).isEqualTo(KpiType.SCHEDULED_SIMPLE);

    }

    @Nested
    @DisplayName("When something goes wrong")
    class WhenSomethingGoesWrong {
        @Test
        void shouldThrowKpiCalculatorException_onUpdateTimeCompletedAndStateByCalculationId() {
            DriverManagerMock.prepareThrow(SQLException.class, connectionMock -> {
                Assertions.assertThatThrownBy(() -> objectUnderTest.updateCompletionState(CALCULATION_ID_1, KpiCalculationState.FINISHED))
                        .hasRootCauseInstanceOf(SQLException.class)
                        .isInstanceOf(KpiCalculatorException.class)
                        .extracting("errorCode", InstanceOfAssertFactories.type(KpiCalculatorErrorCode.class))
                        .isEqualTo(KpiCalculatorErrorCode.KPI_CALCULATION_STATE_PERSISTENCE_ERROR);
            });
        }

        @Test
        void shouldThrowKpiCalculatorException_onUpdateTimeCompletedAndStateWithRetryByCalculationId() {
            DriverManagerMock.prepareThrow(SQLException.class, connectionMock -> {
                final Retry testRetry = Retry.of("testRetry", RetryConfig.custom().retryExceptions(Throwable.class).maxAttempts(1).build());

                Assertions.assertThatThrownBy(() -> objectUnderTest.updateCompletionStateWithRetry(CALCULATION_ID_1, KpiCalculationState.FINISHED, testRetry))
                        .hasRootCauseInstanceOf(SQLException.class)
                        .isInstanceOf(KpiCalculatorException.class)
                        .extracting("errorCode", InstanceOfAssertFactories.type(KpiCalculatorErrorCode.class))
                        .isEqualTo(KpiCalculatorErrorCode.KPI_CALCULATION_STATE_PERSISTENCE_ERROR);
            });
        }

        @Test
        void shouldThrowKpiServiceStartupException_onUpdateRunningCalculationsToLost() {
            DriverManagerMock.prepareThrow(SQLException.class, connectionMock -> {
                final Retry testRetry = Retry.of("testRetry", RetryConfig.custom().retryExceptions(Throwable.class).maxAttempts(1).build());

                Assertions.assertThatThrownBy(() -> objectUnderTest.updateRunningCalculationsToLost(testRetry))
                        .hasCauseInstanceOf(SQLException.class)
                        .isInstanceOf(StartupException.class)
                        .hasMessage("Unable to update the calculation state of running calculation jobs");
            });
        }

        @Test
        void shouldThrowKpiServiceStartupException_onUpdateCalculationsToFinished() {
            DriverManagerMock.prepareThrow(SQLException.class, connectionMock -> {
                final Retry testRetry = Retry.of("testRetry", RetryConfig.custom().retryExceptions(Throwable.class).maxAttempts(1).build());

                Assertions.assertThatThrownBy(() -> objectUnderTest.updateStatesAfterRestore(testRetry))
                        .hasCauseInstanceOf(SQLException.class)
                        .isInstanceOf(CalculationStateCorrectionException.class)
                        .hasMessage("Unable to update the calculation state after restore");
            });
        }

        @Test
        void shouldThrowUncheckedSqlException_onDeleteByTimeCreatedLessThen() {
            DriverManagerMock.prepareThrow(SQLException.class, connectionMock -> {
                Assertions.assertThatThrownBy(() -> objectUnderTest.deleteByTimeCreatedLessThen(TEST_TIME))
                        .hasCauseInstanceOf(SQLException.class)
                        .isInstanceOf(UncheckedSqlException.class);
            });
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("When connection is available")
    class WhenConnectionIsAvailAble {
        @Test
        void shouldSaveCalculation(@Mock final Calculation calculationMock) {
            DriverManagerMock.prepare(connectionMock -> {
                Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.save(calculationMock));

                verify(calculationRepositoryMock).save(connectionMock, calculationMock);
            });
        }

        @Test
        void shouldReturnValidCalculation(@Mock Calculation calculationMock) {
            final UUID calculationId = UUID.fromString("63515664-6a90-4aab-a033-053e8b4a659c");

            doReturn(Optional.of(calculationMock)).when(calculationRepositoryMock).findCalculationToSendToExporter(calculationId);

            final Calculation report = objectUnderTest.findCalculationReadyToBeExported(calculationId);

            Assertions.assertThat(report).isEqualTo(calculationMock);
        }

        @Test
        void shouldThrowException_whenCalculationIsNotFoundById() {
            final UUID calculationId = UUID.fromString("63515664-6a90-4aab-a033-053e8b4a659c");

            doReturn(Optional.empty()).when(calculationRepositoryMock).findCalculationToSendToExporter(calculationId);

            Assertions.assertThatThrownBy(() -> objectUnderTest.findCalculationReadyToBeExported(calculationId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Calculation is not found by the provided calculation id: '%s'", calculationId);
        }

        @Test
        void shouldUpdateTimeCompletedAndStateByCalculationId() {
            try (final MockedStatic<LocalDateTime> localDateTimeMockedStatic = mockStatic(LocalDateTime.class)) {
                DriverManagerMock.prepare(connectionMock -> {
                    localDateTimeMockedStatic.when(LocalDateTime::now).thenReturn(TEST_TIME);

                    Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.updateCompletionState(CALCULATION_ID_1, KpiCalculationState.FINISHED));

                    localDateTimeMockedStatic.verify(LocalDateTime::now);
                    verify(calculationRepositoryMock).updateTimeCompletedAndStateByCalculationId(connectionMock, TEST_TIME, KpiCalculationState.FINISHED, CALCULATION_ID_1);
                });
            }
        }

        @Test
        void shouldUpdateTimeCompletedAndStateWithRetryByCalculationId() {
            DriverManagerMock.prepare(connectionMock -> {
                Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.updateCompletionStateWithRetry(CALCULATION_ID_1, KpiCalculationState.FINISHED, Retry.ofDefaults("default")));
                verify(calculationRepositoryMock).updateStateByCalculationId(connectionMock, KpiCalculationState.FINISHED, CALCULATION_ID_1);
            });
        }

        @Test
        void shouldUpdateRunningCalculationsToLost() {
            try (final MockedStatic<KpiCalculationState> kpiCalculationStateMockedStatic = mockStatic(KpiCalculationState.class)) {
                DriverManagerMock.prepare(connectionMock -> {
                    kpiCalculationStateMockedStatic.when(KpiCalculationState::getRunningKpiCalculationStates).thenReturn(emptyList());

                    Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.updateRunningCalculationsToLost(Retry.ofDefaults("default")));

                    kpiCalculationStateMockedStatic.verify(KpiCalculationState::getRunningKpiCalculationStates);
                    verify(calculationRepositoryMock).updateStateByStates(connectionMock, KpiCalculationState.LOST, emptyList());
                });
            }
        }

        @Test
        void shouldUpdateCalculationsToFinished() {
            try (final MockedStatic<KpiCalculationState> kpiCalculationStateMockedStatic = mockStatic(KpiCalculationState.class)) {
                DriverManagerMock.prepare(connectionMock -> {
                    kpiCalculationStateMockedStatic.when(KpiCalculationState::getRunningKpiCalculationStates).thenReturn(emptyList());
                    kpiCalculationStateMockedStatic.when(KpiCalculationState::getSuccessfulDoneKpiCalculationStates).thenReturn(emptyList());

                    Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.updateStatesAfterRestore(Retry.ofDefaults("default")));

                    kpiCalculationStateMockedStatic.verify(KpiCalculationState::getRunningKpiCalculationStates);
                    kpiCalculationStateMockedStatic.verify(KpiCalculationState::getSuccessfulDoneKpiCalculationStates);
                    verify(calculationRepositoryMock).updateStateByStates(connectionMock, KpiCalculationState.LOST, emptyList());
                    verify(calculationRepositoryMock).updateStateByStates(connectionMock, KpiCalculationState.FINISHED, emptyList());
                });
            }
        }

        @Test
        void shouldFinalizeHangingStartedCalculations() {
            try (final MockedStatic<LocalDateTime> localDateTimeMockedStatic = mockStatic(LocalDateTime.class)) {
                DriverManagerMock.prepare(connectionMock -> {
                    localDateTimeMockedStatic.when(LocalDateTime::now).thenReturn(TEST_TIME);

                    objectUnderTest.finalizeHangingStartedCalculations(Duration.ofHours(12));

                    localDateTimeMockedStatic.verify(LocalDateTime::now);
                    verify(calculationRepositoryMock).updateStateByStateAndTimeCreated(
                            connectionMock,
                            KpiCalculationState.LOST,
                            KpiCalculationState.STARTED,
                            TEST_TIME.minusHours(12)
                    );
                });
            }
        }

        @Test
        void shouldDeleteByTimeCreatedLessThen() {
            DriverManagerMock.prepare(connectionMock -> {
                Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.deleteByTimeCreatedLessThen(TEST_TIME));

                verify(calculationRepositoryMock).deleteByTimeCreatedLessThen(connectionMock, TEST_TIME);
            });
        }

        @Test
        void shouldFindCalculationsCompletedAfter(@Mock final List<Calculation> calculationsMock) {
            final LocalDateTime testTime = LocalDateTime.of(2_022, Month.OCTOBER, 27, 12, 0);
            try (final MockedStatic<LocalDateTime> localDateTimeMockedStatic = mockStatic(LocalDateTime.class)) {
                localDateTimeMockedStatic.when(LocalDateTime::now).thenReturn(testTime);
                when(calculationRepositoryMock.findCalculationsByTimeCreatedIsAfter(testTime.minusMinutes(5))).thenReturn(calculationsMock);

                final List<Calculation> actual = objectUnderTest.findCalculationsCreatedWithin(Duration.ofMinutes(5));

                localDateTimeMockedStatic.verify(LocalDateTime::now);
                verify(calculationRepositoryMock).findCalculationsByTimeCreatedIsAfter(testTime.minusMinutes(5));

                Assertions.assertThat(actual).isEqualTo(calculationsMock);
            }
        }

        @Test
        void shouldFindExecutionGroupByCalculationId() {
            final UUID calculationId = UUID.fromString("63515664-6a90-4aab-a033-053e8b4a659c");
            final String executionGroup = "execution_group";
            when(calculationRepositoryMock.forceFetchExecutionGroupByCalculationId(calculationId)).thenReturn(executionGroup);

            final String actual = objectUnderTest.forceFetchExecutionGroupByCalculationId(calculationId);
            verify(calculationRepositoryMock).forceFetchExecutionGroupByCalculationId(calculationId);
            Assertions.assertThat(actual).isEqualTo(executionGroup);
        }

        @MethodSource("provideAreCalculationsRunningForExecutionGroupData")
        @ParameterizedTest(name = "[{index}] With running calculations of ''{0}'' returns ''{1}''")
        void shouldVerifyAreCalculationsRunningForExecutionGroup(final long runningCalculations, final boolean expected) {
            try (final MockedStatic<KpiCalculationState> kpiCalculationStateMockedStatic = mockStatic(KpiCalculationState.class)) {
                final String executionGroup = "execution_group";

                kpiCalculationStateMockedStatic.when(KpiCalculationState::getRunningKpiCalculationStates).thenReturn(emptyList());
                when(calculationRepositoryMock.countByExecutionGroupAndStates(executionGroup, emptyList())).thenReturn(runningCalculations);

                final boolean actual = objectUnderTest.isAnyCalculationRunning(executionGroup);

                kpiCalculationStateMockedStatic.verify(KpiCalculationState::getRunningKpiCalculationStates);
                verify(calculationRepositoryMock).countByExecutionGroupAndStates(executionGroup, emptyList());

                Assertions.assertThat(actual).isEqualTo(expected);
            }
        }

        private Stream<Arguments> provideAreCalculationsRunningForExecutionGroupData() {
            return Stream.of(Arguments.of(5, true),
                    Arguments.of(0, false));
        }

        @Nested
        @TestInstance(Lifecycle.PER_CLASS)
        class IsAnyCalculationRunning {
            @MethodSource("provideIsAnyCalculationRunningData")
            @ParameterizedTest(name = "[{index}] With running calculations of ''{0}'' returns ''{1}''")
            void shouldVerifyIsAnyCalculationRunning(final long runningCalculations, final boolean expected) {
                try (final MockedStatic<KpiCalculationState> kpiCalculationStateMockedStatic = mockStatic(KpiCalculationState.class)) {
                    final List<String> executionGroups = List.of("execution_group");

                    kpiCalculationStateMockedStatic.when(KpiCalculationState::getRunningKpiCalculationStates).thenReturn(emptyList());
                    when(calculationRepositoryMock.countByExecutionGroupsAndStates(executionGroups, emptyList())).thenReturn(runningCalculations);

                    final boolean actual = objectUnderTest.isAnyCalculationRunning(executionGroups);

                    kpiCalculationStateMockedStatic.verify(KpiCalculationState::getRunningKpiCalculationStates);
                    verify(calculationRepositoryMock).countByExecutionGroupsAndStates(executionGroups, emptyList());

                    Assertions.assertThat(actual).isEqualTo(expected);
                }
            }

            Stream<Arguments> provideIsAnyCalculationRunningData() {
                return Stream.of(
                        Arguments.of(5, true),
                        Arguments.of(0, false)
                );
            }
        }
    }
}