/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.offset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.List;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.KpiExecutionGroup;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.LatestProcessedOffset;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.LatestProcessedOffsetRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OffsetPersistencyKafkaTest {

    static final String TOPIC = "topic";
    static final String EXECUTION_GROUP = "executionGroup";

    @Mock LatestProcessedOffsetRepository latestProcessedOffsetRepositoryMock;
    @InjectMocks OffsetPersistencyKafka objectUnderTest;

    LatestProcessedOffset latestProcessedOffset1;
    KpiExecutionGroup executionGroup;

    @BeforeEach
    void setUp() {

        executionGroup = KpiExecutionGroup.builder()
                .executionGroup(EXECUTION_GROUP)
                .build();

        latestProcessedOffset1 = LatestProcessedOffset.builder()
                .withTopicName(TOPIC)
                .withTopicPartition(1)
                .withTopicPartitionOffset(5L)
                .withFromKafka(true)
                .withExecutionGroup(executionGroup)
                .build();
    }

    @Test
    void shouldCallUpsertLatestProcessedOffset(){
        objectUnderTest.upsertOffset(latestProcessedOffset1);

        verify(latestProcessedOffsetRepositoryMock).upsertLatestProcessedOffset(latestProcessedOffset1);
    }

    @Test
    void shouldSupportSimpleDefinitions() {
        final List<KpiDefinition> simpleDefinitions = List.of(KpiDefinition.builder().withInpDataIdentifier(DataIdentifier.of("identifier_1")).build());

        final boolean actual = objectUnderTest.supports(simpleDefinitions);

        assertThat(actual).isTrue();
    }

    @Test
    void shouldNotSupportNotSimpleDefinitions() {
        final List<KpiDefinition> complexDefinitions = List.of(KpiDefinition.builder().withInpDataIdentifier(DataIdentifier.of(null)).build());

        final boolean actual = objectUnderTest.supports(complexDefinitions);

        assertThat(actual).isFalse();
    }
}
