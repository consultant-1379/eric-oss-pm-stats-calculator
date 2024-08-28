/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Properties;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PropertiesUtils {
    public static Properties copyOf(@NonNull final Properties properties) {
        final Properties copy = new Properties();

        properties.forEach((key, value) -> {
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(value, "value");

            copy.setProperty(String.valueOf(key), String.valueOf(value));
        });

        return copy;
    }
}
