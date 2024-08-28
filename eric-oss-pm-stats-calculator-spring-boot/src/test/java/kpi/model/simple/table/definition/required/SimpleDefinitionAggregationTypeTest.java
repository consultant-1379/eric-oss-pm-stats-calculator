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

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import kpi.model._helper.Serialization;
import kpi.model.api.enumeration.AggregationType;
import kpi.model.api.table.definition.RequiredDefinitionAttribute;
import kpi.model.api.table.definition.RequiredDefinitionAttributeContract;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestReporter;

class SimpleDefinitionAggregationTypeTest implements RequiredDefinitionAttributeContract<AggregationType> {
    @Override
    public RequiredDefinitionAttribute<AggregationType> createInstance() {
        return SimpleDefinitionAggregationType.of(AggregationType.SUM);
    }

    @Override
    public String name() {
        return "aggregation_type";
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public String representation() {
        return "aggregation_type = SUM";
    }

    @Override
    public Stream<DynamicNode> deserializationCustomFailures(final TestReporter testReporter) {
        return Stream.of(dynamicTest("When attribute value 'UNKNOWN_ENUM' is not known", () -> {
            final String content = "{ \"" + name() + "\": \"UNKNOWN_ENUM\" }";
            Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()))
                    .isInstanceOf(InvalidFormatException.class)
                    .hasMessageContaining("\"UNKNOWN_ENUM\": not one of the values accepted for Enum class");
            testReporter.publishEntry(name(), content);
        }));
    }
}