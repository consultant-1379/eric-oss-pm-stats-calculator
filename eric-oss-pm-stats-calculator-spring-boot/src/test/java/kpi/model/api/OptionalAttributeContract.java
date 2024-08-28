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

import static kpi.model.api._helper.Any.any;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;

import java.util.stream.Stream;

import kpi.model.api._helper.OptionalAttributeImpl;
import lombok.Data;
import lombok.experimental.Accessors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(OrderAnnotation.class)
public interface OptionalAttributeContract<T> extends AttributeBaseContract<T> {
    int OPTIONAL_ATTRIBUTE_CONTRACT_ORDER = ATTRIBUTE_BASE_CONTRACT_ORDER + 1_000;

    @Override
    OptionalAttribute<T> createInstance();

    @TestFactory
    @Order(OPTIONAL_ATTRIBUTE_CONTRACT_ORDER + 1)
    @DisplayName("Validate value is overridable")
    default Stream<DynamicContainer> shouldVerifyCanOverride() {
        final DynamicContainer overridableCases = dynamicContainer("Overridable test cases:", DynamicTest.stream(
                provideOverridableCases(),
                arguments -> String.format("Initial value '%s' can be overridden with '%s'", arguments.baseObject(), arguments.newValue()),
                arguments -> {
                    final OptionalAttribute<T> attribute = arguments.baseObject();
                    Assertions.assertThat(attribute.canOverride(arguments.newValue())).isTrue();

                    attribute.overrideIfCan(arguments.newValue());
                    Assertions.assertThat(attribute.value()).isEqualTo(arguments.newValue());
                })
        );

        final DynamicContainer nonOverridableCases = dynamicContainer("Non-overridable test cases:", DynamicTest.stream(
                provideNonOverridableCases(),
                arguments -> String.format("Initial value '%s' can not be overridden with '%s'", arguments.baseObject(), arguments.newValue()),
                arguments -> {
                    final OptionalAttribute<T> attribute = arguments.baseObject();
                    Assertions.assertThat(attribute.canOverride(arguments.newValue())).isFalse();

                    final T value = attribute.value();
                    attribute.overrideIfCan(arguments.newValue());
                    Assertions.assertThat(attribute.value()).isEqualTo(value);
                })
        );

        return Stream.of(overridableCases, nonOverridableCases);
    }

    @TestFactory
    @DisplayName("Validate value")
    @Order(OPTIONAL_ATTRIBUTE_CONTRACT_ORDER + 2)
    default Stream<DynamicContainer> validateValue() {
        return Stream.of(dynamicContainer("Custom validation", provideCustomValueValidations()));
    }

    default Stream<ValueArguments<T>> provideOverridableCases() {
        return Stream.of(ValueArguments.of(new OptionalAttributeImpl<>(null), any()));
    }

    default Stream<ValueArguments<T>> provideNonOverridableCases() {
        return Stream.of(
                ValueArguments.of(new OptionalAttributeImpl<>(null), null),
                ValueArguments.of(new OptionalAttributeImpl<>(any()), null),
                ValueArguments.of(new OptionalAttributeImpl<>(any()), any())
        );
    }

    @Accessors(fluent = true)
    @Data(staticConstructor = "of")
    final class ValueArguments<T> {
        private final OptionalAttribute<T> baseObject;
        private final T newValue;
    }

}