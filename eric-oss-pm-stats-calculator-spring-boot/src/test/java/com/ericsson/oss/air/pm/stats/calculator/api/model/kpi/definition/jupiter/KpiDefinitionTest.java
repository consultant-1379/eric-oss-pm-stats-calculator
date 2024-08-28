/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.jupiter;

import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class KpiDefinitionTest {

    @MethodSource("provideHasDataIdentifierData")
    @ParameterizedTest(name = "[{index}] KPI definition with Data Identifier ''{0}'' has data identifier ==> ''{1}''")
    void verifyHasDataIdentifier(final DataIdentifier dataIdentifier, final boolean expected) {
        final KpiDefinition kpiDefinition = KpiDefinition.builder().withInpDataIdentifier(dataIdentifier).build();

        final boolean actual = kpiDefinition.hasInputDataIdentifier();

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @MethodSource("provideIsScheduledData")
    @ParameterizedTest(name = "[{index}] KPI definition with execution group ''{0}'' is SCHEDULED ==> ''{1}''")
    void shouldVerifyIsScheduled(final String executionGroup, final boolean expected) {
        final KpiDefinition kpiDefinition = KpiDefinition.builder().withExecutionGroup(executionGroup).build();

        final boolean actual = kpiDefinition.isScheduled();

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @MethodSource("provideIsDefaultAggregationPeriodData")
    @ParameterizedTest(name = "[{index}] KPI definition with aggregation period ''{0}'' is DEFAULT ==> ''{1}''")
    void shouldVerifyIsDefaultAggregationPeriod(final String aggregationPeriod, final boolean expected) {
        final KpiDefinition kpiDefinition = KpiDefinition.builder().withAggregationPeriod(aggregationPeriod).build();

        final boolean actual = kpiDefinition.isDefaultAggregationPeriod();

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @MethodSource("provideHasSameIdentifierData")
    @ParameterizedTest(name = "[{index}] KPI definition with DataIdentifier ''{0}'' is the same as ''{1}'' ==> ''{2}''")
    void shouldVerifyHasSameIdentifier(final DataIdentifier identifier, final DataIdentifier toCheck, final boolean expected) {
        final KpiDefinition kpiDefinition = KpiDefinition.builder().withInpDataIdentifier(identifier).build();

        final boolean actual = kpiDefinition.hasSameDataIdentifier(toCheck);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    private static Stream<Arguments> provideIsDefaultAggregationPeriodData() {
        return Stream.of(
                Arguments.of("-1", true),
                Arguments.of("60", false),
                Arguments.of("1440", false)
        );
    }

    private static Stream<Arguments> provideIsScheduledData() {
        return Stream.of(
                Arguments.of(null, false),
                Arguments.of(StringUtils.EMPTY, false),
                Arguments.of("null", false),
                Arguments.of("validExecutionGroup", true),
                Arguments.of("  ", false)
        );
    }

    private static Stream<Arguments> provideHasDataIdentifierData() {
        return Stream.of(
                Arguments.of(DataIdentifier.of(null), false),
                Arguments.of(DataIdentifier.of("dataSpace", "topic", "schema"), true)
        );
    }

    static Stream<Arguments> provideHasSameIdentifierData() {
        return Stream.of(
                Arguments.of(null, DataIdentifier.of("identifier"), false),
                Arguments.of(DataIdentifier.of("otherIdentifier"), DataIdentifier.of("identifier"), false),
                Arguments.of(DataIdentifier.of("identifier"), DataIdentifier.of("identifier"), true)
        );
    }
}