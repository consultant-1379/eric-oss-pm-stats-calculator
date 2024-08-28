/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.graph.model;

import java.util.Map;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString
@EqualsAndHashCode
public final class KpiDefinitionGraph {
    @Delegate
    private final DependencyNetwork dependencyNetwork;
    @Delegate
    private final EntryVertexes entryVertexes;

    private KpiDefinitionGraph(final Map<? extends KpiDefinitionVertex, ? extends Set<KpiDefinitionVertex>> dependencyNetwork,
                               final Set<? extends KpiDefinitionVertex> entryVertexes) {
        this.dependencyNetwork = DependencyNetwork.of(dependencyNetwork);
        this.entryVertexes = EntryVertexes.of(entryVertexes);
    }

    public static KpiDefinitionGraph of(final Map<? extends KpiDefinitionVertex, ? extends Set<KpiDefinitionVertex>> dependencyNetwork,
                                        final Set<? extends KpiDefinitionVertex> entryPoints) {
        return new KpiDefinitionGraph(dependencyNetwork, entryPoints);
    }
}
