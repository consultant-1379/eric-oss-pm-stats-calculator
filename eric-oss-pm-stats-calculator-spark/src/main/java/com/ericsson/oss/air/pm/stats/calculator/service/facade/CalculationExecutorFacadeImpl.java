/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.facade;

import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.KpiCalculator;
import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiCalculatorErrorCode;
import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiCalculatorException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.dataset.SourceDataAvailability;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDatasets;
import com.ericsson.oss.air.pm.stats.calculator.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.facade.api.CalculationExecutorFacade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CalculationExecutorFacadeImpl implements CalculationExecutorFacade {
    private final SourceDataAvailability sourceDataAvailability;

    private final SparkService sparkService;
    private final KpiDefinitionService kpiDefinitionService;

    @Override
    public void calculateKpis(final KpiCalculator kpiCalculator, final TableDatasets loadedTableDatasets) {
        try {
            final Set<KpiDefinition> kpiDefinitions = kpiDefinitionService.loadDefinitionsToCalculate();
            final TableDatasets outputTableDatasets = kpiCalculator.calculate();

            loadedTableDatasets.unPersistDatasets();
            outputTableDatasets.unPersistDatasets();

            sparkService.clearCache();
        } catch (final Exception e) {
            throw new KpiCalculatorException(KpiCalculatorErrorCode.KPI_CALCULATION_ERROR, e);
        }
    }
}
