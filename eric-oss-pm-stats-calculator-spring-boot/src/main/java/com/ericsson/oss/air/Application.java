/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air;


import com.ericsson.oss.air.pm.stats.calculation.ReliabilityThresholdCalculator;
import com.ericsson.oss.air.pm.stats.calculation.limits.period.creator.api.AggregationPeriodCreator;
import com.ericsson.oss.air.pm.stats.calculation.start.time.api.StartTimeCalculator;
import com.ericsson.oss.air.pm.stats.service.api.registry.ReadinessLogRegistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.plugin.core.config.EnablePluginRegistries;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "com.ericsson.oss.air.pm.stats.calculator.configuration")
@EnablePluginRegistries({AggregationPeriodCreator.class, ReliabilityThresholdCalculator.class, StartTimeCalculator.class, ReadinessLogRegistry.class})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}