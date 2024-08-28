/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.offset.cache.model;

import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.cache.CurrentOffsetCache;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.Topic;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Class representing cache key for {@link CurrentOffsetCache}.
 */
@Accessors(fluent = true)
@Data(staticConstructor = "of")
public final class TopicAggregationPeriod {
    private final Topic topic;
    private final Integer aggregationPeriod;

    public static TopicAggregationPeriod of(final String name, final Integer aggregationPeriod) {
        return of(Topic.of(name), aggregationPeriod);
    }
}
