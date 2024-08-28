/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.repository.internal;

import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.FAILED;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.FINISHED;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.IN_PROGRESS;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.Calculation;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.ReadinessLog;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test-containers")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ReadinessLogRepositoryTest {

    @Autowired ReadinessLogRepository objectUnderTest;
    @Autowired CalculationRepository calculationRepository;

    @Nested
    class FindLatestByDataSourceAndExecutionGroup {
        @Nested
        @TestInstance(Lifecycle.PER_CLASS)
        class FindReadinessLog {

            @Test
            void shouldFindLatestReadinessLog() {
                persistCalculation(uuid("f761f4a9-565d-4786-a734-e56865c7143d"), "group_1", testTime(12, 0), null, IN_PROGRESS); /* Unfinished calculation */
                persistCalculation(uuid("c94cef30-4e7e-4004-a59e-ec0f4dcf5a6"), "group_1", testTime(12, 15), testTime(12, 30), FAILED); /* Failed calculation */

                objectUnderTest.saveAll(Arrays.asList(
                        readinessLog(
                                persistCalculation(uuid("02025e87-01b4-4f7b-b946-b185f8930c31"), "group_1", testTime(11, 55), testTime(12, 10), FINISHED),
                                dataSource("group_1"),
                                testTime(11, 50),
                                testTime(12, 10)
                        ),
                        readinessLog( /* Highest collected data time */
                                persistCalculation(uuid("0e2897e8-ad38-441d-bd6f-bee35dbbce62"), "group_1", testTime(12, 0), testTime(12, 15), FINISHED),
                                dataSource("group_1"),
                                testTime(12, 10),
                                testTime(12, 15)
                        ),
                        readinessLog( /* Late data calculation, last in line */
                                persistCalculation(uuid("11550c4e-5a93-4841-a84c-1100a15c2d5b"), "group_1", testTime(12, 5), testTime(12, 20), FINISHED),
                                dataSource("group_1"),
                                testTime(11, 10),
                                testTime(11, 15)
                        ),
                        readinessLog( /* Different execution group */
                                persistCalculation(uuid("4c949ac6-4523-4802-94a1-025fa3365597"), "group2", testTime(15, 0), testTime(15, 30), FINISHED),
                                dataSource("group_2"),
                                testTime(15, 0),
                                testTime(15, 30)
                        )
                ));

                final Optional<LocalDateTime> actual = objectUnderTest.findLatestCollectedTimeByDataSourceAndExecutionGroup(dataSource("group_1"), "group_1");

                Assertions.assertThat(actual).hasValue(testTime(12, 15));
            }
        }

        @Nested
        class NotFindReadinessLog {
            @Test
            void shouldNotFind_whenReadinessLogIsEmpty() {
                persistCalculation(uuid("f761f4a9-565d-4786-a734-e56865c7143d"), "group_1", testTime(12, 0), null, IN_PROGRESS); /* Unfinished calculation */
                Assertions.assertThat(objectUnderTest.count()).as("no readiness logs persisted yet").isZero();

                final Optional<LocalDateTime> actual = objectUnderTest.findLatestCollectedTimeByDataSourceAndExecutionGroup(dataSource("group_1"), "group_1");

                Assertions.assertThat(actual).isEmpty();
            }
        }
    }

    Calculation persistCalculation(final UUID uuid, final String group, final LocalDateTime created, final LocalDateTime completed, final KpiCalculationState state) {
        return calculationRepository.save(
                calculation(uuid, group, state, created, completed)
        );
    }

    static ReadinessLog readinessLog(final Calculation calc,
                                     final String datasource,
                                     final LocalDateTime earliestCollectedData,
                                     final LocalDateTime latestCollectedData) {
        return ReadinessLog.builder()
                           .collectedRowsCount(3L)
                           .datasource(datasource)
                           .earliestCollectedData(earliestCollectedData)
                           .latestCollectedData(latestCollectedData)
                           .kpiCalculationId(calc)
                           .build();
    }

    private static Calculation calculation(final UUID uuid,
                                           final String group,
                                           final KpiCalculationState state,
                                           final LocalDateTime timeCreated,
                                           final LocalDateTime timeCompleted) {
        return Calculation.builder()
                          .id(uuid)
                          .timeCreated(timeCreated)
                          .timeCompleted(timeCompleted)
                          .state(state)
                          .executionGroup(group)
                          .kpiType(KpiType.SCHEDULED_SIMPLE)
                          .build();
    }

    static String dataSource(final String group) {
        return group;
    }

    static UUID uuid(final String uuid) {
        return UUID.fromString(uuid);
    }

    static LocalDateTime testTime(final int hour, final int minute) {
        return LocalDateTime.of(2_022, Month.OCTOBER, 15, hour, minute);
    }
}
