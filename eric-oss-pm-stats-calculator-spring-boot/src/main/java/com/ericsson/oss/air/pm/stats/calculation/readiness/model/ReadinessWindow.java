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

import static com.ericsson.oss.air.pm.stats.utils.LocalDateTimes.max;
import static com.ericsson.oss.air.pm.stats.utils.LocalDateTimes.min;

import java.time.LocalDateTime;

import com.ericsson.oss.air.pm.stats.calculation.readiness.model.domain.DataSource;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;

import com.google.common.base.Preconditions;
import lombok.Data;
import lombok.NonNull;

@Data(staticConstructor = "of")
public final class ReadinessWindow {
    @NonNull
    private final DataSource dataSource;
    @NonNull
    private final LocalDateTime earliestCollectedData;
    @NonNull
    private final LocalDateTime latestCollectedData;

    public static ReadinessWindow of(@NonNull final ReadinessLog readinessLog) {
        return of(
                DataSource.of(readinessLog.getDatasource()),
                readinessLog.getEarliestCollectedData(),
                readinessLog.getLatestCollectedData()
        );
    }

    public static ReadinessWindow mergeWindows(@NonNull final ReadinessWindow left, @NonNull final ReadinessWindow right) {
        Preconditions.checkArgument(
                left.getDataSource().equals(right.getDataSource()),
                String.format(
                        "different data sources for window '%s' <--> '%s",
                        left.getDataSource().source(),
                        right.getDataSource().source()
                )
        );

        return of(
                left.getDataSource(),
                min(left.getEarliestCollectedData(), right.getEarliestCollectedData()),
                max(left.getLatestCollectedData(), right.getLatestCollectedData())
        );
    }
}
