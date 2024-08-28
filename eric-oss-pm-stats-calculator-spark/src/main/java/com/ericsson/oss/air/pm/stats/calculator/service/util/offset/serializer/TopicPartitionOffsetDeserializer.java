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

import java.util.HashMap;
import java.util.Map;

import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset.EndOffset;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TopicPartitionOffsetDeserializer {
    private static final TypeReference<Map<String, Map<Integer, Long>>> MAP_TYPE_REFERENCE = new MapTypeReference();

    private final ObjectMapper objectMapper;

    public Map<TopicPartition, EndOffset> deserialize(final String value) {
        final Map<String, Map<Integer, Long>> content = readValue(value);

        final Map<TopicPartition, EndOffset> result = new HashMap<>();
        content.forEach((topic, partitionEndOffset) -> partitionEndOffset.forEach(
                (partition, offset) -> result.put(new TopicPartition(topic, partition), EndOffset.of(offset))
        ));

        return result;
    }

    @SneakyThrows /* Not expecting issue with deserialization */
    private Map<String, Map<Integer, Long>> readValue(final String content) {
        return objectMapper.readValue(content, MAP_TYPE_REFERENCE);
    }

    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    private static class MapTypeReference extends TypeReference<Map<String, Map<Integer, Long>>> { }
}
