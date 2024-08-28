/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.offset.serializer;

import java.util.Arrays;

import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.LatestProcessedOffset;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.Partition;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.Topic;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.TopicPartitionOffsetDistance.OffsetDistance;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset.EndOffset;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset.StartOffset;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.kafka.common.TopicPartition;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class TopicPartitionOffsetSerializerTest {
    TopicPartitionOffsetSerializer objectUnderTest = new TopicPartitionOffsetSerializer(new ObjectMapper());

    @Test
    void shouldSerializeLatestProcessedOffsets() {
        final String actual = objectUnderTest.serializeLatestProcessedOffset(Topic.of("kafka-topic"), Arrays.asList(
                latestProcessedOffset(0, 10L),
                latestProcessedOffset(1, 15L),
                latestProcessedOffset(2, 20L),
                latestProcessedOffset(3, 25L)
        ));

        Assertions.assertThat(actual).isEqualTo("{\"kafka-topic\":{\"0\":10,\"1\":15,\"2\":20,\"3\":25}}");
    }

    @Test
    void shouldSerializeTopicPartitionEndOffset() {
        final String actual = objectUnderTest.serializeTopicPartitionEndOffset(Topic.of("kafka-topic"), ImmutableMap.of(
                new TopicPartition("kafka-topic", 0), EndOffset.of(10),
                new TopicPartition("kafka-topic", 1), EndOffset.of(15),
                new TopicPartition("kafka-topic", 2), EndOffset.of(20),
                new TopicPartition("kafka-topic", 3), EndOffset.of(25))
        );

        Assertions.assertThat(actual).isEqualTo("{\"kafka-topic\":{\"0\":10,\"1\":15,\"2\":20,\"3\":25}}");
    }

    @Test
    void shouldSerializePartitionOffsetDistance() {
        final String actual = objectUnderTest.serializePartitionOffsetDistance(Topic.of("kafka-topic"), ImmutableMap.of(
                Partition.of(0), OffsetDistance.of(StartOffset.of(0), EndOffset.of(10)),
                Partition.of(1), OffsetDistance.of(StartOffset.of(0), EndOffset.of(15)),
                Partition.of(2), OffsetDistance.of(StartOffset.of(0), EndOffset.of(20)),
                Partition.of(3), OffsetDistance.of(StartOffset.of(0), EndOffset.of(25)))
        );

        Assertions.assertThat(actual).isEqualTo("{\"kafka-topic\":{\"0\":10,\"1\":15,\"2\":20,\"3\":25}}");
    }

    @Test
    void shouldSerializePartitionEndOffset() {
        final String actual = objectUnderTest.serializePartitionEndOffset(Topic.of("kafka-topic"), ImmutableMap.of(
                Partition.of(0), EndOffset.of(10),
                Partition.of(1), EndOffset.of(15),
                Partition.of(2), EndOffset.of(20),
                Partition.of(3), EndOffset.of(25))
        );

        Assertions.assertThat(actual).isEqualTo("{\"kafka-topic\":{\"0\":10,\"1\":15,\"2\":20,\"3\":25}}");
    }

    static LatestProcessedOffset latestProcessedOffset(final int partition, final long offset) {
        return LatestProcessedOffset.builder().withTopicPartition(partition).withTopicPartitionOffset(offset).build();
    }
}