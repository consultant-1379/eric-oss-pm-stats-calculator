/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.simple.table.definition.optional;

import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import kpi.model._helper.Serialization;
import kpi.model.api.table.api.TableAttribute;
import kpi.model.api.table.definition.OptionalDefinitionAttribute;
import kpi.model.api.table.definition.OptionalDefinitionAttributeContract;
import kpi.model.simple.table.optional.SimpleTableDataLookBackLimit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestReporter;

import java.util.stream.Stream;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class SimpleDefinitionDataLookBackLimitTest implements OptionalDefinitionAttributeContract<Integer> {
    @Override
    public OptionalDefinitionAttribute<Integer> createInstance() {
        return SimpleDefinitionDataLookBackLimit.of(10);
    }

    @Override
    public TableAttribute<Integer> createParentInstance() {
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
        return "data_lookback_limit = 10";
    }

    @Override
    public Class<? extends TableAttribute<Integer>> parentClass() {
        return SimpleTableDataLookBackLimit.class;
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