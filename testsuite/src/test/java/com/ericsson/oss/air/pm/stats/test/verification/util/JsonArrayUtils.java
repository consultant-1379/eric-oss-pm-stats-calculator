/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.verification.util;

import java.util.Arrays;
import java.util.stream.IntStream;

import com.google.gson.JsonArray;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonArrayUtils {

    public static JsonArray create(final Number... value) {
        final JsonArray jsonArray = new JsonArray();

        Arrays.stream(value)
              .forEach(jsonArray::add);

        return jsonArray;
    }

    public static JsonArray nCopies(final int n, final Number value) {
        final JsonArray jsonArray = new JsonArray();

        IntStream.range(0, n)
                 .mapToObj(i -> value)
                 .forEach(jsonArray::add);

        return jsonArray;
    }

}
