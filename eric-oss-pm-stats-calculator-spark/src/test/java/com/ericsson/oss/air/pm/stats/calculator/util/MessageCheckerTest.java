/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.kafka.MessageCounter;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.LatestProcessedOffset;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.LatestProcessedOffsetRepository;
import com.ericsson.oss.air.pm.stats.calculator.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.Topic;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset.EndOffset;

import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

@ExtendWith(MockitoExtension.class)
class MessageCheckerTest {

    @Mock KpiDefinitionService kpiDefinitionServiceMock;
    @Mock KpiDefinitionHelperImpl kpiDefinitionHelperMock;
    @Mock MessageCounter messageCounterMock;
    @Mock LatestProcessedOffsetRepository latestProcessedOffsetRepositoryMock;

    @InjectMocks MessageChecker objectUnderTest;

    @Mock Set<KpiDefinition> definitionsMock;

    @Test
    void whenDefinitionsIsEmpty() {
        when(kpiDefinitionServiceMock.loadDefinitionsToCalculate()).thenReturn(Collections.emptySet());

        final boolean actual = objectUnderTest.hasNewMessage();

        verify(kpiDefinitionServiceMock).loadDefinitionsToCalculate();
        verifyNoInteractions(kpiDefinitionHelperMock, messageCounterMock, latestProcessedOffsetRepositoryMock);

        assertThat(actual).isFalse();
    }

    @ParameterizedTest
    @MethodSource("provideHasMessageData")
    void asd(final Map<TopicPartition, EndOffset> kafkaData, final List<LatestProcessedOffset> processed, final boolean expected) {
        when(kpiDefinitionServiceMock.loadDefinitionsToCalculate()).thenReturn(definitionsMock);
        when(kpiDefinitionHelperMock.extractSchemaDetails(definitionsMock)).thenReturn(Set.of(SchemaDetail.builder().withTopic("topic").build()));
        when(kpiDefinitionHelperMock.extractExecutionGroup(definitionsMock)).thenReturn("executionGroup");
        when(messageCounterMock.countMessagesByPartition(Topic.of("topic"))).thenReturn(kafkaData);
        when(latestProcessedOffsetRepositoryMock.findAllForTopic("topic","executionGroup")).thenReturn(processed);

        final boolean actual = objectUnderTest.hasNewMessage();

        verify(kpiDefinitionServiceMock).loadDefinitionsToCalculate();
        verify(kpiDefinitionHelperMock).extractSchemaDetails(definitionsMock);
        verify(kpiDefinitionHelperMock).extractExecutionGroup(definitionsMock);
        verify(messageCounterMock).countMessagesByPartition(Topic.of("topic"));
        verify(latestProcessedOffsetRepositoryMock).findAllForTopic("topic","executionGroup");

        assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> provideHasMessageData() {
        return Stream.of(
                Arguments.of(
                        ImmutableMap.of(                    //Kafka has 13 message (Reminder: end offset is +1 than actual), processed has 13
                                topicPartition(0), endOffset(5),
                                topicPartition(1), endOffset(10)
                        ),
                        List.of(
                                processedOffset(0, 4),
                                processedOffset(1, 9)
                        ),
                        false
                ),
                Arguments.of(                                           //Kafka has 18 message, processed 15
                        ImmutableMap.of(
                                topicPartition(0), endOffset(10),
                                topicPartition(1), endOffset(10)
                        ),
                        List.of(
                                processedOffset(0, 5),
                                processedOffset(1, 10)
                        ),
                        true
                ),
                Arguments.of(
                        ImmutableMap.of(                    //Kafka has 13 message, processed is 20
                                topicPartition(0), endOffset(5),
                                topicPartition(1), endOffset(10)
                        ),
                        List.of(
                                processedOffset(0, 10),
                                processedOffset(1, 10)
                        ),
                        true
                ),
                Arguments.of(
                        ImmutableMap.of(                    //Kafka has 360 message, processed is 360
                                topicPartition(0), endOffset(0),
                                topicPartition(1), endOffset(0),
                                topicPartition(2), endOffset(360)
                        ),
                        List.of(
                                processedOffset(0, 0),
                                processedOffset(1, 0),
                                processedOffset(2, 359)
                        ),
                        false
                ),
                Arguments.of(
                        ImmutableMap.of(                    //Kafka has 360 message, processed is 360
                                topicPartition(0), endOffset(0),
                                topicPartition(1), endOffset(0),
                                topicPartition(2), endOffset(360)
                        ),
                        List.of(
                                processedOffset(0, -1),
                                processedOffset(1, -1),
                                processedOffset(2, 359)
                        ),
                        false
                )
        );
    }

    static EndOffset endOffset(final int offset) {
        return EndOffset.of(offset);
    }

    static TopicPartition topicPartition(final int partition) {
        return new TopicPartition("kafka-topic", partition);
    }

    static LatestProcessedOffset processedOffset(final int partition, final long offset) {
        return LatestProcessedOffset.builder().withTopicPartition(partition).withTopicPartitionOffset(offset).build();
    }
}