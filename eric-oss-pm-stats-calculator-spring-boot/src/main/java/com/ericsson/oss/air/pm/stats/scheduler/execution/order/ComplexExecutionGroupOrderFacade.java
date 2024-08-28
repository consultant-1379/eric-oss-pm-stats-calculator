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

import static lombok.AccessLevel.PUBLIC;

import java.util.List;
import java.util.Queue;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.graph.helper.ExecutionGroupGraphHelper;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.scheduler.execution.order.compute.ComplexExecutionOrderDeterminer;
import com.ericsson.oss.air.pm.stats.scheduler.execution.order.compute.ComplexExecutionOrderFilterer;
import com.ericsson.oss.air.pm.stats.scheduler.execution.order.model.ComplexExecutionOrder;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class ComplexExecutionGroupOrderFacade {
    @Inject
    private ComplexExecutionOrderDeterminer complexExecutionOrderDeterminer;
    @Inject
    private ComplexExecutionOrderFilterer complexExecutionOrderFilterer;
    @Inject
    private ExecutionGroupGraphHelper executionGroupGraphHelper;
    @Inject
    private KpiDefinitionService kpiDefinitionService;

    public Queue<String> sortCalculableComplexExecutionGroups() {
        final List<KpiDefinitionEntity> kpiDefinitionEntities = kpiDefinitionService.findComplexKpis();
        final Graph<String, DefaultEdge> complexExecutionGroupGraph = executionGroupGraphHelper.fetchComplexExecutionGroupGraph(kpiDefinitionEntities);

        final ComplexExecutionOrder complexExecutionOrder = complexExecutionOrderDeterminer.computeExecutionOrder(complexExecutionGroupGraph);
        return complexExecutionOrderFilterer.filterExecutionOrder(complexExecutionOrder);
    }

}
