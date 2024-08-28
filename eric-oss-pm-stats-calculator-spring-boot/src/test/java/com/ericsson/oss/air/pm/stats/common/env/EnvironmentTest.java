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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EnvironmentTest {

    private static final String TEST_PROPERTY = "testProperty";
    private static final String TEST_PROPERTY_VALUE = "testPropertyValue";

    @BeforeEach
    void setUp() {
        System.setProperty(TEST_PROPERTY, TEST_PROPERTY_VALUE);
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(TEST_PROPERTY);
    }

    @CsvSource({
            "testEnvironment,testEnvironmentValue",
            "testProperty,testPropertyValue"
    })
    @ParameterizedTest(name = "[{index}] For environment key of: {0} the value: {1} is returned.")
    void shouldReturnEnvironmentValue(final String environmentKey, final String expected) {
        Assertions.assertThat(Environment.getEnvironmentValue(environmentKey)).isEqualTo(expected);
    }

    @CsvSource({
            "testEnvironment,testEnvironmentValue",
            "testProperty,testPropertyValue",
            "nonExistingKey,defaultValue"
    })
    @ParameterizedTest(name = "[{index}] For environment key of: {0} the value: {1} is returned.")
    void shouldReturnEnvironmentValue_withDefaultValue(final String environmentKey, final String expected) {
        Assertions.assertThat(Environment.getEnvironmentValue(environmentKey, "defaultValue")).isEqualTo(expected);
    }
}