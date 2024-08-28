/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.repository.internal;

import java.util.List;

import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.LatestProcessedOffset;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

public interface LatestProcessedOffsetRepository extends JpaRepository<LatestProcessedOffset, Integer> {

    @Query("SELECT latestProcessedOffset " +
            "FROM LatestProcessedOffset latestProcessedOffset " +
            "INNER JOIN latestProcessedOffset.executionGroup kpiExecutionGroup " +
            "WHERE latestProcessedOffset.topicName = :topicName " +
            "AND kpiExecutionGroup.executionGroup = :executionGroup")
    List<LatestProcessedOffset> findAllForTopic(
            @NonNull @Param("topicName") String topicName,
            @NonNull @Param("executionGroup") String executionGroup);

    @Modifying
    @Query(
            value = "INSERT INTO kpi.latest_processed_offsets (topic_name, topic_partition, topic_partition_offset, from_kafka, execution_group_id) " +
                    "VALUES ( " +
                    "    :#{#latestProcessedOffset.topicName}, " +
                    "    :#{#latestProcessedOffset.topicPartition}, " +
                    "    :#{#latestProcessedOffset.topicPartitionOffset}, " +
                    "    :#{#latestProcessedOffset.fromKafka}, " +
                    "    :#{#latestProcessedOffset.executionGroup.id}" +
                    ") " +
                    "ON CONFLICT ON CONSTRAINT unique_offset " +
                    "DO UPDATE SET " +
                    "   topic_name = :#{#latestProcessedOffset.topicName}, " +
                    "   topic_partition = :#{#latestProcessedOffset.topicPartition}, " +
                    "   topic_partition_offset = :#{#latestProcessedOffset.topicPartitionOffset}, " +
                    "   from_kafka = :#{#latestProcessedOffset.fromKafka}, " +
                    "   execution_group_id = :#{#latestProcessedOffset.executionGroup.id}",
            nativeQuery = true
    )
    void upsertLatestProcessedOffset(@Param("latestProcessedOffset") LatestProcessedOffset latestProcessedOffset);
}
