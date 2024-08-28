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

import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.SPARK_EXECUTOR_CORES;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.SPARK_PARALLELISM;
import static org.mockito.Mockito.mockStatic;

import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.common.env.Environment;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.Verification;

class EnvironmentValueReaderTest {
    private static final String ENVIRONMENT_SPARK_EXECUTOR_CORES = "SPARK_EXECUTOR_CORES";
    private static final String ENVIRONMENT_SPARK_PARALLELISM = "SPARK_PARALLELISM";
    private static final String DEFAULT_SPARK_EXECUTORS_CORES = "12";

    @Nested
    @DisplayName("should read environment for")
    class ShouldReadEnvironmentFor {
        @Nested
        @DisplayName("when environment variable is set")
        class WhenEnvironmentVariableIsSet {
            @Test
            @SetEnvironmentVariable(key = ENVIRONMENT_SPARK_EXECUTOR_CORES, value = DEFAULT_SPARK_EXECUTORS_CORES)
            void andEnvironmentVariableHasNoDefault() {
                final String actual = EnvironmentValueReader.readEnvironmentFor(SPARK_EXECUTOR_CORES);
                Assertions.assertThat(actual).isEqualTo(DEFAULT_SPARK_EXECUTORS_CORES);
            }

            @Test
            @SetEnvironmentVariable(key = ENVIRONMENT_SPARK_PARALLELISM, value = "15")
            void andEnvironmentVariableHasDefault() {
                final String actual = EnvironmentValueReader.readEnvironmentFor(SPARK_PARALLELISM);
                Assertions.assertThat(actual).isEqualTo("15");
            }
        }

        @Nested
        @DisplayName("when environment variable is not set")
        class WhenEnvironmentVariableIsNotSet {
            @Test
            void andEnvironmentVariableHasNoDefault() {
                final String actual = EnvironmentValueReader.readEnvironmentFor(SPARK_EXECUTOR_CORES);
                Assertions.assertThat(actual).isNull();
            }

            @Test
            void andEnvironmentVariableHasDefault() {
                final String actual = EnvironmentValueReader.readEnvironmentFor(SPARK_PARALLELISM);
                Assertions.assertThat(actual).isEqualTo("12");
            }
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("should read as boolean environment for")
    class ShouldReadAsBooleanEnvironmentFor {

        @MethodSource("provideBooleanValueData")
        @ParameterizedTest(name = "[{index}] For environment value: ''{0}'' boolean ''{1}'' returned")
        void verifyBooleanValue(final String environmentValue, final boolean expected) {
            try (final MockedStatic<Environment> environmentMockedStatic = mockStatic(Environment.class)) {
                final Verification verification = () -> Environment.getEnvironmentValue("SPARK_EXECUTOR_CORES");
                environmentMockedStatic.when(verification).thenReturn(environmentValue);
                Assertions.assertThat(EnvironmentValueReader.readAsBooleanEnvironmentFor(SPARK_EXECUTOR_CORES)).isEqualTo(expected);
            }
        }

        private Stream<Arguments> provideBooleanValueData() {
            return Stream.of(Arguments.of(null, false),
                             Arguments.of("false", false),
                             Arguments.of("randomValue", false),
                             Arguments.of("true", true));
        }
    }

}