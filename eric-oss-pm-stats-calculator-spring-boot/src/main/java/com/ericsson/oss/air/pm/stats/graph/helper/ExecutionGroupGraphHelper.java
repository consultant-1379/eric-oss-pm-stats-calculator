/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.graph.helper;

import static lombok.AccessLevel.PUBLIC;

import java.util.Collection;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.graph.KpiDependencyHelper;
import com.ericsson.oss.air.pm.stats.graph.model.DependencyNetwork;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class ExecutionGroupGraphHelper {

    @Inject
    private KpiDependencyHelper kpiDependencyHelper;

    public Graph<String, DefaultEdge> fetchComplexExecutionGroupGraph(final Collection<KpiDefinitionEntity> definitions) {
        return DependencyNetwork.executionGroupGraph(kpiDependencyHelper.createMapOfExecutionGroupDependenciesForComplexKpiDefinitions(definitions));
    }
}
