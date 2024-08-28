/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.startup;

import javax.annotation.PostConstruct;

import com.ericsson.oss.air.pm.stats.calculation.TabularParameterFacade;
import com.ericsson.oss.air.pm.stats.calculator.configuration.CalculatorProperties;
import com.ericsson.oss.air.pm.stats.calculator.configuration.DatasourceConfigLoader;
import com.ericsson.oss.air.pm.stats.common.scheduler.ActivitySchedulerException;
import com.ericsson.oss.air.pm.stats.model.exception.StartupException;
import com.ericsson.oss.air.pm.stats.scheduler.api.KpiCalculatorScheduler;
import com.ericsson.oss.air.pm.stats.service.api.CalculationService;

import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 * Startup bean for <code>eric-oss-pm-stats-calculator</code>.
 * <p>
 * Schedules the KPI calculation when the KPI definitions have been previously persisted to the database.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@DependsOn("flywayIntegration")
public class KpiStartupService {
    private final CalculationService calculationService;
    private final TabularParameterFacade tabularParameterFacade;
    private final DatasourceConfigLoader datasourceConfigLoader;
    private final CalculatorProperties calculatorProperties;
    private final KpiCalculatorScheduler kpiCalculatorScheduler;

    private final Retry updateRunningCalculationsToLostRetry;

    @PostConstruct
    public void onServiceStart() throws StartupException {
        log.info("Starting eric-oss-pm-stats-calculator");

        calculationService.updateRunningCalculationsToLost(updateRunningCalculationsToLostRetry);
        tabularParameterFacade.deleteLostTables();
        datasourceConfigLoader.populateDatasourceRegistry();

        try {
            kpiCalculatorScheduler.scheduleKpiCalculation(calculatorProperties.getKpiExecutionPeriod());
        } catch (final ActivitySchedulerException e) {
            throw new StartupException(e);
        }

        log.info("Started eric-oss-pm-stats-calculator");
    }

}
