/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository;

import static com.ericsson.oss.air.pm.stats._util.TestHelpers.uuid;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.FAILED;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.FINALIZING;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.FINISHED;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.IN_PROGRESS;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.LOST;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.STARTED;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.valueOf;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats._util.AssertionHelpers;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.common.model.collection.CollectionIdProxy;
import com.ericsson.oss.air.pm.stats.model.entity.Calculation;
import com.ericsson.oss.air.pm.stats.model.entity.CalculationReliability;
import com.ericsson.oss.air.pm.stats.repository._util.RepositoryHelpers;
import com.ericsson.oss.air.pm.stats.repository.api.CalculationReliabilityRepository;
import com.ericsson.oss.air.pm.stats.repository.api.CalculationRepository;

import lombok.NonNull;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;

class CalculationRepositoryImplTest {
    static final UUID UUID_1 = UUID.fromString("6cf32e0a-5a0f-4a4a-8d8d-3774ffcd3808");
    static final UUID UUID_2 = UUID.fromString("21ae3afa-eccf-448c-89a7-6c8953087233");
    static final UUID UUID_3 = UUID.fromString("2bd03d55-9cfa-4807-8b76-378e11bb3586");
    static final UUID UUID_4 = UUID.fromString("070e645c-4607-48b6-91ac-826b37c8fe42");

    static final LocalDateTime TEST_TIME = LocalDateTime.of(LocalDate.of(2_022, Month.APRIL, 19), LocalTime.NOON);

    CalculationRepository objectUnderTest = new CalculationRepositoryImpl();

    @Nested
    @DisplayName("When something goes wrong")
    class WhenSomethingGoesWrong {

        @Test
        void shouldThrowUncheckedSqlException_onFindByCalculationId() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findByCalculationId(uuid("bfe45147-5d46-4829-bc30-689283b4f602")));
        }

        @Test
        void shouldThrowUncheckedSqlException_onCountByExecutionGroupAndStates() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.countByExecutionGroupAndStates("execution_group", Collections.emptyList()));
        }

        @Test
        void shouldThrowUncheckedSqlException_onCountByExecutionGroupsAndStates() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.countByExecutionGroupsAndStates(
                    Collections.emptyList(),
                    Collections.emptyList()
            ));
        }

        @Test
        void shouldThrowUncheckedSqlException_onCountByStates() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.countByStates());
        }

        @Test
        void shouldThrowUncheckedSqlException_onFindExecutionGroupByCalculationId() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findExecutionGroupByCalculationId(
                    uuid("bfe45147-5d46-4829-bc30-689283b4f602")
            ));
        }

        @Test
        void shouldThrowUncheckedSqlException_onFindAllByState() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findAllByState(FINISHED));
        }

        @Test
        void shouldThrowUncheckedSqlException_onFindCalculationByTimeCompletedIsAfter() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findCalculationsByTimeCreatedIsAfter(null));
        }

        @Test
        void shouldThrowUncheckedSqlException_onGetLastComplexCompletedTime() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.getLastComplexCalculationReliability(null));
        }

        @Test
        void shouldThrowUncheckedSqlException_onFindKpiTypeByCalculationId() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findKpiTypeByCalculationId(null));
        }
    }

    @Nested
    @DisplayName("When database is available")
    class WhenDatabaseIsAvailable {
        EmbeddedDatabase embeddedDatabase;

        Calculation calculation;

        @BeforeEach
        void setUp() throws SQLException {
            embeddedDatabase = RepositoryHelpers.database("sql/initialize_calculation.sql");
            calculation = motherCalculation();
        }

        @AfterEach
        void tearDown() {
            embeddedDatabase.shutdown();
        }

        @Nested
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("and database is populated")
        class AndDatabaseIsPopulated {

            @BeforeEach
            @SneakyThrows
            void setUp() {
                saveCalculation(calculation);
            }

            @MethodSource("provideFindByCalculationIdData")
            @ParameterizedTest(name = "[{index}] findByCalculationId return: ''{1}'' when calculationId: ''{0}''")
            void should_findByCalculationId(final UUID calculationId, final Optional<KpiCalculationState> expected) {
                RepositoryHelpers.prepare(embeddedDatabase, () -> {
                    final Optional<KpiCalculationState> actual = objectUnderTest.findByCalculationId(calculationId);

                    Assertions.assertThat(actual).isEqualTo(expected);
                });
            }

            @Test
            void shouldFindKpiTypeByCalculationId() {
                RepositoryHelpers.prepare(embeddedDatabase, () -> {
                    final Optional<KpiType> actual = objectUnderTest.findKpiTypeByCalculationId(UUID_1);

                    Assertions.assertThat(actual).hasValue(KpiType.SCHEDULED_SIMPLE);
                });
            }

            @Test
            void shouldFindExecutionGroupByCalculationId() {
                RepositoryHelpers.prepare(embeddedDatabase, () -> {
                    final Optional<String> actual = objectUnderTest.findExecutionGroupByCalculationId(UUID_1);

                    Assertions.assertThat(actual).hasValue("execution_group");
                });
            }

            @Test
            void shouldFindAllByState() {
                RepositoryHelpers.prepare(embeddedDatabase, () -> {
                    final List<Calculation> actual = objectUnderTest.findAllByState(STARTED);

                    Assertions.assertThat(actual).first().satisfies(calculation -> {
                        Assertions.assertThat(calculation.getKpiCalculationState()).isEqualTo(STARTED);
                    });
                });
            }

            @Test
            void shouldFindNoExecutionGroupByCalculationId() {
                RepositoryHelpers.prepare(embeddedDatabase, () -> {
                    final Optional<String> actual = objectUnderTest.findExecutionGroupByCalculationId(
                            UUID.fromString("f9720130-7886-427c-a194-7d10e7cefec5")
                    );

                    Assertions.assertThat(actual).isEmpty();
                });
            }


            @Test
            void shouldFindCalculationToSendToExporter() throws Exception {
                final LocalDateTime timeCompleted = TEST_TIME.plusMinutes(5);

                objectUnderTest.updateTimeCompletedAndStateByCalculationId(
                        embeddedDatabase.getConnection(),
                        timeCompleted,
                        FINALIZING,
                        calculation.getCalculationId()
                );

                RepositoryHelpers.prepare(embeddedDatabase, () -> {
                    final Optional<Calculation> actual = objectUnderTest.findCalculationToSendToExporter(calculation.getCalculationId());

                    Assertions.assertThat(actual).hasValueSatisfying(savedCalculation -> {
                        Assertions.assertThat(savedCalculation.getTimeCompleted()).isEqualTo(timeCompleted);
                        Assertions.assertThat(savedCalculation.getKpiCalculationState()).isEqualTo(FINALIZING);
                    });
                });
            }

            @Test
            void shouldNotFindCalculationToSendToExporter_whenTheStateIsNotAppropriate() throws Exception {
                final LocalDateTime timeCompleted = TEST_TIME.plusMinutes(5);

                objectUnderTest.updateTimeCompletedAndStateByCalculationId(
                        embeddedDatabase.getConnection(),
                        timeCompleted,
                        FINISHED,
                        calculation.getCalculationId()
                );

                RepositoryHelpers.prepare(embeddedDatabase, () -> {
                    final Optional<Calculation> actual = objectUnderTest.findCalculationToSendToExporter(calculation.getCalculationId());
                    Assertions.assertThat(actual).isEmpty();
                });
            }

            @Test
            void shouldUpdateTimeCompletedAndStateByCalculationId() throws Exception {
                final LocalDateTime timeCompleted = TEST_TIME.plusMinutes(5);
                final KpiCalculationState kpiCalculationState = FINISHED;

                objectUnderTest.updateTimeCompletedAndStateByCalculationId(embeddedDatabase.getConnection(),
                        timeCompleted,
                        kpiCalculationState,
                        calculation.getCalculationId());

                final List<Calculation> actual = findAll();

                Assertions.assertThat(actual).satisfiesExactly(calculationSaved -> {
                    Assertions.assertThat(calculationSaved.getTimeCompleted()).isEqualTo(timeCompleted);
                    Assertions.assertThat(calculationSaved.getKpiCalculationState()).isEqualTo(kpiCalculationState);
                });
            }

            @Test
            void shouldUpdateStateByCalculationId() throws Exception {
                final KpiCalculationState kpiCalculationState = FINISHED;

                objectUnderTest.updateStateByCalculationId(embeddedDatabase.getConnection(),
                        kpiCalculationState,
                        calculation.getCalculationId());

                final List<Calculation> actual = findAll();

                Assertions.assertThat(actual).satisfiesExactly(calculationSaved -> {
                    Assertions.assertThat(calculationSaved.getKpiCalculationState()).isEqualTo(kpiCalculationState);
                });
            }

            @MethodSource("provideCountByExecutionGroupAndStatesData")
            @ParameterizedTest(name = "[{index}] Count ''{2}'' rows with executionGroup: ''{0}'' and states: ''{1}''")
            void shouldCountByExecutionGroupAndStates(final String executionGroup, final List<KpiCalculationState> kpiCalculationStates, final long expected) {
                saveCalculation(calculation(UUID_2, "execution_group1", STARTED));
                saveCalculation(calculation(UUID_3, "execution_group1", FINISHED));
                saveCalculation(calculation(UUID_4, "execution_group3", LOST));

                RepositoryHelpers.prepare(embeddedDatabase, () -> {
                    final long actual = objectUnderTest.countByExecutionGroupAndStates(executionGroup, kpiCalculationStates);

                    Assertions.assertThat(actual).isEqualTo(expected);
                });
            }

            @MethodSource("provideCountByStatesData")
            @ParameterizedTest(name = "[{index}] Count rows for states: ''{0}'' expecting: ''{1}''")
            void shouldCountByStates(final Collection<KpiCalculationState> kpiCalculationStates, final Map<KpiCalculationState, Long> expected) {
                saveCalculation(calculation(UUID_3, "execution_group1", FINISHED));
                saveCalculation(calculation(UUID_4, "execution_group3", LOST));

                RepositoryHelpers.prepare(embeddedDatabase, () -> {
                    final Map<KpiCalculationState, Long> actual = kpiCalculationStates.isEmpty()
                            ? objectUnderTest.countByStates()
                            : objectUnderTest.countByStates(kpiCalculationStates);
                    Assertions.assertThat(actual).containsExactlyInAnyOrderEntriesOf(expected);
                });
            }

            @Nested
            @TestInstance(Lifecycle.PER_CLASS)
            class CountByExecutionGroupsAndStates {
                @MethodSource("provideCountByExecutionGroupsAndStatesData")
                @ParameterizedTest(name = "[{index}] Count ''{2}'' rows with executionGroups: ''{0}'' and states: ''{1}''")
                void shouldCountByExecutionGroupsAndStates(final List<String> executionGroups, final List<KpiCalculationState> kpiCalculationStates, final long expected) {
                    saveCalculation(calculation(uuid("d23cfb80-582f-4ef6-bcc3-3321be6f5c13"), "group1", STARTED));
                    saveCalculation(calculation(uuid("8d920e7b-b0d4-494c-90d0-6da850a848b4"), "group1", FINISHED));
                    saveCalculation(calculation(uuid("1fcdea4b-194b-4a36-a7a3-d567af92b661"), "group2", LOST));
                    saveCalculation(calculation(uuid("ccaeccd9-36f5-40de-ac40-c1861da0ef6c"), "group2", FINISHED));
                    saveCalculation(calculation(uuid("ba342915-564b-46b2-8e27-d63a4a9c33a8"), "group3", FINISHED));
                    saveCalculation(calculation(uuid("321bb427-b233-469d-8e21-a4e6d9622f51"), "group4", FINALIZING));

                    RepositoryHelpers.prepare(embeddedDatabase, () -> {
                        final long actual = objectUnderTest.countByExecutionGroupsAndStates(executionGroups, kpiCalculationStates);
                        Assertions.assertThat(actual).isEqualTo(expected);
                    });
                }

                Stream<Arguments> provideCountByExecutionGroupsAndStatesData() {
                    return Stream.of(
                            Arguments.of(executionGroups("unknownExecutionGroup"), states(), 0),
                            Arguments.of(executionGroups("group1"), states(), 0),
                            Arguments.of(executionGroups("group1"), states(FINISHED), 1),
                            Arguments.of(executionGroups("group1", "group2"), states(FINISHED), 2),
                            Arguments.of(executionGroups("group3"), states(LOST), 0)
                    );
                }

                List<KpiCalculationState> states(final KpiCalculationState... states) {
                    return List.of(states);
                }

                List<String> executionGroups(final String... groups) {
                    return List.of(groups);
                }
            }

            @Nested
            @TestInstance(Lifecycle.PER_CLASS)
            class FindCalculationByTimeCreatedIsAfter {
                @MethodSource("provideFindCalculationsByTimeCreatedIsAfterData")
                @ParameterizedTest(name = "[{index}] With test data ''{0}'' and target ''{1}'' ==> ''{2}''")
                void shouldFindCalculationsByTimeCreatedIsAfter(final List<? extends Calculation> testData,
                                                                final LocalDateTime target,
                                                                final List<? extends Calculation> expected) {
                    saveAll(testData);

                    RepositoryHelpers.prepare(embeddedDatabase, () -> {
                        final List<Calculation> actual = objectUnderTest.findCalculationsByTimeCreatedIsAfter(target);

                        Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
                    });
                }

                Stream<Arguments> provideFindCalculationsByTimeCreatedIsAfterData() {
                    return Stream.of(
                            Arguments.of(
                                    testData(
                                            calculation(uuid("d02f817e-24e6-4737-8621-d60228f59a71"), targetTime(12, 0), CollectionIdProxy.COLLECTION_ID),
                                            calculation(uuid("2256452c-5508-4d4f-9623-a7c95c0ae632"), targetTime(12, 1), CollectionIdProxy.COLLECTION_ID),
                                            calculation(uuid("35b833f5-aebd-4c5f-90f3-00ccb24dd54d"), targetTime(12, 2), CollectionIdProxy.COLLECTION_ID)
                                    ),
                                    targetTime(12, 1),
                                    expected(
                                            calculation(uuid("2256452c-5508-4d4f-9623-a7c95c0ae632"), targetTime(12, 1), CollectionIdProxy.COLLECTION_ID),
                                            calculation(uuid("35b833f5-aebd-4c5f-90f3-00ccb24dd54d"), targetTime(12, 2), CollectionIdProxy.COLLECTION_ID)
                                    )
                            ),
                            Arguments.of(
                                    testData(
                                            calculation(uuid("d02f817e-24e6-4737-8621-d60228f59a71"), targetTime(12, 0), CollectionIdProxy.COLLECTION_ID),
                                            calculation(uuid("2256452c-5508-4d4f-9623-a7c95c0ae632"), targetTime(12, 1), CollectionIdProxy.COLLECTION_ID),
                                            calculation(uuid("35b833f5-aebd-4c5f-90f3-00ccb24dd54d"), targetTime(12, 2), CollectionIdProxy.COLLECTION_ID)
                                    ),
                                    targetTime(12, 3),
                                    expected()
                            )
                    );
                }

                LocalDateTime targetTime(final int hour, final int minute) {
                    return LocalDateTime.of(2_022, Month.OCTOBER, 27, hour, minute);
                }

                List<Calculation> testData(final Calculation... calculations) {
                    return List.of(calculations);
                }

                List<Calculation> expected(final Calculation... calculations) {
                    return List.of(calculations);
                }
            }

            @Nested
            @TestInstance(Lifecycle.PER_CLASS)
            class GetLastComplexCompletedTime {

                final LocalDateTime zeroTime = LocalDateTime.of(1970, Month.JANUARY, 1, 0, 0);

                @MethodSource("provideGetLastComplexCompletedTimeData")
                @ParameterizedTest(name = "[{index}] With test data ''{0}'', reliabilities ''{1}'' and target ''{2}'' ==> ''{3}''")
                void shouldGetLastComplexCompletedTime(final List<? extends Calculation> testData,
                                                       final List<CalculationReliability> reliabilities,
                                                       final String target,
                                                       final LocalDateTime expected) {
                    saveAll(testData);
                    saveReliabilities(reliabilities);

                    RepositoryHelpers.prepare(embeddedDatabase, () -> {
                        final LocalDateTime actual = objectUnderTest.getLastComplexCalculationReliability(target);

                        Assertions.assertThat(actual).isEqualTo(expected);
                    });
                }

                Stream<Arguments> provideGetLastComplexCompletedTimeData() {
                    return Stream.of(
                            Arguments.of(
                                    testData(
                                            calculation(uuid("d02f817e-24e6-4737-8621-d60228f59a71"), "COMPLEX", FINISHED, targetTime(11, 00), targetTime(11, 20)),
                                            calculation(uuid("2256452c-5508-4d4f-9623-a7c95c0ae632"), "COMPLEX", FINISHED, targetTime(11, 30), targetTime(11, 50)),
                                            calculation(uuid("35b833f5-aebd-4c5f-90f3-00ccb24dd54d"), "COMPLEX", FAILED, targetTime(12, 00), targetTime(12, 20))
                                    ),
                                    List.of(
                                            calculationReliability(uuid("d02f817e-24e6-4737-8621-d60228f59a71"), targetTime(10, 00), targetTime(11, 15), 1),
                                            calculationReliability(uuid("2256452c-5508-4d4f-9623-a7c95c0ae632"), targetTime(9, 00), targetTime(11, 45), 2)

                                    ),
                                    "COMPLEX",
                                    targetTime(11, 45)
                            ),
                            Arguments.of(
                                    testData(
                                            calculation(uuid("d02f817e-24e6-4737-8621-d60228f59a71"), "COMPLEX", FINISHED, targetTime(11, 00), targetTime(11, 20)),
                                            calculation(uuid("2256452c-5508-4d4f-9623-a7c95c0ae632"), "COMPLEX", FINISHED, targetTime(11, 30), targetTime(11, 50)),
                                            calculation(uuid("35b833f5-aebd-4c5f-90f3-00ccb24dd54d"), "COMPLEX", FAILED, targetTime(12, 00), targetTime(12, 20))
                                    ),
                                    Collections.emptyList(),
                                    "COMPLEX_NOT_PRESENT",
                                    zeroTime
                            )
                    );
                }

                LocalDateTime targetTime(final int hour, final int minute) {
                    return LocalDateTime.of(2_022, Month.NOVEMBER, 20, hour, minute);
                }

                List<Calculation> testData(final Calculation... calculations) {
                    return List.of(calculations);
                }
            }

            @Test
            void shouldDoNothing_whenUpdateTimeCompletedAndStateByCalculationId_andNotFoundByCalculationId() throws Exception {
                final LocalDateTime timeCompleted = TEST_TIME.plusMinutes(5);
                final KpiCalculationState kpiCalculationState = FINISHED;

                objectUnderTest.updateTimeCompletedAndStateByCalculationId(embeddedDatabase.getConnection(),
                        timeCompleted,
                        kpiCalculationState,
                        UUID.fromString("4f9536e6-ada2-40e7-afb8-dfe254feb9dc"));

                final List<Calculation> actual = findAll();

                Assertions.assertThat(actual).satisfiesExactly(calculationSaved -> {
                    Assertions.assertThat(calculationSaved.getTimeCompleted()).isEqualTo(calculation.getTimeCompleted());
                    Assertions.assertThat(calculationSaved.getKpiCalculationState()).isEqualTo(calculationSaved.getKpiCalculationState());
                });
            }

            @Test
            void shouldDoNothing_whenUpdateStateByCalculationId_andNotFoundByCalculationId() throws Exception {
                final KpiCalculationState kpiCalculationState = FINISHED;

                objectUnderTest.updateStateByCalculationId(embeddedDatabase.getConnection(),
                        kpiCalculationState,
                        UUID.fromString("4f9536e6-ada2-40e7-afb8-dfe254feb9dc"));

                final List<Calculation> actual = findAll();

                Assertions.assertThat(actual).satisfiesExactly(calculationSaved -> {
                    Assertions.assertThat(calculationSaved.getKpiCalculationState()).isEqualTo(calculationSaved.getKpiCalculationState());
                });
            }

            @MethodSource("provideDeleteByTimeCreatedLessThenData")
            @ParameterizedTest(name = "[{index}] With created_time minute offset: ''{0}'' rows deleted: ''{1}''")
            void shouldDeleteByTimeCreatedLessThen(final int minuteOffset, final int expected) throws Exception {
                final LocalDateTime localDateTime = TEST_TIME.plusMinutes(minuteOffset);
                final long actual = objectUnderTest.deleteByTimeCreatedLessThen(embeddedDatabase.getConnection(), localDateTime);

                Assertions.assertThat(actual).isEqualTo(expected);
            }

            private Stream<Arguments> provideFindByCalculationIdData() {
                return Stream.of(Arguments.of(UUID_1, Optional.of(STARTED)),
                        Arguments.of(UUID.fromString("144cffb1-2e7e-434c-b2ad-8edf0f2224ef"), Optional.empty()));
            }

            private Stream<Arguments> provideCountByExecutionGroupAndStatesData() {
                return Stream.of(Arguments.of("unknownExecutionGroup", Collections.emptyList(), 0),
                        Arguments.of("execution_group1", Collections.emptyList(), 0),
                        Arguments.of("execution_group1", Collections.singletonList(STARTED), 1),
                        Arguments.of("execution_group1", Arrays.asList(STARTED, FINISHED, LOST), 2));
            }

            private Stream<Arguments> provideCountByStatesData() {
                return Stream.of(
                        Arguments.of(List.of(), new EnumMap<>(Map.of(STARTED, 1L, FINISHED, 1L, LOST, 1L))),
                        Arguments.of(List.of(FINISHED), new EnumMap<>(Map.of(FINISHED, 1L)))
                );
            }

            private Stream<Arguments> provideDeleteByTimeCreatedLessThenData() {
                return Stream.of(Arguments.of(0, 0),
                        Arguments.of(-1, 0),
                        Arguments.of(1, 1));
            }
        }

        @Nested
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("and database is not pre-populated")
        class AndDatabaseIsNotPopulated {
            @MethodSource("provideSaveData")
            @ParameterizedTest(name = "[{index}] Save calculation: ''{0}''")
            void shouldSaveCalculation(final Calculation calculationToSave) throws Exception {
                saveCalculation(calculationToSave);

                final List<Calculation> actual = findAll();

                Assertions.assertThat(actual).satisfiesExactly(calculationSaved -> {
                    Assertions.assertThat(calculationSaved).isEqualTo(calculationToSave);
                });
            }

            @Test
            void shouldNotFindKpiTypeByCalculationId() {
                RepositoryHelpers.prepare(embeddedDatabase, () -> {
                    final Optional<KpiType> actual = objectUnderTest.findKpiTypeByCalculationId(UUID_1);

                    Assertions.assertThat(actual).isEmpty();
                });
            }

            @Nested
            class GetLastComplexCompletedTimeBeforeFirstComplexCalculation {

                final LocalDateTime zeroTime = LocalDateTime.of(1970, Month.JANUARY, 1, 0, 0);

                @Test
                void shouldGetZeroTime() {
                    RepositoryHelpers.prepare(embeddedDatabase, () -> {
                        final LocalDateTime actual = objectUnderTest.getLastComplexCalculationReliability("COMPLEX");

                        Assertions.assertThat(actual).isEqualTo(zeroTime);
                    });
                }
            }

            @Nested
            class UpdateStateByStateAndTimeCreated {
                @Test
                void shouldUpdateStateByStateAndExecutionGroupAndTimeCreated() throws SQLException {
                    final LocalDateTime now = LocalDateTime.now();
                    saveCalculation(calculation(uuid("eee9bcf3-c140-4b59-9007-2b697433da1e"), "ON_DEMAND", STARTED, now.minusMinutes(61)));
                    saveCalculation(calculation(uuid("b734e356-628c-4a09-af05-c2b2c89bec02"), "ON_DEMAND", FINISHED, now.minusMinutes(61)));
                    saveCalculation(calculation(uuid("5addfa62-d61d-4213-8097-29c32d2bfb85"), "ON_DEMAND", STARTED, now.minusMinutes(59)));
                    saveCalculation(calculation(uuid("587a09b0-54e2-424e-80e0-d3715d448e9b"), "group1", FINISHED, now.minusMinutes(61)));
                    saveCalculation(calculation(uuid("a78d24aa-aa5f-4f2c-b327-451813596f63"), "group2", FINISHED, now.minusMinutes(59)));
                    saveCalculation(calculation(uuid("ebbaee83-8963-467e-96d0-618794b060cf"), "group3", STARTED, now.minusMinutes(61)));
                    saveCalculation(calculation(uuid("f362966d-1d4b-4ab4-9e8d-76ebe4b152cf"), "group4", STARTED, now.minusMinutes(59)));

                    final List<UUID> actual = objectUnderTest.updateStateByStateAndTimeCreated(embeddedDatabase.getConnection(), LOST, STARTED, now.minusHours(1));

                    Assertions.assertThat(actual).containsExactly(uuid("ebbaee83-8963-467e-96d0-618794b060cf"));
                }
            }

            @MethodSource("provideUpdateStateByStatesData")
            @ParameterizedTest(name = "[{index}] Calculation state: ''{0}'' updated to: ''{1}''")
            void shouldUpdateStateByStates(final KpiCalculationState kpiCalculationState, final KpiCalculationState expected) throws Exception {
                saveCalculation(calculation(kpiCalculationState));

                objectUnderTest.updateStateByStates(embeddedDatabase.getConnection(),
                        LOST,
                        Arrays.asList(STARTED, IN_PROGRESS));

                final List<Calculation> actual = findAll();

                Assertions.assertThat(actual).satisfiesExactly(savedCalculation -> {
                    Assertions.assertThat(savedCalculation.getKpiCalculationState()).isEqualTo(expected);
                });
            }

            private Stream<Arguments> provideUpdateStateByStatesData() {
                return Stream.of(Arguments.of(STARTED, LOST),
                        Arguments.of(IN_PROGRESS, LOST),
                        Arguments.of(LOST, LOST),
                        Arguments.of(FINISHED, FINISHED),
                        Arguments.of(FAILED, FAILED));
            }

            private Stream<Arguments> provideSaveData() {
                return Stream.of(Arguments.of(Calculation.builder()
                                .withCalculationId(UUID_1)
                                .withExecutionGroup("execution_group")
                                .withTimeCreated(TEST_TIME)
                                .withKpiCalculationState(STARTED)
                                .withParameters("veryLongParameter")
                                .withKpiType(KpiType.SCHEDULED_SIMPLE)
                                .build()),
                        Arguments.of(Calculation.builder()
                                .withCalculationId(UUID_2)
                                .withExecutionGroup("execution_group")
                                .withTimeCreated(TEST_TIME)
                                .withTimeCompleted(TEST_TIME.plusMinutes(5))
                                .withKpiCalculationState(FINISHED)
                                .withKpiType(KpiType.SCHEDULED_SIMPLE)
                                .build()));
            }
        }

        public List<Calculation> findAll() throws SQLException {
            final String sql =
                    "SELECT calculation_id, time_created, time_completed, state, execution_group, parameters, kpi_type " +
                            "FROM kpi_service_db.kpi.kpi_calculation ";
            try (final Connection connection = embeddedDatabase.getConnection();
                 final Statement statement = connection.createStatement();
                 final ResultSet resultSet = statement.executeQuery(sql)) {
                final List<Calculation> result = new ArrayList<>();
                while (resultSet.next()) {
                    result.add(Calculation.builder()
                            .withCalculationId(resultSet.getObject("calculation_id", UUID.class))
                            .withTimeCreated(resultSet.getTimestamp("time_created").toLocalDateTime())
                            .withTimeCompleted(Optional.ofNullable(resultSet.getTimestamp("time_completed"))
                                    .map(Timestamp::toLocalDateTime)
                                    .orElse(null))
                            .withKpiCalculationState(valueOf(resultSet.getString("state")))
                            .withExecutionGroup(resultSet.getString("execution_group"))
                            .withParameters(resultSet.getString("parameters"))
                            .withKpiType(KpiType.valueOf(resultSet.getString("kpi_type")))
                            .build());
                }
                return result;
            }
        }


        @SneakyThrows
        void saveCalculation(final Calculation calculation1) {
            objectUnderTest.save(embeddedDatabase.getConnection(), calculation1);
        }

        @SneakyThrows
        void saveAll(@NonNull final Iterable<? extends Calculation> calculations) {
            for (final Calculation calculation : calculations) {
                objectUnderTest.save(embeddedDatabase.getConnection(), calculation);
            }
        }

        @SneakyThrows
        void saveReliabilities(@NonNull final List<CalculationReliability> reliabilities) {
            CalculationReliabilityRepository calculationReliabilityRepository = new CalculationReliabilityRepositoryImpl();
            calculationReliabilityRepository.save(embeddedDatabase.getConnection(), reliabilities);
        }

        UUID uuid(final String uuid) {
            return UUID.fromString(uuid);
        }

        Calculation calculation(final KpiCalculationState state) {
            return calculation.toBuilder().withKpiCalculationState(state).build();
        }

        Calculation calculation(final UUID id, final String executionGroup, final KpiCalculationState state) {
            return calculation.toBuilder().withCalculationId(id).withExecutionGroup(executionGroup).withKpiCalculationState(state).build();
        }

        Calculation calculation(final UUID id, final LocalDateTime timeCreated, final UUID collId) {
            return motherCalculation().toBuilder().withCalculationId(id).withTimeCreated(timeCreated).withCollectionId(CollectionIdProxy.COLLECTION_ID).build();
        }

        Calculation calculation(final UUID id, final String executionGroup, final KpiCalculationState state, final LocalDateTime timeCreated) {
            return calculation(id, executionGroup, state).toBuilder().withTimeCreated(timeCreated).build();
        }

        Calculation calculation(final UUID id, final String executionGroup, final KpiCalculationState state, final LocalDateTime timeCreated, final LocalDateTime timeCompleted) {
            return calculation(id, executionGroup, state, timeCreated).toBuilder().withTimeCompleted(timeCompleted).build();
        }

        CalculationReliability calculationReliability(final UUID id, final LocalDateTime calculationStartTime, final LocalDateTime reliabilityThreshold, final int kpiId) {
            return CalculationReliability.builder()
                    .withKpiCalculationId(id)
                    .withCalculationStartTime(calculationStartTime)
                    .withReliabilityThreshold(reliabilityThreshold)
                    .withKpiDefinitionId(kpiId)
                    .build();
        }

        private Calculation motherCalculation() {
            return Calculation.builder()
                    .withCalculationId(UUID_1)
                    .withExecutionGroup("execution_group")
                    .withTimeCreated(TEST_TIME)
                    .withKpiCalculationState(STARTED)
                    .withKpiType(KpiType.SCHEDULED_SIMPLE)
                    .build();
        }
    }
}