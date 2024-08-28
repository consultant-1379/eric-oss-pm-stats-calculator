/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.offset.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.LatestProcessedOffset;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.LatestProcessedOffsetRepository;
import com.ericsson.oss.air.pm.stats.calculator.service.KpiDefinitionServiceImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.LatestProcessedOffsetAdjuster;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.Topic;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@CacheConfig(cacheNames = "persisted-topic-offsets")
public class PersistedOffsetCache {
    private final LatestProcessedOffsetRepository latestProcessedOffsetRepository;
    private final LatestProcessedOffsetAdjuster latestProcessedOffsetAdjuster;
    private final KpiDefinitionHelperImpl kpiDefinitionHelper;
    private final KpiDefinitionServiceImpl kpiDefinitionService;

    @Cacheable
    public Map<Topic, List<LatestProcessedOffset>> loadPersistedOffsets() {
        final Map<Topic, List<LatestProcessedOffset>> result = new HashMap<>();

        final Set<KpiDefinition> kpiDefinitions = kpiDefinitionService.loadDefinitionsToCalculate();
        final String executionGroup = kpiDefinitionHelper.extractExecutionGroup(kpiDefinitions);
        final Set<SchemaDetail> details = kpiDefinitionHelper.extractSchemaDetails(kpiDefinitions);

        for (final SchemaDetail detail : details) {
            final String topic = detail.getTopic();
            final List<LatestProcessedOffset> latestProcessedOffsets = latestProcessedOffsetRepository.findAllForTopic(topic, executionGroup);
            result.put(Topic.of(topic), latestProcessedOffsetAdjuster.adjustOffsets(latestProcessedOffsets));
        }

        return result;
    }
}
