/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class KpiDefinitionPayloadTest {
    @MethodSource("provideConstructorArguments")
    @ParameterizedTest(name = "[{index}] kpiDefinitions: {0} converted to {1}")
    void shouldConstructObject(final List<Map<String, Object>> kpiDefinitions,
                               final List<Map<String, Object>> expectedKpiDefinitions) {
        final String source = "source";
        final KpiDefinitionPayload kpiDefinitionPayload = new KpiDefinitionPayload(source, kpiDefinitions);

        Assertions.assertThat(kpiDefinitionPayload.getSource()).isEqualTo(source);
        Assertions.assertThat(kpiDefinitionPayload)
                .extracting("kpiDefinitions", InstanceOfAssertFactories.list(Map.class))
                .isEqualTo(expectedKpiDefinitions);
    }

    @Test
    void shouldReturnUnmodifiableList_whenCallingKpiDefinitionsGetter() {
        final KpiDefinitionPayload kpiDefinitionPayload = new KpiDefinitionPayload();

        Assertions.assertThat(kpiDefinitionPayload.getKpiDefinitions()).isUnmodifiable();
    }

    private static Stream<Arguments> provideConstructorArguments() {
        return Stream.of(Arguments.of(null, Collections.emptyList()),
                Arguments.of(Collections.singletonList(Collections.singletonMap("key", "value")),
                        Collections.singletonList(Collections.singletonMap("key", "value"))));
    }
}