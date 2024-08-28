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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.bucket.distribute.api.BucketDistributor;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.Partition;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.TopicPartitionOffsetDistance;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.TopicPartitionOffsetDistance.OffsetDistance;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset.EndOffset;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset.StartOffset;

import org.apache.kafka.common.TopicPartition;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OffsetBucketCalculatorTest {
    @Mock BucketDistributor bucketDistributorMock;

    @InjectMocks OffsetBucketCalculator objectUnderTest;

    @Test
    void shouldCalculateNextBucket(@Mock final Map<Partition, OffsetDistance> expectedMock) {
        final TopicPartitionOffsetDistance topicPartitionOffsetDistance = TopicPartitionOffsetDistance.of(topicPartition(0), startOffset(0), endOffset(10));

        final Map<Partition, OffsetDistance> partitionOffsetDistance = Collections.singletonMap(
                topicPartitionOffsetDistance.partition(),
                topicPartitionOffsetDistance.offsetDistance()
        );
        when(bucketDistributorMock.distribute(partitionOffsetDistance)).thenReturn(expectedMock);

        final Map<Partition, OffsetDistance> actual = objectUnderTest.calculateNextBucket(Collections.singletonList(topicPartitionOffsetDistance));

        verify(bucketDistributorMock).distribute(partitionOffsetDistance);

        Assertions.assertThat(actual).isEqualTo(expectedMock);
    }

    static StartOffset startOffset(final int offset) {
        return StartOffset.of(offset);
    }

    static EndOffset endOffset(final int offset) {
        return EndOffset.of(offset);
    }

    static TopicPartition topicPartition(final int partition) {
        return new TopicPartition("kafka-topic", partition);
    }
}