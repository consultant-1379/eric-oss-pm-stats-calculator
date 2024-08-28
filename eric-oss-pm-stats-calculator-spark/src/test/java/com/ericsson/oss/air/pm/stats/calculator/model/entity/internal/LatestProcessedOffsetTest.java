/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.model.entity.internal;

import org.apache.kafka.common.TopicPartition;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class LatestProcessedOffsetTest {

    @Test
    void shouldIncrementTopicPartitionOffset() {
        final LatestProcessedOffset latestProcessedOffset = LatestProcessedOffset.builder().withTopicPartitionOffset(0L).build();

        Assertions.assertThat(latestProcessedOffset.getTopicPartitionOffset()).isZero();

        latestProcessedOffset.incrementTopicPartitionOffset();

        Assertions.assertThat(latestProcessedOffset.getTopicPartitionOffset()).isOne();
    }

    @Test
    void shouldVerifyAsTopicPartition() {
        final LatestProcessedOffset latestProcessedOffset =
                LatestProcessedOffset.builder().withTopicName("kafka-topic").withTopicPartition(10).build();

        final TopicPartition actual = latestProcessedOffset.asTopicPartition();

        Assertions.assertThat(actual).isEqualTo(new TopicPartition("kafka-topic", 10));
    }
}