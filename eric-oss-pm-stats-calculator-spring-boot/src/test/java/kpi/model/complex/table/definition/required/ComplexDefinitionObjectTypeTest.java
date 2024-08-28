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
import kpi.model.api.enumeration.ObjectType;
import kpi.model.api.table.definition.RequiredDefinitionAttribute;
import kpi.model.api.table.definition.RequiredDefinitionAttributeContract;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestReporter;

class ComplexDefinitionObjectTypeTest implements RequiredDefinitionAttributeContract<ObjectType> {
    @Override
    public RequiredDefinitionAttribute<ObjectType> createInstance() {
        return ComplexDefinitionObjectType.of("INTEGER");
    }

    @Override
    public String name() {
        return "object_type";
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public String representation() {
        return "object_type = POSTGRES_INTEGER";
    }

    @Override
    public Stream<DynamicNode> deserializationCustomFailures(final TestReporter testReporter) {
        return Stream.of(dynamicTest("When attribute value 'UNKNOWN_ENUM' is not known", () -> {
            final String content = "{ \"" + name() + "\": \"UNKNOWN_ENUM\" }";
            Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()))
                    .isInstanceOf(ValueInstantiationException.class)
                    .hasMessageContaining("'UNKNOWN_ENUM' is not a valid ObjectType");
            testReporter.publishEntry(name(), content);
        }));
    }
}
