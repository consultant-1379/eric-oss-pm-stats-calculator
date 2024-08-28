/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.readiness.utils;

import java.util.List;

import com.ericsson.oss.air.pm.stats.calculation.readiness.model.ReadinessWindow;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReadinessLogMergers {

    public static ReadinessWindow merge(@NonNull final List<? extends ReadinessLog> readinessLogs) {
        return readinessLogs.stream()
                .map(ReadinessWindow::of)
                .reduce(ReadinessWindow::mergeWindows)
                .orElseThrow(() -> new IllegalArgumentException("readinessLogs is empty"));
    }
}
