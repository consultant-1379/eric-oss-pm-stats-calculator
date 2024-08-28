/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.configuration.property;

import java.time.Duration;

import com.ericsson.oss.air.pm.stats.calculator.configuration.property.converter.StringToDurationConverter;
import com.ericsson.oss.air.pm.stats.calculator.configuration.property.util.ConfigurationLoggers;
import com.ericsson.oss.air.pm.stats.calculator.configuration.property.util.ConfigurationLoggers.NestedConfiguration;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Slf4j
@Getter
@ToString
@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "pm-stats-calculator.calculation")
public class CalculationProperties implements InitializingBean {
    private final Duration scheduleIncrement;
    private final Duration endOfExecutionOffset;
    private final Duration maxLookBackPeriod;

    private final String indexedNumericPartitionColumns;

    @Getter(AccessLevel.NONE)
    private final Boolean partitionTableRead;

    private final Integer sparkParallelism;

    private final String schemaRegistryUrl;

    @NestedConfiguration
    private final KafkaProperties kafka;

    public boolean isAllMaxLookBackPeriod() {
        return maxLookBackPeriod.equals(StringToDurationConverter.ALL);
    }

    public boolean isPartitionTableRead() {
        return partitionTableRead;
    }

    public boolean isPartitionColumnProvided() {
        if (StringUtils.isEmpty(indexedNumericPartitionColumns)) {
            log.warn("No partition column specified, database reads will not use partitions");
            return false;
        }

        return true;
    }

    public boolean isPartitionDatasetLoad() {
        return isPartitionTableRead() && isPartitionColumnProvided();
    }

    public Integer getKafkaBucketSize() {
        return kafka.getBucketSize();
    }

    public String getKafkaBootstrapServers() {
        return kafka.getBootstrapServers();
    }

    @Override
    public void afterPropertiesSet() {
        log.info(
                "Calculation properties:{}{}",
                System.lineSeparator(),
                ConfigurationLoggers.createLog(this)
        );
    }

    @Data
    @RequiredArgsConstructor
    public static class KafkaProperties {
        private final Integer bucketSize;
        private final String bootstrapServers;
    }
}
