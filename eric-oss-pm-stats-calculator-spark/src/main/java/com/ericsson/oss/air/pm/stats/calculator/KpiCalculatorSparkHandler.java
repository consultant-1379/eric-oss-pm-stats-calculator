/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator;

import java.time.LocalDateTime;
import java.util.List;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiCalculatorException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.ReadinessLog;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.facade.ReadinessLogFacadeImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.CalculationService;
import com.ericsson.oss.air.pm.stats.calculator.service.SparkServiceImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.logging.mdc.MDCRegistrarImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.reliability.OnDemandReliabilityRegister;
import com.ericsson.oss.air.pm.stats.calculator.util.MessageChecker;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Summary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Spark driver application for KPI calculation.
 * <p>
 * The full name of this class (package name and class name) is referenced by the Spark Launcher, and the <b>main</b> method of this class is used as
 * the entry point by the Spark workers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public final class KpiCalculatorSparkHandler {
    private static final CollectorRegistry COLLECTOR_REGISTRY = new CollectorRegistry();
    private static final String METRIC_NAME = "kpi_calculation_time";

    private final KpiCalculatorSpark kpiCalculatorSpark;

    private final ReadinessLogFacadeImpl readinessLogFacade;
    private final OnDemandReliabilityRegister onDemandReliabilityRegister;

    private final SparkServiceImpl sparkService;
    private final CalculationService calculationService;

    private final MDCRegistrarImpl mdcRegistrar;
    private final MessageChecker messageChecker;

    /**
     * Starts the {@link KpiCalculatorSpark}, which calculates the KPIs.
     *
     * @throws KpiCalculatorException
     *             this exception is thrown if the KPI calculation spark job fails
     */
    @EventListener(ApplicationReadyEvent.class)
    public void startCalculation() {
        calculationService.updateCurrentCalculationState(KpiCalculationState.IN_PROGRESS);

        final List<String> kpiDefinitionNames = sparkService.getKpisToCalculate();
        if (kpiDefinitionNames.isEmpty()) {
            calculationService.updateCurrentCalculationStateAndTime(KpiCalculationState.NOTHING_CALCULATED, LocalDateTime.now());
            log.error("No KPIs to calculate, KPI calculation will not be launched");
            return;
        }

        if (sparkService.isScheduledSimple() && !messageChecker.hasNewMessage()) {
            calculationService.updateCurrentCalculationStateAndTime(KpiCalculationState.NOTHING_CALCULATED, LocalDateTime.now());
            return;
        }

        log.info("Start KPI calculation in Spark for execution group '{}', id: '{}'", sparkService.getExecutionGroup(), sparkService.getCalculationId());

        final String metricMessage = String.format("Time to complete %s KPI calculation", sparkService.getExecutionGroup());
        final Summary calculationTime = Summary.build().name(METRIC_NAME).help(metricMessage)
                .register(COLLECTOR_REGISTRY);
        final Summary.Timer durationTimer = calculationTime.startTimer();

        calculateKpis();

        durationTimer.observeDuration();
    }

    private void calculateKpis() {
        if (log.isInfoEnabled()) {
            log.info("Spark App ID: {}, Calculation Id: {} for the KPI Calculation job", sparkService.getApplicationId(), sparkService.getCalculationId());
        }

        mdcRegistrar.registerLoggingKeys();

        kpiCalculatorSpark.calculate();
        if (sparkService.isScheduledSimple()) {
            final List<ReadinessLog> readinessLogs = readinessLogFacade.persistReadinessLogs();
            if (readinessLogs.isEmpty()) {
                calculationService.updateCurrentCalculationStateAndTime(KpiCalculationState.NOTHING_CALCULATED, LocalDateTime.now());
            }
        } else if (sparkService.isOnDemand()) {
            onDemandReliabilityRegister.persistOnDemandReliabilities();
        }

        log.info("KPI calculation completed for execution group '{}', id: {}", sparkService.getExecutionGroup(), sparkService.getCalculationId());
    }
}
