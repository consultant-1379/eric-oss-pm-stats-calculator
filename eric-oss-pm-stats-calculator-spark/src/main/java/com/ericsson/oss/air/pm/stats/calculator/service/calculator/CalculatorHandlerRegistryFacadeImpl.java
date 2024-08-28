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
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.FilterType;
import com.ericsson.oss.air.pm.stats.calculator.dataset.writer.registry.api.DatasetWriterRegistryFacade;
import com.ericsson.oss.air.pm.stats.calculator.model.exception.CalculatorHandlerNotFoundException;
import com.ericsson.oss.air.pm.stats.calculator.service.calculator.api.CalculatorHandler;
import com.ericsson.oss.air.pm.stats.calculator.service.calculator.api.CalculatorHandlerRegistryFacade;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.sql.SqlProcessorDelegator;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CalculatorHandlerRegistryFacadeImpl implements CalculatorHandlerRegistryFacade {
    private final PluginRegistry<CalculatorHandler, FilterType> calculatorHandlerRegistry;

    //  TODO: Temporally injecting dependencies
    private final KpiDefinitionHelperImpl kpiDefinitionHelper;
    private final DatasetWriterRegistryFacade datasetWriterRegistryFacade;
    private final SqlProcessorDelegator sqlProcessorDelegator;

    @Override
    public KpiCalculator defaultCalculator(final Collection<KpiDefinition> kpiDefinitions) {
        return injectDependencies(calculator(FilterType.DEFAULT).calculator(kpiDefinitions));
    }

    @Override
    public KpiCalculator customCalculator(final Collection<KpiDefinition> kpiDefinitions) {
        return injectDependencies(calculator(FilterType.CUSTOM).calculator(kpiDefinitions));
    }

    @Override
    public CalculatorHandler calculator(final FilterType filterType) {
        return calculatorHandlerRegistry.getPluginFor(filterType, () -> {
            final String message = String.format("%s with type %s not found", CalculatorHandler.class.getSimpleName(), filterType);
            return new CalculatorHandlerNotFoundException(message);
        });
    }

    private KpiCalculator injectDependencies(@NonNull final KpiCalculator kpiCalculator) {
        kpiCalculator.setKpiDefinitionHelper(kpiDefinitionHelper);
        kpiCalculator.setDatasetWriterRegistryFacade(datasetWriterRegistryFacade);
        kpiCalculator.setSqlProcessorDelegator(sqlProcessorDelegator);
        return kpiCalculator;
    }
}
