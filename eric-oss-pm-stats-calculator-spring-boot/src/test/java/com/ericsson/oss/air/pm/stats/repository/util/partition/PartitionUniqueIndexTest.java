/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util.partition;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.common.model.collection.Column;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PartitionUniqueIndexTest {
    private static final String PARTITION_NAME = "partitionName";
    private static final String UNIQUE_INDEX = "uniqueIndex";

    @MethodSource("provideIndexColumns")
    @ParameterizedTest(name = "[{index}] Constructor called with index columns of: {0} and field initialized to: {1}")
    void shouldVerifyPartitionUniqueIndexCreatedWithNewIndexColumns(final List<Column> indexColumns,
                                                                    final Collection<Column> expectedIndexColumns) throws IllegalAccessException {
        final PartitionUniqueIndex partitionUniqueIndex = new PartitionUniqueIndex(PARTITION_NAME, UNIQUE_INDEX, indexColumns);

        final Field declaredField = FieldUtils.getDeclaredField(PartitionUniqueIndex.class, "indexes", true);
        @SuppressWarnings("unchecked") final Collection<Column> actual = (Collection<Column>) declaredField.get(partitionUniqueIndex);

        Assertions.assertThat(actual)
                .isNotSameAs(indexColumns)
                .containsExactlyInAnyOrderElementsOf(expectedIndexColumns);
    }

    @Test
    void shouldReturnNewIndexColumns_whenGetterIsCalled() {
        final PartitionUniqueIndex partitionUniqueIndex = new PartitionUniqueIndex(
                PARTITION_NAME,
                UNIQUE_INDEX,
                Lists.list(Column.of("column1"), Column.of("column2"))
        );

        Assertions.assertThat(partitionUniqueIndex.indexColumns()).containsExactly(
                Column.of("column1"),
                Column.of("column2")
        );
    }

    @Test
    void shouldTrimColumnsWhenNeeded() {
        final PartitionUniqueIndex partitionUniqueIndex = new PartitionUniqueIndex(PARTITION_NAME, UNIQUE_INDEX, List.of(
                Column.of("column1"), Column.of("\"column2\"")
        ));

        Assertions.assertThat(partitionUniqueIndex.indexColumnsWithTrimmedQuotes()).containsExactly(
                Column.of("column1"), Column.of("column2")
        );
    }

    private static Stream<Arguments> provideIndexColumns() {
        final List<Column> indexColumns = Lists.list(Column.of("column1"), Column.of("column2"));
        return Stream.of(
                Arguments.of(null, Collections.emptyList()),
                Arguments.of(indexColumns, indexColumns));
    }
}