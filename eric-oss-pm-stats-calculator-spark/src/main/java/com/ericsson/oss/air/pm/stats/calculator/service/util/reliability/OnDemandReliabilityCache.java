/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.reliability;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.Calculation;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.CalculationReliability;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.KpiDefinition;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Range;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OnDemandReliabilityCache {

    private final Map<KpiDefinition, Range<LocalDateTime>> kpiReliabilities = new ConcurrentHashMap<>();

    public Range<LocalDateTime> merge(
            final KpiDefinition key,
            @NonNull final Range<LocalDateTime> value,
            @NonNull final BiFunction<? super Range<LocalDateTime>, ? super Range<LocalDateTime>, Range<LocalDateTime>> remappingFunction) {
        return kpiReliabilities.merge(key, value, remappingFunction);
    }

    public List<CalculationReliability> extractReliabilities(final Calculation calculation) {

        final List<CalculationReliability> onDemandReliabilities = new ArrayList<>(kpiReliabilities.size());
        kpiReliabilities.forEach((kpiDefinition, reliabilities) -> onDemandReliabilities.add(
                CalculationReliability.builder()
                                      .withCalculation(calculation)
                                      .withCalculationStartTime(reliabilities.getMinimum())
                                      .withReliabilityThreshold(reliabilities.getMaximum())
                                      .withKpiDefinition(kpiDefinition)
                                      .build()));

        return onDemandReliabilities;
    }
}
