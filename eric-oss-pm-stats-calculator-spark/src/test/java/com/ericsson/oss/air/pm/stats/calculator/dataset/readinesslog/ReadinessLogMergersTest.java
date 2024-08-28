/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.readinesslog;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.dataset.readinesslog.exception.ReadinessLogMergeException;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.Calculation;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.ReadinessLog;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ReadinessLogMergersTest {
    @Test
    void shouldVerifyReadinessLogsContainOnlyKnownAttributes() {
        // Check that ReadinessLogs have the same amount of attributes, so we won't forget to update the merge function.
        final List<String> expectedAttributes = Arrays.asList(
                "id",
                "collectedRowsCount",
                "datasource",
                "earliestCollectedData",
                "latestCollectedData",
                "kpiCalculationId"
        );

        for (final Field field : ReadinessLog.class.getDeclaredFields()) {
            /* https://stackoverflow.com/questions/42413735/getting-exception-field-jacocodata-exception-while-running-jenkins-build */
            if (!Modifier.isStatic(field.getModifiers()) &&!field.isSynthetic()) {
                assertThat(field.getName()).isIn(expectedAttributes);
            }
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class Merge {
        @Nested
        class Preconditions {
            @Test
            void shouldFailWhenLogsHaveDifferentDatasource() {
                final ReadinessLog left = readinessLog("datasource1", uuid("c5b334ef-30af-4833-acef-27a40aaf8ab8"));
                final ReadinessLog right = readinessLog("datasource2", uuid("c5b334ef-30af-4833-acef-27a40aaf8ab8"));

                Assertions.assertThatThrownBy(() -> ReadinessLogMergers.merge(left, right))
                          .isInstanceOf(ReadinessLogMergeException.class)
                          .hasMessage(String.format(
                                  "Readiness logs with datasource (%s, %s) can not be merged.",
                                  left.getDatasource(),
                                  right.getDatasource()
                          ));
            }

            @Test
            void shouldFailWhenLogsHaveDifferentCalculationId() {
                final ReadinessLog left = readinessLog("datasource", uuid("c5b334ef-30af-4833-acef-27a40aaf8ab8"));
                final ReadinessLog right = readinessLog("datasource", uuid("d823b4d1-4d09-4427-9ff5-0863768980a2"));

                Assertions.assertThatThrownBy(() -> ReadinessLogMergers.merge(left, right))
                          .isInstanceOf(ReadinessLogMergeException.class)
                          .hasMessage(String.format(
                                  "Readiness logs with calculation id (%s, %s) can not be merged.",
                                  left.getKpiCalculationId().getId(),
                                  right.getKpiCalculationId().getId()
                          ));
            }
        }

        @MethodSource("provideMergeData")
        @ParameterizedTest(name = "[{index}] Readiness logs ''{0}'' and ''{1}'' when merged should result in: ''{2}''")
        void shouldVerifyMerge(final ReadinessLog left, final ReadinessLog right, final ReadinessLog expected) {
            final ReadinessLog actual = ReadinessLogMergers.merge(left, right);
            Assertions.assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        }

        Stream<Arguments> provideMergeData() {
            return Stream.of(
                    Arguments.of(
                            readinessLog(10L, testTime(15), testTime(20)),
                            readinessLog(10L, testTime(20), testTime(15)),
                            readinessLog(20L, testTime(15), testTime(20))
                    ),
                    Arguments.of(
                            readinessLog(15L, testTime(20), testTime(15)),
                            readinessLog(15L, testTime(15), testTime(20)),
                            readinessLog(30L, testTime(15), testTime(20))
                    )
            );
        }

        ReadinessLog readinessLog(final long collectedRows, final LocalDateTime earliestCollectedData, final LocalDateTime latestCollectedData) {
            return ReadinessLog.builder()
                               .datasource("datasource")
                               .kpiCalculationId(calculation(uuid("83f96dee-f674-4bb8-9cbf-f50ccddb339c")))
                               .collectedRowsCount(collectedRows)
                               .earliestCollectedData(earliestCollectedData)
                               .latestCollectedData(latestCollectedData)
                               .build();
        }

        ReadinessLog readinessLog(final String datasource, final UUID calculationId) {
            return ReadinessLog.builder()
                               .datasource(datasource)
                               .kpiCalculationId(calculation(calculationId))
                               .collectedRowsCount(10L)
                               .earliestCollectedData(testTime(15))
                               .latestCollectedData(testTime(15))
                               .build();
        }

        LocalDateTime testTime(final int minute) {
            return LocalDateTime.of(2_022, Month.OCTOBER, 29, 12, minute);
        }

        Calculation calculation(final UUID calculationId) {
            return Calculation.builder().id(calculationId).build();
        }

        UUID uuid(final String uuid) {
            return UUID.fromString(uuid);
        }

    }
}