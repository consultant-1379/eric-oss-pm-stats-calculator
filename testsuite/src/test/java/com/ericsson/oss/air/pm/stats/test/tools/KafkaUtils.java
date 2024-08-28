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

import static java.util.concurrent.TimeUnit.SECONDS;
import static lombok.AccessLevel.PRIVATE;

import java.util.Map;
import java.util.function.IntUnaryOperator;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreatePartitionsResult;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.assertj.core.api.Assertions;

@Slf4j
@NoArgsConstructor(access = PRIVATE)
public final class KafkaUtils {

    public static int partitions(final String topic) {
        try (final KafkaConsumer<String, String> consumer = KafkaFactories.kafkaConsumer()) {
            return consumer.partitionsFor(topic).size();
        }
    }

    @SneakyThrows
    public static void increasePartition(final String topic, final IntUnaryOperator targetOffsetFunction) {
        try (final AdminClient adminClient = KafkaFactories.adminClient()) {
            final int originalPartitions = partitions(topic);
            final int targetPartitions = targetOffsetFunction.applyAsInt(originalPartitions);

            final CreatePartitionsResult result = adminClient.createPartitions(Map.of(
                    topic, NewPartitions.increaseTo(targetPartitions)
            ));

            result.all().get(10, SECONDS);

            if (result.all().isDone()) {
                log.info("Partition number increased from '{}' to '{}' for topic '{}'", originalPartitions, targetPartitions, topic);
            } else {
                Assertions.fail("Could not increase partition for '%s' to the number of '%d from '%d'", topic, targetPartitions, originalPartitions);
            }
        }
    }

}
