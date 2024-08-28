/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.spark;

import static java.util.Optional.ofNullable;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class ReadingOptions {
    private static final String AGGREGATION_BEGIN_TIME_LIMIT = "aggregationBeginTimeLimit";

    private final Map<String, String> properties = new LinkedHashMap<>();

    public static ReadingOptions empty() {
        return new ReadingOptions();
    }

    public void setAggregationBeginTimeLimit(final LocalDateTime value) {
        properties.merge(AGGREGATION_BEGIN_TIME_LIMIT, value.toString(),(oldLimit, newLimit) -> {
            final LocalDateTime parseOldLimit = LocalDateTime.parse(oldLimit);
            final LocalDateTime parseNewLimit = LocalDateTime.parse(newLimit);
            return min(parseOldLimit, parseNewLimit).toString();
        });
    }

    public Optional<LocalDateTime> getAggregationBeginTimeLimit() {
        return ofNullable(properties.get(AGGREGATION_BEGIN_TIME_LIMIT)).map(LocalDateTime::parse);
    }

    private static <T extends Comparable<? super T>> T min(final T a, final T b) {
        return a.compareTo(b) <= 0 ? a : b;
    }
}
