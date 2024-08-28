/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.graph.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Builder
@Accessors(fluent = true)
public class KpiDefinitionVertex {
    @NonNull
    private final String executionGroup;
    @NonNull
    private final String definitionName;

    public static KpiDefinitionVertex of(final String executionGroup, final String definitionName) {
        final KpiDefinitionVertexBuilder builder = builder();
        builder.definitionName(definitionName);
        builder.executionGroup(executionGroup);
        return builder.build();
    }

    public boolean hasDifferentExecutionGroup(final KpiDefinitionVertex kpiDefinitionVertex) {
        return !hasSameExecutionGroup(kpiDefinitionVertex);
    }

    public boolean hasSameExecutionGroup(@NonNull final KpiDefinitionVertex kpiDefinitionVertex) {
        return executionGroup.equals(kpiDefinitionVertex.executionGroup());
    }

    public boolean hasSameDefinitionName(@NonNull final KpiDefinitionVertex kpiDefinitionVertex) {
        return definitionName.equals(kpiDefinitionVertex.definitionName());
    }
}
