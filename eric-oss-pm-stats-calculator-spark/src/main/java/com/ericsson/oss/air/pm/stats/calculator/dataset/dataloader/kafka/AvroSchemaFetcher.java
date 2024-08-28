/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.kafka;

import java.util.Objects;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.SchemaRegistryException;
import com.ericsson.oss.air.pm.stats.calculator.configuration.property.CalculationProperties;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.kafka.debug.SchemaLogger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import za.co.absa.abris.config.AbrisConfig;
import za.co.absa.abris.config.FromAvroConfig;

@Slf4j
@Component
@RequiredArgsConstructor
@CacheConfig(cacheNames = "FromAvroConfig")
public class AvroSchemaFetcher {

    private final CalculationProperties calculationProperties;
    private final SchemaLogger schemaLogger;

    /**
     * Download the AVRO schema from <strong>Schema Registry</strong> component.
     *
     * @param schemaName
     *         name of the schema to download.
     * @return {@link FromAvroConfig} containing AVRO schema for the defined parameters.
     * @throws SchemaRegistryException if fails to retrieve AVRO schema or it is null
     */
    @Cacheable(key = "#schemaName + '-' + #namespace")
    public FromAvroConfig fetchAvroSchema(final String schemaName, final String namespace) throws SchemaRegistryException {

        FromAvroConfig avroConfig;
        try {
            log.info("Starting to retrieve avro schema for schema: {}", schemaName);
            avroConfig = Objects.requireNonNull(AbrisConfig.fromConfluentAvro()
                    .downloadReaderSchemaByLatestVersion()
                    .andRecordNameStrategy(schemaName, namespace)
                    .usingSchemaRegistry(calculationProperties.getSchemaRegistryUrl()), "Failed to retrieve avro schema (schema is null)");
        } catch (final Exception e) {
            log.error("Failed to retrieve avro schema: {}", schemaName);
            throw new SchemaRegistryException("Failed to retrieve avro schema", e);
        }
        schemaLogger.logSchema(avroConfig);

        return avroConfig;
    }
}
