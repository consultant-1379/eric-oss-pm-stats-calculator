/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository;

import static com.ericsson.oss.air.pm.stats._util.TestHelpers.calculations;
import static com.ericsson.oss.air.pm.stats._util.TestHelpers.complexCalculation;
import static com.ericsson.oss.air.pm.stats._util.TestHelpers.complexReadiness;
import static com.ericsson.oss.air.pm.stats._util.TestHelpers.readinessLog;
import static com.ericsson.oss.air.pm.stats._util.TestHelpers.readinessLogs;
import static com.ericsson.oss.air.pm.stats._util.TestHelpers.simpleCalculation;
import static com.ericsson.oss.air.pm.stats._util.TestHelpers.testMinute;
import static com.ericsson.oss.air.pm.stats._util.TestHelpers.uuid;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.FAILED;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.FINALIZING;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.FINISHED;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.STARTED;
import static com.ericsson.oss.air.pm.stats.repository._util.RepositoryHelpers.ComplexReadiness;
import static com.ericsson.oss.air.pm.stats.repository._util.RepositoryHelpers.database;
import static com.ericsson.oss.air.pm.stats.repository._util.RepositoryHelpers.databaseUrl;
import static com.ericsson.oss.air.pm.stats.repository._util.RepositoryHelpers.findAllComplexReadiness;
import static com.ericsson.oss.air.pm.stats.repository._util.RepositoryHelpers.properties;
import static com.ericsson.oss.air.pm.stats.repository._util.RepositoryHelpers.saveReadinessLog;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats._util.AssertionHelpers;
import com.ericsson.oss.air.pm.stats.model.entity.Calculation;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;
import com.ericsson.oss.air.pm.stats.model.exception.UncheckedSqlException;
import com.ericsson.oss.air.pm.stats.repository.api.CalculationRepository;
import com.ericsson.oss.air.pm.stats.test_utils.DatabasePropertiesMock;

import lombok.NonNull;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;

class ComplexReadinessLogRepositoryImplTest {
    CalculationRepository calculationRepository = new CalculationRepositoryImpl();

    EmbeddedDatabase embeddedDatabase;

    ComplexReadinessLogRepositoryImpl objectUnderTest = new ComplexReadinessLogRepositoryImpl();

    @BeforeEach
    void setUp() {
        embeddedDatabase = database(
                "sql/initialize_calculation.sql",
                "sql/initialize_complex_readiness_log.sql"
        );
    }

    @AfterEach
    void tearDown() {
        embeddedDatabase.shutdown();
    }

    @Test
    void shouldRaiseException_whenCouldNotSaveComplexReadinessLog() throws SQLException {
        final Connection connectionMock = mock(Connection.class);
        final ThrowingCallable throwingCallable = () -> objectUnderTest.save(
                connectionMock,
                uuid("52d40d7d-4c95-4533-be71-32b6380865a4"),
                List.of("random-group")
        );

        when(connectionMock.prepareStatement(anyString())).thenThrow(SQLException.class);

        Assertions.assertThatThrownBy(throwingCallable)
                .isInstanceOf(UncheckedSqlException.class);

        verify(connectionMock).prepareStatement(anyString());
    }

    @Test
    void shouldRaiseException_whenSaveWithEmptySimpleExecutionGroups() {
        final ThrowingCallable throwingCallable = () -> objectUnderTest.save(
                connection(),
                uuid("8cdad400-e590-4348-985e-82cb160983e3"),
                List.of()
        );

        Assertions.assertThatThrownBy(throwingCallable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("simpleExecutionGroups is empty. Every complex execution group must depend on a simple execution group.");
    }

    @ParameterizedTest
    @MethodSource("provideSaveData")
    void shouldVerifySave(final UUID complexCalculationId,
                          final Collection<String> simpleExecutionGroups,
                          final List<? extends Calculation> calculations,
                          final List<? extends ReadinessLog> readinessLogs,
                          final List<ComplexReadiness> expected) {
        saveCalculation(calculations);
        saveReadinessLog(connection(), readinessLogs);

        objectUnderTest.save(connection(), complexCalculationId, simpleExecutionGroups);

        final List<ComplexReadiness> actual = findAllComplexReadiness(connection());

        Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    static Stream<Arguments> provideSaveData() {
        final String complexCalculationId = "38b52b38-05b1-4716-8029-192a001ceb7f";

        return Stream.of(
                Arguments.of( /* Should do nothing when there is nothing to insert */
                        complexCalculationId(complexCalculationId),
                        simpleExecutionGroupDependencies("dependency-1", "dependency-2"),
                        calculations(
                                complexCalculation(uuid(complexCalculationId), testMinute(40), testMinute(45), FINISHED, "wanted-group"),           /* current complex */
                                simpleCalculation(uuid("6c7f30d3-5861-46c6-ad39-41738d07d1e5"), testMinute(25), testMinute(30), FAILED, "dependency-1"),      /* bad dependency - state */
                                simpleCalculation(uuid("d4edb240-778b-4108-a0e1-8a465ecf9ae6"), testMinute(25), testMinute(31), STARTED, "dependency-2")      /* bad dependency - state */
                        ),
                        readinessLogs(),
                        complexReadiness()
                ),
                Arguments.of( /* There is no previous complex execution group */
                        complexCalculationId(complexCalculationId),
                        simpleExecutionGroupDependencies("dependency-1", "dependency-2"),
                        calculations(
                                complexCalculation(uuid(complexCalculationId), testMinute(40), testMinute(45), FINISHED, "wanted-group"),                   /* current complex */
                                simpleCalculation(uuid("6c7f30d3-5861-46c6-ad39-41738d07d1e5"), testMinute(25), testMinute(30), FINISHED, "dependency-1"),      /* good dependency */
                                simpleCalculation(uuid("d4edb240-778b-4108-a0e1-8a465ecf9ae6"), testMinute(25), testMinute(31), FINALIZING, "dependency-2")      /* good dependency */
                        ),
                        readinessLogs(
                                readinessLog(1, "datasource-1", 10L, testMinute(15), testMinute(20), uuid("6c7f30d3-5861-46c6-ad39-41738d07d1e5")),    /* good dependency */
                                readinessLog(2, "datasource-2", 10L, testMinute(15), testMinute(20), uuid("d4edb240-778b-4108-a0e1-8a465ecf9ae6"))     /* good dependency */
                        ),
                        complexReadiness(
                                ComplexReadiness.of(1, uuid(complexCalculationId)),
                                ComplexReadiness.of(2, uuid(complexCalculationId))
                        )
                ),
                Arguments.of( /* Handling multiple edge cases */
                        complexCalculationId(complexCalculationId),
                        simpleExecutionGroupDependencies("dependency-1", "dependency-2"),
                        calculations(
                                complexCalculation(uuid(complexCalculationId), testMinute(40), testMinute(45), FINISHED, "wanted-group"),                   /* current complex */
                                complexCalculation(uuid("0b5e2b3f-2f6e-405d-bbff-0040da567b20"), testMinute(35), testMinute(40), FAILED, "wanted-group"),       /* other complex - state difference */
                                complexCalculation(uuid("c31d3dcf-0ebf-494b-9e1e-2857c1bcd5d3"), testMinute(30), testMinute(40), FINISHED, "unwanted-group"),   /* other complex - execution group difference */
                                complexCalculation(uuid("4caab1c0-7a3b-43c2-85c6-6e25e9f98328"), testMinute(15), testMinute(30), FINISHED, "wanted-group"),     /* previous complex - max */
                                complexCalculation(uuid("5756fb61-aa51-4249-bebe-ec7c48ca1bde"), testMinute(0), testMinute(15), FINISHED, "wanted-group"),      /* previous complex */
                                simpleCalculation(uuid("6c7f30d3-5861-46c6-ad39-41738d07d1e5"), testMinute(25), testMinute(30), FINISHED, "dependency-1"),      /* good dependency */
                                simpleCalculation(uuid("d4edb240-778b-4108-a0e1-8a465ecf9ae6"), testMinute(25), testMinute(31), FINALIZING, "dependency-2"),      /* good dependency */
                                simpleCalculation(uuid("3861238f-6e66-4bfd-8fe7-de4e1458bb78"), testMinute(25), testMinute(35), FAILED, "dependency-1"),        /* bad dependency - wrong state */
                                simpleCalculation(uuid("51c85682-39f7-4b90-ae32-8f4fb7adbb1c"), testMinute(25), testMinute(35), FINISHED, "group-1"),           /* bad dependency - wrong group */
                                simpleCalculation(uuid("85a152cd-bffa-4b9b-aa25-630307502cca"), testMinute(15), testMinute(20), FINISHED, "dependency-1"),      /* bad dependency - early time completed */
                                simpleCalculation(uuid("71833324-e6aa-4fe6-86ec-6ddd68ccea35"), testMinute(15), testMinute(20), FINISHED, "dependency-1")       /* bad dependency - early time completed */
                        ),
                        readinessLogs(
                                readinessLog(1, "datasource-1", 10L, testMinute(15), testMinute(20), uuid("6c7f30d3-5861-46c6-ad39-41738d07d1e5")),    /* good dependency */
                                readinessLog(2, "datasource-2", 10L, testMinute(15), testMinute(20), uuid("d4edb240-778b-4108-a0e1-8a465ecf9ae6")),     /* good dependency */
                                readinessLog(3, "datasource-3", 10L, testMinute(15), testMinute(20), uuid("51c85682-39f7-4b90-ae32-8f4fb7adbb1c")),
                                readinessLog(4, "datasource-4", 10L, testMinute(15), testMinute(20), uuid("85a152cd-bffa-4b9b-aa25-630307502cca")),
                                readinessLog(5, "datasource-5", 10L, testMinute(15), testMinute(20), uuid("71833324-e6aa-4fe6-86ec-6ddd68ccea35"))
                        ),
                        complexReadiness(
                                ComplexReadiness.of(1, uuid(complexCalculationId)),
                                ComplexReadiness.of(2, uuid(complexCalculationId))
                        )
                )
        );
    }

    @Test
    void shouldThrowUncheckedSqlException_onFindByCalculationId() {
        AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findByCalculationId(uuid("87719ee6-934b-4ce1-a818-10b6ac1238cc")));
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class GivenAvailableComplexReadinessLog {

        @BeforeEach
        void setUp() {
            embeddedDatabase = database("sql/initialize_readiness_and_complex_readiness_log.sql");
        }

        @AfterEach
        void tearDown() {
            embeddedDatabase.shutdown();
        }

        @MethodSource("provideFindReadinessLogsByCalculationId")
        @ParameterizedTest(name = "[{index}] ''{0}'' has readiness log: ''{1}''")
        void shouldFindReadinessLogsByCalculationId(final UUID complexCalculationId, final List<ReadinessLog> expected) {
            DatabasePropertiesMock.prepare(databaseUrl(embeddedDatabase), properties(), () -> {
                final List<ReadinessLog> actual = objectUnderTest.findByCalculationId(complexCalculationId);

                Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
            });
        }

        Stream<Arguments> provideFindReadinessLogsByCalculationId() {
            return Stream.of(
                    Arguments.of( /* Should return empty if complex_calculation_id not found */
                            uuid("839fc33c-20d8-4824-bfc8-408fb20fa08e"),
                            readinessLogs()
                    ),
                    Arguments.of( /* When found single complex_calculation_id */
                            uuid("87719ee6-934b-4ce1-a818-10b6ac1238cc"),
                            readinessLogs(
                                    readinessLog(2, "datasource2", 15L, LocalDateTime.of(2_022, Month.AUGUST, 3, 18, 0, 0), LocalDateTime.of(2_022, Month.AUGUST, 4, 18, 0, 0), uuid("84edfb50-95d5-4afb-b1e8-103ee4acbeb9"))
                            )
                    ),
                    Arguments.of( /* When found multiple complex_calculation_id */
                            uuid("b77a2a48-92d9-41be-a669-5e9a27c2c1af"),
                            readinessLogs(
                                    readinessLog(2, "datasource2", 15L, LocalDateTime.of(2_022, Month.AUGUST, 3, 18, 0, 0), LocalDateTime.of(2_022, Month.AUGUST, 4, 18, 0, 0), uuid("84edfb50-95d5-4afb-b1e8-103ee4acbeb9")),
                                    readinessLog(4, "datasource4", 25L, LocalDateTime.of(2_022, Month.AUGUST, 3, 17, 0, 0), LocalDateTime.of(2_022, Month.AUGUST, 4, 17, 20, 0), uuid("08673863-2573-4631-9d88-2a87db1b7887"))
                            )
                    )
            );
        }
    }

    static UUID complexCalculationId(final String uuid) {
        return uuid(uuid);
    }

    static List<String> simpleExecutionGroupDependencies(final String... executionGroups) {
        return List.of(executionGroups);
    }

    @SneakyThrows
    Connection connection() {
        return embeddedDatabase.getConnection();
    }

    @SneakyThrows
    void saveCalculation(final Calculation calculation) {
        calculationRepository.save(connection(), calculation);
    }

    void saveCalculation(@NonNull final Collection<? extends Calculation> calculations) {
        calculations.forEach(this::saveCalculation);
    }
}
