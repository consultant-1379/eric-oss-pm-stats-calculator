/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.readiness.model.domain;

import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Data(staticConstructor = "of")
public final class DefinitionName {
    private final String name;

    public boolean hasSameName(@NonNull final KpiDefinitionEntity kpiDefinition) {
        return name.equals(kpiDefinition.name());
    }
}
