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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString
@EqualsAndHashCode
public final class EntryVertexes {
    private final Collection<KpiDefinitionVertex> roots = new HashSet<>();

    private EntryVertexes(@Nonnull final Collection<? extends KpiDefinitionVertex> entryVertexes) {
        Preconditions.checkArgument(!entryVertexes.isEmpty(), "entryVertexes must not be empty");

        roots.addAll(entryVertexes);
    }

    public static EntryVertexes of(final Set<? extends KpiDefinitionVertex> entryVertexes) {
        return new EntryVertexes(entryVertexes);
    }

    public List<KpiDefinitionVertexRoute> extractStartingRoutes(final Function<? super KpiDefinitionVertex, KpiDefinitionVertexRoute> vertexRouteMapper) {
        return roots.stream().map(vertexRouteMapper).collect(Collectors.toList());
    }
}
