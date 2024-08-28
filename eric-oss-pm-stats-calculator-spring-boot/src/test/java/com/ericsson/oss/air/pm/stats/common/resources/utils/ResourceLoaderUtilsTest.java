/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.resources.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ResourceLoaderUtils}.
 */
class ResourceLoaderUtilsTest {

    @Test
    void whenLoadingAClasspathResourceAsString_thenValidContentIsLoaded() throws IOException {
        final String content = ResourceLoaderUtils.getClasspathResourceAsString("schema.json");

        assertThat(content).contains("name_meta_database", "resource", "task", "model", "databaseName", "index");
    }

    @Test
    void whenLoadingAClasspathResourceAsProperties_thenValidContentIsLoaded() throws IOException {
        final Properties properties = ResourceLoaderUtils.getClasspathResourceAsProperties("prop.properties");

        assertThat(properties).containsExactly(entry("key", "value"));
    }

    @Test
    void whenLoadingAClasspathResourceAsInputStream_thenValidContentIsLoaded() throws IOException {
        try (final InputStream inputStream = ResourceLoaderUtils.getClasspathResourceAsStream("inputstream.txt")) {
            final String convertedString = convertStreamToString(inputStream);
            assertThat(convertedString).isEqualTo("inputstream");
        }
    }

    @Test
    void whenLoadingAClasspathResourceAsInputStream_andResourceDoesNotExist_thenIllegalArgumentExceptionIsThrown() {
        assertThatThrownBy(() -> ResourceLoaderUtils.getClasspathResourceAsStream("invalid"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static String convertStreamToString(final InputStream inputStream) {
        try (final Scanner scanner = new Scanner(inputStream).useDelimiter("\\A")) {
            return scanner.hasNext() ? scanner.next() : null;
        }
    }
}