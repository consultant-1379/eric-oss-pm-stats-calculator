/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.startup;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.PostConstruct;

import com.ericsson.oss.air.pm.stats.calculator.configuration.CalculatorProperties;
import com.ericsson.oss.air.pm.stats.model.exception.StartupException;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Uninterruptibles;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaFuture;
import org.springframework.stereotype.Component;

//TODO: make this a simple NewTopic bean when kafka handling is done by spring
@Slf4j
@Component
@AllArgsConstructor
public class KpiExposureStartupService {

    private Properties kafkaAdminProperties;
    private CalculatorProperties calculatorProperties;
    private Properties kpiExposureKafkaTopicProperties;

    @PostConstruct
    public void onServiceStart() {
        if (!topicExists(calculatorProperties.getKafkaExposureTopicName())) {
            createKpiExposureControlTopic();
        }
    }

    private boolean topicExists(final String topicName) {
        try (final Admin admin = Admin.create(kafkaAdminProperties)) {
            final Set<String> topicNames = Uninterruptibles.getUninterruptibly(admin.listTopics().names(), 30, TimeUnit.SECONDS);
            return topicNames.contains(topicName);
        } catch (final ExecutionException | TimeoutException e) {
            throw new StartupException("Error occurred while checking if topic exists with name " + topicName, e);
        }
    }

    private void createKpiExposureControlTopic() {
        try (final Admin admin = Admin.create(kafkaAdminProperties)) {
            final String topic = calculatorProperties.getKafkaExposureTopicName();
            final int numberOfPartitions = 1;
            final short replicationFactor = 1;

            final CreateTopicsResult result = admin.createTopics(
                    Collections.singleton(
                            new NewTopic(topic, numberOfPartitions, replicationFactor).configs(Maps.fromProperties(kpiExposureKafkaTopicProperties))
                    )
            );

            // Call values() to get the result for a specific topic
            final KafkaFuture<Void> future = result.values().get(topic);

            // Call get() to block until the topic creation is complete or has failed
            // if creation failed the ExecutionException wraps the underlying cause.
            Uninterruptibles.getUninterruptibly(future, 30, TimeUnit.SECONDS);

            log.info("Successfully created KPI exposure Kafka topic: '{}'", topic);
        } catch (final ExecutionException | TimeoutException e) {
            throw new StartupException("Error occurred while creating kafka topic for controlling query service exposure.", e);
        }
    }
}
