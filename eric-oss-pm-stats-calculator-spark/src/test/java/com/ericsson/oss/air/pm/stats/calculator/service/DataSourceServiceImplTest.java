/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.repository.internal.api.DataSourceRepository;
import com.ericsson.oss.air.pm.stats.common.model.collection.Database;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataSourceServiceImplTest {
    @Mock DataSourceRepository dataSourceRepositoryMock;

    @InjectMocks DataSourceServiceImpl objectUnderTest;

    @ParameterizedTest
    @MethodSource("provideFindMaxUtcTimeStampData")
    void shouldVerifyFindMaxUtcTimestamp(final Optional<Timestamp> timestamp, final int expected) {
        final Database databaseMock = mock(Database.class);
        final Table tableMock = mock(Table.class);
        when(dataSourceRepositoryMock.findMaxAggregationBeginTime(databaseMock, tableMock)).thenReturn(timestamp);

        final Timestamp actual = objectUnderTest.findMaxUtcTimestamp(databaseMock, tableMock);

        verify(dataSourceRepositoryMock).findMaxAggregationBeginTime(databaseMock, tableMock);

        Assertions.assertThat(actual).hasTime(expected);
    }


    @ParameterizedTest
    @MethodSource("provideFindMinUtcTimeStampData")
    void shouldVerifyFindMinUtcTimestamp(final Optional<Timestamp> timestamp, final int expected) {
        final Database databaseMock = mock(Database.class);
        final Table tableMock = mock(Table.class);
        when(dataSourceRepositoryMock.findAggregationBeginTime(databaseMock, tableMock)).thenReturn(timestamp);

        final Timestamp actual = objectUnderTest.findMinUtcTimestamp(databaseMock, tableMock);

        verify(dataSourceRepositoryMock).findAggregationBeginTime(databaseMock, tableMock);

        Assertions.assertThat(actual).hasTime(expected);
    }

    static Stream<Arguments> provideFindMinUtcTimeStampData() {
        return Stream.of(
                Arguments.of(Optional.empty(), 0),
                Arguments.of(Optional.of(new Timestamp(10)), 10)
        );
    }

    static Stream<Arguments> provideFindMaxUtcTimeStampData() {
        return Stream.of(
                Arguments.of(Optional.empty(), 0),
                Arguments.of(Optional.of(new Timestamp(10)), 10)
        );
    }
}