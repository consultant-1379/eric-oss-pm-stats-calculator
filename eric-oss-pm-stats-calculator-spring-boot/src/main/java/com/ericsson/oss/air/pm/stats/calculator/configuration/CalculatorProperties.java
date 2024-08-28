/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Slf4j
@Getter
@ToString
@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "pm-stats-calculator") //TODO: temporarily flat structure while migration is happening
public class CalculatorProperties {
    private final Integer retentionConfiguredMax;
    private final Integer retentionPeriodDays;

    private final String kpiExecutionPeriod;
    private final String cronRetentionPeriodCheck;

    private final Integer maxNumberOfParallelOnDemandCalculations;
    private final Boolean blockScheduledWhenHandlingOnDemand;
    private final Integer maxHeartbeatToWaitToRecalculateSimples;
    private final Integer queueWeightOnDemandCalculation;
    private final Integer queueWeightScheduledCalculation;
    private final Integer maximumConcurrentCalculations;

    private final Boolean inputSource;
    private final Boolean aggregationPeriod;
    private final Boolean aggregationElements;

    private final String kpiServiceDbUser;
    private final String kpiServiceDbPassword;
    private final String kpiServiceDbDriver;
    private final String kpiServiceDbJdbcConnection;

    private final String kafkaBootstrapServers;
    private final String kafkaExposureTopicName;
    private final String kafkaExecutionReportTopicName;

    private final String sparkMasterUrl;
    private final Integer sparkExecutorStartingPort;

    private final String schemaRegistryUrl;
    private final String dataCatalogUrl;

    private final Long sparkMetricRetentionInSeconds;

}