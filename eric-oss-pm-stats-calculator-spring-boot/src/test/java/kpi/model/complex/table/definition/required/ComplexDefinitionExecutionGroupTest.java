/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.complex.table.definition.required;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.stream.Stream;

import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import kpi.model._helper.Serialization;
import kpi.model.api.table.definition.RequiredDefinitionAttribute;
import kpi.model.api.table.definition.RequiredDefinitionAttributeContract;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestReporter;

class ComplexDefinitionExecutionGroupTest implements RequiredDefinitionAttributeContract<String> {
    @Override
    public RequiredDefinitionAttribute<String> createInstance() {
        return ComplexDefinitionExecutionGroup.of("COMPLEX_1");
    }

    @Override
    public String name() {
        return "execution_group";
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public String representation() {
        return "execution_group = COMPLEX_1";
    }

    @Override
    public Stream<DynamicNode> deserializationCustomFailures(final TestReporter testReporter) {
        return Stream.of("\"\"", "\"  \"").map(value -> dynamicTest(String.format("When attribute value is '%s'", value), () -> {
            final String content = "{ \"" + name() + "\": " + value + " }";
            Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()))
                    .isInstanceOf(ValueInstantiationException.class)
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .getRootCause()
                    .hasMessage("'%s' value '%s' is blank, but this attribute is \"required\", must not be empty", name(), value.replaceAll("\"", ""));
            testReporter.publishEntry(name(), content);
        }));
    }
}
