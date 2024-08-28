/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.tools.schema.registry;

import java.io.IOException;

import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.avro.Schema;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaRegister {

    private static final SchemaRegistryClient client = new CachedSchemaRegistryClient("http://schemaregistry:8081",1);

    public static void register(final String schemaFileName) {
        Schema schema = SchemaUtils.parseSchema(schemaFileName);
        try {
            client.register(schema.getFullName(), new AvroSchema(schema));
        } catch (IOException | RestClientException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Schema registered successfully");
    }
}