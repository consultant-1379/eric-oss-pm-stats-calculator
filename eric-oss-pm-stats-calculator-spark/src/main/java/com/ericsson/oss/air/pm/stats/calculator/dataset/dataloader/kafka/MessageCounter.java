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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.air.pm.stats.calculator.service.util.consumer.KafkaConsumerService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.Topic;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset.EndOffset;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset.StartOffset;
import com.ericsson.oss.air.pm.stats.calculator.util.CollectionHelpers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageCounter {
    private final KafkaConsumer<String, String> kafkaConsumer;

    private final KafkaConsumerService kafkaConsumerService;

    /**
     * Fetches the end offsets from the related <strong>Kafka</strong> {@link Topic}.
     * <br>
     * <strong>Cache</strong>:
     * <br>
     * Required to avoid infinite reading scenario when producer side is faster than the consumer.
     * On no cache in each iteration the service could recognise new messages to be processed creating infinite stream like behaviour.
     * <br>
     * Returned value is cached by {@link Topic#name()}.
     *
     * @param topic
     *         {@link Topic} to fetch end offsets for
     * @return {@link Map} of {@link TopicPartition} key and {@link EndOffset} value storing last offsets for the {@link Topic}
     * @see KafkaConsumer#endOffsets(Collection)
     */
    @Cacheable(key = "#topic.name()", cacheNames = "topic-partition-end-offset")
    public Map<TopicPartition, EndOffset> countMessagesByPartition(final Topic topic) {
        // TODO: Later in one execution group cache key must be broader since in one calculation we can read from multiple kafka servers
        //       <kafka_server_1>.<topic_1> This is ok
        //       <kafka_server_1>.<topic_2> This is ok
        //       <kafka_server_2>.<topic_1> This would read the cache stored for <kafka_server_1>.<topic_1> as <topic_1> is the cache key
        final List<TopicPartition> topicPartitions = kafkaConsumerService.loadTopicPartitions(topic);

        return Collections.unmodifiableMap(
                CollectionHelpers.transformValue(kafkaConsumer.endOffsets(topicPartitions), EndOffset::of)
        );
    }

    /**
     * Fetches the start offsets from the related <strong>Kafka</strong> {@link Topic}.
     *
     * Returned value is cached by {@link Topic#name()}.
     *
     * @param topic
     *         {@link Topic} to fetch beggining offsets for
     * @return {@link Map} of {@link TopicPartition} key and {@link StartOffset} value storing beginning offsets for the {@link Topic}
     * @see KafkaConsumer#beginningOffsets(Collection)
     */
    @Cacheable(key = "#topic.name()", cacheNames = "topic-partition-start-offset")
    public Map<TopicPartition, StartOffset> getStartOffsetForTopic(final Topic topic) {
        final List<TopicPartition> topicPartitions = kafkaConsumerService.loadTopicPartitions(topic);
        return Collections.unmodifiableMap(
            CollectionHelpers.transformValue(kafkaConsumer.beginningOffsets(topicPartitions), StartOffset::of)
        );
    }

}