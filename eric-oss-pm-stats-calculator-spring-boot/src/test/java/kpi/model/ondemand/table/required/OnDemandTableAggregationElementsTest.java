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

import static kpi.model._helper.Mapper.toOnDemandAggregationElements;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.exc.InvalidNullException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import kpi.model._helper.Serialization;
import kpi.model.api.table.RequiredTableAttribute;
import kpi.model.api.table.RequiredTableAttributeContract;
import kpi.model.ondemand.element.OnDemandAggregationElement;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestReporter;

class OnDemandTableAggregationElementsTest implements RequiredTableAttributeContract<List<OnDemandAggregationElement>> {
    @Override
    public RequiredTableAttribute<List<OnDemandAggregationElement>> createInstance() {
        return OnDemandTableAggregationElements.of(toOnDemandAggregationElements("table.column", "table.column1 AS column1", "'${param.column2}' AS column2", "'${column3}' AS column3"));
    }

    @Override
    public String name() {
        return "aggregation_elements";
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public String representation() {
        return "aggregation_elements = [table.column, table.column1 AS column1, '${param.column2}' AS column2, '${column3}' AS column3]";
    }

    @Override
    public Stream<DynamicNode> provideCustomValueValidations() {
        final DynamicNode whenValueIsEmpty = dynamicTest("When value is empty", () -> {
            Assertions.assertThatThrownBy(() -> OnDemandTableAggregationElements.of(List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Table attribute '%s' is empty, but this attribute is \"required\", must not be empty", name());
        });

        final DynamicContainer failure = dynamicContainer("Failure", Stream.of(whenValueIsEmpty));

        return Stream.of(failure);
    }

    @Override
    public Stream<DynamicNode> deserializationCustomFailures(final TestReporter testReporter) {
        final DynamicNode whenAttributeValuesAreMissing = dynamicTest("When attribute values are missing", () -> {
            final String content = "{ \"" + name() + "\": [] }";
            Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()))
                    .isInstanceOf(ValueInstantiationException.class)
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .getRootCause()
                    .hasMessage("Table attribute 'aggregation_elements' is empty, but this attribute is \"required\", must not be empty");
            testReporter.publishEntry(name(), content);
        });

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
                    .hasMessage("'aggregation_element' value '_invalid_value' has invalid format. Format must follow the \"^[a-zA-Z][a-zA-Z0-9_]{0,55}\\.[a-zA-Z][a-zA-Z0-9_]{0,55}$|^[a-zA-Z][a-zA-Z0-9_]{0,55}\\.[a-zA-Z][a-zA-Z0-9_]{0,55}\\s+(?i)AS\\s+[a-zA-Z][a-zA-Z0-9_]{0,55}$|^'\\$\\{[a-zA-Z][a-zA-Z0-9_.]*\\}'\\s+(?i)AS\\s+[a-zA-Z][a-zA-Z0-9_]{0,55}$|^^FDN_PARSE\\([a-zA-Z][a-zA-Z0-9_]{0,55}\\.[a-zA-Z][a-zA-Z0-9_]{0,55}\\,\\s?\".+\"\\)\\s+(?i)AS\\s+[a-zA-Z][a-zA-Z0-9_]{0,55}$\" pattern");
            testReporter.publishEntry(name(), content);
        });

        return Stream.concat(
                Stream.of(whenAttributeValuesAreMissing, whenAnyValueIsNull, whenAnyValueDoesNotMatchPattern),
                whenAnyValueIsInvalid
        );
    }
}
