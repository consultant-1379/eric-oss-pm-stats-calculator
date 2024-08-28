/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.writer;

import com.ericsson.oss.air.pm.stats.calculator.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.reliability.OnDemandReliabilityRegister;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PartitionedDatasetWriterImpl extends AbstractDatasetWriter {
    private final OnDemandReliabilityRegister onDemandReliabilityRegister;

    public PartitionedDatasetWriterImpl(final SparkService sparkService, final KpiDefinitionService kpiDefinitionService,
                                        final OnDemandReliabilityRegister onDemandReliabilityRegister) {
        super(sparkService, kpiDefinitionService, 10_000);
        this.onDemandReliabilityRegister = onDemandReliabilityRegister;
    }

    @Override
    public boolean supports(@NonNull final Integer delimiter) {
        return !kpiDefinitionService.isDefaultAggregationPeriod(delimiter);
    }

    @Override
    public void cacheOnDemandReliability(final Dataset<Row> data) {
        if (sparkService.isOnDemand()) {
            onDemandReliabilityRegister.addReliability(data);
        }
    }
}
