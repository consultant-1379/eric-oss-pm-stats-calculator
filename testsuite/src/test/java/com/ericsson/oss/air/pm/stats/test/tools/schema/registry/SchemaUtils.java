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

import static java.nio.file.Files.readAllLines;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.avro.Schema;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaUtils {

    public static Schema parseSchema(final String schemaFileName) {
        try {
            final List<String> file = readAllLines(Path.of(schemaFileName));
            final String schema = String.join(System.lineSeparator(), file);
            return new Schema.Parser().setValidate(true).parse(schema);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}