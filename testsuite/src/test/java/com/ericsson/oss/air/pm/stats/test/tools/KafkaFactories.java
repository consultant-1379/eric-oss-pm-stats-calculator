/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.tools;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.VALUE_SUBJECT_NAME_STRATEGY;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

import java.util.Map;

import io.confluent.kafka.serializers.subject.RecordNameStrategy;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KafkaFactories {
    private static final String BOOTSTRAP_SERVERS = "eric-data-message-bus-kf:9092";

    public static KafkaConsumer<String, String> kafkaConsumer() {
        return new KafkaConsumer<>(Map.of(
                BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS,
                KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class
        ));
    }

    public static AdminClient adminClient() {
        return AdminClient.create(kafkaAdminProperties());
    }

    public static Map<String, Object> kafkaAdminProperties() {
        return Map.of(BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    }

    public static Map<String, Object> kafkaProducerProperties(final Class<?> keySerializer, final Class<?> valueSerializer) {
        return Map.of(
                BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS,
                KEY_SERIALIZER_CLASS_CONFIG, keySerializer,
                VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer,
                VALUE_SUBJECT_NAME_STRATEGY, RecordNameStrategy.class.getName(),
                SCHEMA_REGISTRY_URL_CONFIG, "http://schemaregistry:8081"
        );
    }

}
