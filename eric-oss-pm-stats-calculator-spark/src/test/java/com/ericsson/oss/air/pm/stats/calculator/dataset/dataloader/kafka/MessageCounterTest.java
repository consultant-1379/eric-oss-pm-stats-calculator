/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.kafka;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.configuration.KafkaConfiguration;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.kafka.MessageCounterTest.KafkaTestConfiguration;
import com.ericsson.oss.air.pm.stats.calculator.service.util.consumer.KafkaConsumerService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.Topic;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset.EndOffset;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset.StartOffset;

import com.google.common.util.concurrent.Uninterruptibles;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MessageCounter.class, KafkaConsumerService.class, KafkaConfiguration.class, KafkaTestConfiguration.class})
class MessageCounterTest {
    @Container static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.1.0"));

    @Autowired AdminClient adminClient;
    @Autowired KafkaTemplate<String, String> kafkaTemplate;

    @Autowired MessageCounter objectUnderTest;

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
        public KafkaTemplate<String, String> kafkaTemplate(final ProducerFactory<String, String> producerFactory) {
            return new KafkaTemplate<>(producerFactory);
        }
        @Bean
        public AdminClient kafkaAdminClient(@NonNull final KafkaProperties kafkaProperties) {
            return AdminClient.create(kafkaProperties.buildAdminProperties());
        }
    }

    @Test
    void shouldReturnUnModifiableMap_onCountMessagesByPartition() {
        final Map<TopicPartition, EndOffset> actual = objectUnderTest.countMessagesByPartition(Topic.of("topic"));
        assertThatThrownBy(() -> actual.put(new TopicPartition("topic", 0), EndOffset.of(10))).isInstanceOf(UnsupportedOperationException.class);
    }

    @MethodSource("provideCountMessagesByPartitionData")
    @ParameterizedTest(name = "[{index}] Topic: ''{0}'' Partition-0: ''{1}'' Partition-1: ''{2}'' Partition-2: ''{3}'' Partition-3: ''{4}''")
    void shouldCountMessagesByPartition(final String topic, final long firstPartition, final long secondPartition, final long thirdPartition, final long fourthPartition) {
        inSync(adminClient.createTopics(singletonList(new NewTopic(topic, 4, (short) 1))));

        sendNMessagesToPartition(topic, 0, firstPartition);
        sendNMessagesToPartition(topic, 1, secondPartition);
        sendNMessagesToPartition(topic, 2, thirdPartition);
        sendNMessagesToPartition(topic, 3, fourthPartition);

        final Map<TopicPartition, EndOffset> actual = objectUnderTest.countMessagesByPartition(Topic.of(topic));

        assertThat(actual).containsOnly(
                entry(new TopicPartition(topic, 0), EndOffset.of(firstPartition)),
                entry(new TopicPartition(topic, 1), EndOffset.of(secondPartition)),
                entry(new TopicPartition(topic, 2), EndOffset.of(thirdPartition)),
                entry(new TopicPartition(topic, 3), EndOffset.of(fourthPartition))
        );
    }

    static Stream<Arguments> provideCountMessagesByPartitionData() {
        return Stream.of(
                Arguments.of("topic-0", onFirst(0), onSecond(0), onThird(0), onFourth(0)),
                Arguments.of("topic-1", onFirst(1), onSecond(0), onThird(0), onFourth(1)),
                Arguments.of("topic-2", onFirst(1), onSecond(3), onThird(5), onFourth(5))
        );
    }
    @MethodSource("provideGetStartOffsetForTopicData")
    @ParameterizedTest(name = "[{index}] Topic: ''{0}'' Partition-0: ''{1}'' Partition-1: ''{2}'' Partition-2: ''{3}'' Partition-3: ''{4}''")
    void shouldGetStartOffsetForTopic(final String topic, final long firstPartition, final long secondPartition, final long thirdPartition, final long fourthPartition) {
        inSync(adminClient.createTopics(singletonList(new NewTopic(topic, 4, (short) 1))));

        sendNMessagesToPartition(topic, 0, firstPartition);
        sendNMessagesToPartition(topic, 1, secondPartition);
        sendNMessagesToPartition(topic, 2, thirdPartition);
        sendNMessagesToPartition(topic, 3, fourthPartition);

        final Map<TopicPartition, StartOffset> actual = objectUnderTest.getStartOffsetForTopic(Topic.of(topic));

        assertThat(actual).containsOnly(
            entry(new TopicPartition(topic, 0), StartOffset.of(0)),
            entry(new TopicPartition(topic, 1), StartOffset.of(0)),
            entry(new TopicPartition(topic, 2), StartOffset.of(0)),
            entry(new TopicPartition(topic, 3), StartOffset.of(0))
        );
    }

    static Stream<Arguments> provideGetStartOffsetForTopicData() {
        return Stream.of(
            Arguments.of("topic-3", onFirst(0), onSecond(0), onThird(0), onFourth(0)),
            Arguments.of("topic-4", onFirst(1), onSecond(0), onThird(0), onFourth(1)),
            Arguments.of("topic-5", onFirst(1), onSecond(3), onThird(5), onFourth(5))
        );
    }

    private void sendNMessagesToPartition(final String topic, final int partition, final long n) {
        LongStream.range(0, n).forEach(i -> inSync(kafkaTemplate.send(topic, partition, "key", "value")));
    }

    static void inSync(@NonNull final CreateTopicsResult createTopicsResult) {
        inSync(createTopicsResult.all());
    }

    @SneakyThrows
    static void inSync(final Future<?> future) {
        Uninterruptibles.getUninterruptibly(future, 10, TimeUnit.SECONDS);
    }

    static int onFirst(final int numberOfMessages) { return numberOfMessages; }
    static int onSecond(final int numberOfMessages) { return numberOfMessages; }
    static int onThird(final int numberOfMessages) { return numberOfMessages; }
    static int onFourth(final int numberOfMessages) { return numberOfMessages; }
}