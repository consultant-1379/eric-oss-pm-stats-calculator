/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.offset;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.dataset.offset.OffsetPersistencyKafka;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.KpiExecutionGroup;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.LatestProcessedOffset;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.ExecutionGroupRepository;
import com.ericsson.oss.air.pm.stats.calculator.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.cache.facade.OffsetCacheFacade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OffsetHandlerKafkaTest {

    private static final String EXECUTION_GROUP = "ExecutionGroup";

    @Mock KpiDefinitionHelperImpl kpiDefinitionHelperMock;
    @Mock KpiDefinitionService kpiDefinitionServiceMock;
    @Mock ExecutionGroupRepository executionGroupRepositoryMock;
    @Mock OffsetPersistencyKafka offsetPersistencyMock;
    @Mock OffsetCacheFacade offsetCacheFacadeMock;
    @Mock SparkService sparkServiceMock;

    @InjectMocks OffsetHandlerKafka objectUnderTest;

    Set<KpiDefinition> kpiDefinitions = emptySet();

    @Test
    void shouldVerifyGetKpisForAggregationPeriodWithTimestampParameters() {
        when(kpiDefinitionHelperMock.filterByAggregationPeriod(60, kpiDefinitions)).thenReturn(Collections.emptyList());

        objectUnderTest.getKpisForAggregationPeriodWithTimestampParameters(60, kpiDefinitions);

        verify(kpiDefinitionHelperMock).filterByAggregationPeriod(60, kpiDefinitions);
    }

    @Nested
    @DisplayName("Verify support")
    class VerifySupport {

        @Test
        void shouldSupport() {
            when(kpiDefinitionServiceMock.areScheduledSimple(kpiDefinitions)).thenReturn(true);

            final boolean actual = objectUnderTest.supports(kpiDefinitions);

            verify(kpiDefinitionServiceMock).areScheduledSimple(kpiDefinitions);

            assertThat(actual).isTrue();
        }

        @Test
        void shouldNotSupport() {
            final boolean actual = objectUnderTest.supports(kpiDefinitions);

            verify(kpiDefinitionServiceMock).areScheduledSimple(kpiDefinitions);

            assertThat(actual).isFalse();
        }
    }

    @Nested
    @DisplayName("Verify save offsets")
    class SaveOffsets {

        @Mock LatestProcessedOffset latestProcessedOffsetMock;

        @Test
        void shouldSaveOffsets() {
            final List<LatestProcessedOffset> offsetsList = Collections.singletonList(latestProcessedOffsetMock);
            final KpiExecutionGroup executionGroup = KpiExecutionGroup.builder()
                    .executionGroup(EXECUTION_GROUP)
                    .build();

            when(sparkServiceMock.getExecutionGroup()).thenReturn(EXECUTION_GROUP);
            when(executionGroupRepositoryMock.findByExecutionGroup(EXECUTION_GROUP)).thenReturn(Optional.of(executionGroup));
            when(offsetCacheFacadeMock.getCurrentOffsets()).thenReturn(offsetsList);

            objectUnderTest.saveOffsets();

            verify(sparkServiceMock).getExecutionGroup();
            verify(executionGroupRepositoryMock).findByExecutionGroup(EXECUTION_GROUP);
            verify(offsetPersistencyMock).upsertOffset(latestProcessedOffsetMock);
            verify(latestProcessedOffsetMock).setExecutionGroup(executionGroup);

        }

        @Test
        void shouldNotSaveAnythingIfListIsEmpty() {
            when(sparkServiceMock.getExecutionGroup()).thenReturn(EXECUTION_GROUP);
            when(executionGroupRepositoryMock.findByExecutionGroup(EXECUTION_GROUP)).thenReturn(Optional.empty());

            objectUnderTest.saveOffsets();

            verify(sparkServiceMock).getExecutionGroup();
            verify(executionGroupRepositoryMock).findByExecutionGroup(EXECUTION_GROUP);
            verifyNoMoreInteractions(offsetCacheFacadeMock, offsetPersistencyMock);
        }
    }

}