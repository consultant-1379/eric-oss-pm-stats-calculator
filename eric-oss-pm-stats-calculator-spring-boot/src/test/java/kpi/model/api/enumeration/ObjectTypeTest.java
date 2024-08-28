/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.api.enumeration;

import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ObjectTypeTest {
    @MethodSource("provideArgumentData")
    @ParameterizedTest(name = "[{index}] {0} has regex: {1}, postgresType: {2}, columnType: {3}, defaultValue: {4}")
    void shouldVerifyObjectTypeMetadataRemainsTheSame(final ArgumentsAccessor arguments) {
        final ObjectType objectType = (ObjectType) arguments.get(0);
        final String regex = arguments.getString(1);
        final String postgresType = arguments.getString(2);
        final String columnType = arguments.getString(3);
        final String defaultValue = arguments.getString(4);

        Assertions.assertThat(objectType.getRegEx()).isEqualTo(regex);
        Assertions.assertThat(objectType.getPostgresType()).isEqualTo(postgresType);
        Assertions.assertThat(objectType.getColumnType()).isEqualTo(columnType);
        Assertions.assertThat(objectType.getDefaultValue()).isEqualTo(defaultValue);
    }

    @MethodSource("provideJsonNameData")
    @ParameterizedTest(name = "[{index}] {1} converted to: {0}")
    void shouldConvertToJson(final ObjectType objectType, final String wantedValue) {
        Assertions.assertThat(ObjectType.from(wantedValue)).isEqualTo(objectType);
    }

    @MethodSource("provideTypeRegexArgumentData")
    @ParameterizedTest(name = "[{index}] {0}`s regex matches: {1}")
    void shouldTestRegex(final ObjectType objectType, final String value) {
        Assertions.assertThat(value).matches(Pattern.compile(objectType.getRegEx()));
    }

    @Test
    void shouldReturnAllValues() {
        Assertions.assertThat(ObjectType.values()).containsExactlyInAnyOrder(
                ObjectType.POSTGRES_INTEGER_ARRAY_SIZE,
                ObjectType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE,
                ObjectType.POSTGRES_INTEGER,
                ObjectType.POSTGRES_FLOAT_ARRAY_SIZE,
                ObjectType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE,
                ObjectType.POSTGRES_FLOAT,
                ObjectType.POSTGRES_LONG_ARRAY_SIZE,
                ObjectType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE,
                ObjectType.POSTGRES_LONG,
                ObjectType.POSTGRES_REAL,
                ObjectType.POSTGRES_BOOLEAN,
                ObjectType.POSTGRES_STRING,
                ObjectType.POSTGRES_UNLIMITED_STRING,
                ObjectType.POSTGRES_TIMESTAMP
        );
    }

    static Stream<Arguments> provideTypeRegexArgumentData() {
        return Stream.of(
                Arguments.of(ObjectType.POSTGRES_INTEGER_ARRAY_SIZE, "INTEGER[0]"),
                Arguments.of(ObjectType.POSTGRES_INTEGER_ARRAY_SIZE, "INTEGER[9]"),
                Arguments.of(ObjectType.POSTGRES_INTEGER_ARRAY_SIZE, "INTEGER[100]"),
                Arguments.of(ObjectType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, "INTEGER[]"),
                Arguments.of(ObjectType.POSTGRES_INTEGER, "INTEGER"),
                Arguments.of(ObjectType.POSTGRES_FLOAT_ARRAY_SIZE, "FLOAT[0]"),
                Arguments.of(ObjectType.POSTGRES_FLOAT_ARRAY_SIZE, "FLOAT[9]"),
                Arguments.of(ObjectType.POSTGRES_FLOAT_ARRAY_SIZE, "FLOAT[100]"),
                Arguments.of(ObjectType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, "FLOAT[]"),
                Arguments.of(ObjectType.POSTGRES_FLOAT, "FLOAT"),
                Arguments.of(ObjectType.POSTGRES_LONG_ARRAY_SIZE, "LONG[0]"),
                Arguments.of(ObjectType.POSTGRES_LONG_ARRAY_SIZE, "LONG[9]"),
                Arguments.of(ObjectType.POSTGRES_LONG_ARRAY_SIZE, "LONG[100]"),
                Arguments.of(ObjectType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, "LONG[]"),
                Arguments.of(ObjectType.POSTGRES_LONG, "LONG"),
                Arguments.of(ObjectType.POSTGRES_REAL, "REAL"),
                Arguments.of(ObjectType.POSTGRES_BOOLEAN, "BOOLEAN"),
                Arguments.of(ObjectType.POSTGRES_STRING, "STRING"),
                Arguments.of(ObjectType.POSTGRES_UNLIMITED_STRING, "TEXT"),
                Arguments.of(ObjectType.POSTGRES_TIMESTAMP, "TIMESTAMP")
        );
    }

    static Stream<Arguments> provideJsonNameData() {
        return Stream.of(
                Arguments.of(ObjectType.POSTGRES_INTEGER_ARRAY_SIZE, "INTEGER[0]"),
                Arguments.of(ObjectType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, "INTEGER[]"),
                Arguments.of(ObjectType.POSTGRES_INTEGER, "INTEGER"),
                Arguments.of(ObjectType.POSTGRES_FLOAT_ARRAY_SIZE, "FLOAT[6]"),
                Arguments.of(ObjectType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, "FLOAT[]"),
                Arguments.of(ObjectType.POSTGRES_FLOAT, "FLOAT"),
                Arguments.of(ObjectType.POSTGRES_LONG_ARRAY_SIZE, "LONG[100]"),
                Arguments.of(ObjectType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, "LONG[]"),
                Arguments.of(ObjectType.POSTGRES_LONG, "LONG"),
                Arguments.of(ObjectType.POSTGRES_REAL, "REAL"),
                Arguments.of(ObjectType.POSTGRES_BOOLEAN, "BOOLEAN"),
                Arguments.of(ObjectType.POSTGRES_STRING, "STRING"),
                Arguments.of(ObjectType.POSTGRES_UNLIMITED_STRING, "TEXT"),
                Arguments.of(ObjectType.POSTGRES_TIMESTAMP, "TIMESTAMP")
        );
    }

    static Stream<Arguments> provideArgumentData() {
        return Stream.of(
                Arguments.of(ObjectType.POSTGRES_INTEGER_ARRAY_SIZE, "INTEGER\\[[0-9]+\\]", "_int4", "_int4", "{}"),
                Arguments.of(ObjectType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, "INTEGER\\[\\]", "_int4", "_int4", "{}"),
                Arguments.of(ObjectType.POSTGRES_INTEGER, "INTEGER", "int4", "int4", "0"),
                Arguments.of(ObjectType.POSTGRES_FLOAT_ARRAY_SIZE, "FLOAT\\[[0-9]+\\]", "_float8", "_float8", "{}"),
                Arguments.of(ObjectType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, "FLOAT\\[\\]", "_float8", "_float8", "{}"),
                Arguments.of(ObjectType.POSTGRES_FLOAT, "FLOAT", "double precision", "float8", "0.0"),
                Arguments.of(ObjectType.POSTGRES_LONG_ARRAY_SIZE, "LONG\\[[0-9]+\\]", "_int8", "_int8", "{}"),
                Arguments.of(ObjectType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, "LONG\\[\\]", "_int8", "_int8", "{}"),
                Arguments.of(ObjectType.POSTGRES_LONG, "LONG", "int8", "int8", "0"),
                Arguments.of(ObjectType.POSTGRES_REAL, "REAL", "real", "float4", "0"),
                Arguments.of(ObjectType.POSTGRES_BOOLEAN, "BOOLEAN", "boolean", "bool", "false"),
                Arguments.of(ObjectType.POSTGRES_STRING, "STRING", "varchar(255)", "varchar", "''"),
                Arguments.of(ObjectType.POSTGRES_UNLIMITED_STRING, "TEXT", "varchar", "varchar", "''"),
                Arguments.of(ObjectType.POSTGRES_TIMESTAMP, "TIMESTAMP", "TIMESTAMP WITHOUT TIME ZONE", "timestamp", "current_timestamp")
        );
    }
}