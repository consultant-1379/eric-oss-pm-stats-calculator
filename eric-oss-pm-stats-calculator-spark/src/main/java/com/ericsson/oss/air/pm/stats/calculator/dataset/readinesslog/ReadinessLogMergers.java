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

import com.ericsson.oss.air.pm.stats.calculator.dataset.readinesslog.exception.ReadinessLogMergeException;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.ReadinessLog;
import com.ericsson.oss.air.pm.stats.calculator.util.LocalDateTimes;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReadinessLogMergers {

    public static ReadinessLog merge(@NonNull final ReadinessLog left, @NonNull final ReadinessLog right){
        if (left.hasDifferentDatasource(right)) {
            throw new ReadinessLogMergeException(String.format(
                    "Readiness logs with datasource (%s, %s) can not be merged.",
                    left.getDatasource(),
                    right.getDatasource()
            ));
        }

        if (left.hasDifferentCalculationId(right)) {
            throw new ReadinessLogMergeException(String.format(
                    "Readiness logs with calculation id (%s, %s) can not be merged.",
                    left.getKpiCalculationId().getId(),
                    right.getKpiCalculationId().getId()
            ));
        }

        final ReadinessLog result = new ReadinessLog();
        result.setDatasource(left.getDatasource());
        result.setKpiCalculationId(left.getKpiCalculationId());
        result.setCollectedRowsCount(Math.addExact(left.getCollectedRowsCount(), right.getCollectedRowsCount()));
        result.setEarliestCollectedData(LocalDateTimes.min(left.getEarliestCollectedData(), right.getEarliestCollectedData()));
        result.setLatestCollectedData(LocalDateTimes.max(left.getLatestCollectedData(), right.getLatestCollectedData()));

        return result;
    }
}
