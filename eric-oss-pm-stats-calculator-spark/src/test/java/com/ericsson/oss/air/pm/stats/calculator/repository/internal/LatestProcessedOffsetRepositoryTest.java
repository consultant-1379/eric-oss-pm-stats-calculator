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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.KpiExecutionGroup;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.LatestProcessedOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test-containers")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LatestProcessedOffsetRepositoryTest {

    private static final String EXECUTION_GROUP = "executionGroup1";
    private static final String TOPIC = "topic1";

    @Autowired LatestProcessedOffsetRepository objectUnderTest;
    @Autowired ExecutionGroupRepository executionGroupRepository;

    KpiExecutionGroup executionGroup;
    LatestProcessedOffset latestProcessedOffset;

    @BeforeEach
    void setUp() {
        executionGroup = executionGroupRepository.save(KpiExecutionGroup.builder().executionGroup(EXECUTION_GROUP).build());

        latestProcessedOffset = LatestProcessedOffset.builder()
                .withTopicName(TOPIC)
                .withTopicPartition(1)
                .withTopicPartitionOffset(10L)
                .withExecutionGroup(executionGroup)
                .withFromKafka(true)
                .build();
    }

    @Nested
    @DisplayName("Test find all for topic")
    class FindAllForTopic {

        LatestProcessedOffset latestProcessedOffset2;
        LatestProcessedOffset latestProcessedOffset3;
        LatestProcessedOffset latestProcessedOffset4;
        KpiExecutionGroup executionGroup2;
        String groupName = "another";

        @BeforeEach
        void setUp() {
            executionGroup2 = executionGroupRepository.save(KpiExecutionGroup.builder().executionGroup(groupName).build());

            latestProcessedOffset2 = LatestProcessedOffset.builder()
                    .withTopicName(TOPIC)
                    .withTopicPartition(2)
                    .withTopicPartitionOffset(10L)
                    .withExecutionGroup(executionGroup)
                    .withFromKafka(false)
                    .build();

            latestProcessedOffset3 = LatestProcessedOffset.builder()
                    .withTopicName(TOPIC)
                    .withTopicPartition(1)
                    .withTopicPartitionOffset(15L)
                    .withExecutionGroup(executionGroup2)
                    .withFromKafka(true)
                    .build();

            latestProcessedOffset4 = LatestProcessedOffset.builder()
                    .withTopicName("topic2")
                    .withTopicPartition(2)
                    .withTopicPartitionOffset(14L)
                    .withExecutionGroup(executionGroup)
                    .withFromKafka(true)
                    .build();

            objectUnderTest.save(latestProcessedOffset3);
            objectUnderTest.save(latestProcessedOffset4);
        }

        @Test
        void shouldFindAll() {
            objectUnderTest.save(latestProcessedOffset);
            objectUnderTest.save(latestProcessedOffset2);

            final List<LatestProcessedOffset> actual = objectUnderTest.findAllForTopic(TOPIC, EXECUTION_GROUP);

            assertThat(actual).hasSize(2).containsExactlyInAnyOrder(latestProcessedOffset, latestProcessedOffset2);
        }

        @Test
        void shouldNotFindAnything() {
            final List<LatestProcessedOffset> actual = objectUnderTest.findAllForTopic(TOPIC, EXECUTION_GROUP);

            assertThat(actual).isEmpty();
        }
    }

    @Nested
    @DisplayName("Test upsert latest processed offset")
    class UpsertLatestProcessedOffset{

        @Test
        void shouldInsert() {
            objectUnderTest.upsertLatestProcessedOffset(new LatestProcessedOffset(null, TOPIC, 1, 1L, true, executionGroup));

            assertThat(objectUnderTest.findAll()).hasSize(1);
        }

        @Test
        void shouldUpdate() {
            objectUnderTest.upsertLatestProcessedOffset(new LatestProcessedOffset(null, TOPIC, 1, 10L, true, executionGroup));
            objectUnderTest.upsertLatestProcessedOffset(new LatestProcessedOffset(null, TOPIC, 1, 100L, true, executionGroup));

            final List<LatestProcessedOffset> actual = objectUnderTest.findAll();

            assertThat(actual).first().extracting(LatestProcessedOffset::getTopicPartitionOffset).isEqualTo(100L);
        }
    }
}