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

import static java.util.Collections.min;

import java.time.LocalDateTime;
import java.util.List;

import com.ericsson.oss.air.pm.stats.calculation.readiness.model.ReadinessWindow;
import com.ericsson.oss.air.pm.stats.service.util.CollectionHelpers;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Builder
@Accessors(fluent = true)
public class ReadinessBound {

    private final LocalDateTime upperReadinessBound;
    private final LocalDateTime lowerReadinessBound;

    public static ReadinessBound fromWindows(final int dataReliabilityOffset, final List<ReadinessWindow> readinessWindows) {
        return builder().lowerReadinessBound(findLowerReadinessBound(readinessWindows))
                .upperReadinessBound(findUpperReadinessBound(readinessWindows).minusMinutes(dataReliabilityOffset))
                .build();
    }

    private static LocalDateTime findUpperReadinessBound(final List<ReadinessWindow> readinessWindows) {
        return min(CollectionHelpers.transform(readinessWindows, ReadinessWindow::getLatestCollectedData));
    }

    private static LocalDateTime findLowerReadinessBound(final List<ReadinessWindow> readinessWindows) {
        return min(CollectionHelpers.transform(readinessWindows, ReadinessWindow::getEarliestCollectedData));
    }
}
