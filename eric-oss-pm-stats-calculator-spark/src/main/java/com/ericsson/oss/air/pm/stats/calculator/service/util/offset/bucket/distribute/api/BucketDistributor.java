/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.offset.bucket.distribute.api;

import java.util.Map;

import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.Partition;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.TopicPartitionOffsetDistance.OffsetDistance;

public interface BucketDistributor {
    Map<Partition, OffsetDistance> distribute(Map<Partition, OffsetDistance> partitionOffsetDistance);
}
