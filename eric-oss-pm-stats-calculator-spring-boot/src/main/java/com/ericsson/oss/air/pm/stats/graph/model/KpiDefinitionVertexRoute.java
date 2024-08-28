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

import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.function.BiPredicate;

import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString
@EqualsAndHashCode(doNotUseGetters = true)
public final class KpiDefinitionVertexRoute {
    private final Deque<KpiDefinitionVertex> route = new LinkedList<>();

    @Getter
    private boolean containsLoop;
    @Getter
    private boolean completed;
    @Getter
    private boolean visited;

    /**
     * {@link BiPredicate} identifying on what level loop is checked.
     * <p>
     * Loop can be detected between different execution groups and within the same execution group.
     */
    @Exclude
    private final BiPredicate<KpiDefinitionVertexRoute, KpiDefinitionVertex> loopDetector;

    private KpiDefinitionVertexRoute(
            final KpiDefinitionVertex kpiDefinitionVertex, @NonNull final BiPredicate<KpiDefinitionVertexRoute, KpiDefinitionVertex> loopDetector
    ) {
        this.loopDetector = loopDetector;
        route.addLast(kpiDefinitionVertex);
    }

    private KpiDefinitionVertexRoute(@NonNull final KpiDefinitionVertexRoute kpiDefinitionVertexRoute) {
        route.addAll(kpiDefinitionVertexRoute.getRoute());
        visited = kpiDefinitionVertexRoute.isVisited();
        completed = kpiDefinitionVertexRoute.isCompleted();
        containsLoop = kpiDefinitionVertexRoute.doesContainLoop();
        loopDetector = kpiDefinitionVertexRoute.loopDetector;
    }

    public static KpiDefinitionVertexRoute copyOf(final KpiDefinitionVertexRoute kpiDefinitionVertexRoute) {
        return new KpiDefinitionVertexRoute(kpiDefinitionVertexRoute);
    }

    public static KpiDefinitionVertexRoute sameExecutionGroupRoute(final KpiDefinitionVertex kpiDefinitionVertex) {
        return new KpiDefinitionVertexRoute(kpiDefinitionVertex, (instance, nextVertex) -> {
            Preconditions.checkNotNull(nextVertex, "'nextVertex' is null");
            return instance.isInCurrentGroup(nextVertex) && instance.isAlreadyVisitedDefinition(nextVertex);
        });
    }

    public static KpiDefinitionVertexRoute differentExecutionGroupRoute(final KpiDefinitionVertex kpiDefinitionVertex) {
        return new KpiDefinitionVertexRoute(kpiDefinitionVertex, (instance, nextVertex) -> {
            Preconditions.checkNotNull(nextVertex, "'nextVertex' is null");
            return instance.isNotInCurrentGroup(nextVertex) && instance.isAlreadyVisitedExecutionGroup(nextVertex);
        });
    }

    public void markVisited() {
        visited = true;
    }

    public void markCompleted() {
        completed = true;
    }

    public boolean isNotVisited() {
        return !visited;
    }

    public boolean doesContainLoop() {
        return containsLoop;
    }

    public Collection<KpiDefinitionVertex> getRoute() {
        return Collections.unmodifiableCollection(route);
    }

    public void addVertex(final KpiDefinitionVertex kpiDefinitionVertex) {
        if (visited) {
            return;
        }

        if (doesContainLoop(kpiDefinitionVertex)) {
            containsLoop = true;
            visited = true;
        }

        route.addLast(kpiDefinitionVertex);
    }

    public KpiDefinitionVertex getLastVertex() {
        return route.peekLast();
    }

    private boolean doesContainLoop(final KpiDefinitionVertex kpiDefinitionVertex) {
        return loopDetector.test(this, kpiDefinitionVertex);
    }

    private boolean isInCurrentGroup(final KpiDefinitionVertex kpiDefinitionVertex) {
        final KpiDefinitionVertex last = route.getLast();
        return last.hasSameExecutionGroup(kpiDefinitionVertex);
    }

    private boolean isNotInCurrentGroup(final KpiDefinitionVertex kpiDefinitionVertex) {
        return !isInCurrentGroup(kpiDefinitionVertex);
    }

    private boolean isAlreadyVisitedDefinition(final KpiDefinitionVertex kpiDefinitionVertex) {
        return route.contains(kpiDefinitionVertex);
    }

    private boolean isAlreadyVisitedExecutionGroup(final KpiDefinitionVertex kpiDefinitionVertex) {
        return route.stream().anyMatch(oldKpiDefinitionVertex -> oldKpiDefinitionVertex.hasSameExecutionGroup(kpiDefinitionVertex));
    }
}

