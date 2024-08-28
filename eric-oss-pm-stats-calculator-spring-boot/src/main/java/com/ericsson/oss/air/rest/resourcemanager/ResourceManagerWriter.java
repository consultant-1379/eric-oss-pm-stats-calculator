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

import java.util.List;

import com.ericsson.oss.air.rest.resourcemanager.model.request.KafkaUserPermissionsDto;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class ResourceManagerWriter {
    private static final String SCHEDULED_TOPIC = "pm-stats-calc-handling-avro-scheduled";
    private static final String ON_DEMAND_TOPIC = "pm-stats-calc-handling-avro-on-demand";

    @Value("${pm-stats-calculator.kafka-resource-manager-address}")
    private String address;

    @Autowired
    private RestTemplate restTemplate;

    public void createOrUpdateOutputTopicKafkaUser(final String kafkaUserName) {
        final KafkaUserPermissionsDto.TopicConfigurationsDto topicConfigurations = KafkaUserPermissionsDto.TopicConfigurationsDto.builder()
                .permission("read-only")
                .build();
        final List<Pair<String, KafkaUserPermissionsDto.TopicConfigurationsDto>> topics = List.of(
                Pair.of(SCHEDULED_TOPIC, topicConfigurations),
                Pair.of(ON_DEMAND_TOPIC, topicConfigurations)
        );
        final KafkaUserPermissionsDto kafkaUserPermissionsDto = KafkaUserPermissionsDto.builder()
                .topics(topics)
                .build();

        createOrUpdateKafkaUser(kafkaUserName, kafkaUserPermissionsDto);

    }

    private void createOrUpdateKafkaUser(final String kafkaUserName, final KafkaUserPermissionsDto kafkaUserPermissionsDto) {
        final String url = String.format("%s/resources/v2/users/%s", address, kafkaUserName);

        try {
            final ResponseEntity<Object> response = restTemplate.postForEntity(url, kafkaUserPermissionsDto, Object.class);

            if (response.getStatusCode().equals(HttpStatus.OK)) {
                log.info("Kafka user: {} was created/updated.", kafkaUserName);
            } else {
                log.warn("Creating/updating Kafka user: {}, was unsuccessful", kafkaUserName);
            }
        } catch (final RestClientException ex) {
            log.warn("Creating/updating Kafka user: {}, was unsuccessful", kafkaUserName, ex);
        }
    }
}