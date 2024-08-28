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

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.stream.Stream;

import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import kpi.model._helper.Serialization;
import kpi.model.api.table.OptionalTableAttribute;
import kpi.model.api.table.OptionalTableAttributeContract;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestReporter;

class SimpleTableDataLookBackLimitTest implements OptionalTableAttributeContract<Integer> {
    @Override
    public OptionalTableAttribute<Integer> createInstance() {
        return SimpleTableDataLookBackLimit.of(60);
    }

    @Override
    public String name() {
        return "data_lookback_limit";
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public String representation() {
        return "data_lookback_limit = 60";
    }

    @Override
    public Integer defaultValue() {
        return 7_200;
    }

    @Override
    public Stream<DynamicNode> deserializationCustomFailures(final TestReporter testReporter) {
        return Stream.of(dynamicTest("When attribute value is not greater than 0", () -> {
            final String content = "{ \"" + name() + "\": -5 }";
            Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()))
                    .isInstanceOf(ValueInstantiationException.class)
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .getRootCause()
                    .hasMessage("'%s' value '-5' must be greater than 0", name());
            testReporter.publishEntry(name(), content);
        }));
    }
}