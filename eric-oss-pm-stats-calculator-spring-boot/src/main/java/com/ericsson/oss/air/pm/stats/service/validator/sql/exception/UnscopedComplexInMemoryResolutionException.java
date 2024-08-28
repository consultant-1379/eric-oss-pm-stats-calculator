/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql.exception;

import static org.apache.arrow.util.Preconditions.checkArgument;

import java.util.Collection;

import com.ericsson.oss.air.pm.stats.service.validator.sql.reference.RelationReferenceResolution;

import kpi.model.api.table.definition.KpiDefinition;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

@Getter
@Accessors(fluent = true)
public class UnscopedComplexInMemoryResolutionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final HashSetValuedHashMap<KpiDefinition, RelationReferenceResolution> unscopedResolutions;

    public UnscopedComplexInMemoryResolutionException(
            @NonNull final HashSetValuedHashMap<KpiDefinition, RelationReferenceResolution> unscopedResolutions
    ) {
        checkArgument(areAllComplexKpiDefinition(unscopedResolutions.keySet()), "'unscopedResolutions' must contain only COMPLEX definitions");
        this.unscopedResolutions = unscopedResolutions;
    }

    private static boolean areAllComplexKpiDefinition(@NonNull final Collection<KpiDefinition> kpiDefinitions) {
        return kpiDefinitions.stream().allMatch(KpiDefinition::isScheduledComplex);
    }
}
