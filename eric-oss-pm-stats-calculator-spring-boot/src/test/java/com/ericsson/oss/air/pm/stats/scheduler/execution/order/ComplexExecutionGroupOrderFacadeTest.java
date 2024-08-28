/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.scheduler.execution.order;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.ericsson.oss.air.pm.stats.graph.helper.ExecutionGroupGraphHelper;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.scheduler.execution.order.compute.ComplexExecutionOrderDeterminer;
import com.ericsson.oss.air.pm.stats.scheduler.execution.order.compute.ComplexExecutionOrderFilterer;
import com.ericsson.oss.air.pm.stats.scheduler.execution.order.model.ComplexExecutionOrder;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;

import org.assertj.core.api.Assertions;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ComplexExecutionGroupOrderFacadeTest {
    @Mock
    ComplexExecutionOrderDeterminer complexExecutionOrderDeterminerMock;
    @Mock
    ComplexExecutionOrderFilterer complexExecutionOrderFiltererMock;
    @Mock
    ExecutionGroupGraphHelper executionGroupGraphHelperMock;
    @Mock
    KpiDefinitionService kpiDefinitionServiceMock;

    @InjectMocks
    ComplexExecutionGroupOrderFacade objectUnderTest;

    @Test
    void shouldSortCalculableComplexExecutionGroups(
            @Mock final List<KpiDefinitionEntity> definitionsMock,
            @Mock final Graph<String, DefaultEdge> complexExecutionGroupGraphMock,
            @Mock final ComplexExecutionOrder complexExecutionOrderMock) {
        when(kpiDefinitionServiceMock.findComplexKpis()).thenReturn(definitionsMock);
        when(executionGroupGraphHelperMock.fetchComplexExecutionGroupGraph(definitionsMock)).thenReturn(complexExecutionGroupGraphMock);
        when(complexExecutionOrderDeterminerMock.computeExecutionOrder(complexExecutionGroupGraphMock)).thenReturn(complexExecutionOrderMock);
        when(complexExecutionOrderFiltererMock.filterExecutionOrder(complexExecutionOrderMock)).thenReturn(new LinkedList<>());

        final Queue<String> actual = objectUnderTest.sortCalculableComplexExecutionGroups();

        verify(kpiDefinitionServiceMock).findComplexKpis();
        verify(executionGroupGraphHelperMock).fetchComplexExecutionGroupGraph(definitionsMock);
        verify(complexExecutionOrderDeterminerMock).computeExecutionOrder(complexExecutionGroupGraphMock);
        verify(complexExecutionOrderFiltererMock).filterExecutionOrder(complexExecutionOrderMock);

        Assertions.assertThat(actual).isEmpty();
    }
}
