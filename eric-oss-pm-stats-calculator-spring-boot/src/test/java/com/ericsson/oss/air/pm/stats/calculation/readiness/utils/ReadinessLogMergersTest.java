/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.readiness.utils;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.List;

import com.ericsson.oss.air.pm.stats.calculation.readiness.model.ReadinessWindow;
import com.ericsson.oss.air.pm.stats.calculation.readiness.model.domain.DataSource;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ReadinessLogMergersTest {
    static final LocalDateTime TEST_TIME = LocalDateTime.of(2_022, Month.OCTOBER, 8, 12, 0);

    @Test
    void shouldThrowException_whenReadinessLogsEmpty_onMerge() {
        Assertions.assertThatThrownBy(() -> ReadinessLogMergers.merge(Collections.emptyList()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("readinessLogs is empty");
    }

    @Test
    void shouldMergeReadinessLogs() {
        final ReadinessWindow actual = ReadinessLogMergers.merge(List.of(
                readinessLog(TEST_TIME, TEST_TIME.plusMinutes(15), "datasource"),
                readinessLog(TEST_TIME.plusMinutes(5), TEST_TIME.plusMinutes(10), "datasource"),
                readinessLog(TEST_TIME.plusMinutes(10), TEST_TIME.plusMinutes(5), "datasource"),
                readinessLog(TEST_TIME.plusMinutes(15), TEST_TIME, "datasource")
        ));

        Assertions.assertThat(actual.getDataSource()).isEqualTo(DataSource.of("datasource"));
        Assertions.assertThat(actual.getEarliestCollectedData()).isEqualTo(TEST_TIME);
        Assertions.assertThat(actual.getLatestCollectedData()).isEqualTo(TEST_TIME.plusMinutes(15));
    }

    static ReadinessLog readinessLog(final LocalDateTime earliestCollectedData, final LocalDateTime latestCollectedData, final String datasource) {
        return ReadinessLog.builder()
                .withDatasource(datasource)
                .withEarliestCollectedData(earliestCollectedData)
                .withLatestCollectedData(latestCollectedData)
                .build();
    }
}