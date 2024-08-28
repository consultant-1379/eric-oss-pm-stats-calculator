/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

class KpiDataTypeTest {
    @ParameterizedTest(name = "[{index}] {0} has regex: {1}, postgresType: {2}, columnType: {3}, defaultValue: {4}")
    @ArgumentsSource(KpiDataTypeArgumentProvider.class)
    void shouldVerifyKpiDataTypeMetadataRemainsTheSame(final KpiDataType kpiDataType,
                                                       final String regex,
                                                       final String postgresType,
                                                       final String columnType,
                                                       final String defaultValue) {
        Assertions.assertThat(kpiDataType.getRegEx()).isEqualTo(regex);
        Assertions.assertThat(kpiDataType.getPostgresType()).isEqualTo(postgresType);
        Assertions.assertThat(kpiDataType.getColumnType()).isEqualTo(columnType);
        Assertions.assertThat(kpiDataType.getDefaultValue()).isEqualTo(defaultValue);
    }

    @ParameterizedTest(name = "[{index}] {1} converted to: {0}")
    @ArgumentsSource(KpiDataTypeJsonNameArgumentProvider.class)
    void shouldConvertToJson(final KpiDataType kpiDataType, final String wantedValue) {
        Assertions.assertThat(KpiDataType.forValue(wantedValue)).isEqualTo(kpiDataType);
    }

    @ParameterizedTest(name = "[{index}] avro type: {0} converted to: {1}")
    @MethodSource("provideSchemaData")
    void shouldGiveBackValueForSchemaType(final Schema schema, KpiDataType expected) {
        final KpiDataType actual = KpiDataType.forSchemaType(schema);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest(name = "[{index}] {0}`s regex matches: {1}")
    @ArgumentsSource(KpiDataTypeRegexArgumentProvider.class)
    void shouldTestRegex(final KpiDataType kpiDataType, final String value) {
        Assertions.assertThat(value).matches(Pattern.compile(kpiDataType.getRegEx()));
    }

    @Test
    void shouldThrowException_whenInvalidKpiDataTypeGiven() {
        Assertions.assertThatThrownBy(() -> KpiDataType.forValue("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid KpiDataType: 'unknown'");
    }

    @Test
    void shouldCreateValidValues() {
        final List<String> actual = KpiDataType.getValidValuesAsList();
        Assertions.assertThat(actual)
                .containsExactlyInAnyOrder("INTEGER[[0-9]+]",
                        "INTEGER[]",
                        "INT(EGER)?",
                        "FLOAT[[0-9]+]",
                        "FLOAT[]",
                        "FLOAT|DOUBLE",
                        "LONG[[0-9]+]",
                        "LONG[]",
                        "LONG",
                        "REAL",
                        "BOOLEAN",
                        "STRING",
                        "TEXT",
                        "TIMESTAMP");

    }

    @Test
    void shouldReturnUnmodifiableList_whenAccessingValues() {
        final List<String> actual = KpiDataType.getValidValuesAsList();
        Assertions.assertThat(actual).isUnmodifiable();
    }

    @Test
    void shouldReturnUnmodifiableList_WhenAccessingValidValues() {
        final List<String> actual = KpiDataType.getValidValuesAsList();
        Assertions.assertThat(actual).isUnmodifiable();
    }

    @Test
    void shouldReturnAllValues() {
        Assertions.assertThat(KpiDataType.values())
                .containsExactlyInAnyOrder(KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE,
                        KpiDataType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE,
                        KpiDataType.POSTGRES_INTEGER,
                        KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE,
                        KpiDataType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE,
                        KpiDataType.POSTGRES_FLOAT,
                        KpiDataType.POSTGRES_LONG_ARRAY_SIZE,
                        KpiDataType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE,
                        KpiDataType.POSTGRES_LONG,
                        KpiDataType.POSTGRES_REAL,
                        KpiDataType.POSTGRES_BOOLEAN,
                        KpiDataType.POSTGRES_STRING,
                        KpiDataType.POSTGRES_UNLIMITED_STRING,
                        KpiDataType.POSTGRES_TIMESTAMP);
    }

    @Test
    void shouldFailToConvertSchemaTypeForMultipleExactUnionType() {
        final Schema schema = Schema.createUnion(Schema.create(Type.STRING), Schema.create(Type.LONG));
        Assertions.assertThatThrownBy(() -> KpiDataType.forSchemaType(schema))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("union type should only have one non null exact type");
    }

    private static final class KpiDataTypeRegexArgumentProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) {
            return Stream.of(Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE, "INTEGER[0]"),
                    Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE, "INTEGER[9]"),
                    Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE, "INTEGER[100]"),
                    Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, "INTEGER[]"),
                    Arguments.of(KpiDataType.POSTGRES_INTEGER, "INTEGER"),
                    Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE, "FLOAT[0]"),
                    Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE, "FLOAT[9]"),
                    Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE, "FLOAT[100]"),
                    Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, "FLOAT[]"),
                    Arguments.of(KpiDataType.POSTGRES_FLOAT, "FLOAT"),
                    Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_SIZE, "LONG[0]"),
                    Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_SIZE, "LONG[9]"),
                    Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_SIZE, "LONG[100]"),
                    Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, "LONG[]"),
                    Arguments.of(KpiDataType.POSTGRES_LONG, "LONG"),
                    Arguments.of(KpiDataType.POSTGRES_REAL, "REAL"),
                    Arguments.of(KpiDataType.POSTGRES_BOOLEAN, "BOOLEAN"),
                    Arguments.of(KpiDataType.POSTGRES_STRING, "STRING"),
                    Arguments.of(KpiDataType.POSTGRES_UNLIMITED_STRING, "TEXT"),
                    Arguments.of(KpiDataType.POSTGRES_TIMESTAMP, "TIMESTAMP"));
        }
    }

    private static final class KpiDataTypeJsonNameArgumentProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) {
            return Stream.of(Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE, "INTEGER[0]"),
                    Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, "INTEGER[]"),
                    Arguments.of(KpiDataType.POSTGRES_INTEGER, "INTEGER"),
                    Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE, "FLOAT[6]"),
                    Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, "FLOAT[]"),
                    Arguments.of(KpiDataType.POSTGRES_FLOAT, "FLOAT"),
                    Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_SIZE, "LONG[100]"),
                    Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, "LONG[]"),
                    Arguments.of(KpiDataType.POSTGRES_LONG, "LONG"),
                    Arguments.of(KpiDataType.POSTGRES_REAL, "REAL"),
                    Arguments.of(KpiDataType.POSTGRES_BOOLEAN, "BOOLEAN"),
                    Arguments.of(KpiDataType.POSTGRES_STRING, "STRING"),
                    Arguments.of(KpiDataType.POSTGRES_UNLIMITED_STRING, "TEXT"),
                    Arguments.of(KpiDataType.POSTGRES_TIMESTAMP, "TIMESTAMP"));
        }
    }

    private static final class KpiDataTypeArgumentProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) {
            return Stream.of(Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE, "INTEGER\\[[0-9]+\\]", "_int4", "_int4", "{}"),
                    Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, "INTEGER\\[\\]", "_int4", "_int4", "{}"),
                    Arguments.of(KpiDataType.POSTGRES_INTEGER, "INT(EGER)?", "int4", "int4", "0"),
                    Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE, "FLOAT\\[[0-9]+\\]", "_float8", "_float8", "{}"),
                    Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, "FLOAT\\[\\]", "_float8", "_float8", "{}"),
                    Arguments.of(KpiDataType.POSTGRES_FLOAT, "FLOAT|DOUBLE", "double precision", "float8", "0.0"),
                    Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_SIZE, "LONG\\[[0-9]+\\]", "_int8", "_int8", "{}"),
                    Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, "LONG\\[\\]", "_int8", "_int8", "{}"),
                    Arguments.of(KpiDataType.POSTGRES_LONG, "LONG", "int8", "int8", "0"),
                    Arguments.of(KpiDataType.POSTGRES_REAL, "REAL", "real", "float4", "0"),
                    Arguments.of(KpiDataType.POSTGRES_BOOLEAN, "BOOLEAN", "boolean", "bool", "false"),
                    Arguments.of(KpiDataType.POSTGRES_STRING, "STRING", "varchar(255)", "varchar", "''"),
                    Arguments.of(KpiDataType.POSTGRES_UNLIMITED_STRING, "TEXT", "varchar", "varchar", "''"),
                    Arguments.of(KpiDataType.POSTGRES_TIMESTAMP, "TIMESTAMP", "TIMESTAMP WITHOUT TIME ZONE", "timestamp", "current_timestamp"));
        }
    }

    static Stream<Arguments> provideSchemaData() {
        return Stream.of(
                Arguments.of(Schema.create(Type.BOOLEAN), KpiDataType.POSTGRES_BOOLEAN),
                Arguments.of(Schema.create(Type.STRING), KpiDataType.POSTGRES_STRING),
                Arguments.of(Schema.create(Type.FLOAT), KpiDataType.POSTGRES_FLOAT),
                Arguments.of(Schema.create(Type.LONG), KpiDataType.POSTGRES_LONG),
                Arguments.of(Schema.create(Type.INT), KpiDataType.POSTGRES_INTEGER),
                Arguments.of(Schema.createUnion(Schema.create(Type.LONG), Schema.create(Type.NULL)), KpiDataType.POSTGRES_LONG),
                Arguments.of(Schema.createUnion(Schema.create(Type.NULL), Schema.create(Type.LONG)), KpiDataType.POSTGRES_LONG)
        );
    }
}