/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.model.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class InstancesTest {
    @MethodSource("provideInstanceOfData")
    @ParameterizedTest(name = "[{index}] collection: ''{0}'' instance of: ''{1}'' ==>  ''{2}''")
    void shouldVerifyInstanceOf(final Collection<Integer> collection, final Class<?> clazz, final boolean expected) {
        final boolean actual = Instances.isInstanceOf(collection, clazz);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldReturnEmptyList() {
        final List<?> datas = Instances.cast(Collections.emptyList(), Optional.class);
        Assertions.assertThat(datas).isEmpty();
    }

    @MethodSource("provideCastData")
    @ParameterizedTest(name = "[{index}] collection: ''{0}'' instance of: ''{1}'' ==>  ''{2}''")
    <T> void shouldCastCollection(final Collection<?> collection, final Class<T> clazz) {
        final List<?> datas = Instances.cast(collection, clazz);

        Assertions.assertThat(datas.get(0)).isInstanceOf(clazz);
    }

    private static Stream<Arguments> provideCastData() {
        return Stream.of(
                Arguments.of(Arrays.asList("filter1", "filter2"), String.class),
                Arguments.of(Arrays.asList(new Filter("filter1"), new Filter("filter2")), Filter.class),
                Arguments.of(Arrays.asList(1, 2, 3), Integer.class)
        );
    }

    private static Stream<Arguments> provideInstanceOfData() {
        return Stream.of(
                Arguments.of(Arrays.asList(1, 2, 3), String.class, false),
                Arguments.of(Arrays.asList(1, 2, 3), Integer.class, true),
                Arguments.of(Collections.emptyList(), Object.class, false)
        );
    }
}