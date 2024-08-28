/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.facade;

import static com.ericsson.oss.air.pm.stats._util.TestHelpers.calculationReliabilities;
import static com.ericsson.oss.air.pm.stats._util.TestHelpers.calculationReliability;
import static com.ericsson.oss.air.pm.stats._util.TestHelpers.definitions;
import static com.ericsson.oss.air.pm.stats._util.TestHelpers.kpiDefinitionEntity;
import static com.ericsson.oss.air.pm.stats._util.TestHelpers.lowerReadinessBound;
import static com.ericsson.oss.air.pm.stats._util.TestHelpers.readinessBound;
import static com.ericsson.oss.air.pm.stats._util.TestHelpers.testTime;
import static com.ericsson.oss.air.pm.stats._util.TestHelpers.upperReadinessBound;
import static com.ericsson.oss.air.pm.stats._util.TestHelpers.uuid;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculation.limits.ReadinessBoundCollector;
import com.ericsson.oss.air.pm.stats.calculation.limits.model.ReadinessBound;
import com.ericsson.oss.air.pm.stats.calculation.limits.period.creator.HourlyAggregationPeriodCreator;
import com.ericsson.oss.air.pm.stats.calculation.limits.period.creator.registry.AggregationPeriodCreatorRegistry;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation.KpiCalculationJob;
import com.ericsson.oss.air.pm.stats.model.entity.CalculationReliability;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.service.api.CalculationReliabilityService;
import com.ericsson.oss.air.pm.stats.service.api.CalculationService;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ComplexReliabilityThresholdFacadeTest {
    @Mock
    CalculationReliabilityService calculationReliabilityServiceMock;
    @Mock
    KpiDefinitionService kpiDefinitionServiceMock;
    @Mock
    ReadinessBoundCollector readinessBoundCollectorMock;
    @Mock
    CalculationService calculationServiceMock;
    @Mock
    AggregationPeriodCreatorRegistry aggregationPeriodCreatorRegistryMock;
    @InjectMocks
    ComplexReliabilityThresholdFacade objectUnderTest;

    @Test
    void shouldPersistReliabilityThresholdWhenKpiHasAggregationPeriod() {
        final UUID calculationId = uuid("36984960-d9b8-4b7c-9fb1-7af332fb2415");
        final String complexExecutionGroup = "complexExecutionGroup";
        final String kpiDefinitionName = "kpiDefinition1";
        final KpiCalculationJob kpiCalculationJob = kpiCalculationJob(calculationId, complexExecutionGroup, KpiType.SCHEDULED_COMPLEX);
        final List<CalculationReliability> calculationReliabilities = calculationReliabilities(calculationReliability(testTime(1, 12, 0), testTime(1, 13, 0), calculationId, 1));
        final KpiDefinitionEntity kpiDefinition = kpiDefinitionEntity(kpiDefinitionName, complexExecutionGroup, 1, 60, 1);
        final KpiDefinitionEntity kpiDefinition2 = kpiDefinitionEntity("kpiDefinition2", complexExecutionGroup, 1, 60, 2);
        final Map<String, Integer> exampleMap = Map.of("kpiDefinition1", 1, "kpiDefinition2", 2);

        final Map<KpiDefinitionEntity, ReadinessBound> calculatedReadinessBoundMap = Map.of(
                kpiDefinition,
                readinessBound(lowerReadinessBound(1, 12, 0), upperReadinessBound(1, 13, 0)));

        when(kpiDefinitionServiceMock.findKpiDefinitionsByExecutionGroup(complexExecutionGroup)).thenReturn(List.of(kpiDefinition, kpiDefinition2));
        when(readinessBoundCollectorMock.calculateReadinessBounds(complexExecutionGroup, List.of(kpiDefinition, kpiDefinition2))).thenReturn(calculatedReadinessBoundMap);
        when(aggregationPeriodCreatorRegistryMock.aggregationPeriod(60)).thenReturn(new HourlyAggregationPeriodCreator());

        objectUnderTest.persistComplexReliabilityThreshold(kpiCalculationJob);

        verify(readinessBoundCollectorMock).calculateReadinessBounds(complexExecutionGroup, List.of(kpiDefinition, kpiDefinition2));
        verify(kpiDefinitionServiceMock).findKpiDefinitionsByExecutionGroup(complexExecutionGroup);
        verify(aggregationPeriodCreatorRegistryMock).aggregationPeriod(60);
        verify(calculationReliabilityServiceMock).save(calculationReliabilities);
    }

    @Test
    void shouldPersistReliabilityThresholdWhenKpiHasNoAggregationPeriod() {
        final UUID calculationId = uuid("36984960-d9b8-4b7c-9fb1-7af332fb2415");
        final String complexExecutionGroup = "complexExecutionGroup";
        final String kpiDefinitionName = "kpiDefinition1";

        final KpiCalculationJob kpiCalculationJob = kpiCalculationJob(calculationId, complexExecutionGroup, KpiType.SCHEDULED_COMPLEX);
        final List<CalculationReliability> calculationReliabilities = calculationReliabilities(calculationReliability(testTime(1, 12, 0), testTime(1, 12, 50), calculationId, 1));
        final KpiDefinitionEntity kpiDefinition = kpiDefinitionEntity(kpiDefinitionName, complexExecutionGroup, 1, -1, 1);

        final Map<KpiDefinitionEntity, ReadinessBound> calculatedReadinessBoundMap = Map.of(
                kpiDefinition,
                readinessBound(lowerReadinessBound(1, 12, 0), upperReadinessBound(1, 12, 50)));

        when(kpiDefinitionServiceMock.findKpiDefinitionsByExecutionGroup(complexExecutionGroup)).thenReturn(definitions(calculatedReadinessBoundMap));
        when(readinessBoundCollectorMock.calculateReadinessBounds(complexExecutionGroup, List.of(kpiDefinition))).thenReturn(calculatedReadinessBoundMap);

        objectUnderTest.persistComplexReliabilityThreshold(kpiCalculationJob);

        verify(readinessBoundCollectorMock).calculateReadinessBounds(complexExecutionGroup, List.of(kpiDefinition));
        verify(kpiDefinitionServiceMock).findKpiDefinitionsByExecutionGroup(complexExecutionGroup);
        verify(calculationReliabilityServiceMock).save(calculationReliabilities);
    }

    @Test
    void shouldNotPersistReliabilityThreshold_whenJobTypeIsNotComplex() {
        final UUID calculationId = uuid("36984960-d9b8-4b7c-9fb1-7af332fb2415");
        final String simpleGroup = "simpleGroup";
        final KpiCalculationJob kpiCalculationJob = kpiCalculationJob(calculationId, simpleGroup, KpiType.SCHEDULED_SIMPLE);

        objectUnderTest.persistComplexReliabilityThreshold(kpiCalculationJob);

        verify(calculationReliabilityServiceMock, never()).save(any());
    }

    @Test
    void shouldNotPersistReliabilityThreshold_whenCalculationReliabilitiesIsEmpty() {
        final UUID calculationId = uuid("36984960-d9b8-4b7c-9fb1-7af332fb2415");
        final String complexExecutionGroup = "complexExecutionGroup";
        final String kpiDefinitionName = "kpiDefinition1";
        final KpiCalculationJob kpiCalculationJob = kpiCalculationJob(calculationId, complexExecutionGroup, KpiType.SCHEDULED_COMPLEX);
        final KpiDefinitionEntity kpiDefinition = kpiDefinitionEntity(kpiDefinitionName, complexExecutionGroup, 1, 60, 1);

        when(kpiDefinitionServiceMock.findKpiDefinitionsByExecutionGroup(complexExecutionGroup)).thenReturn(List.of(kpiDefinition));
        when(readinessBoundCollectorMock.calculateReadinessBounds(complexExecutionGroup, List.of(kpiDefinition))).thenReturn(Collections.emptyMap());

        objectUnderTest.persistComplexReliabilityThreshold(kpiCalculationJob);

        verify(readinessBoundCollectorMock).calculateReadinessBounds(complexExecutionGroup, List.of(kpiDefinition));
        verify(kpiDefinitionServiceMock).findKpiDefinitionsByExecutionGroup(complexExecutionGroup);
        verify(calculationServiceMock).updateCompletionState(calculationId, KpiCalculationState.NOTHING_CALCULATED);
        verifyNoInteractions(calculationReliabilityServiceMock);
    }

    static KpiCalculationJob kpiCalculationJob(final UUID calculationId, final String executionGroup, final KpiType jobType) {
        return KpiCalculationJob.builder()
                .withCalculationId(calculationId)
                .withExecutionGroup(executionGroup)
                .withJobType(jobType)
                .withTimeCreated(new Timestamp(0)) /* Ignored */
                .build();
    }
}
