/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.offset;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.kafka.clients.admin.RecordsToDelete.beforeOffset;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.ericsson.oss.air.pm.stats.calculator.configuration.KafkaConfiguration;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.KpiExecutionGroup;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.LatestProcessedOffset;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.ExecutionGroupRepository;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.LatestProcessedOffsetRepository;
import com.ericsson.oss.air.pm.stats.calculator.service.util.consumer.KafkaConsumerService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.LatestProcessedOffsetAdjusterTest.KafkaTestConfiguration;

import com.google.common.util.concurrent.Uninterruptibles;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringSerializer;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@DataJpaTest(showSql = false)
@ActiveProfiles("test-containers")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@EntityScan("com.ericsson.oss.air.pm.stats.calculator.model.entity")
@EnableJpaRepositories("com.ericsson.oss.air.pm.stats.calculator.repository")
@ContextConfiguration(classes = {LatestProcessedOffsetAdjuster.class, KafkaConsumerService.class, KafkaConfiguration.class, KafkaTestConfiguration.class})
class LatestProcessedOffsetAdjusterTest {
    @Container static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.1.0"));

    @Autowired AdminClient adminClient;
    @Autowired KafkaTemplate<String, String> kafkaTemplate;

    @Autowired LatestProcessedOffsetRepository latestProcessedOffsetRepository;
    @Autowired ExecutionGroupRepository executionGroupRepository;

    @Autowired LatestProcessedOffsetAdjuster objectUnderTest;

    /**
     * Counts each unit test to provide unique {@link #topic} and {@link #executionGroup} names
     * eliminating the need of recreating environmental context.
     */
    static AtomicInteger testCounter = new AtomicInteger(0);

    String topic;
    String executionGroup;

    @DynamicPropertySource
    static void kafkaProperties(@NonNull final DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.consumer.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);

        registry.add("spring.kafka.producer.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
        registry.add("spring.kafka.producer.key-serializer", StringSerializer.class::getName);
        registry.add("spring.kafka.producer.value-serializer", StringSerializer.class::getName);

        registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
    }

    @TestConfiguration
    @Import(KafkaAutoConfiguration.class)
    static class KafkaTestConfiguration {
        @Bean
        public ProducerFactory<String, String> producerFactory(@NonNull final KafkaProperties kafkaProperties) {
            return new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties());
        }

        @Bean
        public AdminClient kafkaAdminClient(@NonNull final KafkaProperties kafkaProperties) {
            return AdminClient.create(kafkaProperties.buildAdminProperties());
        }
    }

    @BeforeEach
    void setUp(final TestReporter testReporter) {
        final int counter = testCounter.getAndIncrement();

        topic = "topic-" + counter;
        executionGroup = "group-" + counter;

        testReporter.publishEntry("topic", topic);
        testReporter.publishEntry("executionGroup", executionGroup);
    }

    @Test
    void shouldAdjustOffsets_whenTheyAreOutOfRange() {
        createTopic(6);

        sendNMessagesToPartition(0, 100);
        sendNMessagesToPartition(1, 100);
        sendNMessagesToPartition(2, 100);
        sendNMessagesToPartition(3, 100);
        sendNMessagesToPartition(4, 100);
        sendNMessagesToPartition(5, 100);

        adminClient.deleteRecords(Map.of(
                topicPartition(0), beforeOffset(25),
                topicPartition(1), beforeOffset(25),
                topicPartition(2), beforeOffset(50),
                topicPartition(3), beforeOffset(50),
                topicPartition(4), beforeOffset(75),
                topicPartition(5), beforeOffset(75)
        ));

        //  Records available on Kafka for the test:
        //      topic-0: [25 - 100]
        //      topic-1: [25 - 100]
        //      topic-2: [50 - 100]
        //      topic-3: [50 - 100]
        //      topic-4: [75 - 100]
        //      topic-5: [75 - 100]

        persistOffsets(executionGroup(executionGroup), List.of(
                latestProcessedOffset(0, 15L), // known offset < actual offset, reset
                latestProcessedOffset(1, 25L), // known offset OK
                latestProcessedOffset(2, 49L), // known offset OK, it is 1 off but needs to start from 50
                latestProcessedOffset(3, 75L), // known offset OK
                latestProcessedOffset(4, 99L), // known offset OK, offset + 1 still = endOffset
                latestProcessedOffset(5, 105L) // known offset > actual offset, reset
        ));

        final List<LatestProcessedOffset> actual = objectUnderTest.adjustOffsets(latestProcessedOffsetRepository.findAllForTopic(topic, executionGroup));

        final List<Tuple> expected = List.of(
                tuple(topicPartition(0), 25L),
                tuple(topicPartition(1), 25L),
                tuple(topicPartition(2), 49L),
                tuple(topicPartition(3), 75L),
                tuple(topicPartition(4), 99L),
                tuple(topicPartition(5), 75L)
        );

        assertLatestProcessOffsets(actual, expected);
        assertLatestProcessOffsets(latestProcessedOffsetRepository.findAllForTopic(topic, executionGroup), expected);
    }

    @Test
    void shouldRemoveLatestProcessedOffset_whenItIsNoLongerAvailableOnKafka() {
        createTopic(1);

        sendNMessagesToPartition(0, 5);

        //  Records available on Kafka for the test:
        //      topic-0: [0 - 5]

        persistOffsets(executionGroup(executionGroup), List.of(
                latestProcessedOffset(0, 3L), // known offset OK
                latestProcessedOffset(1, 10L) // offset for partition does not exist anymore
        ));

        final List<LatestProcessedOffset> actual = objectUnderTest.adjustOffsets(latestProcessedOffsetRepository.findAllForTopic(topic, executionGroup));

        final List<Tuple> expected = List.of(tuple(topicPartition(0), 3L));

        assertLatestProcessOffsets(actual, expected);
        assertLatestProcessOffsets(latestProcessedOffsetRepository.findAllForTopic(topic, executionGroup), expected);
    }

    @Test
    void shouldCreateOffset_whenItIsANewPartition() {
        createTopic(2);

        sendNMessagesToPartition(0, 5);
        sendNMessagesToPartition(1, 5);

        //  Records available on Kafka for the test:
        //      topic-0: [0 - 5]
        //      topic-1: [0 - 5]

        persistOffsets(executionGroup(executionGroup), List.of(latestProcessedOffset(0, 3L)));

        final List<LatestProcessedOffset> actual = objectUnderTest.adjustOffsets(latestProcessedOffsetRepository.findAllForTopic(topic, executionGroup));

        final List<Tuple> expected = List.of(
                tuple(topicPartition(0), 3L),
                tuple(topicPartition(1), 0L)
        );

        assertLatestProcessOffsets(actual, expected);
        assertLatestProcessOffsets(latestProcessedOffsetRepository.findAllForTopic(topic, executionGroup), expected);
    }

    TopicPartition topicPartition(final int partition) {
        return new TopicPartition(topic, partition);
    }

    void persistOffsets(final KpiExecutionGroup kpiExecutionGroup, final Collection<LatestProcessedOffset> latestProcessedOffsets) {
        executionGroupRepository.save(kpiExecutionGroup);
        latestProcessedOffsets.forEach(latestProcessedOffset -> latestProcessedOffset.setExecutionGroup(kpiExecutionGroup));
        latestProcessedOffsetRepository.saveAll(latestProcessedOffsets);
    }

    LatestProcessedOffset latestProcessedOffset(final int partition, final long offset) {
        return new LatestProcessedOffset(null, topic, partition, offset, false, null);
    }

    void createTopic(final int numPartitions) {
        inSync(adminClient.createTopics(singletonList(new NewTopic(topic, numPartitions, (short) 1))));
    }

    void sendNMessagesToPartition(final int partition, final long numberOfMessages) {
        for (long i = 0; i < numberOfMessages; i++) {
            inSync(kafkaTemplate.send(topic, partition, "key", randomAlphabetic(10)));
        }
    }

    static void assertLatestProcessOffsets(final Collection<LatestProcessedOffset> actual, final List<Tuple> expected) {
        assertThat(actual).map(
                LatestProcessedOffset::asTopicPartition, LatestProcessedOffset::getTopicPartitionOffset
        ).containsExactlyInAnyOrder(expected.toArray(Tuple[]::new));
    }

    static KpiExecutionGroup executionGroup(final String executionGroup) {
        return new KpiExecutionGroup(null, executionGroup, null, null);
    }

    @SneakyThrows
    static void inSync(final Future<?> future) {
        Uninterruptibles.getUninterruptibly(future, 10, TimeUnit.SECONDS);
    }

    static void inSync(final CreateTopicsResult createTopicsResult) {
        inSync(createTopicsResult.all());
    }
}