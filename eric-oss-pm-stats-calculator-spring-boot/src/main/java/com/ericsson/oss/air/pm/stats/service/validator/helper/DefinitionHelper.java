/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.helper;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.service.util.CollectionHelpers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefinitionHelper {

    public static Set<DataIdentifier> getDistinctDataIdentifiers(final @NonNull Collection<? extends KpiDefinitionEntity> simpleDefinitions) {
        return CollectionHelpers.collectDistinctBy(simpleDefinitions, KpiDefinitionEntity::dataIdentifier);
    }

    public static List<KpiDefinitionEntity> getDefinitionsWithSameIdentifier(
            final DataIdentifier dataIdentifier,
            @NonNull final Collection<? extends KpiDefinitionEntity> simpleDefinitions) {
        return simpleDefinitions.stream().filter(definition -> dataIdentifier.equals(definition.dataIdentifier())).collect(Collectors.toList());
    }
}