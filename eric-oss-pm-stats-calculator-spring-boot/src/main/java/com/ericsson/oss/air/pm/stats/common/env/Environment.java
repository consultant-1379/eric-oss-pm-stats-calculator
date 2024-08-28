/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.env;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * A utility class for retrieving environment/system values.
 *
 * @see System#getProperty(String)
 * @see System#getenv(String)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Environment {

    /**
     * Returns the {@code propertyName} specified if it is set as a {@link System} property or as a {@link System} environment variable. The order of
     * the search is property first then environment variable.
     * <p>
     * If neither is set then null is returned.
     *
     * @param propertyName
     *            the property to search for
     * @return the value of the searched property, otherwise {@code null}
     */
    public static String getEnvironmentValue(final String propertyName) {
        return System.getProperty(propertyName, System.getenv(propertyName));
    }

    /**
     * Returns the {@code propertyName} specified if it is set as a {@link System} property or as a {@link System} environment variable. The order of
     * the search is property first then environment variable.
     * <p>
     * If neither is set then the default value provided is returned.
     *
     * @param propertyName
     *            the property name to search for
     * @param defaultValue
     *            the default value if no property exists
     * @return The value of the searched name, otherwise the {@code defaultValue}
     */
    public static String getEnvironmentValue(final String propertyName, final String defaultValue) {
        final String envVariable = System.getenv(propertyName);
        if (envVariable != null) {
            return envVariable;
        }
        return System.getProperty(propertyName, defaultValue);
    }
}
