/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository;

import static com.ericsson.oss.air.pm.stats.repository._util.RepositoryHelpers.database;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats._util.AssertionHelpers;
import com.ericsson.oss.air.pm.stats.common.model.collection.CollectionIdProxy;
import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.LatestProcessedOffset;
import com.ericsson.oss.air.pm.stats.repository._util.RepositoryHelpers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;

class LatestProcessedOffsetsRepositoryImplTest {

    LatestProcessedOffsetsRepositoryImpl objectUnderTest = new LatestProcessedOffsetsRepositoryImpl();

    @Test
    void shouldThrowUncheckedSqlExceptionOnFindAll() {
        AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findAll());
    }

    @Test
    void shouldThrowUncheckedSqlExceptionOnFindForExecutionGroup() {
        AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findAllForExecutionGroup("topic"));
    }

    @Test
    void shouldThrowUncheckedSqlExceptionOnDelete() {
        AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.deleteOffsetsByTopic(List.of("topic")));
    }

    @Nested
    @DisplayName("Given an available database")
    class GivenAnAvailableDatabase {
        EmbeddedDatabase embeddedDatabase;

        @BeforeEach
        void init() {
            embeddedDatabase = database("sql/initialize_latest_processed_offsets.sql");
        }

        @AfterEach
        void tearDown() {
            embeddedDatabase.shutdown();
        }

        @Test
        void shouldFindAll() {
            RepositoryHelpers.prepare(embeddedDatabase, () -> {
                final List<LatestProcessedOffset> actual = objectUnderTest.findAll();

                assertThat(actual).containsExactlyInAnyOrder(
                        latestProcessedOffset(1, "topic1", 0, 1, "exe_group1", CollectionIdProxy.COLLECTION_ID),
                        latestProcessedOffset(2, "topic1", 1, 1, "exe_group1", CollectionIdProxy.COLLECTION_ID),
                        latestProcessedOffset(3, "topic2", 0, 2, "exe_group2", CollectionIdProxy.COLLECTION_ID),
                        latestProcessedOffset(4, "topic2", 1, 2, "exe_group2", CollectionIdProxy.COLLECTION_ID)
                );
            });
        }

        @Test
        void shouldFindAllForExecGroup() {
            RepositoryHelpers.prepare(embeddedDatabase, () -> {
                final List<LatestProcessedOffset> actual = objectUnderTest.findAllForExecutionGroup("exe_group1");

                assertThat(actual).containsExactlyInAnyOrder(
                        latestProcessedOffset(1, "topic1", 0, 1, "exe_group1", CollectionIdProxy.COLLECTION_ID),
                        latestProcessedOffset(2, "topic1", 1, 1, "exe_group1", CollectionIdProxy.COLLECTION_ID)
                );
            });
        }

        @Test
        void shouldDeleteForTopic() {
            RepositoryHelpers.prepare(embeddedDatabase, () -> {
                objectUnderTest.deleteOffsetsByTopic(List.of("topic1"));
            });
            //TODO: overload prepare to make it usable multiple times
            RepositoryHelpers.prepare(embeddedDatabase, () -> {
                final List<LatestProcessedOffset> actual = objectUnderTest.findAll();

                assertThat(actual).containsExactlyInAnyOrder(
                        latestProcessedOffset(3, "topic2", 0, 2, "exe_group2", CollectionIdProxy.COLLECTION_ID),
                        latestProcessedOffset(4, "topic2", 1, 2, "exe_group2", CollectionIdProxy.COLLECTION_ID)
                );
            });
        }
    }

    static LatestProcessedOffset latestProcessedOffset(final int id, final String topic, final int partition, final int exeId, final String executionName, final UUID collectionId) {
        final ExecutionGroup executionGroup = ExecutionGroup.builder().withId(exeId).withName(executionName).build();
        return LatestProcessedOffset.builder()
                .withId(id)
                .withTopicName(topic)
                .withTopicPartition(partition)
                .withTopicPartitionOffset(10L)
                .withFromKafka(false)
                .withExecutionGroup(executionGroup)
                .withCollectionId(collectionId)
                .build();
    }

}