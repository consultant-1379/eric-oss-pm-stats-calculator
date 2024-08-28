/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.offset.bucket;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.bucket.distribute.api.BucketDistributor;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.Partition;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.TopicPartitionOffsetDistance;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.TopicPartitionOffsetDistance.OffsetDistance;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OffsetBucketCalculator {
    private final BucketDistributor bucketDistributor;

    public Map<Partition, OffsetDistance> calculateNextBucket(@NonNull final List<TopicPartitionOffsetDistance> topicPartitionOffsetDistances) {
        return bucketDistributor.distribute(topicPartitionOffsetDistances.stream().collect(Collectors.toMap(
                        TopicPartitionOffsetDistance::partition,
                        TopicPartitionOffsetDistance::offsetDistance
        )));
    }
}
