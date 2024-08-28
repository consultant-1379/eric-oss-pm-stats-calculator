/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.api;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.stream.Stream;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import kpi.model._helper.Serialization;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.TestReporter;

@TestMethodOrder(OrderAnnotation.class)
public interface RequiredAttributeContract<T> extends AttributeBaseContract<T> {
    int REQUIRED_ATTRIBUTE_CONTRACT_ORDER = ATTRIBUTE_BASE_CONTRACT_ORDER + 1_000;

    @Override
    RequiredAttribute<T> createInstance();

    @Test
    @Override
    @Order(REQUIRED_ATTRIBUTE_CONTRACT_ORDER + 1)
    @DisplayName("Validate contained value is not overridable")
    default void shouldOverrideValue() {
        final RequiredAttribute<T> attributeOriginal = createInstance();
        final RequiredAttribute<T> attributeNew = createInstance();

        Assertions.assertThatThrownBy(() -> attributeOriginal.overrideValue(attributeNew.value()))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("Required attribute is not overridable");
    }

    @TestFactory
    @DisplayName("Validate value")
    @Order(REQUIRED_ATTRIBUTE_CONTRACT_ORDER + 2)
    default Stream<DynamicContainer> validateValue() {
        return Stream.of(
                DynamicContainer.dynamicContainer("Default validation", Stream.of(
                        dynamicTest("Value is required", () -> {
                            final RequiredAttribute<T> attribute = createInstance();
                            Assertions.assertThatThrownBy(() -> attribute.validateValue(null))
                                    .isInstanceOf(IllegalArgumentException.class)
                                    .hasMessage("'%s' has null value, but this attribute is \"required\", must not be null", name());
                        }))),
                DynamicContainer.dynamicContainer("Custom validation", provideCustomValueValidations())
        );
    }

    default Stream<DynamicNode> deserializationRequiredAttribute(final TestReporter testReporter) {
        final DynamicNode whenAttributeIsMissing = dynamicTest("When attribute is missing", () -> {
            final String content = "{ }";
            Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()))
                    .isInstanceOf(MismatchedInputException.class)
                    .hasMessageContaining("Missing required creator property '%s'", name());
            testReporter.publishEntry(name(), content);
        });

        final DynamicNode whenAttributeValueIsNull = dynamicTest("When attribute value is null", () -> {
            final String content = "{ \"" + name() + "\": null }";
            Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()))
                    .isInstanceOf(ValueInstantiationException.class)
                    .getRootCause()
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("'%s' has null value, but this attribute is \"required\", must not be null", name());
            testReporter.publishEntry(name(), content);
        });

        return Stream.of(whenAttributeIsMissing, whenAttributeValueIsNull);
    }
}
