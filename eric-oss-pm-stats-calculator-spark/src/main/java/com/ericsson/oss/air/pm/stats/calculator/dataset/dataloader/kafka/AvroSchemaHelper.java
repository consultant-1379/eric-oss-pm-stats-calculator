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

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;
import org.springframework.stereotype.Component;
import za.co.absa.abris.config.FromAvroConfig;

@Slf4j
@Component
public class AvroSchemaHelper {

    public Schema parseSchema(@NonNull final FromAvroConfig avroConfig) {
        final Parser parser = new Parser();
        return parser.parse(avroConfig.schemaString());
    }
}
