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

import com.ericsson.oss.air.pm.stats.service.api.KpiExposureService;
import com.ericsson.oss.air.pm.stats.service.facade.KafkaOffsetCheckerFacade;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 * Startup bean to avoid Kafka usage in <code>KpiStartupService</code> bean.
 */

@Component
@AllArgsConstructor
@DependsOn({"kpiExposureStartupService", "flywayIntegration"})
public class KafkaKpiStartupServiceMediator {
    private KafkaOffsetCheckerFacade kafkaOffsetCheckerFacade;
    private KpiExposureService kpiExposureService;

    @PostConstruct
    public void callKafkaStartupRelatedMethods() {
        kafkaOffsetCheckerFacade.compareDatabaseToKafka();
        kpiExposureService.updateExposure();
    }
}
