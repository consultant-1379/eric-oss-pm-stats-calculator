/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.readinesslog;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collection;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.Calculation;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.ReadinessLog;

import org.junit.jupiter.api.Test;

class ReadinessLogCacheTest {
    ReadinessLogCache objectUnderTest = new ReadinessLogCache();

    @Test
    void shouldFetchReadinessLogs() {
        final Collection<ReadinessLog> actual = objectUnderTest.fetchAlLReadinessLogs();
        assertThat(actual).isUnmodifiable().isEmpty();
    }

    @Test
    void shouldVerifyPut() {
        final ReadinessLog readinessLog1 = readinessLog("datasource1",uuid("02a4d4c1-8a13-4793-903a-fc43451a3fad"));
        final ReadinessLog readinessLog2 = readinessLog("datasource2",uuid("91d017da-c161-4561-8986-3ab60ef473e8"));
        final ReadinessLog readinessLog3 = readinessLog("datasource2",uuid("148d62a5-d775-4a95-b135-59215b179f01"));

        objectUnderTest.put(readinessLog1, 60);
        objectUnderTest.put(readinessLog2, 60);
        objectUnderTest.put(readinessLog3, 60); /* Override readinessLog2 */

        final Collection<ReadinessLog> actual = objectUnderTest.fetchAlLReadinessLogs();

        assertThat(actual).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                readinessLog1,
                readinessLog3
        );
    }

    @Test
    void shouldVerifyMerge() {
        final ReadinessLog readinessLog1 = readinessLog("datasource-1", 10, testTime(10), testTime(15));
        final ReadinessLog readinessLog2 = readinessLog("datasource-1", 20, testTime(15), testTime(20));
        final ReadinessLog readinessLog3 = readinessLog("datasource-2", 20, testTime(15), testTime(20));
        final ReadinessLog readinessLog4 = readinessLog("datasource-2", 20, testTime(15), testTime(20));

        objectUnderTest.merge(readinessLog1, 60);
        objectUnderTest.merge(readinessLog2, 60);
        objectUnderTest.merge(readinessLog3, 60);
        objectUnderTest.merge(readinessLog4, 15);

        assertThat(objectUnderTest.fetchAlLReadinessLogs()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                readinessLog("datasource-1", 30, testTime(10), testTime(20)),
                readinessLog("datasource-2", 20, testTime(15), testTime(20))
        );
    }

    ReadinessLog readinessLog(
            final String datasource, final long collectedRows, final LocalDateTime earliestCollectedData, final LocalDateTime latestCollectedData
    ) {
        return ReadinessLog.builder()
                           .datasource(datasource)
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