/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator;

import static lombok.AccessLevel.PUBLIC;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.graph.DependencyFinder;
import com.ericsson.oss.air.pm.stats.model.exception.KpiDefinitionValidationException;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class DependencyValidator {

    @Inject
    private DependencyFinder dependencyFinder;

    public void validateNoDanglingDependenciesAfterDeletingDefinitions(final Collection<String> kpiDefinitionNames, final UUID collectionId) {
        final Map<String, Set<String>> danglingDependencies = dependencyFinder.findInadequateDependencies(kpiDefinitionNames, collectionId);

        if (!danglingDependencies.isEmpty()) {
            throw KpiDefinitionValidationException.badRequest(String.format("The Following KPIs have dependencies that would be deleted: [%s]",
                    danglingDependencies.entrySet()
                            .stream()
                            .map(entry -> String.format("%s: [%s]", entry.getKey(), String.join(", ", entry.getValue())))
                            .collect(Collectors.joining(", "))
            ));
        }
    }
}