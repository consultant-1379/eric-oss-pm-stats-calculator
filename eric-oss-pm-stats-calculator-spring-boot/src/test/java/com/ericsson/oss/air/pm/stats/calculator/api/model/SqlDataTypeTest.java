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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.stream.Stream;

class SqlDataTypeTest {
    @ParameterizedTest
    @ArgumentsSource(SqlDataTypeMetadataArgumentProvider.class)
    void shouldSqlDataTypeMetadataRemainTheSame(final SqlDataType sqlDataType, final String jsonType, final String sqlType) {
        Assertions.assertThat(sqlDataType.getJsonType()).isEqualTo(jsonType);
        Assertions.assertThat(sqlDataType.getSqlType()).isEqualTo(sqlType);
    }

    @ParameterizedTest
    @EnumSource(value = SqlDataType.class, names = {"INT", "LONG", "SHORT", "DOUBLE", "FLOAT"})
    void shouldVerifyNumericTypes(final SqlDataType sqlDataType) {
        Assertions.assertThat(sqlDataType.isNumeric()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = SqlDataType.class, names = {"BOOLEAN", "BYTE", "SERIAL", "STRING", "STRING_ARRAY"})
    void shouldVerifyNonNumericTypes(final SqlDataType sqlDataType) {
        Assertions.assertThat(sqlDataType.isNumeric()).isFalse();
    }

    @ParameterizedTest
    @ArgumentsSource(SqlDataTypeJsonNameArgumentProvider.class)
    void shouldConvertToJson(final SqlDataType sqlDataType, final String wantedValue) {
        Assertions.assertThat(SqlDataType.forValue(wantedValue)).isEqualTo(sqlDataType);
    }

    @Test
    void shouldReturnAllValues() {
        final List<SqlDataType> actual = SqlDataType.valuesAsList();

        Assertions.assertThat(actual)
                .containsExactlyInAnyOrder(SqlDataType.BOOLEAN,
                        SqlDataType.BYTE,
                        SqlDataType.DOUBLE,
                        SqlDataType.FLOAT,
                        SqlDataType.INT,
                        SqlDataType.LONG,
                        SqlDataType.SERIAL,
                        SqlDataType.SHORT,
                        SqlDataType.STRING,
                        SqlDataType.STRING_ARRAY);
    }

    private static final class SqlDataTypeJsonNameArgumentProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return Stream.of(Arguments.of(SqlDataType.BOOLEAN, "boolean"),
                    Arguments.of(SqlDataType.BYTE, "byte"),
                    Arguments.of(SqlDataType.DOUBLE, "double"),
                    Arguments.of(SqlDataType.FLOAT, "float"),
                    Arguments.of(SqlDataType.INT, "int"),
                    Arguments.of(SqlDataType.LONG, "long"),
                    Arguments.of(SqlDataType.SERIAL, "index"),
                    Arguments.of(SqlDataType.SHORT, "short"),
                    Arguments.of(SqlDataType.STRING, "String"),
                    Arguments.of(SqlDataType.STRING_ARRAY, "String[]"),
                    Arguments.of(SqlDataType.DOUBLE, "unknown"));
        }
    }

    private static final class SqlDataTypeMetadataArgumentProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return Stream.of(Arguments.of(SqlDataType.BOOLEAN, "boolean", "boolean"),
                    Arguments.of(SqlDataType.BYTE, "byte", "byte"),
                    Arguments.of(SqlDataType.DOUBLE, "double", "double precision"),
                    Arguments.of(SqlDataType.FLOAT, "float", "real"),
                    Arguments.of(SqlDataType.INT, "int", "integer"),
                    Arguments.of(SqlDataType.LONG, "long", "bigint"),
                    Arguments.of(SqlDataType.SERIAL, "index", "serial"),
                    Arguments.of(SqlDataType.SHORT, "short", "smallint"),
                    Arguments.of(SqlDataType.STRING, "String", "varchar(255)"),
                    Arguments.of(SqlDataType.STRING_ARRAY, "String[]", "varchar(255)[]"));
        }
    }
}