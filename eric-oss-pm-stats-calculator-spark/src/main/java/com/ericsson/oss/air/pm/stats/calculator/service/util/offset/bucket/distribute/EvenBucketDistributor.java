/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.offset.bucket.distribute;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.ericsson.oss.air.pm.stats.calculator.configuration.property.CalculationProperties;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.bucket.distribute.api.BucketDistributor;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.Partition;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.TopicPartitionOffsetDistance.OffsetDistance;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EvenBucketDistributor implements BucketDistributor {
    private final CalculationProperties calculationProperties;

    @Override
    public Map<Partition, OffsetDistance> distribute(@NonNull final Map<Partition, OffsetDistance> partitionOffsetDistance) {
        long remainingBucketSize = calculationProperties.getKafkaBucketSize();
        long remainingDistance = remainingDistance(partitionOffsetDistance.values());

        Preconditions.checkArgument(
                remainingBucketSize <= remainingDistance, /* In this case we could use "latest" */
                "remainingBucketSize<'%s'> is greater than remaining distance<'%s'>",
                remainingBucketSize,
                remainingDistance
        );

        final Map<Partition, OffsetDistance> result = Maps.newHashMapWithExpectedSize(partitionOffsetDistance.size());

        for (final Entry<Partition, OffsetDistance> entry : partitionOffsetDistance.entrySet()) {
            final Partition partition = entry.getKey();
            final OffsetDistance offsetDistance = entry.getValue();

            final long currentDistance = offsetDistance.distance();
            final long coveredDistance = Math.round((double) Math.multiplyExact(remainingBucketSize, currentDistance) / remainingDistance);

            result.put(partition, offsetDistance.withCoveredDistance(coveredDistance));

            remainingBucketSize -= coveredDistance;
            remainingDistance -= currentDistance;
        }

        return result;
    }

    private static long remainingDistance(@NonNull final Collection<OffsetDistance> offsetDistances) {
        return offsetDistances.stream().map(OffsetDistance::distance).mapToLong(Long::longValue).sum();
    }
}
