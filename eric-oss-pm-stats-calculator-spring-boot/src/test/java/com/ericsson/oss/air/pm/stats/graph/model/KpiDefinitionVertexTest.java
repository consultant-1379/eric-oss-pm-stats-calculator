/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.graph.model;

import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class KpiDefinitionVertexTest {

    @MethodSource("provideSameExecutionGroupData")
    @ParameterizedTest(name = "[{index}] Vertex with group ''{0}'' and vertex with group ''{1}'' has same execution group ==> ''{2}''")
    void shouldVerifyIsSameExecutionGroup(final String groupA, final String groupB, final boolean expected) {
        final KpiDefinitionVertex vertex1 = vertex(groupA, "name");
        final KpiDefinitionVertex vertex2 = vertex(groupB, "name");

        final boolean actual = vertex1.hasSameExecutionGroup(vertex2);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @MethodSource("provideDifferentExecutionGroupData")
    @ParameterizedTest(name = "[{index}] Vertex with group ''{0}'' and vertex with group ''{1}'' has different execution group ==> ''{2}''")
    void shouldVerifyDifferentExecutionGroup(final String groupA, final String groupB, final boolean expected) {
        final KpiDefinitionVertex vertex1 = vertex(groupA, "name");
        final KpiDefinitionVertex vertex2 = vertex(groupB, "name");

        final boolean actual = vertex1.hasDifferentExecutionGroup(vertex2);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @CsvSource({
            "definition-1,definition-1,true",
            "definition-1,definition-2,false",
            "definition-1,Definition-1,false",
    })
    @ParameterizedTest(name = "[{index}] Vertex with definition ''{0}'' and vertex with definition ''{1}'' has same definition ==> ''{2}''")
    void shouldTestSameDefinitionName(final String name1, final String name2, final boolean expected) {
        final KpiDefinitionVertex vertex1 = vertex("group", name1);
        final KpiDefinitionVertex vertex2 = vertex("group", name2);

        final boolean actual = vertex1.hasSameDefinitionName(vertex2);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> provideSameExecutionGroupData() {
        return Stream.of(
                Arguments.of("group1", "group1", true),
                Arguments.of("GrOuP1", "group1", false)
        );
    }

    static Stream<Arguments> provideDifferentExecutionGroupData() {
        return Stream.of(
                Arguments.of("group1", "group1", false),
                Arguments.of("GrOuP1", "group1", true)
        );
    }

    static KpiDefinitionVertex vertex(final String group, final String name) {
        return KpiDefinitionVertex.builder().executionGroup(group).definitionName(name).build();
    }
}
