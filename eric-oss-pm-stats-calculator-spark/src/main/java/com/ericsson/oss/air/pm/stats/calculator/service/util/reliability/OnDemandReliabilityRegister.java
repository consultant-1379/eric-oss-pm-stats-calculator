/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.reliability;

import static org.apache.spark.sql.functions.max;
import static org.apache.spark.sql.functions.min;

import java.sql.Timestamp;
import java.util.List;

import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.Calculation;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.CalculationReliability;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.CalculationRepository;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.KpiDefinitionRepository;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.OnDemandReliabilityRepository;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.util.RangeUtils;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OnDemandReliabilityRegister {
    public static final String AGGREGATION_BEGIN_TIME = "aggregation_begin_time";
    public static final String AGGREGATION_END_TIME = "aggregation_end_time";

    private final SparkService sparkService;
    private final CalculationRepository calculationRepository;
    private final OnDemandReliabilityRepository onDemandReliabilityRepository;
    private final OnDemandReliabilityCache onDemandReliabilityCache;
    private final KpiDefinitionRepository kpiDefinitionRepository;

    public void addReliability(final Dataset<Row> dataset) {
        if (dataset.isEmpty()) {
            return;
        }
        final List<String> kpis = sparkService.getKpisToCalculate();

        final Sets.SetView<String> kpisInDataset = Sets.intersection(
                Sets.newHashSet(dataset.columns()),
                Sets.newHashSet(kpis)
        );

        kpisInDataset.forEach(kpiName -> {
            //TODO: extract this to a helper, and use it in the ReadinessLogRegisterImpl too
            final Row row = dataset.select(kpiName, AGGREGATION_BEGIN_TIME, AGGREGATION_END_TIME)
                                   .agg(min(AGGREGATION_BEGIN_TIME).as("minTimestamp"),
                                        max(AGGREGATION_END_TIME).as("maxTimestamp"))
                                   .head();

            final Timestamp earliest = row.getAs("minTimestamp");
            final Timestamp latest = row.getAs("maxTimestamp");

            onDemandReliabilityCache.merge(
                    kpiDefinitionRepository.forceFindByName(kpiName),
                    RangeUtils.makeRange(earliest, latest),
                    RangeUtils::mergeRanges /* With custom and default filters the calculation happens in iterations */
            );
        });
    }

    public void persistOnDemandReliabilities() {
        final Calculation calculation = calculationRepository.forceFetchById(sparkService.getCalculationId());
        final List<CalculationReliability> onDemandReliabilities = onDemandReliabilityCache.extractReliabilities(calculation);

        onDemandReliabilityRepository.saveAll(onDemandReliabilities);
    }
}
