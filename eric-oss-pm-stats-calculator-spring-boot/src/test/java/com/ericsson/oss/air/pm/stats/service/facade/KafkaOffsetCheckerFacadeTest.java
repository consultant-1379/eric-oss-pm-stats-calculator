/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.facade;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.model.entity.LatestProcessedOffset;
import com.ericsson.oss.air.pm.stats.repository.api.LatestProcessedOffsetsRepository;
import com.ericsson.oss.air.pm.stats.service.helper.KafkaReader;

import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KafkaOffsetCheckerFacadeTest {

    @Mock
    KafkaReader kafkaReaderMock;
    @Mock
    LatestProcessedOffsetsRepository latestProcessedOffsetsRepositoryMock;

    @InjectMocks
    KafkaOffsetCheckerFacade objectUnderTest;

    static final String executionGroup = "executionGroup";
    static final String topicName1 = "kafka-topic1";
    static final String topicName2 = "kafka-topic2";

    @Test
    void shouldNotDeleteAnythingWhileKafkaOffsetIsBigger() {
        when(latestProcessedOffsetsRepositoryMock.findAll()).thenReturn(List.of(latestProcessedOffset()));
        when(kafkaReaderMock.gatherEndOffsetsForTopic(topicName1)).thenReturn(Map.of(new TopicPartition(topicName1, 0), 15L));

        objectUnderTest.compareDatabaseToKafka();

        verify(latestProcessedOffsetsRepositoryMock).findAll();
        verify(kafkaReaderMock).gatherEndOffsetsForTopic(topicName1);
        verify(latestProcessedOffsetsRepositoryMock).deleteOffsetsByTopic(Set.of());
    }

    @Test
    void shouldDeleteWhenKafkaOffsetIsLower() {
        when(latestProcessedOffsetsRepositoryMock.findAll()).thenReturn(List.of(latestProcessedOffset()));
        when(kafkaReaderMock.gatherEndOffsetsForTopic(topicName1)).thenReturn(Map.of(new TopicPartition(topicName1, 0), 0L));

        objectUnderTest.compareDatabaseToKafka();

        verify(latestProcessedOffsetsRepositoryMock).findAll();
        verify(kafkaReaderMock).gatherEndOffsetsForTopic(topicName1);
        verify(latestProcessedOffsetsRepositoryMock).deleteOffsetsByTopic(Set.of(topicName1));
    }

    @Test
    void verifyHasNewMessage_whenProcessedOffsetsAreNull() {
        when(latestProcessedOffsetsRepositoryMock.findAllForExecutionGroup(executionGroup)).thenReturn(new ArrayList<>());

        final boolean actual = objectUnderTest.hasNewMessage(executionGroup);

        verify(latestProcessedOffsetsRepositoryMock).findAllForExecutionGroup(executionGroup);

        assertThat(actual).isTrue();
    }

    @Test
    void verifyHasNewMessage_whenFirstAnalyzedTopicHasNewMessage() {
        //Kafka has:         Any message on topic kafka-topic1 and 11 message on topic kafka-topic2
        //processed has:     11  message on topic kafka-topic1 and 20 message on topic kafka-topic2
        final Map<TopicPartition, Long> kafkaDataTopic2 = Map.of(
                topicPartition(topicName2, 0), 3L,
                topicPartition(topicName2, 1), 10L
        );

        final List<LatestProcessedOffset> processed = List.of(
                processedOffset(topicName1, 0, 4),
                processedOffset(topicName1, 1, 7),
                processedOffset(topicName2, 0, 5),
                processedOffset(topicName2, 1, 15)
        );

        when(latestProcessedOffsetsRepositoryMock.findAllForExecutionGroup(executionGroup)).thenReturn(processed);
        when(kafkaReaderMock.gatherEndOffsetsForTopic(topicName2)).thenReturn(kafkaDataTopic2);

        final boolean actual = objectUnderTest.hasNewMessage(executionGroup);

        verify(latestProcessedOffsetsRepositoryMock).findAllForExecutionGroup(executionGroup);
        verify(kafkaReaderMock, never()).gatherEndOffsetsForTopic(topicName1);
        verify(kafkaReaderMock).gatherEndOffsetsForTopic(topicName2);

        assertThat(actual).isTrue();
    }

    @Test
    void verifyHasNoNewMessageOnKafkaForTopic() {
        when(latestProcessedOffsetsRepositoryMock.findAllForExecutionGroup(executionGroup)).thenReturn(List.of(
                processedOffset(topicName1, 0, 0),
                processedOffset(topicName1, 1, 0),
                processedOffset(topicName1, 2, 359)
        ));
        when(kafkaReaderMock.gatherEndOffsetsForTopic(topicName1)).thenReturn(Map.of(
                topicPartition(topicName1, 0), 0L,
                topicPartition(topicName1, 1), 0L,
                topicPartition(topicName1, 2), 360L
        ));

        final boolean actual = objectUnderTest.hasNewMessage(executionGroup);

        verify(latestProcessedOffsetsRepositoryMock).findAllForExecutionGroup(executionGroup);
        verify(kafkaReaderMock).gatherEndOffsetsForTopic(topicName1);

        assertThat(actual).isFalse();
    }

    @Test
    void verifyHasNoNewMessageOnKafkaForTopic_whenOffsetsAreNegative() {
        when(latestProcessedOffsetsRepositoryMock.findAllForExecutionGroup(executionGroup)).thenReturn(List.of(
                processedOffset(topicName1, 0, -1),
                processedOffset(topicName1, 1, -1),
                processedOffset(topicName1, 2, 359)
        ));
        when(kafkaReaderMock.gatherEndOffsetsForTopic(topicName1)).thenReturn(Map.of(
                topicPartition(topicName1, 0), 0L,
                topicPartition(topicName1, 1), 0L,
                topicPartition(topicName1, 2), 360L
        ));

        final boolean actual = objectUnderTest.hasNewMessage(executionGroup);

        verify(latestProcessedOffsetsRepositoryMock).findAllForExecutionGroup(executionGroup);
        verify(kafkaReaderMock).gatherEndOffsetsForTopic(topicName1);

        assertThat(actual).isFalse();
    }

    @ParameterizedTest
    @MethodSource("provideHasMessageData")
    void verifyHasNewMessage_whenFirstAnalyzedTopicHasNoNewMessage(final Map<TopicPartition, Long> kafkaData1,
                                                                   final Map<TopicPartition, Long> kafkaData2,
                                                                   final List<LatestProcessedOffset> processed,
                                                                   final boolean expected) {
        when(latestProcessedOffsetsRepositoryMock.findAllForExecutionGroup(executionGroup)).thenReturn(processed);
        when(kafkaReaderMock.gatherEndOffsetsForTopic(topicName1)).thenReturn(kafkaData1);
        when(kafkaReaderMock.gatherEndOffsetsForTopic(topicName2)).thenReturn(kafkaData2);

        final boolean actual = objectUnderTest.hasNewMessage(executionGroup);

        verify(latestProcessedOffsetsRepositoryMock).findAllForExecutionGroup(executionGroup);
        verify(kafkaReaderMock).gatherEndOffsetsForTopic(topicName1);
        verify(kafkaReaderMock).gatherEndOffsetsForTopic(topicName2);

        assertThat(actual).isEqualTo(expected);
    }

    static LatestProcessedOffset latestProcessedOffset() {
        return LatestProcessedOffset.builder().withTopicPartition(0).withTopicName(topicName1).withTopicPartitionOffset(10L).build();
    }

    static Stream<Arguments> provideHasMessageData() {
        return Stream.of(
                Arguments.of(
                        //Kafka has:         13 message on topic kafka-topic1 and 9 message on topic kafka-topic2
                        //processed has:     13 message on topic kafka-topic1 and 9 message on topic kafka-topic2
                        Map.of(
                                topicPartition(topicName1, 0), 5L,
                                topicPartition(topicName1, 1), 10L
                        ),
                        Map.of(
                                topicPartition(topicName2, 0), 8L,
                                topicPartition(topicName2, 1), 3L
                        ),
                        List.of(
                                processedOffset(topicName1, 0, 4),
                                processedOffset(topicName1, 1, 9),
                                processedOffset(topicName2, 0, 7),
                                processedOffset(topicName2, 1, 2)
                        ),
                        false
                ),
                Arguments.of(
                        //Kafka has:         15 message on topic kafka-topic1 and 7 message on topic kafka-topic2
                        //processed has:     10 message on topic kafka-topic1 and 7 message on topic kafka-topic2
                        Map.of(
                                topicPartition(topicName1, 0), 6L,
                                topicPartition(topicName1, 1), 11L
                        ),
                        Map.of(
                                topicPartition(topicName2, 0), 2L,
                                topicPartition(topicName2, 1), 7L
                        ),
                        List.of(
                                processedOffset(topicName1, 0, 5),
                                processedOffset(topicName1, 1, 5),
                                processedOffset(topicName2, 0, 1),
                                processedOffset(topicName2, 1, 6)
                        ),
                        true
                )
        );
    }

    static TopicPartition topicPartition(final String topicName, final int partition) {
        return new TopicPartition(topicName, partition);
    }

    static LatestProcessedOffset processedOffset(final String topicName, final int partition, final long offset) {
        return LatestProcessedOffset.builder()
                .withTopicPartition(partition)
                .withTopicPartitionOffset(offset)
                .withTopicName(topicName)
                .build();
    }
}