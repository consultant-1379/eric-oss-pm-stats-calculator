/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.api.table.definition;

import java.util.stream.Stream;

import kpi.model.api.OptionalAttributeContract;
import kpi.model.api.table.api.TableAttribute;
import kpi.model.api.table.definition._helper.OptionalDefinitionAttributeImpl;
import kpi.model.api.table.definition._helper.UnknownTableAttribute;
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
public interface OptionalDefinitionAttributeContract<T> extends OptionalAttributeContract<T> {
    int OPTIONAL_DEFINITION_ATTRIBUTE_CONTRACT_ORDER = OPTIONAL_ATTRIBUTE_CONTRACT_ORDER + 1_000;

    @Override
    OptionalDefinitionAttribute<T> createInstance();

    TableAttribute<T> createParentInstance();

    Class<? extends TableAttribute<T>> parentClass();

    @Test
    @Order(OPTIONAL_DEFINITION_ATTRIBUTE_CONTRACT_ORDER + 1)
    @DisplayName("Validate parent class is correctly configured")
    default void shouldVerifyParentClass() {
        final OptionalDefinitionAttribute<T> attribute = createInstance();
        Assertions.assertThat(attribute.parentClass()).isEqualTo(parentClass());
    }

    @Test
    @Order(OPTIONAL_DEFINITION_ATTRIBUTE_CONTRACT_ORDER + 2)
    @DisplayName("Validate value cannot be overridden from different parent class")
    default void shouldRaiseException_whenDifferentParentClass() {
        final OptionalDefinitionAttribute<T> attribute = createInstance();
        final UnknownTableAttribute<T> parentAttribute = new UnknownTableAttribute<>();

        Assertions.assertThatThrownBy(() -> attribute.overrideBy(parentAttribute))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Attribute '%s' is not parent of '%s'", parentAttribute.name(), attribute.name());
    }

    @Test
    @Order(OPTIONAL_DEFINITION_ATTRIBUTE_CONTRACT_ORDER + 3)
    @DisplayName("Validate value is inherited from parent class")
    default void shouldOverrideBy() {
        final OptionalDefinitionAttribute<T> attribute = new OptionalDefinitionAttributeImpl<>(null, parentClass());
        final TableAttribute<T> parentAttribute = createParentInstance();
        attribute.overrideBy(parentAttribute);

        Assertions.assertThat(attribute.value()).isEqualTo(parentAttribute.value());
    }

    @Test
    @Order(OPTIONAL_DEFINITION_ATTRIBUTE_CONTRACT_ORDER + 4)
    @DisplayName("Validate value is not inherited from parent class")
    default void shouldNotOverrideBy() {
        final OptionalDefinitionAttribute<T> attribute = createInstance();
        final TableAttribute<T> parentAttribute = createParentInstance();
        attribute.overrideBy(parentAttribute);

        Assertions.assertThat(attribute.value()).isEqualTo(attribute.value());
        Assertions.assertThat(attribute.value()).isNotEqualTo(parentAttribute.value());
    }

    @TestFactory
    @DisplayName("Deserialization")
    @Order(OPTIONAL_DEFINITION_ATTRIBUTE_CONTRACT_ORDER + 5)
    default Stream<DynamicNode> shouldDeserialize(final TestReporter testReporter) {
        final DynamicContainer failure = DynamicContainer.dynamicContainer("Failure", deserializationCustomFailures(testReporter));

        final DynamicContainer success = DynamicContainer.dynamicContainer("Success", Stream.concat(
                Stream.of(deserializationSuccessful(testReporter)),
                deserializationCustomSuccesses(testReporter)
        ));

        return Stream.of(failure, success);
    }

}