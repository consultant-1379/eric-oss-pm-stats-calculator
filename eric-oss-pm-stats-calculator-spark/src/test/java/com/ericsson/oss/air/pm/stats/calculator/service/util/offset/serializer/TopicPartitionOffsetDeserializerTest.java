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

import java.util.Map;

import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset.EndOffset;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.TopicPartition;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

class TopicPartitionOffsetDeserializerTest {
    TopicPartitionOffsetDeserializer objectUnderTest = new TopicPartitionOffsetDeserializer(new ObjectMapper());

    @Test
    void shouldDeserialize() {
        final Map<TopicPartition, EndOffset> actual = objectUnderTest.deserialize("{\"kafka-topic\": {\"0\": 15, \"1\": 30, \"2\": 10 }}");
        Assertions.assertThat(actual).containsExactlyInAnyOrderEntriesOf(ImmutableMap.of(
                new TopicPartition("kafka-topic", 0), EndOffset.of(15),
                new TopicPartition("kafka-topic", 1), EndOffset.of(30),
                new TopicPartition("kafka-topic", 2), EndOffset.of(10)
        ));
    }

}