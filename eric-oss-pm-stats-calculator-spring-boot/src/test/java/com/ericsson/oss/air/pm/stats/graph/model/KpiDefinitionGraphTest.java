/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.graph.model;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

import java.util.Collections;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class KpiDefinitionGraphTest {

    @Test
    void shouldInstantiate() {
        final KpiDefinitionVertex vertex1 = KpiDefinitionVertex.builder().executionGroup("group1").definitionName("1").build();

        final KpiDefinitionGraph actual = KpiDefinitionGraph.of(Collections.singletonMap(vertex1, emptySet()), singleton(vertex1));

        Assertions.assertThat(actual).isNotNull();
    }
}