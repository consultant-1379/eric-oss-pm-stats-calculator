/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.offset.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.LatestProcessedOffset;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.LatestProcessedOffsetRepository;
import com.ericsson.oss.air.pm.stats.calculator.service.KpiDefinitionServiceImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.LatestProcessedOffsetAdjuster;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.Topic;

import org.assertj.core.data.MapEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersistedOffsetCacheTest {

    public static final String EXECUTION_GROUP = "executionGroup";
    public static final String TOPIC = "topic";

    @Mock KpiDefinitionHelperImpl kpiDefinitionHelperMock;
    @Mock KpiDefinitionServiceImpl kpiDefinitionServiceMock;
    @Mock LatestProcessedOffsetAdjuster latestProcessedOffsetAdjusterMock;
    @Mock LatestProcessedOffsetRepository latestProcessedOffsetRepositoryMock;

    @InjectMocks PersistedOffsetCache objectUnderTest;

    KpiDefinition kpiDefinition;
    SchemaDetail schemaDetail;
    LatestProcessedOffset latestProcessedOffset;
    Set<KpiDefinition> kpiDefinitions = new HashSet<>();
    List<LatestProcessedOffset> latestProcessedOffsetList = new ArrayList<>();

    @BeforeEach
    void setUp() {
        schemaDetail = SchemaDetail.builder().withTopic(TOPIC).build();

        kpiDefinition = KpiDefinition.builder()
                .withExecutionGroup(EXECUTION_GROUP)
                .withSchemaDetail(schemaDetail)
                .build();

        kpiDefinitions.add(kpiDefinition);

        latestProcessedOffset = LatestProcessedOffset.builder()
                .withId(1)
                .withTopicName(TOPIC)
                .build();

        latestProcessedOffsetList.add(latestProcessedOffset);
    }

    @Test
    void shouldLoadPersistedOffsets() {
        when(kpiDefinitionServiceMock.loadDefinitionsToCalculate()).thenReturn(kpiDefinitions);
        when(kpiDefinitionHelperMock.extractExecutionGroup(kpiDefinitions)).thenReturn(EXECUTION_GROUP);
        when(kpiDefinitionHelperMock.extractSchemaDetails(kpiDefinitions)).thenReturn(Collections.singleton(schemaDetail));
        when(latestProcessedOffsetRepositoryMock.findAllForTopic(TOPIC, EXECUTION_GROUP)).thenReturn(latestProcessedOffsetList);
        when(latestProcessedOffsetAdjusterMock.adjustOffsets(latestProcessedOffsetList)).thenReturn(latestProcessedOffsetList);

        Map<Topic, List<LatestProcessedOffset>> actual = objectUnderTest.loadPersistedOffsets();

        verify(kpiDefinitionServiceMock).loadDefinitionsToCalculate();
        verify(kpiDefinitionHelperMock).extractExecutionGroup(kpiDefinitions);
        verify(kpiDefinitionHelperMock).extractSchemaDetails(kpiDefinitions);
        verify(latestProcessedOffsetRepositoryMock).findAllForTopic(TOPIC, EXECUTION_GROUP);
        verify(latestProcessedOffsetAdjusterMock).adjustOffsets(latestProcessedOffsetList);

        assertThat(actual).containsOnly(MapEntry.entry(Topic.of(TOPIC), latestProcessedOffsetList));
    }
}