/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.api.FilterHandlerRegistryFacade;
import com.ericsson.oss.air.pm.stats.calculator.util.KpiCalculatorTimeSlot;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

@ExtendWith(MockitoExtension.class)
class FilterPreparatoryImplTest {
    @Mock FilterHandlerRegistryFacade filterHandlerRegistryFacadeMock;

    @InjectMocks FilterPreparatoryImpl objectUnderTest;

    @Test
    void shouldGenerateTimeBasedFilter() {
        final Filter actual = objectUnderTest.generateTimeBasedFilter(new KpiCalculatorTimeSlot(
                Timestamp.valueOf("2022-08-01 12:00:00"),
                Timestamp.valueOf("2022-08-01 12:59:59")
        ));

        Assertions.assertThat(actual).isEqualTo(
                Filter.of(
                        "aggregation_begin_time BETWEEN TO_TIMESTAMP('2022-08-01 12:00:00.0') AND TO_TIMESTAMP('2022-08-01 12:59:59.0')"
                )
        );
    }

    @Nested
    @DisplayName("Testing preparation of filter")
    class PrepareFilter {
        @Mock DefaultFilterHandlerImpl defaultFilterHandlerMock;

        @ParameterizedTest(name = "[{index}] With table ''{0}'' and filter ''{1}'' prepared filter returned ==> ''{2}''")
        @MethodSource("com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.FilterPreparatoryImplTest#provideEmptyFilterData")
        void prepareFilter_whenFilterIsEmpty(final Table table, final String filter, final String expected) {
            final String actual = objectUnderTest.prepareFilter(FilterType.CUSTOM, table, filter);
            Assertions.assertThat(actual).isEqualTo(expected);
        }

        @Test
        void prepareFilter_whenFilterIsNotEmpty() {
            when(filterHandlerRegistryFacadeMock.filterHandler(FilterType.DEFAULT)).thenReturn(defaultFilterHandlerMock);

            objectUnderTest.prepareFilter(FilterType.DEFAULT, Table.of("table"), "filter");

            verify(filterHandlerRegistryFacadeMock).filterHandler(FilterType.DEFAULT);
            verify(defaultFilterHandlerMock).filterSql(Table.of("table"), "filter");
        }
    }

    static Stream<Arguments> provideEmptyFilterData() {
        return Stream.of(
                Arguments.of(Table.of("table"), null, "SELECT * FROM table"),
                Arguments.of(Table.of("table"), StringUtils.EMPTY, "SELECT * FROM table")
        );
    }
}