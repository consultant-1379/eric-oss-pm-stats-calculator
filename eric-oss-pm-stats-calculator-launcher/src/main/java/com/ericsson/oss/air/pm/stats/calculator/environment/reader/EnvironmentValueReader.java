/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.environment.reader;

import com.ericsson.oss.air.pm.stats.common.env.Environment;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EnvironmentValueReader {

    /**
     * Reads environment value.
     *
     * @param environmentValue
     *         {@link EnvironmentValue} to read
     * @return returns the value associated with the environment value or its default
     * @implNote The implementation is building on
     * <br>
     * {@link Environment#getEnvironmentValue(String, String)} and
     * <br>
     * {@link Environment#getEnvironmentValue(String)}
     */
    public static String readEnvironmentFor(final EnvironmentValue environmentValue) {
        return environmentValue.hasDefaultValue()
                ? Environment.getEnvironmentValue(environmentValue.name(), environmentValue.getDefaultValue())
                : Environment.getEnvironmentValue(environmentValue.name());
    }

    /**
     * Reads environment value and converts it to <strong>boolean</strong>.
     *
     * @param environmentValue
     *         {@link EnvironmentValue} to read
     * @return returns the value associated with the environment value or its default
     * @implNote The implementation is building on {@link #readEnvironmentFor(EnvironmentValue)}.
     */
    public static boolean readAsBooleanEnvironmentFor(final EnvironmentValue environmentValue) {
        return Boolean.parseBoolean(readEnvironmentFor(environmentValue));
    }
}
