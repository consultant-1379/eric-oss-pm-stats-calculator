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

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.stream.Stream;

import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import kpi.model._helper.Serialization;
import kpi.model.api.table.api.TableAttribute;
import kpi.model.api.table.definition.OptionalDefinitionAttribute;
import kpi.model.api.table.definition.OptionalDefinitionAttributeContract;
import kpi.model.simple.table.required.SimpleTableInpDataIdentifier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestReporter;

class SimpleDefinitionInpDataIdentifierTest implements OptionalDefinitionAttributeContract<String> {
    @Override
    public OptionalDefinitionAttribute<String> createInstance() {
        return SimpleDefinitionInpDataIdentifier.of("dataSpace|category|schema");
    }

    @Override
    public TableAttribute<String> createParentInstance() {
        return SimpleTableInpDataIdentifier.of("dataSpace|category|parent_schema");
    }

    @Override
    public String name() {
        return "inp_data_identifier";
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public String representation() {
        return "inp_data_identifier = dataSpace|category|schema";
    }

    @Override
    public Class<? extends TableAttribute<String>> parentClass() {
        return SimpleTableInpDataIdentifier.class;
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
