/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.readiness.window;

import static org.assertj.core.api.Assertions.entry;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculation.readiness.model.ReadinessWindow;
import com.ericsson.oss.air.pm.stats.calculation.readiness.model.domain.DataSource;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ReadinessWindowCalculatorTest {
    static final LocalDateTime TEST_TIME = LocalDateTime.of(2_022, Month.OCTOBER, 8, 12, 0);

    ReadinessWindowCalculator objectUnderTest = new ReadinessWindowCalculator();

    @Test
    void shouldCalculateReadinessWindow() {
        final ReadinessLog readinessLog11 = readinessLog(1, TEST_TIME, TEST_TIME.plusMinutes(5), "datasource1");
        final ReadinessLog readinessLog12 = readinessLog(2, TEST_TIME.plusMinutes(10), TEST_TIME.plusMinutes(15), "datasource2");
        final ReadinessLog readinessLog13 = readinessLog(3, TEST_TIME.plusMinutes(15), TEST_TIME.plusMinutes(20), "datasource3");

        final ReadinessLog readinessLog21 = readinessLog(4, TEST_TIME.plusMinutes(5), TEST_TIME.plusMinutes(10), "datasource1");
        final ReadinessLog readinessLog22 = readinessLog(5, TEST_TIME.plusMinutes(5), TEST_TIME.plusMinutes(10), "datasource2");
        final ReadinessLog readinessLog23 = readinessLog(6, TEST_TIME.plusMinutes(10), TEST_TIME.plusMinutes(15), "datasource3");

        final ReadinessLog readinessLog31 = readinessLog(7, TEST_TIME.plusMinutes(15), TEST_TIME.plusMinutes(20), "datasource1");
        final ReadinessLog readinessLog32 = readinessLog(8, TEST_TIME, TEST_TIME.plusMinutes(15), "datasource2");
        final ReadinessLog readinessLog33 = readinessLog(9, TEST_TIME.plusMinutes(5), TEST_TIME.plusMinutes(15), "datasource3");

        final Map<DataSource, ReadinessWindow> actual = objectUnderTest.calculateReadinessWindows(List.of(
                Set.of(readinessLog11, readinessLog12, readinessLog13),
                Set.of(readinessLog21, readinessLog22, readinessLog23),
                Set.of(readinessLog31, readinessLog32, readinessLog33)
        ));

        Assertions.assertThat(actual).containsOnly(
                entry(DataSource.of("datasource1"), ReadinessWindow.of(DataSource.of("datasource1"), TEST_TIME, TEST_TIME.plusMinutes(20))),
                entry(DataSource.of("datasource2"), ReadinessWindow.of(DataSource.of("datasource2"), TEST_TIME, TEST_TIME.plusMinutes(15))),
                entry(DataSource.of("datasource3"), ReadinessWindow.of(DataSource.of("datasource3"), TEST_TIME.plusMinutes(5), TEST_TIME.plusMinutes(20)))
        );
    }

    static ReadinessLog readinessLog(final int id, final LocalDateTime earliestCollectedData, final LocalDateTime latestCollectedData, final String datasource) {
        return ReadinessLog.builder()
                .withId(id)
                .withDatasource(datasource)
                .withEarliestCollectedData(earliestCollectedData)
                .withLatestCollectedData(latestCollectedData)
                .build();
    }
}