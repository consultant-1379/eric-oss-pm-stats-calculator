/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.resourcemanager.model.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.ericsson.oss.air.pm.stats.common.resources.utils.ResourceLoaderUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

class KafkaUserPermissionsDtoTest {
    private static final String KAFKA_USER_PERMISSIONS = "json/kafka_user_permissions.json";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @SneakyThrows
    @Test
    void shouldSerialize() {
        final KafkaUserPermissionsDto.TopicConfigurationsDto topicConfigurationsDto = KafkaUserPermissionsDto.TopicConfigurationsDto.builder()
                .permission("read-only")
                .build();

        final List<Pair<String, KafkaUserPermissionsDto.TopicConfigurationsDto>> topics = List.of(
                Pair.of("topic1", topicConfigurationsDto),
                Pair.of("topic2", topicConfigurationsDto));
        final KafkaUserPermissionsDto dto = KafkaUserPermissionsDto.builder()
                .topics(topics)
                .build();

        final JsonNode actual = OBJECT_MAPPER.valueToTree(dto);
        final JsonNode expected = OBJECT_MAPPER.readTree(ResourceLoaderUtils.getClasspathResourceAsString(KAFKA_USER_PERMISSIONS));

        assertThat(actual).isEqualTo(expected);
    }
}