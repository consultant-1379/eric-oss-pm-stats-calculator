/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.ericsson.oss.air.pm.stats.calculator.api.dto.kafka.query.service.KpiExposureConfig;
import com.ericsson.oss.air.pm.stats.calculator.api.dto.kafka.query.service.KpiTableExposureConfig;
import com.ericsson.oss.air.pm.stats.calculator.api.dto.kafka.query.service.KpiTableExposureConfig.Type;
import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiCalculatorException;
import com.ericsson.oss.air.pm.stats.calculator.configuration.CalculatorProperties;
import com.ericsson.oss.air.pm.stats.repository.api.DatabaseRepository;
import com.ericsson.oss.air.pm.stats.service.util.KpiExposureConfigSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@ExtendWith(MockitoExtension.class)
class KpiExposureUpdaterTest {

    private static final String EXPOSURE_TOPIC_NAME = "kpi-exposure-topic";

    @Container
    static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.1.0"));

    @Mock DatabaseRepository databaseRepositoryMock;
    @Mock CalculatorProperties calculatorPropertiesMock;

    Properties kafkaProducerProperties = new Properties();

    KpiExposureUpdater objectUnderTest;

    @BeforeEach
    void setUp() {
        when(calculatorPropertiesMock.getKafkaExposureTopicName()).thenReturn(EXPOSURE_TOPIC_NAME);

        kafkaProducerProperties.put("bootstrap.servers", KAFKA_CONTAINER.getBootstrapServers());
        kafkaProducerProperties.put("key.serializer", StringSerializer.class.getName());
        kafkaProducerProperties.put("value.serializer", KpiExposureConfigSerializer.class.getName());

        objectUnderTest = new KpiExposureUpdater(databaseRepositoryMock, calculatorPropertiesMock, new KafkaProducer<>(kafkaProducerProperties));
    }

    @AfterEach
    void tearDown() {
        final Properties adminProperties = new Properties();
        adminProperties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());
        try (final Admin admin = Admin.create(adminProperties)) {
            admin.deleteTopics(Collections.singleton(EXPOSURE_TOPIC_NAME)).all().get();
        } catch (final ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Timeout(value = 1, unit = TimeUnit.MINUTES)
    void shouldSendCorrectMessage_whenExposingOneTable() {
        when(databaseRepositoryMock.findAllOutputTablesWithoutPartition()).thenReturn(Collections.emptyList());

        objectUnderTest.updateVisibility("schema", "test-table", Type.TABLE, true);

        verify(databaseRepositoryMock).findAllOutputTablesWithoutPartition();

        final List<ConsumerRecord<String, KpiExposureConfig>> records = getRecords();
        assertThat(records).hasSize(1);

        final ConsumerRecord<String, KpiExposureConfig> record = records.get(0);
        assertThat(record.key()).isEqualTo("schema");
        assertThat(record.value().getTimestamp()).isLessThanOrEqualTo(System.currentTimeMillis());
        assertThat(record.value().getExposure()).containsExactlyInAnyOrder(new KpiTableExposureConfig("test-table", Type.TABLE));
    }

    @Test
    @Timeout(value = 1, unit = TimeUnit.MINUTES)
    void shouldSendCorrectMessage_whenExposingMultipleTables() {
        when(databaseRepositoryMock.findAllOutputTablesWithoutPartition()).thenReturn(Collections.emptyList());

        objectUnderTest.updateVisibility("schema", "kpi_test-table1", Type.TABLE, true);

        when(databaseRepositoryMock.findAllOutputTablesWithoutPartition()).thenReturn(List.of("kpi_test-table1"));
        objectUnderTest.updateVisibility("schema", "kpi_test-table2", Type.TABLE, true);

        verify(databaseRepositoryMock, times(2)).findAllOutputTablesWithoutPartition();

        final List<ConsumerRecord<String, KpiExposureConfig>> records = getRecords();
        assertThat(records).hasSize(2);

        final ConsumerRecord<String, KpiExposureConfig> firstRecord = records.get(0);
        assertThat(firstRecord.key()).isEqualTo("schema");
        assertThat(firstRecord.value().getTimestamp()).isLessThanOrEqualTo(System.currentTimeMillis());
        assertThat(firstRecord.value().getExposure()).containsExactlyInAnyOrder(new KpiTableExposureConfig("kpi_test-table1", Type.TABLE));

        final ConsumerRecord<String, KpiExposureConfig> secondRecord = records.get(1);
        assertThat(secondRecord.key()).isEqualTo("schema");
        assertThat(secondRecord.value().getTimestamp()).isLessThanOrEqualTo(System.currentTimeMillis());
        assertThat(secondRecord.value().getExposure()).containsExactlyInAnyOrder(new KpiTableExposureConfig("kpi_test-table1", Type.TABLE), new KpiTableExposureConfig("kpi_test-table2", Type.TABLE));
    }

    @Test
    @Timeout(value = 1, unit = TimeUnit.MINUTES)
    void shouldSendCorrectMessage_whenHidingExposedTable() {
        when(databaseRepositoryMock.findAllOutputTablesWithoutPartition()).thenReturn(Collections.emptyList());

        objectUnderTest.updateVisibility("schema", "test-table", Type.TABLE, true);
        objectUnderTest.updateVisibility("schema", "test-table", Type.TABLE, false);

        verify(databaseRepositoryMock, times(2)).findAllOutputTablesWithoutPartition();

        final List<ConsumerRecord<String, KpiExposureConfig>> records = getRecords();
        assertThat(records).hasSize(2);

        final ConsumerRecord<String, KpiExposureConfig> firstRecord = records.get(0);
        assertThat(firstRecord.key()).isEqualTo("schema");
        assertThat(firstRecord.value().getTimestamp()).isLessThanOrEqualTo(System.currentTimeMillis());
        assertThat(firstRecord.value().getExposure()).containsExactlyInAnyOrder(new KpiTableExposureConfig("test-table", Type.TABLE));

        final ConsumerRecord<String, KpiExposureConfig> secondRecord = records.get(1);
        assertThat(secondRecord.key()).isEqualTo("schema");
        assertThat(secondRecord.value()).isNull();
    }

    @Test
    @Timeout(value = 1, unit = TimeUnit.MINUTES)
    void shouldThrowException_whenMessageSendingFails() {
        kafkaProducerProperties.put("value.serializer", StringSerializer.class.getName());
        objectUnderTest = new KpiExposureUpdater(databaseRepositoryMock, calculatorPropertiesMock, new KafkaProducer<>(kafkaProducerProperties));

        assertThatThrownBy(() -> objectUnderTest.updateVisibility("schema", "test-table", Type.TABLE, true))
                .isInstanceOf(KpiCalculatorException.class);
    }

    @Test
    @Timeout(value = 1, unit = TimeUnit.MINUTES)
    void shouldUpdateExposure() {
        when(databaseRepositoryMock.findAllOutputTablesWithoutPartition()).thenReturn(List.of("test-table"));

        objectUnderTest.updateExposureInfo("schema");

        assertThat(getRecords()).first().satisfies(record -> {
            assertThat(record.key()).isEqualTo("schema");
            assertThat(record.value().getTimestamp()).isLessThanOrEqualTo(System.currentTimeMillis());
            assertThat(record.value().getExposure()).containsExactlyInAnyOrder(new KpiTableExposureConfig("test-table", Type.TABLE));
        });

        // We drop the table as all KPI definitions are deleted by the retention
        when(databaseRepositoryMock.findAllOutputTablesWithoutPartition()).thenReturn(List.of());

        objectUnderTest.updateExposureInfo("schema");

        assertThat(getRecords()).first().satisfies(record -> {
            assertThat(record.key()).isEqualTo("schema");
            assertThat(record.value()).isNull();
        });

        verify(databaseRepositoryMock, times(2)).findAllOutputTablesWithoutPartition();
    }

    static Properties getConsumerProperties() {
        final Properties consumerProperties = new Properties();
        consumerProperties.put("bootstrap.servers", KAFKA_CONTAINER.getBootstrapServers());
        consumerProperties.put("key.deserializer", StringDeserializer.class.getName());
        consumerProperties.put("value.deserializer", TestMessageDeserializer.class.getName());
        consumerProperties.put("group.id", "test");
        consumerProperties.put("auto.offset.reset", "earliest");
        return consumerProperties;
    }

    static List<ConsumerRecord<String, KpiExposureConfig>> getRecords() {
        try (final Consumer<String, KpiExposureConfig> consumer = new KafkaConsumer<>(getConsumerProperties())) {
            consumer.subscribe(Collections.singleton(EXPOSURE_TOPIC_NAME));

            final ConsumerRecords<String, KpiExposureConfig> consumerRecords = consumer.poll(Duration.ofSeconds(5));
            final List<ConsumerRecord<String, KpiExposureConfig>> records = new ArrayList<>();
            consumerRecords.forEach(records::add);
            return records;
        }
    }

    public static class TestMessageDeserializer implements Deserializer<KpiExposureConfig> {

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public KpiExposureConfig deserialize(final String s, final byte[] bytes) {
            try {
                return objectMapper.readValue(bytes, KpiExposureConfig.class);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
