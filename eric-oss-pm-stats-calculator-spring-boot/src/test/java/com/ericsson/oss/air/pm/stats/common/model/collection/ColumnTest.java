/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.model.collection;

import static com.ericsson.oss.air.pm.stats.common.model.collection.Column.AGGREGATION_BEGIN_TIME;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Column.AGGREGATION_END_TIME;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.Collection;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

class ColumnTest {
    @Test
    void shouldVerifyInternals() {
        final Collection<Column> actual = Column.internals();
        Assertions.assertThat(actual).containsExactlyInAnyOrder(AGGREGATION_BEGIN_TIME, AGGREGATION_END_TIME);
    }

    @TestFactory
    Stream<DynamicTest> shouldVerifyIsInternal() {
        return Stream.of(
                dynamicTest(String.format("'%s' is internal", AGGREGATION_BEGIN_TIME.getName()),() -> {
                    Assertions.assertThat(AGGREGATION_BEGIN_TIME.isInternal()).isTrue();
                }),
                dynamicTest(String.format("'%s' is internal", AGGREGATION_END_TIME.getName()),() -> {
                    Assertions.assertThat(AGGREGATION_END_TIME.isInternal()).isTrue();
                }),
                dynamicTest("'random_column' is not internal",() -> {
                    Assertions.assertThat(Column.of("random_column").isInternal()).isFalse();
                })
        );
    }
}