/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util.partition;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PartitionTest {
    private static final LocalDate TEST_DATE_START = LocalDate.of(2_022, Month.APRIL, 11);
    private static final LocalDate TEST_DATE_END = TEST_DATE_START.plusDays(4);
    private static final String PARTITION = "partition";
    private static final String TABLE = "table";

    @MethodSource("provideUniqueIndexColumns")
    @ParameterizedTest(name = "[{index}] Constructor called with unique index of: {0} and field initialized to: {1}")
    void shouldVerifyPartitionCreatedWithNewUniqueIndexColumns(final Set<String> uniqueIndexColumns,
                                                               final Collection<String> expectedUniqueIndexColumns) throws IllegalAccessException {
        final Partition partition = new Partition(PARTITION, TABLE, TEST_DATE_START, TEST_DATE_END, uniqueIndexColumns);

        final Field declaredField = FieldUtils.getDeclaredField(Partition.class, "uniqueIndexColumns", true);
        @SuppressWarnings("unchecked") final Collection<String> actual = (Collection<String>) declaredField.get(partition);

        Assertions.assertThat(actual).isNotSameAs(uniqueIndexColumns).containsExactlyInAnyOrderElementsOf(expectedUniqueIndexColumns);
    }

    @Test
    void shouldReturnNewUniqueIndexColumns_whenGetterIsCalled() {
        final Set<String> uniqueIndexColumns = Sets.set("column1", "column2");
        final Partition partition = new Partition(PARTITION, TABLE, TEST_DATE_START, TEST_DATE_END, uniqueIndexColumns);
        partition.setUniqueIndexColumns(uniqueIndexColumns);

        Assertions.assertThat(partition.getUniqueIndexColumns())
                .isNotSameAs(uniqueIndexColumns)
                .containsExactlyInAnyOrderElementsOf(uniqueIndexColumns);
    }

    static Stream<Arguments> provideUniqueIndexColumns() {
        final Set<String> uniqueIndexColumns = Sets.set("column1", "column2");
        return Stream.of(Arguments.of(null, Collections.emptySet()),
                Arguments.of(uniqueIndexColumns, uniqueIndexColumns));
    }
}