/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;

import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PostgresDataTypeChangeValidatorTest {

    PostgresDataTypeChangeValidator objectUnderTest = new PostgresDataTypeChangeValidator();

    @Test
    void shouldVerifyAllEnumsChangeableTypes() {
        final Map<KpiDataType, Set<KpiDataType>> validDataTypeChanges = readValidDataTypeChangesField();

        Assertions.assertThat(validDataTypeChanges).as("All KpiDataTypes should have their conversions defined").containsKeys(
                KpiDataType.values()
        );
    }

    @MethodSource("provideIntegerArrayWithSizeData")
    @ParameterizedTest(name = "[{index}] sourceDataType: ''{0}'' can be changed to ''{1}'' ==> ''{2}''")
    void shouldReturnInvalidIntegerArrayWithSizeDataTransformations(final KpiDataType sourceDataType, final KpiDataType targetDataType, final boolean expectedResult) {
        final boolean actual = objectUnderTest.isInvalidChange(sourceDataType, targetDataType);
        Assertions.assertThat(actual).isEqualTo(expectedResult);
    }

    @MethodSource("provideIntegerArrayWithUndefinedSizeData")
    @ParameterizedTest(name = "[{index}] sourceDataType: ''{0}'' can be changed to ''{1}'' ==> ''{2}''")
    void shouldReturnInvalidIntegerArrayUndefinedSizeDataTransformations(final KpiDataType sourceDataType, final KpiDataType targetDataType, final boolean expectedResult) {
        final boolean actual = objectUnderTest.isInvalidChange(sourceDataType, targetDataType);
        Assertions.assertThat(actual).isEqualTo(expectedResult);
    }

    @MethodSource("provideIntegerData")
    @ParameterizedTest(name = "[{index}] sourceDataType: ''{0}'' can be changed to ''{1}'' ==> ''{2}''")
    void shouldReturnInvalidIntegerDataTransformations(final KpiDataType sourceDataType, final KpiDataType targetDataType, final boolean expectedResult) {
        final boolean actual = objectUnderTest.isInvalidChange(sourceDataType, targetDataType);
        Assertions.assertThat(actual).isEqualTo(expectedResult);
    }

    @MethodSource("provideFloatArrayWithSizeData")
    @ParameterizedTest(name = "[{index}] sourceDataType: ''{0}'' can be changed to ''{1}'' ==> ''{2}''")
    void shouldReturnInvalidFloatArrayWithSizeDataTransformations(final KpiDataType sourceDataType, final KpiDataType targetDataType, final boolean expectedResult) {
        final boolean actual = objectUnderTest.isInvalidChange(sourceDataType, targetDataType);
        Assertions.assertThat(actual).isEqualTo(expectedResult);
    }

    @MethodSource("provideFloatArrayWithUndefinedSizeData")
    @ParameterizedTest(name = "[{index}] sourceDataType: ''{0}'' can be changed to ''{1}'' ==> ''{2}''")
    void shouldReturnInvalidFloatArrayUndefinedSizeDataTransformations(final KpiDataType sourceDataType, final KpiDataType targetDataType, final boolean expectedResult) {
        final boolean actual = objectUnderTest.isInvalidChange(sourceDataType, targetDataType);
        Assertions.assertThat(actual).isEqualTo(expectedResult);
    }

    @MethodSource("provideFloatData")
    @ParameterizedTest(name = "[{index}] sourceDataType: ''{0}'' can be changed to ''{1}'' ==> ''{2}''")
    void shouldReturnInvalidFloatDataTransformations(final KpiDataType sourceDataType, final KpiDataType targetDataType, final boolean expectedResult) {
        final boolean actual = objectUnderTest.isInvalidChange(sourceDataType, targetDataType);
        Assertions.assertThat(actual).isEqualTo(expectedResult);
    }

    @MethodSource("provideLongArrayWithSizeData")
    @ParameterizedTest(name = "[{index}] sourceDataType: ''{0}'' can be changed to ''{1}'' ==> ''{2}''")
    void shouldReturnInvalidLongArrayWithSizeDataTransformations(final KpiDataType sourceDataType, final KpiDataType targetDataType, final boolean expectedResult) {
        final boolean actual = objectUnderTest.isInvalidChange(sourceDataType, targetDataType);
        Assertions.assertThat(actual).isEqualTo(expectedResult);
    }

    @MethodSource("provideLongArrayWithUndefinedSizeData")
    @ParameterizedTest(name = "[{index}] sourceDataType: ''{0}'' can be changed to ''{1}'' ==> ''{2}''")
    void shouldReturnInvalidLongArrayUndefinedSizeDataTransformations(final KpiDataType sourceDataType, final KpiDataType targetDataType, final boolean expectedResult) {
        final boolean actual = objectUnderTest.isInvalidChange(sourceDataType, targetDataType);
        Assertions.assertThat(actual).isEqualTo(expectedResult);
    }

    @MethodSource("provideLongData")
    @ParameterizedTest(name = "[{index}] sourceDataType: ''{0}'' can be changed to ''{1}'' ==> ''{2}''")
    void shouldReturnInvalidLongDataTransformations(final KpiDataType sourceDataType, final KpiDataType targetDataType, final boolean expectedResult) {
        final boolean actual = objectUnderTest.isInvalidChange(sourceDataType, targetDataType);
        Assertions.assertThat(actual).isEqualTo(expectedResult);
    }

    @MethodSource("provideRealData")
    @ParameterizedTest(name = "[{index}] sourceDataType: ''{0}'' can be changed to ''{1}'' ==> ''{2}''")
    void shouldReturnInvalidRealDataTransformations(final KpiDataType sourceDataType, final KpiDataType targetDataType, final boolean expectedResult) {
        final boolean actual = objectUnderTest.isInvalidChange(sourceDataType, targetDataType);
        Assertions.assertThat(actual).isEqualTo(expectedResult);
    }

    @MethodSource("provideBooleanData")
    @ParameterizedTest(name = "[{index}] sourceDataType: ''{0}'' can be changed to ''{1}'' ==> ''{2}''")
    void shouldReturnInvalidBooleanDataTransformations(final KpiDataType sourceDataType, final KpiDataType targetDataType, final boolean expectedResult) {
        final boolean actual = objectUnderTest.isInvalidChange(sourceDataType, targetDataType);
        Assertions.assertThat(actual).isEqualTo(expectedResult);
    }

    @MethodSource("provideStringData")
    @ParameterizedTest(name = "[{index}] sourceDataType: ''{0}'' can be changed to ''{1}'' ==> ''{2}''")
    void shouldReturnInvalidStringDataTransformations(final KpiDataType sourceDataType, final KpiDataType targetDataType, final boolean expectedResult) {
        final boolean actual = objectUnderTest.isInvalidChange(sourceDataType, targetDataType);
        Assertions.assertThat(actual).isEqualTo(expectedResult);
    }

    @MethodSource("provideTextData")
    @ParameterizedTest(name = "[{index}] sourceDataType: ''{0}'' can be changed to ''{1}'' ==> ''{2}''")
    void shouldReturnInvalidTextDataTransformations(final KpiDataType sourceDataType, final KpiDataType targetDataType, final boolean expectedResult) {
        final boolean actual = objectUnderTest.isInvalidChange(sourceDataType, targetDataType);
        Assertions.assertThat(actual).isEqualTo(expectedResult);
    }

    @MethodSource("provideTimestampData")
    @ParameterizedTest(name = "[{index}] sourceDataType: ''{0}'' can be changed to ''{1}'' ==> ''{2}''")
    void shouldReturnInvalidTimestampDataTransformations(final KpiDataType sourceDataType, final KpiDataType targetDataType, final boolean expectedResult) {
        final boolean actual = objectUnderTest.isInvalidChange(sourceDataType, targetDataType);
        Assertions.assertThat(actual).isEqualTo(expectedResult);
    }

    static Stream<Arguments> provideIntegerArrayWithSizeData() {
        return Stream.of(
                Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE, KpiDataType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, false),
                Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE, KpiDataType.POSTGRES_INTEGER, true),
                Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE, KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE, false),
                Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE, KpiDataType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, false),
                Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE, KpiDataType.POSTGRES_LONG_ARRAY_SIZE, false),
                Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE, KpiDataType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, false),
                Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE, KpiDataType.POSTGRES_FLOAT, true),
                Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE, KpiDataType.POSTGRES_LONG, true),
                Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE, KpiDataType.POSTGRES_REAL, true),
                Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE, KpiDataType.POSTGRES_BOOLEAN, true),
                Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE, KpiDataType.POSTGRES_STRING, false),
                Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE, KpiDataType.POSTGRES_UNLIMITED_STRING, false),
                Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE, KpiDataType.POSTGRES_TIMESTAMP, true)
        );
    }

    static Stream<Arguments> provideIntegerArrayWithUndefinedSizeData() {
        return Stream.of(
                Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE, false),
                Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_INTEGER, true),
                Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE, false),
                Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, false),
                Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_FLOAT, true),
                Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_LONG_ARRAY_SIZE, false),
                Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, false),
                Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_LONG, true),
                Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_REAL, true),
                Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_BOOLEAN, true),
                Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_STRING, false),
                Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_UNLIMITED_STRING, false),
                Arguments.of(KpiDataType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_TIMESTAMP, true)
        );
    }

    static Stream<Arguments> provideIntegerData() {
        return Stream.of(
                Arguments.of(KpiDataType.POSTGRES_INTEGER, KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_INTEGER, KpiDataType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_INTEGER, KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_INTEGER, KpiDataType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_INTEGER, KpiDataType.POSTGRES_FLOAT, false),
                Arguments.of(KpiDataType.POSTGRES_INTEGER, KpiDataType.POSTGRES_LONG_ARRAY_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_INTEGER, KpiDataType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_INTEGER, KpiDataType.POSTGRES_LONG, false),
                Arguments.of(KpiDataType.POSTGRES_INTEGER, KpiDataType.POSTGRES_REAL, false),
                Arguments.of(KpiDataType.POSTGRES_INTEGER, KpiDataType.POSTGRES_BOOLEAN, true),
                Arguments.of(KpiDataType.POSTGRES_INTEGER, KpiDataType.POSTGRES_STRING, false),
                Arguments.of(KpiDataType.POSTGRES_INTEGER, KpiDataType.POSTGRES_UNLIMITED_STRING, false),
                Arguments.of(KpiDataType.POSTGRES_INTEGER, KpiDataType.POSTGRES_TIMESTAMP, false)
        );
    }

    static Stream<Arguments> provideFloatArrayWithSizeData() {
        return Stream.of(
                Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE, KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE, KpiDataType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE, KpiDataType.POSTGRES_INTEGER, true),
                Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE, KpiDataType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, false),
                Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE, KpiDataType.POSTGRES_FLOAT, true),
                Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE, KpiDataType.POSTGRES_LONG_ARRAY_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE, KpiDataType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE, KpiDataType.POSTGRES_LONG, true),
                Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE, KpiDataType.POSTGRES_REAL, true),
                Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE, KpiDataType.POSTGRES_BOOLEAN, true),
                Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE, KpiDataType.POSTGRES_STRING, false),
                Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE, KpiDataType.POSTGRES_UNLIMITED_STRING, false),
                Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE, KpiDataType.POSTGRES_TIMESTAMP, true)
        );
    }

    static Stream<Arguments> provideFloatArrayWithUndefinedSizeData() {
        return Stream.of(
                Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_INTEGER, true),
                Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE, false),
                Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_FLOAT, true),
                Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_LONG_ARRAY_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_LONG, true),
                Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_REAL, true),
                Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_BOOLEAN, true),
                Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_STRING, false),
                Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_UNLIMITED_STRING, false),
                Arguments.of(KpiDataType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_TIMESTAMP, true)
        );
    }

    static Stream<Arguments> provideFloatData() {
        return Stream.of(
                Arguments.of(KpiDataType.POSTGRES_FLOAT, KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_FLOAT, KpiDataType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_FLOAT, KpiDataType.POSTGRES_INTEGER, true),
                Arguments.of(KpiDataType.POSTGRES_FLOAT, KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_FLOAT, KpiDataType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_FLOAT, KpiDataType.POSTGRES_LONG_ARRAY_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_FLOAT, KpiDataType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_FLOAT, KpiDataType.POSTGRES_LONG, false),
                Arguments.of(KpiDataType.POSTGRES_FLOAT, KpiDataType.POSTGRES_REAL, true),
                Arguments.of(KpiDataType.POSTGRES_FLOAT, KpiDataType.POSTGRES_BOOLEAN, true),
                Arguments.of(KpiDataType.POSTGRES_FLOAT, KpiDataType.POSTGRES_STRING, false),
                Arguments.of(KpiDataType.POSTGRES_FLOAT, KpiDataType.POSTGRES_UNLIMITED_STRING, false),
                Arguments.of(KpiDataType.POSTGRES_FLOAT, KpiDataType.POSTGRES_TIMESTAMP, true)
        );
    }

    static Stream<Arguments> provideLongArrayWithSizeData() {
        return Stream.of(
                Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_SIZE, KpiDataType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_SIZE, KpiDataType.POSTGRES_INTEGER, true),
                Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_SIZE, KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_SIZE, KpiDataType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_SIZE, KpiDataType.POSTGRES_FLOAT, true),
                Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_SIZE, KpiDataType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, false),
                Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_SIZE, KpiDataType.POSTGRES_LONG, true),
                Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_SIZE, KpiDataType.POSTGRES_REAL, true),
                Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_SIZE, KpiDataType.POSTGRES_BOOLEAN, true),
                Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_SIZE, KpiDataType.POSTGRES_STRING, false),
                Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_SIZE, KpiDataType.POSTGRES_UNLIMITED_STRING, false),
                Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_SIZE, KpiDataType.POSTGRES_TIMESTAMP, true)
        );
    }

    static Stream<Arguments> provideLongArrayWithUndefinedSizeData() {
        return Stream.of(
                Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_INTEGER, true),
                Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_FLOAT, true),
                Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_LONG_ARRAY_SIZE, false),
                Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_LONG, true),
                Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_REAL, true),
                Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_BOOLEAN, true),
                Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_STRING, false),
                Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_UNLIMITED_STRING, false),
                Arguments.of(KpiDataType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, KpiDataType.POSTGRES_TIMESTAMP, true)
        );
    }

    static Stream<Arguments> provideLongData() {
        return Stream.of(
                Arguments.of(KpiDataType.POSTGRES_LONG, KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_LONG, KpiDataType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_LONG, KpiDataType.POSTGRES_INTEGER, true),
                Arguments.of(KpiDataType.POSTGRES_LONG, KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_LONG, KpiDataType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_LONG, KpiDataType.POSTGRES_FLOAT, false),
                Arguments.of(KpiDataType.POSTGRES_LONG, KpiDataType.POSTGRES_LONG_ARRAY_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_LONG, KpiDataType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_LONG, KpiDataType.POSTGRES_REAL, true),
                Arguments.of(KpiDataType.POSTGRES_LONG, KpiDataType.POSTGRES_BOOLEAN, true),
                Arguments.of(KpiDataType.POSTGRES_LONG, KpiDataType.POSTGRES_STRING, false),
                Arguments.of(KpiDataType.POSTGRES_LONG, KpiDataType.POSTGRES_UNLIMITED_STRING, false),
                Arguments.of(KpiDataType.POSTGRES_LONG, KpiDataType.POSTGRES_TIMESTAMP, true)
        );
    }

    static Stream<Arguments> provideRealData() {
        return Stream.of(
                Arguments.of(KpiDataType.POSTGRES_REAL, KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_REAL, KpiDataType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_REAL, KpiDataType.POSTGRES_INTEGER, false),
                Arguments.of(KpiDataType.POSTGRES_REAL, KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_REAL, KpiDataType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_REAL, KpiDataType.POSTGRES_FLOAT, false),
                Arguments.of(KpiDataType.POSTGRES_REAL, KpiDataType.POSTGRES_LONG_ARRAY_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_REAL, KpiDataType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_REAL, KpiDataType.POSTGRES_LONG, true),
                Arguments.of(KpiDataType.POSTGRES_REAL, KpiDataType.POSTGRES_BOOLEAN, true),
                Arguments.of(KpiDataType.POSTGRES_REAL, KpiDataType.POSTGRES_STRING, false),
                Arguments.of(KpiDataType.POSTGRES_REAL, KpiDataType.POSTGRES_UNLIMITED_STRING, false),
                Arguments.of(KpiDataType.POSTGRES_REAL, KpiDataType.POSTGRES_TIMESTAMP, true)
        );
    }

    static Stream<Arguments> provideBooleanData() {
        return Stream.of(
                Arguments.of(KpiDataType.POSTGRES_BOOLEAN, KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_BOOLEAN, KpiDataType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_BOOLEAN, KpiDataType.POSTGRES_INTEGER, false),
                Arguments.of(KpiDataType.POSTGRES_BOOLEAN, KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_BOOLEAN, KpiDataType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_BOOLEAN, KpiDataType.POSTGRES_FLOAT, true),
                Arguments.of(KpiDataType.POSTGRES_BOOLEAN, KpiDataType.POSTGRES_LONG_ARRAY_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_BOOLEAN, KpiDataType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_BOOLEAN, KpiDataType.POSTGRES_LONG, true),
                Arguments.of(KpiDataType.POSTGRES_BOOLEAN, KpiDataType.POSTGRES_REAL, true),
                Arguments.of(KpiDataType.POSTGRES_BOOLEAN, KpiDataType.POSTGRES_STRING, false),
                Arguments.of(KpiDataType.POSTGRES_BOOLEAN, KpiDataType.POSTGRES_UNLIMITED_STRING, false),
                Arguments.of(KpiDataType.POSTGRES_BOOLEAN, KpiDataType.POSTGRES_TIMESTAMP, true)
        );
    }

    static Stream<Arguments> provideStringData() {
        return Stream.of(
                Arguments.of(KpiDataType.POSTGRES_STRING, KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_STRING, KpiDataType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_STRING, KpiDataType.POSTGRES_INTEGER, true),
                Arguments.of(KpiDataType.POSTGRES_STRING, KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_STRING, KpiDataType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_STRING, KpiDataType.POSTGRES_FLOAT, true),
                Arguments.of(KpiDataType.POSTGRES_STRING, KpiDataType.POSTGRES_LONG_ARRAY_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_STRING, KpiDataType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_STRING, KpiDataType.POSTGRES_LONG, true),
                Arguments.of(KpiDataType.POSTGRES_STRING, KpiDataType.POSTGRES_REAL, true),
                Arguments.of(KpiDataType.POSTGRES_STRING, KpiDataType.POSTGRES_BOOLEAN, true),
                Arguments.of(KpiDataType.POSTGRES_STRING, KpiDataType.POSTGRES_UNLIMITED_STRING, false),
                Arguments.of(KpiDataType.POSTGRES_STRING, KpiDataType.POSTGRES_TIMESTAMP, false)
        );
    }

    static Stream<Arguments> provideTextData() {
        return Stream.of(
                Arguments.of(KpiDataType.POSTGRES_UNLIMITED_STRING, KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_UNLIMITED_STRING, KpiDataType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_UNLIMITED_STRING, KpiDataType.POSTGRES_INTEGER, true),
                Arguments.of(KpiDataType.POSTGRES_UNLIMITED_STRING, KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_UNLIMITED_STRING, KpiDataType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_UNLIMITED_STRING, KpiDataType.POSTGRES_FLOAT, true),
                Arguments.of(KpiDataType.POSTGRES_UNLIMITED_STRING, KpiDataType.POSTGRES_LONG_ARRAY_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_UNLIMITED_STRING, KpiDataType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_UNLIMITED_STRING, KpiDataType.POSTGRES_LONG, true),
                Arguments.of(KpiDataType.POSTGRES_UNLIMITED_STRING, KpiDataType.POSTGRES_REAL, true),
                Arguments.of(KpiDataType.POSTGRES_UNLIMITED_STRING, KpiDataType.POSTGRES_BOOLEAN, true),
                Arguments.of(KpiDataType.POSTGRES_UNLIMITED_STRING, KpiDataType.POSTGRES_STRING, false),
                Arguments.of(KpiDataType.POSTGRES_UNLIMITED_STRING, KpiDataType.POSTGRES_TIMESTAMP, false)
        );
    }

    static Stream<Arguments> provideTimestampData() {
        return Stream.of(
                Arguments.of(KpiDataType.POSTGRES_TIMESTAMP, KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_TIMESTAMP, KpiDataType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_TIMESTAMP, KpiDataType.POSTGRES_INTEGER, true),
                Arguments.of(KpiDataType.POSTGRES_TIMESTAMP, KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_TIMESTAMP, KpiDataType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_TIMESTAMP, KpiDataType.POSTGRES_FLOAT, true),
                Arguments.of(KpiDataType.POSTGRES_TIMESTAMP, KpiDataType.POSTGRES_LONG_ARRAY_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_TIMESTAMP, KpiDataType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, true),
                Arguments.of(KpiDataType.POSTGRES_TIMESTAMP, KpiDataType.POSTGRES_LONG, true),
                Arguments.of(KpiDataType.POSTGRES_TIMESTAMP, KpiDataType.POSTGRES_REAL, true),
                Arguments.of(KpiDataType.POSTGRES_TIMESTAMP, KpiDataType.POSTGRES_BOOLEAN, true),
                Arguments.of(KpiDataType.POSTGRES_TIMESTAMP, KpiDataType.POSTGRES_STRING, false),
                Arguments.of(KpiDataType.POSTGRES_TIMESTAMP, KpiDataType.POSTGRES_UNLIMITED_STRING, false)
        );
    }

    @SneakyThrows
    private Map<KpiDataType, Set<KpiDataType>> readValidDataTypeChangesField() {
        @SuppressWarnings("unchecked") final Map<KpiDataType, Set<KpiDataType>> validDataTypeChanges =
                (Map<KpiDataType, Set<KpiDataType>>) FieldUtils.readField(objectUnderTest, "validDataTypeChanges", true);

        return validDataTypeChanges;
    }

}