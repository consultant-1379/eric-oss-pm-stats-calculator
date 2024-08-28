/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.startup;

import javax.annotation.PostConstruct;

import com.ericsson.oss.air.pm.stats.calculation.event.SendingReportToExporterEvent;
import com.ericsson.oss.air.pm.stats.service.api.CalculationService;
import com.ericsson.oss.air.pm.stats.service.exporter.ExecutionReportSender;
import com.ericsson.oss.air.pm.stats.service.exporter.ExecutionReportTopicCreator;
import com.ericsson.oss.air.pm.stats.service.exporter.util.KpiExporterException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 * Startup service to send execution report for calculations that has not been exported.
 */
@Slf4j
@Component
@AllArgsConstructor
@DependsOn("flywayIntegration")
public class KpiExecutionReportStartupService {
    private ExecutionReportTopicCreator executionReportTopicCreator;
    private ExecutionReportSender executionReportSender;
    private CalculationService calculationService;

    //TODO Convert this service into a scheduled job
    @PostConstruct
    public void onServiceStart() {
        executionReportTopicCreator.createTopic();

        calculationService.findCalculationReadyToBeExported().forEach(calculation -> {
            log.info("Starting to export calculation with id: '{}'", calculation.getCalculationId());
            try {
                executionReportSender.sendExecutionReport(SendingReportToExporterEvent.of(calculation.getCalculationId()));
            } catch (final KpiExporterException e) {
                log.error(String.valueOf(e));
            }
        });
    }
}
