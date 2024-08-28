/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.kafka.MessageCounter;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.LatestProcessedOffset;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.LatestProcessedOffsetRepository;
import com.ericsson.oss.air.pm.stats.calculator.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.Topic;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset.EndOffset;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageChecker {

    private final KpiDefinitionService kpiDefinitionService;
    private final KpiDefinitionHelperImpl kpiDefinitionHelper;
    private final MessageCounter messageCounter;
    private final LatestProcessedOffsetRepository latestProcessedOffsetRepository;

    public boolean hasNewMessage() {
        final Set<KpiDefinition> definitions = kpiDefinitionService.loadDefinitionsToCalculate();

        if (definitions.isEmpty()) {
            return false;
        }

        final Set<SchemaDetail> details = kpiDefinitionHelper.extractSchemaDetails(definitions);
        final String executionGroup = kpiDefinitionHelper.extractExecutionGroup(definitions);

        for (final SchemaDetail schemaDetail : details) {
            final Map<TopicPartition, EndOffset> ends = messageCounter.countMessagesByPartition(Topic.of(schemaDetail.getTopic()));
            final List<LatestProcessedOffset> offsets = latestProcessedOffsetRepository.findAllForTopic(schemaDetail.getTopic(), executionGroup);

            final long totalMessages = CollectionHelpers.sumExact(ends.values().stream().map(EndOffset::number));
            final long computedMessages = computedMessages(offsets);

            if (totalMessages != computedMessages) {
                return true;
            }
        }
        return false;
    }

    private static long computedMessages(final List<LatestProcessedOffset> latestProcessedOffsets) {
        long readMessages = 0;

        for (final LatestProcessedOffset latestProcessedOffset : latestProcessedOffsets) {
            final long offset = latestProcessedOffset.getTopicPartitionOffset();
            if (offset > 0) {
                //  If offset is 0, then nothing has been read, so we need not increment
                //  If offset is not 0, then it is saved exclusively, so we have to increment
                readMessages += (offset + 1);
            }
        }

        return readMessages;
    }
}