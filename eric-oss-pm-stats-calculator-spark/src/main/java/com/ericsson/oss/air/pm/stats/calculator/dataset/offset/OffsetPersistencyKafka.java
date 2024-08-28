/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.offset;

import java.util.Collection;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.LatestProcessedOffset;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.LatestProcessedOffsetRepository;
import com.ericsson.oss.air.pm.stats.model.KpiDefinitionHandler;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OffsetPersistencyKafka implements OffsetPersistency {

    private final LatestProcessedOffsetRepository latestProcessedOffsetRepository;

    @Override
    public void upsertOffset(final LatestProcessedOffset latestProcessedOffset) {
        latestProcessedOffsetRepository.upsertLatestProcessedOffset(latestProcessedOffset);
    }

    @Override
    public boolean supports(@NonNull final Collection<KpiDefinition> delimiter){
        return KpiDefinitionHandler.areKpiDefinitionsSimpleKpis(delimiter);
    }
}
