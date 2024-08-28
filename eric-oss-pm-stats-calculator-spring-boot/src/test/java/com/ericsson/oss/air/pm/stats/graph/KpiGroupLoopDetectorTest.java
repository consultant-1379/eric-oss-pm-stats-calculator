/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.graph;

import static org.assertj.core.util.Sets.newLinkedHashSet;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.graph.model.KpiDefinitionGraph;
import com.ericsson.oss.air.pm.stats.graph.model.KpiDefinitionVertex;
import com.ericsson.oss.air.pm.stats.graph.route.RouteHelper;

import com.google.common.collect.Maps;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Cases documented at <a href="https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/IDUN/KPI+Execution+Group+loop+detection+test+scenarios">Confluence</a>.
 */
@ExtendWith(MockitoExtension.class)
class KpiGroupLoopDetectorTest {
    @Spy
    RouteHelper routeHelper;
    @Spy
    GraphCycleDetector graphCycleDetector;

    private static final String GREEN = "green";
    private static final String YELLOW = "yellow";
    private static final String PINK = "pink";
    private static final String BLUE = "blue";
    private static final String BROWN = "brown";
    private static final String RED = "red";
    private static final String ORANGE = "orange";

    @InjectMocks
    KpiGroupLoopDetector objectUnderTest;

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class ShouldDetectNoCycles {

        final Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> network = Maps.newHashMapWithExpectedSize(11);
        final KpiDefinitionVertex green0 = createVertex(GREEN, 0);
        final KpiDefinitionVertex blue6 = createVertex(BLUE, 6);

        @BeforeAll
        void setupGraphWithNoCycle() {
            final KpiDefinitionVertex green1 = createVertex(GREEN, 1);
            final KpiDefinitionVertex green3 = createVertex(GREEN, 3);

            final KpiDefinitionVertex yellow2 = createVertex(YELLOW, 2);

            final KpiDefinitionVertex pink4 = createVertex(PINK, 4);
            final KpiDefinitionVertex pink5 = createVertex(PINK, 5);

            final KpiDefinitionVertex blue7 = createVertex(BLUE, 7);

            final KpiDefinitionVertex brown8 = createVertex(BROWN, 8);
            final KpiDefinitionVertex brown9 = createVertex(BROWN, 9);
            final KpiDefinitionVertex brown10 = createVertex(BROWN, 10);

            network.put(green0, newLinkedHashSet(green1, green3));
            network.put(green1, newLinkedHashSet(yellow2));
            network.put(green3, newLinkedHashSet(pink4, pink5));
            network.put(yellow2, newLinkedHashSet());
            network.put(pink4, newLinkedHashSet());
            network.put(pink5, newLinkedHashSet());
            network.put(blue6, newLinkedHashSet(blue7));
            network.put(blue7, newLinkedHashSet(brown8));
            network.put(brown8, newLinkedHashSet(brown9, brown10));
            network.put(brown9, newLinkedHashSet());
            network.put(brown10, newLinkedHashSet());
        }

        @Test
        void shouldDetectNoCycle() {
            final List<Collection<KpiDefinitionVertex>> actual = objectUnderTest.collectLoopsOnExecutionGroups(
                    KpiDefinitionGraph.of(network, newLinkedHashSet(green0, blue6))
            );

            Assertions.assertThat(actual).isEmpty();
        }

        @Test
        void shouldDetectNoCycleInSameGroup() {
            final List<Collection<KpiDefinitionVertex>> actual = objectUnderTest.collectLoopsOnKpiDefinitions(
                    KpiDefinitionGraph.of(network, newLinkedHashSet(green0, blue6))
            );

            Assertions.assertThat(actual).isEmpty();
        }
    }

    @Nested
    class ShouldDetectCycles {
        @Test
        void testCase_1() {
            final KpiDefinitionVertex green0 = createVertex(GREEN, 0);
            final KpiDefinitionVertex green1 = createVertex(GREEN, 1);
            final KpiDefinitionVertex green3 = createVertex(GREEN, 3);
            final KpiDefinitionVertex green4 = createVertex(GREEN, 4);

            final KpiDefinitionVertex pink2 = createVertex(PINK, 2);
            final KpiDefinitionVertex pink5 = createVertex(PINK, 5);
            final KpiDefinitionVertex pink6 = createVertex(PINK, 6);

            final Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> network = Maps.newHashMapWithExpectedSize(7);
            network.put(green0, newLinkedHashSet(green1, pink2));
            network.put(green1, newLinkedHashSet(green3));
            network.put(green3, newLinkedHashSet(green4));
            network.put(green4, newLinkedHashSet());
            network.put(pink2, newLinkedHashSet(pink6));
            network.put(pink5, newLinkedHashSet(pink6));
            network.put(pink6, newLinkedHashSet(green4));

            final List<Collection<KpiDefinitionVertex>> actual = objectUnderTest.collectLoopsOnExecutionGroups(
                    KpiDefinitionGraph.of(network, newLinkedHashSet(green0, pink5))
            );

            Assertions.assertThat(actual).first().satisfies(route -> {
                Assertions.assertThat(route).containsExactly(green0, pink2, pink6, green4);
            });
        }

        @Test
        void testCase_2() {
            final KpiDefinitionVertex green0 = createVertex(GREEN, 0);
            final KpiDefinitionVertex green1 = createVertex(GREEN, 1);
            final KpiDefinitionVertex green3 = createVertex(GREEN, 3);

            final KpiDefinitionVertex red7 = createVertex(RED, 7);

            final KpiDefinitionVertex pink2 = createVertex(PINK, 2);
            final KpiDefinitionVertex pink5 = createVertex(PINK, 5);
            final KpiDefinitionVertex pink6 = createVertex(PINK, 6);
            final KpiDefinitionVertex pink11 = createVertex(PINK, 11);

            final KpiDefinitionVertex blue8 = createVertex(BLUE, 8);
            final KpiDefinitionVertex blue9 = createVertex(BLUE, 9);
            final KpiDefinitionVertex blue10 = createVertex(BLUE, 10);

            final Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> network = Maps.newHashMapWithExpectedSize(11);
            network.put(green0, newLinkedHashSet(red7, green1, pink2));
            network.put(green1, newLinkedHashSet(green3, blue8));
            network.put(green3, newLinkedHashSet());
            network.put(red7, newLinkedHashSet(green3));
            network.put(pink2, newLinkedHashSet(blue8, pink6));
            network.put(pink5, newLinkedHashSet(pink6));
            network.put(pink6, newLinkedHashSet(blue10));
            network.put(pink11, newLinkedHashSet(pink2));
            network.put(blue8, newLinkedHashSet(blue9));
            network.put(blue9, newLinkedHashSet());
            network.put(blue10, newLinkedHashSet());

            final List<Collection<KpiDefinitionVertex>> actual = objectUnderTest.collectLoopsOnExecutionGroups(
                    KpiDefinitionGraph.of(network, newLinkedHashSet(green0, pink11, pink5))
            );

            Assertions.assertThat(actual).first().satisfies(route -> {
                Assertions.assertThat(route).containsExactly(green0, red7, green3);
            });
        }

        @Test
        void testCase_3() {
            final KpiDefinitionVertex green0 = createVertex(GREEN, 0);
            final KpiDefinitionVertex green1 = createVertex(GREEN, 1);
            final KpiDefinitionVertex green2 = createVertex(GREEN, 2);
            final KpiDefinitionVertex green3 = createVertex(GREEN, 3);
            final KpiDefinitionVertex green4 = createVertex(GREEN, 4);

            final KpiDefinitionVertex yellow5 = createVertex(YELLOW, 5);
            final KpiDefinitionVertex yellow11 = createVertex(YELLOW, 11);

            final KpiDefinitionVertex blue6 = createVertex(BLUE, 6);
            final KpiDefinitionVertex blue7 = createVertex(BLUE, 7);
            final KpiDefinitionVertex blue8 = createVertex(BLUE, 8);
            final KpiDefinitionVertex blue9 = createVertex(BLUE, 9);

            final KpiDefinitionVertex pink14 = createVertex(PINK, 14);
            final KpiDefinitionVertex orange10 = createVertex(ORANGE, 10);

            final Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> network = Maps.newHashMapWithExpectedSize(13);
            network.put(green0, newLinkedHashSet(green1, green3));
            network.put(green1, newLinkedHashSet(green2));
            network.put(green2, newLinkedHashSet());
            network.put(green3, newLinkedHashSet(green4, yellow5));
            network.put(green4, newLinkedHashSet());
            network.put(yellow5, newLinkedHashSet(yellow11));
            network.put(yellow11, newLinkedHashSet(green4));
            network.put(blue6, newLinkedHashSet(blue7));
            network.put(blue7, newLinkedHashSet(pink14, blue8));
            network.put(blue8, newLinkedHashSet(blue9, orange10));
            network.put(blue9, newLinkedHashSet());
            network.put(pink14, newLinkedHashSet(blue9));
            network.put(orange10, newLinkedHashSet());


            final List<Collection<KpiDefinitionVertex>> actual = objectUnderTest.collectLoopsOnExecutionGroups(
                    KpiDefinitionGraph.of(network, newLinkedHashSet(green0, blue6))
            );

            Assertions.assertThat(actual)
                    .hasSize(2).as("number of cycles")
                    .satisfiesExactly(
                            route -> Assertions.assertThat(route).containsExactly(blue6, blue7, pink14, blue9),
                            route -> Assertions.assertThat(route).containsExactly(green0, green3, yellow5, yellow11, green4)
                    );
        }

        @Test
        void testCase_4() {
            final KpiDefinitionVertex green6 = createVertex(GREEN, 6);
            final KpiDefinitionVertex green7 = createVertex(GREEN, 7);
            final KpiDefinitionVertex green8 = createVertex(GREEN, 8);
            final KpiDefinitionVertex green10 = createVertex(GREEN, 10);
            final KpiDefinitionVertex green15 = createVertex(GREEN, 15);

            final KpiDefinitionVertex pink14 = createVertex(PINK, 14);

            final KpiDefinitionVertex blue9 = createVertex(BLUE, 9);

            final KpiDefinitionVertex yellow16 = createVertex(YELLOW, 16);

            final Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> network = Maps.newHashMapWithExpectedSize(8);
            network.put(green6, newLinkedHashSet(green7));
            network.put(green7, newLinkedHashSet(pink14, green8));
            network.put(green8, newLinkedHashSet(blue9, green10));
            network.put(green10, newLinkedHashSet(green15));
            network.put(green15, newLinkedHashSet());
            network.put(pink14, newLinkedHashSet(blue9));
            network.put(blue9, newLinkedHashSet(yellow16));
            network.put(yellow16, newLinkedHashSet(green15));

            final List<Collection<KpiDefinitionVertex>> actual = objectUnderTest.collectLoopsOnExecutionGroups(
                    KpiDefinitionGraph.of(network, newLinkedHashSet(green6))
            );

            Assertions.assertThat(actual)
                    .hasSize(2).as("number of cycles")
                    .satisfiesExactly(
                            route -> Assertions.assertThat(route).containsExactly(green6, green7, pink14, blue9, yellow16, green15),
                            route -> Assertions.assertThat(route).containsExactly(green6, green7, green8, blue9, yellow16, green15)
                    );
        }

        /**
         * <img src="./../../../../../../../../resources/graphs/loop-detector/test_case_5.png" alt="Created graph" />
         */
        @Test
        void testCase_5() {
            final KpiDefinitionVertex green0 = createVertex(GREEN, 0);
            final KpiDefinitionVertex green1 = createVertex(GREEN, 1);
            final KpiDefinitionVertex green3 = createVertex(GREEN, 3);
            final KpiDefinitionVertex green4 = createVertex(GREEN, 4);

            final KpiDefinitionVertex pink2 = createVertex(PINK, 2);
            final KpiDefinitionVertex pink5 = createVertex(PINK, 5);
            final KpiDefinitionVertex pink6 = createVertex(PINK, 6);

            final Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> network = Maps.newHashMapWithExpectedSize(7);
            network.put(green0, newLinkedHashSet(green1));
            network.put(green1, newLinkedHashSet(green0));
            network.put(green3, newLinkedHashSet(green0));
            network.put(green4, newLinkedHashSet());
            network.put(pink2, newLinkedHashSet(pink5));
            network.put(pink5, newLinkedHashSet(pink6));
            network.put(pink6, newLinkedHashSet(pink2));

            final List<Collection<KpiDefinitionVertex>> actual = objectUnderTest.collectLoopsOnKpiDefinitions(
                    KpiDefinitionGraph.of(network, newLinkedHashSet(green3, green4, pink5))
            );

            Assertions.assertThat(actual)
                    .hasSize(2).as("number of cycles")
                    .satisfiesExactly(
                            route -> Assertions.assertThat(route).containsExactly(green3, green0, green1, green0),
                            route -> Assertions.assertThat(route).containsExactly(pink5, pink6, pink2, pink5)
                    );
        }

        /**
         * <img src="./../../../../../../../../resources/graphs/loop-detector/test_case_6.png" alt="Created graph" />
         */
        @Test
        void testCase_6() {
            final KpiDefinitionVertex green0 = createVertex(GREEN, 0);
            final KpiDefinitionVertex green1 = createVertex(GREEN, 1);
            final KpiDefinitionVertex green3 = createVertex(GREEN, 3);
            final KpiDefinitionVertex green4 = createVertex(GREEN, 4);

            final KpiDefinitionVertex pink2 = createVertex(PINK, 2);
            final KpiDefinitionVertex pink5 = createVertex(PINK, 5);
            final KpiDefinitionVertex pink6 = createVertex(PINK, 6);

            final Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> network = Maps.newHashMapWithExpectedSize(7);
            network.put(green0, newLinkedHashSet(green1));
            network.put(green1, newLinkedHashSet(green3));
            network.put(green3, newLinkedHashSet(green0));
            network.put(green4, newLinkedHashSet(pink6));
            network.put(pink2, newLinkedHashSet(pink5));
            network.put(pink5, newLinkedHashSet(pink6));
            network.put(pink6, newLinkedHashSet(pink2));

            final List<Collection<KpiDefinitionVertex>> actual = objectUnderTest.collectLoopsOnKpiDefinitions(
                    KpiDefinitionGraph.of(network, newLinkedHashSet(green3, green4))
            );

            Assertions.assertThat(actual)
                    .hasSize(2).as("number of cycles")
                    .satisfiesExactly(
                            route -> Assertions.assertThat(route).containsExactly(green3, green0, green1, green3),
                            route -> Assertions.assertThat(route).containsExactly(green4, pink6, pink2, pink5, pink6)
                    );
        }

        @Test
        void testCase_7() {
            final KpiDefinitionVertex green0 = createVertex(GREEN, 0);

            final Map<KpiDefinitionVertex, Set<KpiDefinitionVertex>> network = Maps.newHashMapWithExpectedSize(1);
            network.put(green0, newLinkedHashSet(green0));

            final List<Collection<KpiDefinitionVertex>> actual = objectUnderTest.collectLoopsOnKpiDefinitions(
                    KpiDefinitionGraph.of(network, newLinkedHashSet(green0))
            );

            Assertions.assertThat(actual)
                    .hasSize(1).as("number of cycles")
                    .satisfiesExactly(route -> Assertions.assertThat(route).containsExactly(green0, green0));
        }

    }

    static KpiDefinitionVertex createVertex(final String color, final Integer number) {
        return KpiDefinitionVertex.builder().executionGroup(color).definitionName(String.valueOf(number)).build();
    }

}