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

import static com.ericsson.oss.air.pm.stats._util.TestHelpers.calculationReliabilities;
import static com.ericsson.oss.air.pm.stats._util.TestHelpers.calculationReliability;
import static com.ericsson.oss.air.pm.stats._util.TestHelpers.testTime;
import static com.ericsson.oss.air.pm.stats._util.TestHelpers.uuid;
import static com.ericsson.oss.air.pm.stats.repository._util.RepositoryHelpers.findAllCalculationReliability;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats._util.AssertionHelpers;
import com.ericsson.oss.air.pm.stats.model.entity.CalculationReliability;
import com.ericsson.oss.air.pm.stats.model.exception.UncheckedSqlException;
import com.ericsson.oss.air.pm.stats.repository._util.RepositoryHelpers;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;

class CalculationReliabilityRepositoryImplTest {
    static final UUID CALCULATION_ID = uuid("b10de8fb-2417-44cd-802b-19e0b13fd3a5");
    static final UUID CALCULATION_ID_2 = uuid("991be859-1487-4fe2-bbea-fcbc7e06e7ca");

    CalculationReliabilityRepositoryImpl objectUnderTest = new CalculationReliabilityRepositoryImpl();
    EmbeddedDatabase embeddedDatabase;

    @BeforeEach
    void setUp() {
        embeddedDatabase = RepositoryHelpers.database(
                "sql/initialize_kpi_definition.sql",
                "sql/initialize_calculation_reliability.sql"
        );
    }

    @AfterEach
    void tearDown() {
        embeddedDatabase.shutdown();
    }

    @Test
    void shouldFindReliabilityThrowUncheckedSqlException() {
        AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findReliabilityThresholdByCalculationId(CALCULATION_ID));
    }

    @Test
    void shouldFindCalculationStartThrowUncheckedSqlException() {
        AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findCalculationStartByCalculationId(CALCULATION_ID));
    }

    @Test
    void shouldFindReliabilityByKpiNameThrowUncheckedSqlException() {
        AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findMaxReliabilityThresholdByKpiName());
    }

    @Test
    void shouldFindReliabilitiesByCalculationId() {
        RepositoryHelpers.prepare(embeddedDatabase, () -> {
            final Map<String, LocalDateTime> actual = objectUnderTest.findReliabilityThresholdByCalculationId(CALCULATION_ID);

            assertThat(actual).containsExactly(
                    Map.entry("kpiDefinition1", LocalDateTime.of(2022, Month.AUGUST, 4, 0, 0, 0)),
                    Map.entry("kpiDefinition2", LocalDateTime.of(2022, Month.AUGUST, 3, 5, 0, 0)));
        });
    }

    @Test
    void shouldFindCalculationStartByCalculationId() {
        RepositoryHelpers.prepare(embeddedDatabase, () -> {
            final Map<String, LocalDateTime> actual = objectUnderTest.findCalculationStartByCalculationId(CALCULATION_ID);

            assertThat(actual).containsExactly(
                    Map.entry("kpiDefinition1", LocalDateTime.of(2022, Month.AUGUST, 3, 0, 0, 0)),
                    Map.entry("kpiDefinition2", LocalDateTime.of(2022, Month.AUGUST, 3, 2, 0, 0)));
        });
    }

    @Test
    void shouldFindMaxReliabilities() {
        RepositoryHelpers.prepare(embeddedDatabase, () -> {
            final Map<String, LocalDateTime> actual = objectUnderTest.findMaxReliabilityThresholdByKpiName();

            assertThat(actual).containsExactly(
                    Map.entry("kpiDefinition1", LocalDateTime.of(2022, Month.AUGUST, 4, 0, 0, 0)),
                    Map.entry("kpiDefinition2", LocalDateTime.of(2022, Month.AUGUST, 3, 6, 0, 0)));
        });
    }

    @Test
    void shouldRaiseException_whenCouldNotSaveCalculationReliability() throws SQLException {
        final Connection connectionMock = mock(Connection.class);
        final ThrowableAssert.ThrowingCallable throwingCallable = () -> objectUnderTest.save(
                connectionMock,
                any()
        );

        when(connectionMock.prepareStatement(any())).thenThrow(SQLException.class);

        Assertions.assertThatThrownBy(throwingCallable)
                .isInstanceOf(UncheckedSqlException.class);

        verify(connectionMock).prepareStatement(anyString());
    }

    @ParameterizedTest
    @MethodSource("provideSaveData")
    void shouldVerifySave(final List<CalculationReliability> calculationReliabilitiesToSave,
                          final List<CalculationReliability> expectedCalculationReliabilities) {

        objectUnderTest.save(connection(), calculationReliabilitiesToSave);

        final List<CalculationReliability> actual = findAllCalculationReliability(connection());

        Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expectedCalculationReliabilities);
    }

    static Stream<Arguments> provideSaveData() {
        final UUID complexCalculationId1 = uuid("38b52b38-05b1-4716-8029-192a001ceb7f");

        return Stream.of(
                Arguments.of(
                        calculationReliabilities(
                                calculationReliability(testTime(1, 12, 0), testTime(1, 13, 0), complexCalculationId1, 3)
                        ),
                        calculationReliabilities(
                                calculationReliability(testTime(3, 0, 0), testTime(4, 0, 0), CALCULATION_ID, 1),
                                calculationReliability(testTime(3, 2, 0), testTime(3, 5, 0), CALCULATION_ID, 2),
                                calculationReliability(testTime(3, 3, 0), testTime(3, 6, 0), CALCULATION_ID_2, 2),

                                calculationReliability(testTime(1, 12, 0), testTime(1, 13, 0), complexCalculationId1, 3)
                        )
                ),
                Arguments.of(
                        calculationReliabilities(),
                        calculationReliabilities(
                                calculationReliability(testTime(3, 0, 0), testTime(4, 0, 0), CALCULATION_ID, 1),
                                calculationReliability(testTime(3, 2, 0), testTime(3, 5, 0), CALCULATION_ID, 2),
                                calculationReliability(testTime(3, 3, 0), testTime(3, 6, 0), CALCULATION_ID_2, 2)
                        )
                )
        );
    }

    @SneakyThrows
    Connection connection() {
        return embeddedDatabase.getConnection();
    }
}
