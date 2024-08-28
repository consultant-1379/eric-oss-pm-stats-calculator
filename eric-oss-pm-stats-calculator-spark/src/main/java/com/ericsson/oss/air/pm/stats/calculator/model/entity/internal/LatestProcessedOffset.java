/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.model.entity.internal;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.ericsson.oss.air.pm.stats.calculator.api.annotation.Generated;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.kafka.common.TopicPartition;
import org.hibernate.Hibernate;

@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder (setterPrefix = "with")
@Table(name = "latest_processed_offsets", schema = "kpi",
        uniqueConstraints = {@UniqueConstraint(
                name = "unique_offset",
                columnNames = {"topic_name", "topic_partition", "execution_group_id"})
})
public class LatestProcessedOffset implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "topic_name", nullable = false)
    private String topicName;

    @Column(name = "topic_partition", nullable = false)
    private Integer topicPartition;

    @Column(name = "topic_partition_offset", nullable = false)
    private Long topicPartitionOffset;

    @Column(name = "from_kafka", nullable = false)
    private Boolean fromKafka;

    @ManyToOne
    @JoinColumn(name = "execution_group_id")
    private KpiExecutionGroup executionGroup;

    public TopicPartition asTopicPartition() {
        return new TopicPartition(topicName, topicPartition);
    }

    /**
     * The last read offset gets persisted/cached that is <strong>INCLUSIVE</strong>.
     * <strong>Kafka</strong> reads inclusively for start and exclusively for end, so the offset needs to be increased to start from an un-read offset.
     * <ol>
     *     <li>
     *         <strong>ITERATION</strong>:
     *         [10 - 19] - Cached/persisted offsets (next iteration would read 19, but 19 has already been processed)
     *     </li>
     *     <li>
     *         <strong>ITERATION</strong>:
     *         [20 - 29] - Cached/persisted offsets (start offset is the end offset of the previous iteration, needs to be adjusted)
     *     </li>
     * </ol>
     * <strong>NOTE</strong>: In <strong>Spark</strong> the 1. <strong>ITERATION</strong> is represented as [10 - 20) but read as [10 - 19]
     *
     * @return true if the {@link #topicPartitionOffset} needs to be incremented otherwise false
     */
    public boolean doesNeedIncrement() {
        return !fromKafka;
    }

    public void incrementTopicPartitionOffset() {
        topicPartitionOffset += 1;
    }

    @Override
    @Generated
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !Objects.equals(Hibernate.getClass(this), Hibernate.getClass(obj))) {
            return false;
        }
        final LatestProcessedOffset that = (LatestProcessedOffset) obj;
        return id != null && id.equals(that.id);
    }

    @Override
    @Generated
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    @Generated
    public String toString() {
        return String.format(
                "%s(id = '%d', executionGroup = '%s', topicPartition = '%s', offset = '%d', fromKafka = '%s')",
                getClass().getSimpleName(), id,
                executionGroup == null ? "null" : executionGroup.getExecutionGroup(),
                asTopicPartition(), topicPartitionOffset, fromKafka
        );
    }
}

