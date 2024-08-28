/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.graph.utils;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.graph.model.KpiDefinitionVertex;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GraphCycleRepresentationUtils {

    public static List<String> processExecutionGroupRepresentations(@NonNull final List<? extends Collection<KpiDefinitionVertex>> loops) {
        return loops.stream().map(vertexes -> representation(vertexes, KpiDefinitionVertex::hasSameExecutionGroup)).collect(toList());
    }

    public static List<String> processKpiDefinitionRepresentations(@NonNull final List<? extends Collection<KpiDefinitionVertex>> loops) {
        loops.forEach(GraphCycleRepresentationUtils::verifySameExecutionGroup);
        return loops.stream().map(vertexes -> representation(vertexes, KpiDefinitionVertex::hasSameDefinitionName)).collect(toList());
    }

    private static void verifySameExecutionGroup(@NonNull final Collection<KpiDefinitionVertex> vertexes) {
        final Set<String> executionGroups = vertexes.stream().map(KpiDefinitionVertex::executionGroup).collect(toSet());
        checkArgument(
                executionGroups.size() == 1,
                String.format("execution group must be unique but found '%s'", executionGroups)
        );
    }

    private static String representation(
            @NonNull final Collection<? extends KpiDefinitionVertex> kpiDefinitionVertexes,
            @NonNull final BiPredicate<KpiDefinitionVertex, KpiDefinitionVertex> cyclePredicate
    ) {
        checkArgument(kpiDefinitionVertexes.size() >= 2, "The provided route has to have at least two vertexes");

        final LinkedList<KpiDefinitionVertex> originalRoute = new LinkedList<>(kpiDefinitionVertexes);
        final LinkedList<KpiDefinitionVertex> cycleRoute = new LinkedList<>();

        cycleRoute.add(originalRoute.removeLast());
        final ListIterator<KpiDefinitionVertex> loopIterator = originalRoute.listIterator(originalRoute.size());
        while (loopIterator.hasPrevious()) {
            cycleRoute.addFirst(loopIterator.previous());
            if (cyclePredicate.test(cycleRoute.getFirst(), cycleRoute.getLast())) {
                return computeCycleRepresentation(cycleRoute);
            }
        }
        throw new IllegalArgumentException("Original route contained no cycle");
    }

    private static String computeCycleRepresentation(@NonNull final Collection<? extends KpiDefinitionVertex> cycleRoute) {
        return cycleRoute.stream().map(GraphCycleRepresentationUtils::vertexToString).collect(Collectors.joining(" -> "));
    }

    private static String vertexToString(@NonNull final KpiDefinitionVertex kpiDefinitionVertex) {
        return kpiDefinitionVertex.executionGroup() + '.' + kpiDefinitionVertex.definitionName();
    }
}
