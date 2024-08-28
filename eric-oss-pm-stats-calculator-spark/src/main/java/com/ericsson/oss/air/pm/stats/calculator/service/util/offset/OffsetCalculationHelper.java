/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.offset;

import java.util.List;
import java.util.Map;

import com.ericsson.oss.air.pm.stats.calculator.configuration.property.CalculationProperties;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.kafka.MessageCounter;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.Topic;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.TopicPartitionOffsetDistance;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.TopicPartitionOffsetDistance.OffsetDistance;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset.EndOffset;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.serializer.TopicPartitionOffsetDeserializer;
import com.ericsson.oss.air.pm.stats.calculator.util.CollectionHelpers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OffsetCalculationHelper {
    private final CalculationProperties calculationProperties;
    private final MessageCounter messageCounter;
    private final TopicPartitionOffsetDeserializer topicPartitionOffsetDeserializer;

    public boolean isLastRead(final Topic topic, final String endingOffset) {
        final long totalMessages = sumExactMessages(messageCounter.countMessagesByPartition(topic));
        final long processedMessages = sumExactMessages(topicPartitionOffsetDeserializer.deserialize(endingOffset));

        return totalMessages <= processedMessages;
    }

    public boolean isNoTooMuchData(@NonNull final List<TopicPartitionOffsetDistance> topicPartitionOffsetDistances) {
        return remainingDistances(topicPartitionOffsetDistances) <= calculationProperties.getKafkaBucketSize();
    }

    private static long sumExactMessages(@NonNull final Map<TopicPartition, EndOffset> topicPartitionEndOffset) {
        return CollectionHelpers.sumExact(topicPartitionEndOffset.values().stream().map(EndOffset::number));
    }

    private static long remainingDistances(final @NonNull List<TopicPartitionOffsetDistance> topicPartitionOffsetDistances) {
        return topicPartitionOffsetDistances.stream().map(TopicPartitionOffsetDistance::offsetDistance).mapToLong(OffsetDistance::distance).sum();
    }

}