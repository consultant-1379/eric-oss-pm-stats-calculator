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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.kafka.MessageCounter;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.LatestProcessedOffset;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.LatestProcessedOffset.LatestProcessedOffsetBuilder;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.bucket.OffsetBucketCalculator;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.cache.facade.OffsetCacheFacade;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.cache.model.TopicAggregationPeriod;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.distance.OffsetDistanceCalculator;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.Partition;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.Topic;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.TopicPartitionOffsetDistance;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.TopicPartitionOffsetDistance.OffsetDistance;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset.EndOffset;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.serializer.TopicPartitionOffsetDeserializer;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.serializer.TopicPartitionOffsetSerializer;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.common.TopicPartition;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OffsetCalculatorFacadeImpl {
    private static final String OFFSET = "offset";
    private static final String PARTITION = "partition";

    private final MessageCounter messageCounter;
    private final OffsetBucketCalculator offsetBucketCalculator;
    private final OffsetCacheFacade offsetCacheFacade;
    private final OffsetCalculationHelper offsetCalculationHelper;
    private final OffsetDistanceCalculator offsetDistanceCalculator;

    private final TopicPartitionOffsetSerializer topicPartitionOffsetSerializer;
    private final TopicPartitionOffsetDeserializer topicPartitionOffsetDeserializer;

    public void generateOffsetsListAndStore(final Dataset<Row> dataset, final String topic, final Integer aggregationPeriod) {
        final List<LatestProcessedOffset> latestProcessedOffsets = new ArrayList<>();
        final List<Row> offsets = dataset.groupBy(functions.col(PARTITION).as(PARTITION))
                                         .agg(functions.max(OFFSET).as(OFFSET)) /* Reads inclusively ==> the last processed offset is read */
                                         .collectAsList();

        for (final Row row : offsets) {
            latestProcessedOffsets.add(latestProcessedOffset(topic, row.getAs(PARTITION), row.getAs(OFFSET)));
        }

        offsetCacheFacade.cache(TopicAggregationPeriod.of(topic, aggregationPeriod), latestProcessedOffsets);
    }

    public void generateOffsetsListAndStore(final String topic, final Integer aggregationPeriod, final String endOffsets) {
        final List<LatestProcessedOffset> latestProcessedOffsets = new ArrayList<>();

        //  Spark reads end offsets exclusively meaning if we define the following end offsets:
        //  "topic1":  "0": 63, "1": 64, "2": 73
        //  we will technically read the following end offsets
        //             "0": 62; "1": 63; "2": 72
        //  In order to match with this offset management we have to subtract one from the end offset JSON

        topicPartitionOffsetDeserializer.deserialize(endOffsets).forEach((topicPartition, endOffset) -> {
            final int partition = topicPartition.partition();
            final long offset = endOffset.number() - 1;
            latestProcessedOffsets.add(latestProcessedOffset(topic, partition, offset));
        });

        offsetCacheFacade.cache(TopicAggregationPeriod.of(topic, aggregationPeriod), latestProcessedOffsets);
    }

    public String defineStartingOffset(final Topic topic, final Integer aggregationPeriod) {
        final List<LatestProcessedOffset> latestProcessedOffsets = offsetCacheFacade.determineStartingOffset(topic, aggregationPeriod);
        return CollectionUtils.isEmpty(latestProcessedOffsets)
                ? "earliest"
                : topicPartitionOffsetSerializer.serializeLatestProcessedOffset(topic, latestProcessedOffsets);
    }

    public String defineEndOffset(final Topic topic, final Integer aggregationPeriod) {
        final Map<TopicPartition, EndOffset> topicPartitionEndOffset = messageCounter.countMessagesByPartition(topic);
        final List<TopicPartitionOffsetDistance> topicPartitionOffsetDistances = offsetDistanceCalculator.calculateOffsetDistances(
                offsetCacheFacade.determineStartingOffset(topic, aggregationPeriod),
                topicPartitionEndOffset
        );

        if (offsetCalculationHelper.isNoTooMuchData(topicPartitionOffsetDistances)) {
            return topicPartitionOffsetSerializer.serializeTopicPartitionEndOffset(topic, topicPartitionEndOffset);
        }

        final Map<Partition, OffsetDistance> nextBucket = offsetBucketCalculator.calculateNextBucket(topicPartitionOffsetDistances);
        return topicPartitionOffsetSerializer.serializePartitionOffsetDistance(topic, nextBucket);
    }

    private static LatestProcessedOffset latestProcessedOffset(final String topic, final int partition, final long topicPartitionOffset) {
        final LatestProcessedOffsetBuilder builder = LatestProcessedOffset.builder();
        builder.withTopicName(topic);
        builder.withTopicPartition(partition);
        builder.withTopicPartitionOffset(topicPartitionOffset);
        builder.withFromKafka(false);
        return builder.build();
    }
}
