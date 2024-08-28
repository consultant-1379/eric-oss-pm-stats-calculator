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

import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.LatestProcessedOffset;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.LatestProcessedOffsetRepository;
import com.ericsson.oss.air.pm.stats.calculator.service.util.consumer.KafkaConsumerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.Validate;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class LatestProcessedOffsetAdjuster {
    private final LatestProcessedOffsetRepository latestProcessedOffsetRepository;

    private final KafkaConsumer<String, String> kafkaConsumer;
    private final KafkaConsumerService kafkaConsumerService;

    /**
     * Adjust the provided {@link List} of {@link LatestProcessedOffset} by fixing the following problems:
     * <ul>
     *     <li>
     *         When the offset is out of range then it sets to the <strong>earliest</strong> available for the topic-partition
     *     </li>
     *     <li>
     *         When the partition referenced by the {@link LatestProcessedOffset} is no longer available then it drops
     *         the {@link LatestProcessedOffset}
     *      </li>
     *      <li>
     *          When new partition added for the topic then it creates an initialized {@link LatestProcessedOffset} reference
     *      </li>
     * </ul>
     *
     * @param latestProcessedOffsets {@link List} of {@link LatestProcessedOffset} to be adjusted
     * @return the adjusted {@link List} of {@link LatestProcessedOffset}
     */
    @Transactional
    @SuppressWarnings("squid:S3776")
    public List<LatestProcessedOffset> adjustOffsets(final List<LatestProcessedOffset> latestProcessedOffsets) {
        latestProcessedOffsets.stream().collect(
                groupingBy(LatestProcessedOffset::getTopicName, groupingBy(LatestProcessedOffset::getExecutionGroup))
        ).forEach((topic, executionGroups) -> {
            final List<TopicPartition> topicPartitions = kafkaConsumerService.loadTopicPartitions(topic);
            final Map<TopicPartition, Long> startOffsets = kafkaConsumer.beginningOffsets(topicPartitions);
            final Map<TopicPartition, Long> endOffsets = kafkaConsumer.endOffsets(topicPartitions);

            log.info("Fetched offsets for topic '{}':{}{}", topic, lineSeparator(), prettyPrint(startOffsets, endOffsets));

            executionGroups.forEach((executionGroup, executionGroupOffsets) -> {
                final HashMap<TopicPartition, Long> localStartOffsets = new HashMap<>(startOffsets);
                final HashMap<TopicPartition, Long> localEndOffsets = new HashMap<>(endOffsets);

                executionGroupOffsets.forEach(latestProcessedOffset -> {
                    final TopicPartition topicPartition = latestProcessedOffset.asTopicPartition();

                    final Long startOffset = localStartOffsets.remove(topicPartition);
                    final Long endOffset = localEndOffsets.remove(topicPartition);
                    //The saved offset is the last read by spark, in this iteration we will need to start from +1 hence the increment in case of offset not from kafka
                    //TODO: rethink the whole fromKafka part, as current implementations saves all as not fromKafka, with a -1 in case it was
                    final Long currentRead = latestProcessedOffset.getTopicPartitionOffset();
                    final long actualOffset = latestProcessedOffset.doesNeedIncrement() ? (currentRead + 1) : currentRead;

                    if (startOffset == null || endOffset == null) {
                        //  Topic-partition was removed from Kafka - we need to delete it from our database as well
                        logDataLoss();
                        log.warn("{} does not exist anymore, removed from the collection and database", latestProcessedOffset);

                        latestProcessedOffsets.remove(latestProcessedOffset);
                        latestProcessedOffsetRepository.delete(latestProcessedOffset);
                        return;
                    }

                    if (startOffset <= actualOffset && actualOffset <= endOffset) {
                        log.info("{} is valid for the fetched offsets [{} - {}]", latestProcessedOffset, startOffset, endOffset);
                        return;
                    }


                    //  Offset is out of range at this point - we need to update it
                    if (actualOffset < startOffset) {
                        logDataLoss();
                    }
                    log.warn("{} is not actual anymore, updating the value for the currently known start offset '{}'", latestProcessedOffset, startOffset);
                    latestProcessedOffset.setTopicPartitionOffset(startOffset);
                    latestProcessedOffset.setFromKafka(true);
                    latestProcessedOffsetRepository.save(latestProcessedOffset);
                });

                if (MapUtils.isNotEmpty(localStartOffsets) || MapUtils.isNotEmpty(localEndOffsets)) {
                    // Offsets are not exhausted for topic-executionGroup that means we discovered a new partition that has not been maintained
                    // by the Calculator. We have to create a LatestProcessedOffset record for it

                    validateEqualSize(localStartOffsets, localEndOffsets);

                    localStartOffsets.forEach((topicPartition, startOffset) -> {
                        final LatestProcessedOffset latestProcessedOffset = latestProcessedOffsetRepository.save(new LatestProcessedOffset(
                                null, topicPartition.topic(), topicPartition.partition(), startOffset, false, executionGroup
                        ));

                        log.info("{} has been created part of the new partition discovery", latestProcessedOffset);

                        latestProcessedOffsets.add(latestProcessedOffset);
                    });
                }
            });
        });

        return latestProcessedOffsets;
    }

    private String prettyPrint(final Map<TopicPartition, Long> startOffsets, final Map<TopicPartition, Long> endOffsets) {
        validateEqualSize(startOffsets, endOffsets);

        final Map<TopicPartition, Range<Long>> rangedOffsets = new TreeMap<>(
                comparing(TopicPartition::topic).thenComparingInt(TopicPartition::partition)
        );

        startOffsets.forEach((topicPartition, startOffset) -> {
            final Long endOffset = endOffsets.get(topicPartition);
            rangedOffsets.put(topicPartition, Range.between(startOffset, endOffset));
        });

        try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             final PrintStream printStream = new PrintStream(byteArrayOutputStream, false, UTF_8)) {

            MapUtils.verbosePrint(printStream, null, rangedOffsets);

            return byteArrayOutputStream.toString(UTF_8);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void validateEqualSize(final Map<TopicPartition, Long> startOffsets, final Map<TopicPartition, Long> endOffsets) {
        Validate.isTrue(startOffsets.size() == endOffsets.size(), "'startOffsets' has different size than 'endOffsets'");
    }

    private static void logDataLoss() {
        log.error("Processed Kafka offsets indicate lost input data at system level! KPI results may be corrupted.");
    }

}
