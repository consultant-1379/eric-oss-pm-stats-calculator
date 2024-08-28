/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.limits.model;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import com.ericsson.oss.air.pm.stats.calculation.readiness.model.ReadinessWindow;
import com.ericsson.oss.air.pm.stats.calculation.readiness.model.domain.DataSource;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ReadinessBoundTest {
    @Nested
    class VerifyLowerReadinessBound {
        final LocalDateTime testTime = LocalDateTime.of(2_022, Month.OCTOBER, 8, 12, 0);

        @Test
        void shouldFindMinimumLowerReadinessBound() {
            final ReadinessBound readinessBound = ReadinessBound.fromWindows(10, List.of(
                    readinessWindow(testTime, testTime.plusMinutes(5)),
                    readinessWindow(testTime.minusMinutes(5), testTime.plusMinutes(5))
            ));

            Assertions.assertThat(readinessBound.lowerReadinessBound()).isEqualTo(testTime.minusMinutes(5));
        }
    }

    @Nested
    class VerifyUpperReadinessBound {
        final LocalDateTime testTime = LocalDateTime.of(2_022, Month.OCTOBER, 8, 12, 0);

        @Test
        void shouldFindMinimumUpperReadinessBound() {
            final ReadinessBound readinessBound = ReadinessBound.fromWindows(15, List.of(
                    readinessWindow(testTime, testTime.plusMinutes(5)),
                    readinessWindow(testTime, testTime.plusMinutes(10))
            ));

            Assertions.assertThat(readinessBound.upperReadinessBound()).isEqualTo(testTime.minusMinutes(10));
        }
    }

    static ReadinessWindow readinessWindow(final LocalDateTime earliestCollectedData, final LocalDateTime latestCollectedData) {
        return ReadinessWindow.of(DataSource.of("datasource"), earliestCollectedData, latestCollectedData);
    }
}