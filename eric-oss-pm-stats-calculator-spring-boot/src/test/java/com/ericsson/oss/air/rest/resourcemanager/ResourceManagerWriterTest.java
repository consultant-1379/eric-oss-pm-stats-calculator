/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.resourcemanager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.List;

import com.ericsson.oss.air.rest.resourcemanager.model.request.KafkaUserPermissionsDto;

import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(classes = ResourceManagerWriter.class)
@ActiveProfiles("test")
class ResourceManagerWriterTest {
    @Autowired
    ResourceManagerWriter objectUnderTest;

    @MockBean
    RestTemplate restTemplateMock;

    @Test
    void shouldCreateOrUpdateOutputTopicKafkaUser_notThrowException_whenStatusIs200() {
        final String expectedURI = "eric-oss-kf-resource-manager:8080/resources/v2/users/kafkaUserName";
        final KafkaUserPermissionsDto.TopicConfigurationsDto topicConfigurationsDto = KafkaUserPermissionsDto.TopicConfigurationsDto.builder()
                .permission("read-only")
                .build();

        final List<Pair<String, KafkaUserPermissionsDto.TopicConfigurationsDto>> topics = List.of(
                Pair.of("pm-stats-calc-handling-avro-scheduled", topicConfigurationsDto),
                Pair.of("pm-stats-calc-handling-avro-on-demand", topicConfigurationsDto));
        final KafkaUserPermissionsDto expectedDto = KafkaUserPermissionsDto.builder()
                .topics(topics)
                .build();

        ResponseEntity<Object> response = new ResponseEntity<>(HttpStatus.OK);
        doReturn(response).when(restTemplateMock).postForEntity(eq(expectedURI), eq(expectedDto), any());

        Assertions.assertThatCode(() -> objectUnderTest.createOrUpdateOutputTopicKafkaUser("kafkaUserName")).doesNotThrowAnyException();

        verify(restTemplateMock).postForEntity(eq(expectedURI), eq(expectedDto), any());
    }

    @Test
    void shouldCreateOrUpdateOutputTopicKafkaUser_notThrowException_whenStatusIsNot200() {
        final String expectedURI = "eric-oss-kf-resource-manager:8080/resources/v2/users/kafkaUserName";
        final KafkaUserPermissionsDto.TopicConfigurationsDto topicConfigurationsDto = KafkaUserPermissionsDto.TopicConfigurationsDto.builder()
                .permission("read-only")
                .build();

        final List<Pair<String, KafkaUserPermissionsDto.TopicConfigurationsDto>> topics = List.of(
                Pair.of("pm-stats-calc-handling-avro-scheduled", topicConfigurationsDto),
                Pair.of("pm-stats-calc-handling-avro-on-demand", topicConfigurationsDto));
        final KafkaUserPermissionsDto expectedDto = KafkaUserPermissionsDto.builder()
                .topics(topics)
                .build();

        ResponseEntity<Object> response = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        doReturn(response).when(restTemplateMock).postForEntity(eq(expectedURI), eq(expectedDto), any());

        Assertions.assertThatCode(() -> objectUnderTest.createOrUpdateOutputTopicKafkaUser("kafkaUserName")).doesNotThrowAnyException();

        verify(restTemplateMock).postForEntity(eq(expectedURI), eq(expectedDto), any());
    }

    @Test
    void shouldCreateOrUpdateOutputTopicKafkaUser_notThrowException_restTemplateThrowsException() {
        final String expectedURI = "eric-oss-kf-resource-manager:8080/resources/v2/users/kafkaUserName";
        final KafkaUserPermissionsDto.TopicConfigurationsDto topicConfigurationsDto = KafkaUserPermissionsDto.TopicConfigurationsDto.builder()
                .permission("read-only")
                .build();

        final List<Pair<String, KafkaUserPermissionsDto.TopicConfigurationsDto>> topics = List.of(
                Pair.of("pm-stats-calc-handling-avro-scheduled", topicConfigurationsDto),
                Pair.of("pm-stats-calc-handling-avro-on-demand", topicConfigurationsDto));
        final KafkaUserPermissionsDto expectedDto = KafkaUserPermissionsDto.builder()
                .topics(topics)
                .build();

        doThrow(RestClientException.class).when(restTemplateMock).postForEntity(eq(expectedURI), eq(expectedDto), any());

        Assertions.assertThatCode(() -> objectUnderTest.createOrUpdateOutputTopicKafkaUser("kafkaUserName")).doesNotThrowAnyException();

        verify(restTemplateMock).postForEntity(eq(expectedURI), eq(expectedDto), any());
    }
}