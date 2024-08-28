/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.simple.table.optional;

import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import kpi.model.api.table.OptionalTableAttribute;
import kpi.model.api.table.OptionalTableAttributeContract;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;

class SimpleTableAggregationPeriodTest implements OptionalTableAttributeContract<Integer> {
    @Override
    public OptionalTableAttribute<Integer> createInstance() {
        return SimpleTableAggregationPeriod.of(60);
    }

    @Override
    public String name() {
        return "aggregation_period";
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public String representation() {
        return "aggregation_period = 60";
    }

    @Override
    public Integer defaultValue() {
        return -1;
    }

    @Override
    public Stream<DynamicNode> provideCustomValueValidations() {
        final DynamicContainer whenValueIsNotKnown = dynamicContainer(
                "When value is not known",
                IntStream.of(-1, 45).mapToObj(value -> dynamicTest(String.format("Value '%d' is not known", value), () -> {
                    Assertions.assertThatThrownBy(() -> SimpleTableAggregationPeriod.of(value))
                            .isInstanceOf(IllegalArgumentException.class)
                            .hasMessage("value '%s' for '%s' is not valid", value, name());
                }))
        );

        final DynamicContainer failure = dynamicContainer("Failure", Stream.of(whenValueIsNotKnown));

        final DynamicContainer whenValueIsKnown = dynamicContainer(
                "When value is known",
                IntStream.of(15, 60, 1_440).mapToObj(value -> dynamicTest(String.format("Value '%d' is known", value), () -> {
                    Assertions.assertThatNoException().isThrownBy(() -> SimpleTableAggregationPeriod.of(value));
                }))
        );

        final DynamicContainer success = dynamicContainer("Success", Stream.of(whenValueIsKnown));

        return Stream.of(failure, success);
    }

}