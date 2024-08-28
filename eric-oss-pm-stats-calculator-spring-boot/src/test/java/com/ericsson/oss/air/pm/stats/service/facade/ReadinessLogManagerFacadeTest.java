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

import static com.ericsson.oss.air.pm.stats._util.TestHelpers.uuid;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation.KpiCalculationJob;
import com.ericsson.oss.air.pm.stats.repository.SimpleKpiDependencyCache;
import com.ericsson.oss.air.pm.stats.service.api.ComplexReadinessLogService;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReadinessLogManagerFacadeTest {
    @Mock
    ComplexReadinessLogService complexReadinessLogServiceMock;
    @Mock
    KpiDefinitionService kpiDefinitionServiceMock;
    @Mock
    SimpleKpiDependencyCache simpleKpiDependencyCacheMock;

    @InjectMocks
    ReadinessLogManagerFacade objectUnderTest;

    @Test
    void shouldPersistComplexReadinessLog() {
        final UUID calculationId = uuid("36984960-d9b8-4b7c-9fb1-7af332fb2415");
        final String executionGroup = "complexGroup";
        final List<String> complexKpiDefinitionNames = List.of("complex-1", "complex-2");
        final Set<String> simpleExecutionGroups = Set.of("simple-group-1", "simple-group-2");

        when(kpiDefinitionServiceMock.findKpiDefinitionNamesByExecutionGroup(executionGroup)).thenReturn(complexKpiDefinitionNames);
        when(simpleKpiDependencyCacheMock.transitiveDependencyExecutionGroupsOf(complexKpiDefinitionNames)).thenReturn(simpleExecutionGroups);

        objectUnderTest.persistComplexReadinessLog(kpiCalculationJob(calculationId, executionGroup, KpiType.SCHEDULED_COMPLEX));

        verify(kpiDefinitionServiceMock).findKpiDefinitionNamesByExecutionGroup(executionGroup);
        verify(simpleKpiDependencyCacheMock).transitiveDependencyExecutionGroupsOf(complexKpiDefinitionNames);
        verify(complexReadinessLogServiceMock).save(calculationId, simpleExecutionGroups);
    }

    @Test
    void shouldNotPersistComplexReadinessLog_whenJobTypeIsNotComplex() {
        final UUID calculationId = uuid("36984960-d9b8-4b7c-9fb1-7af332fb2415");
        final String simpleGroup = "simpleGroup";

        objectUnderTest.persistComplexReadinessLog(kpiCalculationJob(calculationId, simpleGroup, KpiType.SCHEDULED_SIMPLE));

        verify(complexReadinessLogServiceMock, never()).save(any(UUID.class), anyCollection());
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