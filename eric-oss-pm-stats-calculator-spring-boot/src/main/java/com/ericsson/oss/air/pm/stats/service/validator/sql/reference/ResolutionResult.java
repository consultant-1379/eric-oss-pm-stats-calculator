/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql.reference;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;

import kpi.model.api.table.definition.KpiDefinition;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Accessors(fluent = true)
public final class ResolutionResult {
    private final KpiDefinition kpiDefinition;
    private final Set<RelationReferenceResolution> resolutions = new HashSet<>();

    public String kpiDefinitionName() {
        return kpiDefinition.name().value();
    }

    public boolean isUnResolved() {
        return !isResolved();
    }

    public boolean isResolved() {
        return resolutions.stream().noneMatch(RelationReferenceResolution::isUnresolved);
    }

    public boolean contains(final Reference reference) {
        return resolutions().stream().anyMatch(resolution -> resolution.reference().equals(reference));
    }

    public Set<RelationReferenceResolution> unresolvedResolutions() {
        return resolutions.stream().filter(RelationReferenceResolution::isUnresolved).collect(Collectors.toSet());
    }

    public RelationReferenceResolution addResolvedResolution(final Relation relation, final Reference reference) {
        final RelationReferenceResolution resolution = addResolution(relation, reference, true);
        log.info("KPI Definition '{}' reference '{}' is resolved", kpiDefinitionName(), resolution);
        return resolution;
    }

    public RelationReferenceResolution addUnresolvedResolution(final Relation relation, final Reference reference) {
        final RelationReferenceResolution resolution = addResolution(relation, reference, false);
        log.info("KPI Definition '{}' reference '{}' is not resolved", kpiDefinitionName(), resolution);
        return resolution;
    }

    private RelationReferenceResolution addResolution(final Relation relation, final Reference reference, final boolean isResolved) {
        final RelationReferenceResolution relationReferenceResolution = new RelationReferenceResolution(relation, reference);
        if (isResolved) {
            relationReferenceResolution.resolve();
        }
        resolutions.add(relationReferenceResolution);
        return relationReferenceResolution;
    }

}
