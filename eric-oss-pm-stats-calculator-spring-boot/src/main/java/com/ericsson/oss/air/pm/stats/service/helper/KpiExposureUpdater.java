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

import static com.google.common.util.concurrent.Uninterruptibles.getUninterruptibly;
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import com.ericsson.oss.air.pm.stats.calculator.api.dto.kafka.query.service.KpiExposureConfig;
import com.ericsson.oss.air.pm.stats.calculator.api.dto.kafka.query.service.KpiTableExposureConfig;
import com.ericsson.oss.air.pm.stats.calculator.api.dto.kafka.query.service.KpiTableExposureConfig.Type;
import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiCalculatorErrorCode;
import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiCalculatorException;
import com.ericsson.oss.air.pm.stats.calculator.configuration.CalculatorProperties;
import com.ericsson.oss.air.pm.stats.repository.api.DatabaseRepository;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class KpiExposureUpdater {

    private DatabaseRepository databaseRepository;
    private CalculatorProperties calculatorProperties;
    private Producer<String, KpiExposureConfig> kafkaProducer;

    public void updateVisibility(final String schema, final String name, final Type type, final boolean visible) {
        log.info("Updating exposure for {} '{}', set to {}", type, name, visible);

        sendExposureInfo(schema, configs -> {
            final KpiTableExposureConfig config = new KpiTableExposureConfig(name, type);
            if (visible) {
                final boolean add = configs.add(config);
                log.info("'{}' has been added '{}'", config, add);
            } else {
                final boolean remove = configs.remove(config);
                log.info("'{}' has been removed '{}'", config, remove);
            }
        });
    }

    public void updateExposureInfo(final String schema) {
        log.info("Updating exposure of tables");

        sendExposureInfo(schema, configs -> {
        });
    }

    private void sendExposureInfo(final String schema, @NonNull final Consumer<Set<KpiTableExposureConfig>> configConsumer) {
        final List<String> tableNames = databaseRepository.findAllOutputTablesWithoutPartition();
        final Set<KpiTableExposureConfig> exposedConfiguration = tableNames.stream().map(KpiTableExposureConfig::ofTable).collect(toSet());

        configConsumer.accept(exposedConfiguration);

        if (exposedConfiguration.isEmpty()) {
            //Send tombstone message
            sendMessage(schema, null);
        } else {
            sendMessage(schema, new KpiExposureConfig(System.currentTimeMillis(), exposedConfiguration));
        }
    }

    private void sendMessage(final String schema, final KpiExposureConfig exposureConfig) {
        final ProducerRecord<String, KpiExposureConfig> producerRecord = new ProducerRecord<>(calculatorProperties.getKafkaExposureTopicName(), schema, exposureConfig);
        try {
            final RecordMetadata recordMetadata = getUninterruptibly(kafkaProducer.send(producerRecord), 10, TimeUnit.SECONDS);
            log.info("Message sent for '{}' topic with offset '{}'", schema, recordMetadata.offset());
        } catch (final ExecutionException | TimeoutException | RuntimeException e) {
            throw new KpiCalculatorException(KpiCalculatorErrorCode.KPI_EXTERNAL_DATASOURCE_ERROR, e);
        }
    }
}
