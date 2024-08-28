/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util;

import static com.ericsson.oss.air.pm.stats.model.entity.KpiDefinition.COLUMN_SCHEMA_CATEGORY;
import static com.ericsson.oss.air.pm.stats.model.entity.KpiDefinition.COLUMN_SCHEMA_DATA_SPACE;
import static com.ericsson.oss.air.pm.stats.model.entity.KpiDefinition.COLUMN_SCHEMA_NAME;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

class ColumnUtilsTest {
    private static final LocalDateTime TEST_TIME = LocalDateTime.of(LocalDate.of(2_022, Month.APRIL, 19), LocalTime.NOON);

    @MethodSource("provideNullableIntegerData")
    @ParameterizedTest(name = "[{index}] Object: ''{0}'' is converted to: ''{1}''")
    void shouldVerifyNullableInteger(final Object object, final Object expected) {
        final Integer actual = ColumnUtils.nullableInteger(object);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @MethodSource("provideNullableBooleanData")
    @ParameterizedTest(name = "[{index}] Object: ''{0}'' is converted to: ''{1}''")
    void shouldVerifyNullableBoolean(final Object object, final Object expected) {
        final Boolean actual = ColumnUtils.nullableBoolean(object);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @MethodSource("provideNullableStringData")
    @ParameterizedTest(name = "[{index}] Object: ''{0}'' is converted to: ''{1}''")
    void shouldVerifyNullableString(final Object object, final Object expected) {
        final String actual = ColumnUtils.nullableString(object);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @MethodSource("provideNullableListData")
    @ParameterizedTest(name = "[{index}] Object: ''{0}'' is converted to: ''{1}''")
    void shouldVerifyNullableList(final List<String> object, final Object expected) {
        final Object[] actual = ColumnUtils.nullableList(object);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @MethodSource("provideNullableTimeStampData")
    @ParameterizedTest(name = "[{index}] Object: ''{0}'' is converted to: ''{1}''")
    void shouldVerifyNullableTimeStamp(final LocalDateTime object, final Timestamp expected) {
        final Timestamp actual = ColumnUtils.nullableTimestamp(object);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldParseUniqueIndexColumns() {
        final List<Column> actual = ColumnUtils.parseUniqueIndexColumns("hello everyone (index2, index1, index3)");

        Assertions.assertThat(actual).containsExactly(Column.of("index1"), Column.of("index2"), Column.of("index3"));
    }

    @MethodSource("provideDatabaseEntry")
    @ParameterizedTest(name = "[{index}] List: ''{0}'' can be mapped to a Data identifier of value: ''{1}''")
    void shouldReturnDataIdentifier(final List<String> resultSetEntry, final DataIdentifier expected) throws SQLException {
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.getString(COLUMN_SCHEMA_DATA_SPACE)).thenReturn(resultSetEntry.get(0));
        Mockito.when(rs.getString(COLUMN_SCHEMA_CATEGORY)).thenReturn(resultSetEntry.get(1));
        Mockito.when(rs.getString(COLUMN_SCHEMA_NAME)).thenReturn(resultSetEntry.get(2));
        DataIdentifier actual = ColumnUtils.decideIfPresent(rs);
        Assertions.assertThat(actual).isEqualTo(expected);
        Mockito.verify(rs).getString(COLUMN_SCHEMA_DATA_SPACE);
        Mockito.verify(rs).getString(COLUMN_SCHEMA_CATEGORY);
        Mockito.verify(rs).getString(COLUMN_SCHEMA_NAME);
    }

    private static Stream<Arguments> provideNullableIntegerData() {
        return Stream.of(Arguments.arguments(1, 1),
                Arguments.arguments("1", 1),
                Arguments.arguments(null, null));
    }

    private static Stream<Arguments> provideNullableBooleanData() {
        return Stream.of(Arguments.arguments(true, true),
                Arguments.arguments("true", true),
                Arguments.arguments(null, null));
    }

    private static Stream<Arguments> provideNullableStringData() {
        return Stream.of(Arguments.arguments("string", "string"),
                Arguments.arguments(null, null));
    }

    private static Stream<Arguments> provideNullableListData() {
        return Stream.of(Arguments.arguments(Arrays.asList("val1", "val2"), new Object[]{"val1", "val2"}),
                Arguments.arguments(null, null));
    }

    private static Stream<Arguments> provideNullableTimeStampData() {
        return Stream.of(Arguments.of(TEST_TIME, Timestamp.valueOf(TEST_TIME)),
                Arguments.of(null, null));
    }

    private static Stream<Arguments> provideDatabaseEntry() {
        return Stream.of(Arguments.of(Arrays.asList("dataSpace", "topic", "schema"), DataIdentifier.of("dataSpace", "topic", "schema")),
                Arguments.of(Arrays.asList("dataSpace", null, null), null),
                Arguments.of(Arrays.asList(null, null, "schema"), null),
                Arguments.of(Arrays.asList(null, "topic", null), null));
    }
}