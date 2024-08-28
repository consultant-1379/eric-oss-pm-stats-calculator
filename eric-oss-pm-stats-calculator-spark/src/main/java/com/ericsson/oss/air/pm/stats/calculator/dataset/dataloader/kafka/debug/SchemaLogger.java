/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.kafka.debug;

import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.kafka.AvroSchemaHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.springframework.stereotype.Component;
import za.co.absa.abris.config.FromAvroConfig;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaLogger {
    private final AvroSchemaHelper avroSchemaHelper;

    public void logSchema(final FromAvroConfig avroConfig) {
        if (log.isDebugEnabled()) {
            final Schema schema = avroSchemaHelper.parseSchema(avroConfig);
            log.debug(
                    "Schema registry contained schema for name '{}' and namespace '{}' is:{}{}",
                    schema.getName(),
                    schema.getNamespace(),
                    System.lineSeparator(),
                    schema.toString(true)
            );
        }
    }
}
