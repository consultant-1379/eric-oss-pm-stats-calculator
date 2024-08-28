/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.calculator;

import java.util.Collection;

import com.ericsson.oss.air.pm.stats.calculator.KpiCalculator;
import com.ericsson.oss.air.pm.stats.calculator.KpiCalculatorDefaultFilter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.FilterType;
import com.ericsson.oss.air.pm.stats.calculator.dataset.offset.registry.OffsetPersistencyRegistryFacadeImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.calculator.api.CalculatorHandler;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.sql.SqlProcessorDelegator;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.SparkSession;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultCalculatorHandlerImpl implements CalculatorHandler {
    private final SparkService sparkService;
    private final KpiDefinitionHelperImpl kpiDefinitionHelper;
    private final SqlProcessorDelegator sqlProcessorDelegator;
    private final OffsetPersistencyRegistryFacadeImpl offsetPersistencyRegistryFacade;
    private final SparkSession sparkSession;

    @Override
    public KpiCalculator calculator(final Collection<KpiDefinition> kpiDefinitions) {
        final Integer aggregationPeriod = kpiDefinitionHelper.extractAggregationPeriod(kpiDefinitions);

        return new KpiCalculatorDefaultFilter(
                sparkService,
                aggregationPeriod,
                kpiDefinitions,
                sqlProcessorDelegator,
                sparkSession,
                offsetPersistencyRegistryFacade
        );
    }

    @Override
    public boolean supports(@NonNull final FilterType delimiter) {
        return delimiter == FilterType.DEFAULT;
    }
}
