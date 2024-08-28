/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka;

import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset.EndOffset;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset.StartOffset;

import com.google.common.base.Preconditions;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import lombok.experimental.Accessors;
import org.apache.kafka.common.TopicPartition;

@Accessors(fluent = true)
@Data(staticConstructor = "of")
public final class TopicPartitionOffsetDistance {
    private final Topic topic;
    private final Partition partition;
    private final OffsetDistance offsetDistance;

    public static TopicPartitionOffsetDistance of(final TopicPartition topicPartition,
                                                  final StartOffset startOffset,
                                                  final EndOffset endOffset) {
        return of(Topic.from(topicPartition), Partition.from(topicPartition), OffsetDistance.of(startOffset, endOffset));
    }

    /**
     * The {@link OffsetDistance} merges the information on offsets known in the database and on Kafka.
     * <br>
     * The {@link OffsetDistance#startOffset} is the latest offset from the database and the {@link OffsetDistance#endOffset} is the last offset
     * on Kafka.
     * <br>
     * The {@link OffsetDistance#distance()} is the number of the unread messages waiting to be processed.
     */
    @Data
    @With
    @Accessors(fluent = true)
    public static final class OffsetDistance {
        private final StartOffset startOffset;
        private final EndOffset endOffset;

        private OffsetDistance(@NonNull final StartOffset startOffset, @NonNull final EndOffset endOffset) {
            Preconditions.checkArgument(startOffset.number() <= endOffset.number(), "startOffset is greater than endOffset");

            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }

        public static OffsetDistance of(final StartOffset startOffset, final EndOffset endOffset) {
            return new OffsetDistance(startOffset, endOffset);
        }

        public OffsetDistance withCoveredDistance(final long coveredDistance) {
            final long start = startOffset.number();
            final long end = endOffset.number();

            Preconditions.checkArgument(
                    start + coveredDistance <= end,
                    "start offset('%s') plus covered distance('%s') is greater than the end offset('%s')",
                    start,
                    coveredDistance,
                    end
            );

            return withEndOffset(EndOffset.of(start + coveredDistance));
        }

        public long distance() {
            return endOffset.number() - startOffset.number();
        }
    }
}
