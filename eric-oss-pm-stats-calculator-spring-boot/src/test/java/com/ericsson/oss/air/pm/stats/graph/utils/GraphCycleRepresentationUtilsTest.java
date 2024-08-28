/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.graph.utils;

import static com.ericsson.oss.air.pm.stats.graph.utils.GraphCycleRepresentationUtils.processExecutionGroupRepresentations;
import static com.ericsson.oss.air.pm.stats.graph.utils.GraphCycleRepresentationUtils.processKpiDefinitionRepresentations;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.graph.model.KpiDefinitionVertex;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class GraphCycleRepresentationUtilsTest {
    static final KpiDefinitionVertex GREEN_0 = createVertex("green", 0);
    static final KpiDefinitionVertex GREEN_1 = createVertex("green", 1);
    static final KpiDefinitionVertex GREEN_3 = createVertex("green", 3);
    static final KpiDefinitionVertex YELLOW_2 = createVertex("yellow", 2);
    static final KpiDefinitionVertex PINK_4 = createVertex("pink", 4);
    static final KpiDefinitionVertex PINK_5 = createVertex("pink", 5);

    @MethodSource("provideCycleGroupRouteData")
    @ParameterizedTest(name = "[{index}] Route of vertexes ''{0}'' cycle ==> ''{1}''")
    void withExecutionGroupCircleListErrorResponseDataTest(final Collection<KpiDefinitionVertex> vertexes, final String expected) {
        final List<String> actual = processExecutionGroupRepresentations(singletonList(vertexes));
        assertThat(actual).containsOnly(expected);
    }

    @MethodSource("provideCycleKpiDefinitionRouteData")
    @ParameterizedTest(name = "[{index}] Route of vertexes ''{0}'' cycle ==> ''{1}''")
    void withKpiDefinitionCircleListErrorResponseDataTest(final Collection<KpiDefinitionVertex> vertexes, final String expected) {
        final List<String> actual = processKpiDefinitionRepresentations(singletonList(vertexes));
        assertThat(actual).containsOnly(expected);
    }

    @Test
    void exceptionWithEmptyInputDataTest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> processExecutionGroupRepresentations(singletonList(emptyList())))
                .withMessage("The provided route has to have at least two vertexes")
                .withNoCause();
    }

    @Test
    void exceptionWithNoCycleDataTest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> processExecutionGroupRepresentations(singletonList(asList(YELLOW_2, PINK_4, GREEN_3))))
                .withMessage("Original route contained no cycle")
                .withNoCause();
    }

    @Test
    void exceptionWhenDifferentExecutionGroup() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> processKpiDefinitionRepresentations(singletonList(asList(YELLOW_2, PINK_4, GREEN_3))))
                .withMessage("execution group must be unique but found '[pink, green, yellow]'")
                .withNoCause();
    }

    static Stream<Arguments> provideCycleGroupRouteData() {
        return Stream.of(
                Arguments.of(asList(GREEN_0, GREEN_1, PINK_4, YELLOW_2, GREEN_3), "green.1 -> pink.4 -> yellow.2 -> green.3"),
                Arguments.of(asList(GREEN_0, PINK_5, PINK_4, GREEN_1, YELLOW_2, GREEN_3), "green.1 -> yellow.2 -> green.3")
        );
    }

    static Stream<Arguments> provideCycleKpiDefinitionRouteData() {
        return Stream.of(
                Arguments.of(asList(GREEN_0, GREEN_1, GREEN_3, GREEN_1), "green.1 -> green.3 -> green.1"),
                Arguments.of(asList(GREEN_0, GREEN_1, GREEN_0), "green.0 -> green.1 -> green.0")
        );
    }

    static KpiDefinitionVertex createVertex(final String color, final Integer number) {
        return KpiDefinitionVertex.builder().executionGroup(color).definitionName(String.valueOf(number)).build();
    }
}
