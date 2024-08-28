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

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DatasetWriterImpl extends AbstractDatasetWriter {

    public DatasetWriterImpl(final SparkService sparkService, final KpiDefinitionService kpiDefinitionService) {
        super(sparkService, kpiDefinitionService, 10_000);
    }

    @Override
    public boolean supports(@NonNull final Integer delimiter) {
        return kpiDefinitionService.isDefaultAggregationPeriod(delimiter);
    }

    @Override
    public void cacheOnDemandReliability(final Dataset<Row> data) {
        // We do not persist ON_DEMAND reliability for DEFAULT_AGGREGATION_PERIOD
    }
}
