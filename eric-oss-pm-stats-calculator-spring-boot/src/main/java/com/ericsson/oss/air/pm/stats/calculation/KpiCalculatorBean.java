/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation;

import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.FAILED;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.FINALIZING;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.NOTHING_CALCULATED;

import java.util.UUID;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculation.api.KpiCalculator;
import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiCalculatorErrorCode;
import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiCalculatorException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation.KpiCalculationJob;
import com.ericsson.oss.air.pm.stats.scheduler.api.KpiCalculationMediator;
import com.ericsson.oss.air.pm.stats.service.api.CalculationService;
import com.ericsson.oss.air.pm.stats.service.facade.ComplexReliabilityThresholdFacade;
import com.ericsson.oss.air.pm.stats.service.facade.ReadinessLogManagerFacade;
import com.ericsson.oss.air.pm.stats.service.helper.CalculationLauncher;
import com.ericsson.oss.air.pm.stats.service.metric.SparkMeterService;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

/**
 * Class used to handle KPI calculations.
 */
@Slf4j
@Stateless
@NoArgsConstructor
@AllArgsConstructor
public class KpiCalculatorBean implements KpiCalculator {
    @Inject
    private CalculationLauncher calculationLauncher;
    @Inject
    private CalculationService calculationService;
    @Inject
    private KpiCalculationMediator kpiCalculationMediator;
    @Inject
    private ReadinessLogManagerFacade readinessLogManagerFacade;
    @Inject
    private ComplexReliabilityThresholdFacade complexReliabilityThresholdFacade;
    @Inject
    private TabularParameterFacade tabularParameterFacade;
    @Inject
    private SparkMeterService sparkMeterService;

    @Inject
    private ExecutionReportEventPublisher executionReportEventPublisher;

    @Async
    @Override
    public void calculateKpis(@NonNull final KpiCalculationJob kpiCalculationJob) {
        final String executionGroup = kpiCalculationJob.getExecutionGroup();
        final UUID calculationId = kpiCalculationJob.getCalculationId();

        final long startTimeMillis = sparkMeterService.meterCalculationStart(executionGroup, calculationId);

        try {
            log.info("Launching Spark configuration for KPI calculation");
            calculationLauncher.launchCalculation(kpiCalculationJob);
        } catch (final Exception e) {
            log.error(KpiCalculatorErrorCode.KPI_CALCULATION_ERROR.getErrorMessage(), e);
            kpiCalculationMediator.removeRunningCalculation(calculationId);
            calculationService.updateCompletionState(calculationId, FAILED);
            sparkMeterService.meterCalculationDuration(startTimeMillis, executionGroup, calculationId, FAILED);
            tabularParameterFacade.deleteTables(kpiCalculationJob);
            throw new KpiCalculatorException(KpiCalculatorErrorCode.KPI_CALCULATION_ERROR, e);
        }

        if (kpiCalculationJob.isComplex()) {
            complexReliabilityThresholdFacade.persistComplexReliabilityThreshold(kpiCalculationJob);
        }

        if (calculationService.forceFindByCalculationId(calculationId) == NOTHING_CALCULATED) {
            kpiCalculationMediator.removeRunningCalculation(calculationId);
            sparkMeterService.meterCalculationDuration(startTimeMillis, executionGroup, calculationId, NOTHING_CALCULATED);
            return;
        }

        readinessLogManagerFacade.persistComplexReadinessLog(kpiCalculationJob);
        kpiCalculationMediator.removeRunningCalculation(calculationId);
        calculationService.updateCompletionState(calculationId, FINALIZING);
        sparkMeterService.meterCalculationDuration(startTimeMillis, executionGroup, calculationId, FINALIZING);
        tabularParameterFacade.deleteTables(kpiCalculationJob);
        executionReportEventPublisher.pushEvent(calculationId);
    }
}