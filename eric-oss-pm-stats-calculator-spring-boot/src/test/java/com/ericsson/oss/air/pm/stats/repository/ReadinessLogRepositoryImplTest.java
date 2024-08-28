/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats._util.AssertionHelpers;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.common.model.collection.CollectionIdProxy;
import com.ericsson.oss.air.pm.stats.model.entity.Calculation;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;
import com.ericsson.oss.air.pm.stats.repository._util.RepositoryHelpers;
import com.ericsson.oss.air.pm.stats.repository.api.CalculationRepository;

import lombok.SneakyThrows;
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

class ReadinessLogRepositoryImplTest {

    private static final ReadinessLog READINESS_LOG1 = readinessLog(
            1,
            "datasource1",
            10L,
            testTime(3, 17, 0),
            testTime(4, 17, 0),
            UUID.fromString("84edfb50-95d5-4afb-b1e8-103ee4acbeb9"));

    private static final ReadinessLog READINESS_LOG2 = readinessLog(
            2,
            "datasource2",
            15L,
            testTime(3, 18, 0),
            testTime(4, 18, 0),
            UUID.fromString("84edfb50-95d5-4afb-b1e8-103ee4acbeb9"));

    private static final ReadinessLog READINESS_LOG3 = readinessLog(
            3,
            "datasource3",
            20L,
            testTime(3, 19, 0),
            testTime(4, 19, 0),
            UUID.fromString("c5c46e48-32cf-488e-bd31-803d8078efbe"));

    private static final ReadinessLog READINESS_LOG4 = readinessLog(
            4,
            "datasource4",
            25L,
            testTime(3, 17, 0),
            testTime(4, 17, 20),
            UUID.fromString("08673863-2573-4631-9d88-2a87db1b7887"));

    private static final ReadinessLog READINESS_LOG5 = readinessLog(
            5,
            "datasource5",
            15L,
            testTime(3, 17, 30),
            testTime(4, 18, 0),
            UUID.fromString("131a2534-2c26-4f60-9169-4d27a872454b"));

    private ReadinessLogRepositoryImpl objectUnderTest;
    private EmbeddedDatabase embeddedDatabase;

    @BeforeEach
    void setUp() {
        objectUnderTest = new ReadinessLogRepositoryImpl();

        embeddedDatabase = RepositoryHelpers.database("sql/initialize_readiness_log.sql", "sql/initialize_calculation.sql");
    }

    @AfterEach
    void tearDown() {
        embeddedDatabase.shutdown();
    }

    @Test
    void shouldThrowUncheckedSqlException_onFindByCalculationId() {
        AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findByCalculationId(READINESS_LOG1.getKpiCalculationId()));
    }

    @Test
    void shouldThrowUncheckedSqlException_onFindLatestReadinessLogByExecutionGroup() {
        AssertionHelpers.assertUncheckedSqlException(
                () -> objectUnderTest.findLatestReadinessLogsByExecutionGroup("complexGroup", List.of("executionGroup")));
    }

    @Test
    void shouldFindReadinessLogsByCalculationId() {
        RepositoryHelpers.prepare(embeddedDatabase, () -> {
            final List<ReadinessLog> actual = objectUnderTest.findByCalculationId(READINESS_LOG1.getKpiCalculationId());

            assertThat(actual).containsExactlyInAnyOrder(READINESS_LOG1, READINESS_LOG2);
        });
    }

    private static ReadinessLog readinessLog(
            final int id, final String datasource, final long collectedRowsCount, final LocalDateTime earliestCollectedData,
            final LocalDateTime latestCollectedData, final UUID kpiCalculationId) {
        return ReadinessLog.builder()
                .withId(id)
                .withDatasource(datasource)
                .withCollectedRowsCount(collectedRowsCount)
                .withEarliestCollectedData(earliestCollectedData)
                .withLatestCollectedData(latestCollectedData)
                .withKpiCalculationId(kpiCalculationId)
                .build();
    }

    private static LocalDateTime testTime(final int dayOfMonth, final int hour, final int minute) {
        return LocalDateTime.of(2_022, Month.AUGUST, dayOfMonth, hour, minute, 0);
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class FindLatestReadinessLogByExecutionGroup {
        CalculationRepository calculationRepository = new CalculationRepositoryImpl();

        @MethodSource("provideData")
        @ParameterizedTest(name = "[{index}] Complex Group  ''{0}'' ''{1}''")
        void shouldFindLatestReadinessLogByExecutionGroup(final String complexGroupName, final List<String> targetGroups,
                                                          final List<ReadinessLog> requirements) {
            final LocalDateTime testTime = LocalDateTime.of(2_022, Month.OCTOBER, 8, 10, 15);
            final String targetGroup = "targetGroup";

            persistCalculation(READINESS_LOG4.getKpiCalculationId().toString(), targetGroup, testTime.minusMinutes(15), KpiCalculationState.FINISHED,
                    KpiType.SCHEDULED_SIMPLE, CollectionIdProxy.COLLECTION_ID);
            persistCalculation(READINESS_LOG1.getKpiCalculationId().toString(), targetGroup, testTime.minusMinutes(10), KpiCalculationState.FINISHED,
                    KpiType.SCHEDULED_SIMPLE, CollectionIdProxy.COLLECTION_ID);
            persistCalculation(READINESS_LOG3.getKpiCalculationId().toString(), targetGroup, testTime.minusMinutes(5), KpiCalculationState.FAILED,
                    KpiType.SCHEDULED_SIMPLE, CollectionIdProxy.COLLECTION_ID);
            persistCalculation(READINESS_LOG5.getKpiCalculationId().toString(), "anotherTargetGroup", testTime.minusMinutes(5),
                    KpiCalculationState.FINISHED, KpiType.SCHEDULED_SIMPLE, CollectionIdProxy.COLLECTION_ID);
            persistCalculation("d02687a0-80fc-4276-aee8-b37dfc1ef3b5", "otherGroup", testTime.minusMinutes(1), KpiCalculationState.FINISHED,
                    KpiType.SCHEDULED_SIMPLE, CollectionIdProxy.COLLECTION_ID);
            persistCalculation("eeeeeeee-2573-4631-9d88-2a87db1b7887", "availableComplexGroup", testTime.minusMinutes(15).plusSeconds(1),
                    KpiCalculationState.FINISHED, KpiType.SCHEDULED_SIMPLE, CollectionIdProxy.COLLECTION_ID);

            RepositoryHelpers.prepare(embeddedDatabase, () -> {
                final List<ReadinessLog> actual = objectUnderTest.findLatestReadinessLogsByExecutionGroup(complexGroupName, targetGroups);

                assertThat(actual).containsExactlyInAnyOrderElementsOf(requirements);
            });

        }

        Stream<Arguments> provideData() {
            return Stream.of(Arguments.of("availableComplexGroup",
                            List.of("targetGroup", "anotherTargetGroup"),
                            List.of(READINESS_LOG1,
                                    READINESS_LOG2,
                                    READINESS_LOG5)),
                    Arguments.of("notAvailableComplexGroup",
                            List.of("targetGroup"),
                            List.of(READINESS_LOG1,
                                    READINESS_LOG2,
                                    READINESS_LOG4)));
        }

        @SneakyThrows
        void persistCalculation(final String id, final String executionGroup, final LocalDateTime completed, final KpiCalculationState state,
                                final KpiType kpiType, final UUID collectionId) {
            calculationRepository.save(
                    embeddedDatabase.getConnection(),
                    Calculation.builder()
                            .withCalculationId(UUID.fromString(id))
                            .withExecutionGroup(executionGroup)
                            .withTimeCreated(completed.minusMinutes(5)) /* Not relevant  */
                            .withTimeCompleted(completed)
                            .withKpiCalculationState(state).withKpiType(kpiType)
                            .withCollectionId(collectionId)
                            .build());
        }
    }
}
