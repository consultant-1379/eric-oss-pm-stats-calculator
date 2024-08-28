/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.simple.table.definition.required;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.stream.Stream;

import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import kpi.model._helper.Serialization;
import kpi.model.api.table.definition.RequiredDefinitionAttribute;
import kpi.model.api.table.definition.RequiredDefinitionAttributeContract;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestReporter;

class SimpleDefinitionExpressionTest implements RequiredDefinitionAttributeContract<String> {
    @Override
    public RequiredDefinitionAttribute<String> createInstance() {
        return SimpleDefinitionExpression.of("SELECT *");
    }

    @Override
    public String name() {
        return "expression";
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public String representation() {
        return "expression = SELECT *";
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

        final DynamicNode whenAttributeValueContainsFrom = dynamicTest("When attribute value contains 'FROM'", () -> {
            final String value = "SELECT * FROM <table>";
            final String content = "{ \"" + name() + "\": \"" + value + "\" }";
            Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()))
                    .isInstanceOf(ValueInstantiationException.class)
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .getRootCause()
                    .hasMessage("'%s' value '%s' cannot contain 'FROM'", name(), value);
            testReporter.publishEntry(name(), content);
        });

        final DynamicNode whenAttributeValueContainsTableName = dynamicTest("When attribute value contains 'kpi_post_agg://'", () -> {
            final String value = "kpi_post_agg://";
            final String content = "{ \"" + name() + "\": \"" + value + "\" }";
            Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()))
                    .isInstanceOf(ValueInstantiationException.class)
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .getRootCause()
                    .hasMessage("'%s' value '%s' cannot contain 'kpi_post_agg://'", name(), value);
            testReporter.publishEntry(name(), content);
        });

        return Stream.concat(whenAttributeValueIsBlank, Stream.of(whenAttributeValueContainsFrom, whenAttributeValueContainsTableName));
    }
}
