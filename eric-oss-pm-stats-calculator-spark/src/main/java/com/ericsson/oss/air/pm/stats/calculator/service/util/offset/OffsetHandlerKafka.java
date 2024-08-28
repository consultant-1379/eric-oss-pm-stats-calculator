/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.offset;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.dataset.offset.OffsetPersistencyKafka;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.LatestProcessedOffset;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.ExecutionGroupRepository;
import com.ericsson.oss.air.pm.stats.calculator.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.cache.facade.OffsetCacheFacade;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OffsetHandlerKafka implements OffsetHandler {
    private final OffsetCacheFacade offsetCacheFacade;
    private final ExecutionGroupRepository executionGroupRepository;
    private final KpiDefinitionHelperImpl kpiDefinitionHelper;
    private final KpiDefinitionService kpiDefinitionService;
    private final OffsetPersistencyKafka offsetPersistencyKafka;
    private final SparkService sparkService;

    @Override
    public boolean supports(@NonNull final Set<KpiDefinition> kpiDefinitions) {
        return kpiDefinitionService.areScheduledSimple(kpiDefinitions);
    }

    @Override
    public void calculateOffsets(final Set<KpiDefinition> defaultFilterKpis) {
        //  Postgres-like offsets are not calculated for Kafka
    }

    @Override
    public List<KpiDefinition> getKpisForAggregationPeriodWithTimestampParameters(final Integer aggPeriodInMinutes, final Collection<KpiDefinition> kpiDefinitions) {
        return kpiDefinitionHelper.filterByAggregationPeriod(aggPeriodInMinutes, kpiDefinitions);
    }

    @Override
    @Transactional
    public void saveOffsets() {
        final String executionGroup = sparkService.getExecutionGroup();
        executionGroupRepository.findByExecutionGroup(executionGroup).ifPresent(kpiExecutionGroup -> {
            for (final LatestProcessedOffset offset : offsetCacheFacade.getCurrentOffsets()) {
                offset.setExecutionGroup(kpiExecutionGroup);
                offsetPersistencyKafka.upsertOffset(offset);
            }
        });
    }

}
