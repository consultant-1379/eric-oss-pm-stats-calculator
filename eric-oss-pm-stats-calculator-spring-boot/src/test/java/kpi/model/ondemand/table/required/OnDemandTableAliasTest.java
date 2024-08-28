/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.ondemand.table.required;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.function.Function;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import kpi.model._helper.Serialization;
import kpi.model.api.table.RequiredTableAttribute;
import kpi.model.api.table.RequiredTableAttributeContract;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestReporter;

class OnDemandTableAliasTest implements RequiredTableAttributeContract<String> {
    @Override
    public RequiredTableAttribute<String> createInstance() {
        return OnDemandTableAlias.of("alias_dummy");
    }

    @Override
    public String name() {
        return "alias";
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public String representation() {
        return "alias = alias_dummy";
    }

    @Override
    public Stream<DynamicNode> deserializationCustomFailures(final TestReporter testReporter) {
        final Stream<DynamicNode> whenAttributeValueIsBlank = Stream.of("\"\"", "\"  \"")
                .map(value -> dynamicTest(String.format("When attribute value is '%s'", value), () -> {
                    final String content = "{ \"" + name() + "\": " + value + " }";
                    Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()))
                            .isInstanceOf(ValueInstantiationException.class)
                            .hasRootCauseInstanceOf(IllegalArgumentException.class)
                            .getRootCause()
                            .hasMessage("'%s' value '%s' is blank, but this attribute is \"required\", must not be empty", name(), value.replaceAll("\"", ""));
                    testReporter.publishEntry(name(), content);
                }));

        final DynamicNode whenAttributeValueIsParameterized = dynamicTest("When attribute value contains parameter token", () -> {
            final String content = "{ \"" + name() + "\": \"${<parameter>}\" }";
            Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()))
                    .isInstanceOf(ValueInstantiationException.class)
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .getRootCause()
                    .hasMessageContaining("The attribute '%s' does not support parameters", name());
            testReporter.publishEntry(name(), content);
        });

        final Stream<DynamicNode> whenAttributeValueDoesNotMatchPattern = Stream.of("Alias_dummy", "aliasDummy")
                .map(value -> dynamicTest(String.format("When attribute value is '%s'", value), () -> {
                    final String content = "{ \"" + name() + "\": \"" + value + "\" }";
                    Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()))
                            .isInstanceOf(ValueInstantiationException.class)
                            .hasRootCauseInstanceOf(IllegalArgumentException.class)
                            .getRootCause()
                            .hasMessage("'%s' value '%s' has invalid format. Format must follow the \"^[a-z][a-z0-9_]{0,38}$\" pattern", name(), value);
                    testReporter.publishEntry(name(), content);
                }));

        return Stream.concat(
                Stream.of(whenAttributeValueIsBlank, whenAttributeValueDoesNotMatchPattern).flatMap(Function.identity()),
                Stream.of(whenAttributeValueIsParameterized)
        );
    }
}
