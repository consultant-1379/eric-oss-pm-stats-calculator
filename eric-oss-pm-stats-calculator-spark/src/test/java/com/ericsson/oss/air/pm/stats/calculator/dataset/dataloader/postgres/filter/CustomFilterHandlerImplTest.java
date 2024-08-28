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

import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.api.FilterHandler;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CustomFilterHandlerImplTest {
    FilterHandler objectUnderTest = new CustomFilterHandlerImpl();

    @MethodSource("provideSupportData")
    @ParameterizedTest(name = "[{index}] Should support type ''{0}'' ==> ''{1}''")
    void shouldSupport(final FilterType filterType, final boolean expected) {
        final boolean actual = objectUnderTest.supports(filterType);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldGenerateFilterSql() {
        final String actual = objectUnderTest.filterSql(Table.of("table"), "filter");

        Assertions.assertThat(actual).isEqualTo("SELECT * FROM table WHERE filter");
    }

    static Stream<Arguments> provideSupportData() {
        return Stream.of(
                Arguments.of(FilterType.CUSTOM, true),
                Arguments.of(FilterType.DEFAULT, false)
        );
    }
}