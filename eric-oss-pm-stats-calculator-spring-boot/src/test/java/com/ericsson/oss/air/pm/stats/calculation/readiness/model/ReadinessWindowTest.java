/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.readiness.model;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculation.readiness.model.domain.DataSource;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ReadinessWindowTest {
    private static final String DATASOURCE = "datasource";
    private static final LocalDateTime TEST_TIME = LocalDateTime.of(2_022, Month.OCTOBER, 8, 12, 0);

    @Nested
    class CreateInstance {
        @Test
        void fromReadinessLog() {
            final ReadinessWindow actual = readinessWindow(TEST_TIME, TEST_TIME.plusMinutes(5));

            Assertions.assertThat(actual.getDataSource()).isEqualTo(DataSource.of(DATASOURCE));
            Assertions.assertThat(actual.getEarliestCollectedData()).isEqualTo(TEST_TIME);
            Assertions.assertThat(actual.getLatestCollectedData()).isEqualTo(TEST_TIME.plusMinutes(5));
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class MergeWindows {
        @Test
        void shouldRaiseException_whenDataSourcesAreNotEqual() {
            final ThrowingCallable throwingCallable = () -> ReadinessWindow.mergeWindows(
                    ReadinessWindow.of(readinessLog(TEST_TIME, TEST_TIME, "datasource1")),
                    ReadinessWindow.of(readinessLog(TEST_TIME, TEST_TIME, "datasource2"))
            );

            Assertions.assertThatThrownBy(throwingCallable)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("different data sources for window '%s' <--> '%s", "datasource1", "datasource2");
        }

        @MethodSource("provideMergeWindowsData")
        @ParameterizedTest(name = "[{index}] merge(''{0}'', ''{1}'') ==> ''{2}''")
        void shouldMergeWindows(final ReadinessWindow left, final ReadinessWindow right, final ReadinessWindow expected) {
            final ReadinessWindow actual = ReadinessWindow.mergeWindows(left, right);

            Assertions.assertThat(actual).isEqualTo(expected);
        }

        Stream<Arguments> provideMergeWindowsData() {
            return Stream.of(
                    Arguments.of(
                            left(TEST_TIME, TEST_TIME.plusMinutes(5)),
                            right(TEST_TIME, TEST_TIME.plusMinutes(5)),
                            expected(TEST_TIME, TEST_TIME.plusMinutes(5))
                    ),
                    Arguments.of(
                            left(TEST_TIME.minusMinutes(5), TEST_TIME.plusMinutes(5)),
                            right(TEST_TIME, TEST_TIME.plusMinutes(5)),
                            expected(TEST_TIME.minusMinutes(5), TEST_TIME.plusMinutes(5))
                    ),
                    Arguments.of(
                            left(TEST_TIME, TEST_TIME),
                            right(TEST_TIME, TEST_TIME.plusMinutes(5)),
                            expected(TEST_TIME, TEST_TIME.plusMinutes(5))
                    ),
                    Arguments.of(
                            left(TEST_TIME.plusMinutes(5), TEST_TIME.plusMinutes(5)),
                            right(TEST_TIME, TEST_TIME),
                            expected(TEST_TIME, TEST_TIME.plusMinutes(5))
                    )
            );
        }

        ReadinessWindow left(final LocalDateTime earliestCollectedData, final LocalDateTime latestCollectedData) {
            return readinessWindow(earliestCollectedData, latestCollectedData);
        }

        ReadinessWindow right(final LocalDateTime earliestCollectedData, final LocalDateTime latestCollectedData) {
            return readinessWindow(earliestCollectedData, latestCollectedData);
        }

        ReadinessWindow expected(final LocalDateTime earliestCollectedData, final LocalDateTime latestCollectedData) {
            return readinessWindow(earliestCollectedData, latestCollectedData);
        }
    }

    static ReadinessWindow readinessWindow(final LocalDateTime earliestCollectedData, final LocalDateTime latestCollectedData) {
        return ReadinessWindow.of(readinessLog(earliestCollectedData, latestCollectedData, DATASOURCE));
    }

    static ReadinessLog readinessLog(final LocalDateTime earliestCollectedData, final LocalDateTime latestCollectedData, final String datasource) {
        return ReadinessLog.builder()
                .withDatasource(datasource)
                .withEarliestCollectedData(earliestCollectedData)
                .withLatestCollectedData(latestCollectedData)
                .build();
    }
}