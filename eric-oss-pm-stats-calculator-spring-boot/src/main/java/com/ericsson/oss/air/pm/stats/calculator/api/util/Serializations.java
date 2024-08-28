/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.util;

import static lombok.AccessLevel.PRIVATE;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.SerializationException;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public final class Serializations {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String writeValueAsString(final Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (final Exception e) {
            throw new SerializationException(e);
        }
    }
}
