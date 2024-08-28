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

import static kpi.model._helper.Mapper.toAggregationElements;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.List;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.common.model.attribute.Attribute;

import com.fasterxml.jackson.databind.exc.InvalidNullException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import kpi.model._helper.Serialization;
import kpi.model.api.table.api.TableAttribute;
import kpi.model.api.table.definition.OptionalDefinitionAttribute;
import kpi.model.api.table.definition.OptionalDefinitionAttributeContract;
import kpi.model.simple.element.SimpleAggregationElement;
import kpi.model.simple.table.required.SimpleTableAggregationElements;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestReporter;

class SimpleDefinitionAggregationElementsTest implements OptionalDefinitionAttributeContract<List<SimpleAggregationElement>> {
    @Override
    public OptionalDefinitionAttribute<List<SimpleAggregationElement>> createInstance() {
        return SimpleDefinitionAggregationElements.of(toAggregationElements("table.column", "table.column1 AS column1"));
    }

    @Override
    public TableAttribute<List<SimpleAggregationElement>> createParentInstance() {
        return SimpleTableAggregationElements.of(toAggregationElements("table.column3", "table.column4 AS column4"));
    }

    @Override
    public String name() {
        return "aggregation_elements";
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public String representation() {
        return "aggregation_elements = [table.column, table.column1 AS column1]";
    }

    @Override
    public Class<? extends TableAttribute<List<SimpleAggregationElement>>> parentClass() {
        return SimpleTableAggregationElements.class;
    }

    @Override
    public Stream<ValueArguments<List<SimpleAggregationElement>>> provideOverridableCases() {
        return Stream.of(
                ValueArguments.of(SimpleDefinitionAggregationElements.of(List.of()), toAggregationElements("table.column"))
        );
    }

    @Override
    public Stream<ValueArguments<List<SimpleAggregationElement>>> provideNonOverridableCases() {
        return Stream.of(
                ValueArguments.of(SimpleDefinitionAggregationElements.of(toAggregationElements("table.column")), null),
                ValueArguments.of(SimpleDefinitionAggregationElements.of(toAggregationElements("table.column")), toAggregationElements("table.column1"))
        );
    }

    @Override
    public Stream<DynamicNode> deserializationCustomSuccesses(final TestReporter testReporter) {
        return Stream.of(dynamicTest("When attribute values are missing", () -> {
            final String content = "{ \"" + name() + "\": [] }";
            final Attribute<List<SimpleAggregationElement>> attribute = Serialization.deserialize(content, deduceSelfClass());
            Assertions.assertThat(attribute.value()).isEmpty();
            testReporter.publishEntry(name(), content);
        }));
    }

    @Override
    public Stream<DynamicNode> deserializationCustomFailures(final TestReporter testReporter) {
        final DynamicNode whenAnyValueIsNull = dynamicTest("When any attribute value is null", () -> {
            final String content = "{ \"" + name() + "\": [null] }";
            Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()))
                    .isInstanceOf(InvalidNullException.class)
                    .hasMessageContaining("Invalid `null` value encountered for property \"%s\"", name());
            testReporter.publishEntry(name(), content);
        });

        final Stream<DynamicNode> whenAnyValueIsInvalid = Stream.of("\"\"", "\"  \"").map(value -> {
            return dynamicTest(String.format("When any attribute value is '%s'", value), () -> {
                final String content = "{ \"" + name() + "\": [" + value + "] }";
                Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()))
                        .isInstanceOf(ValueInstantiationException.class)
                        .hasRootCauseInstanceOf(IllegalArgumentException.class)
                        .getRootCause()
                        .hasMessage("Table attribute '%s' is empty, but this attribute is \"required\", must not be empty", value.replaceAll("\"", ""));
                testReporter.publishEntry(name(), content);
            });
        });

        final DynamicNode whenAnyValueDoesNotMatchPattern = dynamicTest("When any attribute value is [\"_invalid_value\"]", () -> {
            final String content = "{ \"" + name() + "\": [\"_invalid_value\"] }";
            Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()))
                    .isInstanceOf(ValueInstantiationException.class)
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .getRootCause()
                    .hasMessage("'aggregation_element' value '_invalid_value' has invalid format. Format must follow the \"^[a-zA-Z][a-zA-Z0-9_]{0,55}\\.[a-zA-Z][a-zA-Z0-9_]{0,55}$|^[a-zA-Z][a-zA-Z0-9_]{0,55}\\.[a-zA-Z][a-zA-Z0-9_]{0,55} (?i)AS [a-zA-Z][a-zA-Z0-9_]{0,55}$|^'\\$\\{[a-zA-Z][a-zA-Z0-9_.]*\\}' (?i)AS [a-zA-Z][a-zA-Z0-9_]{0,55}$\" pattern");
            testReporter.publishEntry(name(), content);
        });

        return Stream.concat(
                Stream.of(whenAnyValueIsNull, whenAnyValueDoesNotMatchPattern),
                whenAnyValueIsInvalid
        );
    }
}
