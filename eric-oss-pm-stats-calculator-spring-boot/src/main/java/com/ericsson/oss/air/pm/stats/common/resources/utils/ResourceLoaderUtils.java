/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.resources.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;

/**
 * This class defines utilities for loading resources.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceLoaderUtils {

    /**
     * Method used to get a resource as an {@link InputStream} from a given classpath.
     *
     * @param path
     *            the path to the resource you want to load
     * @return the loaded resource as an {@link InputStream}
     * @throws IllegalArgumentException
     *             thrown if the supplied path has no resource to load
     */
    public static InputStream getClasspathResourceAsStream(final String path) {
        final InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);

        if (inputStream == null) {
            throw new IllegalArgumentException(String.format("InputStream is null for path '%s'", path));
        }

        return inputStream;
    }

    /**
     * Method used to get a resource as a {@link Properties} from a given classpath.
     *
     * @param path
     *            the path to the resource you want to load
     * @return the loaded resource as a {@link Properties}
     * @throws IllegalArgumentException
     *             thrown if the supplied path has no resource to load
     * @throws IOException
     *             thrown if there is any error loading the file or converting it
     */
    public static Properties getClasspathResourceAsProperties(final String path) throws IOException {
        try (final InputStream inputStream = getClasspathResourceAsStream(path)) {
            return convertStreamToProperties(inputStream);
        }
    }

    private static Properties convertStreamToProperties(final InputStream inputStream) throws IOException {
        final Properties properties = new Properties();
        properties.load(inputStream);
        return properties;
    }

    /**
     * Method used to get a resource as a {@link String} from a given classpath.
     *
     * @param path
     *            the path to the resource you want to load
     * @return the loaded resource as a {@link String}, or an empty {@link String}
     * @throws IllegalArgumentException
     *             thrown if the supplied path has no resource to load
     * @throws IOException
     *             thrown if there is any error loading the file or converting it
     */
    public static String getClasspathResourceAsString(final String path) throws IOException {
        try (final InputStream inputStream = getClasspathResourceAsStream(path)) {
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        }
    }
}
