/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.tools.producer.serializer;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public final class FloatArraySerializer extends StdSerializer<String> {
    public FloatArraySerializer() {
        super(String.class);
    }

    @Override
    public void serialize(final String row,
                          final JsonGenerator jsonGenerator,
                          final SerializerProvider serializerProvider) throws IOException {
        if (row == null) {
            jsonGenerator.writeNull();
        } else {
            jsonGenerator.writeObject(Arrays.stream(row.replace("{","")
                            .replace("}","")
                            .split(","))
                    .map(Double::parseDouble)
                    .collect(Collectors.toList()));
        }
    }
}
