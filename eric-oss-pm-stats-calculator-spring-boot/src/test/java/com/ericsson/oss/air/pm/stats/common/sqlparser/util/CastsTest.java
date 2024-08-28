/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.sqlparser.util;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.Optional;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class CastsTest {

    @TestFactory
    Stream<DynamicTest> shouldVerifyTryCast() {
        return Stream.of(
                dynamicTest("Verify can cast if assignable", () -> {
                    final Optional<String> actual = Casts.tryCast("hello", String.class);
                    Assertions.assertThat(actual).hasValue("hello");
                }),
                dynamicTest("Verify can cast if not assignable", () -> {
                    final Optional<String> actual = Casts.tryCast(1, String.class);
                    Assertions.assertThat(actual).isEmpty();
                })
        );
    }

}