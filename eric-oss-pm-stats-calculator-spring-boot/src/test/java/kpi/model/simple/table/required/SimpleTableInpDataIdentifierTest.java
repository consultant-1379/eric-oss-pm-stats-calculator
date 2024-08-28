/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.simple.table.required;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.stream.Stream;

import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import kpi.model._helper.Serialization;
import kpi.model.api.table.RequiredTableAttribute;
import kpi.model.api.table.RequiredTableAttributeContract;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestReporter;

class SimpleTableInpDataIdentifierTest implements RequiredTableAttributeContract<String> {
    @Override
    public RequiredTableAttribute<String> createInstance() {
        return SimpleTableInpDataIdentifier.of("dataSpace|category|schema");
    }

    @Override
    public String name() {
        return "inp_data_identifier";
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public String representation() {
        return "inp_data_identifier = dataSpace|category|schema";
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

        final DynamicNode whenAttributeValueDoesNotMatchPattern = dynamicTest("When attribute value is 'dummy_identifier'", () -> {
            final String content = "{ \"" + name() + "\": \"dummy_identifier\" }";
            Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()))
                    .isInstanceOf(ValueInstantiationException.class)
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .getRootCause()
                    .hasMessage("'%s' value 'dummy_identifier' has invalid format. Format must follow the \"^[^\\|]+\\|[^\\|]+\\|[^\\|]+$\" pattern", name());
            testReporter.publishEntry(name(), content);
        });

        return Stream.concat(whenAttributeValueIsBlank, Stream.of(whenAttributeValueDoesNotMatchPattern));
    }
}
