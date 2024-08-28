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

import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.KPI_SERVICE_DB_DATASOURCE_TYPE;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.KPI_SERVICE_DB_EXPRESSION_TAG;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.ON_DEMAND_EXECUTOR_CORES;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.ON_DEMAND_EXECUTOR_MEMORY;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.OVERRIDE_EXECUTOR_JMX_PORT;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.SAVE_EVENT_LOGS;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.SPARK_ADAPTIVE_SQL;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.SPARK_MAX_REGISTERED_RESOURCES_WAITING_TIME;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.SPARK_MIN_REGISTERED_RESOURCES_RATIO;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.SPARK_PARALLELISM;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.SPARK_SHUFFLE_PARTITIONS;
import static com.ericsson.oss.air.pm.stats.calculator.environment.reader.EnvironmentValue.values;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.common.model.DatasourceType;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

class EnvironmentValueTest {
    //  Mandatory - no default environment variables
    private static final String SPARK_EXECUTOR_CORES = "SPARK_EXECUTOR_CORES";
    private static final String SPARK_EXECUTOR_MEMORY = "SPARK_EXECUTOR_MEMORY";
    private static final String SPARK_MASTER_URL = "SPARK_MASTER_URL";
    private static final String SPARK_DRIVER_MEMORY = "SPARK_DRIVER_MEMORY";
    private static final String SPARK_MAX_CORES = "SPARK_MAX_CORES";
    private static final String SPARK_DRIVER_HOST = "SPARK_DRIVER_HOST";
    private static final String SPARK_DRIVER_BINDADDRESS = "SPARK_DRIVER_BINDADDRESS";
    private static final String KPI_SERVICE_DB_USER = "KPI_SERVICE_DB_USER";
    private static final String KPI_SERVICE_DB_PASSWORD = "KPI_SERVICE_DB_PASSWORD";
    private static final String KPI_SERVICE_DB_DRIVER = "KPI_SERVICE_DB_DRIVER";
    private static final String KPI_SERVICE_DB_JDBC_CONNECTION = "KPI_SERVICE_DB_JDBC_CONNECTION";
    private static final int ENV_VALUE_EXPECTED_COUNT = 24;

    @Test
    void shouldVerifyEnvironmentValuesSize() {
        Assertions.assertThat(values())
                  .as("environment value was added, consider adding it to the test as well")
                  .hasSizeLessThanOrEqualTo(ENV_VALUE_EXPECTED_COUNT);
        Assertions.assertThat(values())
                  .as("environment value was removed, consider removing from the test as well")
                  .hasSizeGreaterThanOrEqualTo(ENV_VALUE_EXPECTED_COUNT);
    }

    @Nested
    @DisplayName("when environment value has no default")
    class WhenEnvironmentValueHasNoDefault {

        @NoDefaults
        @ParameterizedTest(name = "[{index}] ''{0}'' has no default value")
        void shouldReturnFalseWhenEnvironmentValueHasNoDefault(final EnvironmentValue environmentValue) {
            Assertions.assertThat(environmentValue.hasDefaultValue()).isFalse();
        }

        @NoDefaults
        @ParameterizedTest(name = "[{index}] Cannot get default value of: ''{0}''")
        void shouldThrowUnsupportedOperationExceptionWhenEnvironmentValueHasNoDefault_andGetValueIsCalled(final EnvironmentValue environmentValue) {
            Assertions.assertThatThrownBy(environmentValue::getDefaultValue)
                      .isInstanceOf(UnsupportedOperationException.class)
                      .hasMessage("'%s' has no default value.", environmentValue.name());
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("when environment value has default")
    class WhenEnvironmentValueHasDefault {

        @Defaults
        @ParameterizedTest(name = "[{index}] ''{0}'' has default value")
        void shouldReturnTrueWhenEnvironmentValueHasDefault(final EnvironmentValue environmentValue) {
            Assertions.assertThat(environmentValue.hasDefaultValue()).isTrue();
        }

        @MethodSource("provideDefaultValuesData")
        @ParameterizedTest(name = "[{index}] ''{0}'' has default: ''{1}''")
        void shouldGetDefaultValue(final EnvironmentValue environmentValue, final String expected) {
            Assertions.assertThat(EnvironmentValueReader.readEnvironmentFor(environmentValue)).isEqualTo(expected);
        }

        private Stream<Arguments> provideDefaultValuesData() {
            return Stream.of(Arguments.of(SPARK_PARALLELISM, "12"),
                    Arguments.of(SPARK_MIN_REGISTERED_RESOURCES_RATIO, "0.0"),
                    Arguments.of(SPARK_MAX_REGISTERED_RESOURCES_WAITING_TIME, "30s"),
                    Arguments.of(SPARK_SHUFFLE_PARTITIONS, "12"),
                    Arguments.of(SPARK_ADAPTIVE_SQL, "true"),
                    Arguments.of(SAVE_EVENT_LOGS, "false"),
                    Arguments.of(OVERRIDE_EXECUTOR_JMX_PORT, "false"),
                    Arguments.of(KPI_SERVICE_DB_PASSWORD, ""),
                    Arguments.of(KPI_SERVICE_DB_DATASOURCE_TYPE, DatasourceType.FACT.name()),
                    Arguments.of(KPI_SERVICE_DB_EXPRESSION_TAG, Datasource.KPI_DB.getName()));
        }

        @Nested
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("and depending on other environment value")
        class AndDependingOnOtherEnvironmentValue {
            private static final String EXPECTED_VALUE = "expectedValue";

            @Test
            @SetEnvironmentVariable(key = "SPARK_EXECUTOR_MEMORY", value = EXPECTED_VALUE)
            void shouldValidateDefault_ON_DEMAND_EXECUTOR_MEMORY() {
                Assertions.assertThat(EnvironmentValueReader.readEnvironmentFor(ON_DEMAND_EXECUTOR_MEMORY)).isEqualTo(EXPECTED_VALUE);
            }

            @Test
            @SetEnvironmentVariable(key = "SPARK_EXECUTOR_CORES", value = EXPECTED_VALUE)
            void shouldValidateDefault_ON_DEMAND_EXECUTOR_CORES() {
                Assertions.assertThat(EnvironmentValueReader.readEnvironmentFor(ON_DEMAND_EXECUTOR_CORES)).isEqualTo(EXPECTED_VALUE);
            }

            @MethodSource("provideOtherDependencyData")
            @ParameterizedTest(name = "[{index}] Environment variable: ''{0}'' depending on: ''{1}'', but ''{1}'' is not set")
            void shouldThrowNullPointerException_whenOtherDependencyIsNotSet(final EnvironmentValue parent, final EnvironmentValue child) {
                Assertions.assertThatThrownBy(() -> EnvironmentValueReader.readEnvironmentFor(parent))
                          .isInstanceOf(NullPointerException.class)
                          .hasMessage("'%1$s' depending on '%2$s', but '%2$s' has no default value.",
                                      parent.name(),
                                      child.name());
            }

            private Stream<Arguments> provideOtherDependencyData() {
                return Stream.of(Arguments.of(ON_DEMAND_EXECUTOR_CORES, SPARK_EXECUTOR_CORES),
                                 Arguments.of(ON_DEMAND_EXECUTOR_MEMORY, SPARK_EXECUTOR_MEMORY));
            }
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @EnumSource(value = EnvironmentValue.class, mode = Mode.INCLUDE, names = {
            SPARK_EXECUTOR_MEMORY,
            SPARK_EXECUTOR_CORES,
            SPARK_MASTER_URL,
            SPARK_DRIVER_MEMORY,
            SPARK_MAX_CORES,
            SPARK_DRIVER_HOST,
            SPARK_DRIVER_BINDADDRESS,
            KPI_SERVICE_DB_USER,
            KPI_SERVICE_DB_DRIVER,
            KPI_SERVICE_DB_JDBC_CONNECTION
    })
    @interface NoDefaults {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @EnumSource(value = EnvironmentValue.class, mode = Mode.EXCLUDE, names = {
            SPARK_EXECUTOR_MEMORY,
            SPARK_EXECUTOR_CORES,
            SPARK_MASTER_URL,
            SPARK_DRIVER_MEMORY,
            SPARK_MAX_CORES,
            SPARK_DRIVER_HOST,
            SPARK_DRIVER_BINDADDRESS,
            KPI_SERVICE_DB_USER,
            KPI_SERVICE_DB_DRIVER,
            KPI_SERVICE_DB_JDBC_CONNECTION
    })
    @interface Defaults {}
}