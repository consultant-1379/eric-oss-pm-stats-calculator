/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.api.table;

import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.common.model.attribute.Attribute;

import kpi.model._helper.Serialization;
import kpi.model.api.OptionalAttributeContract;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.TestReporter;

@TestMethodOrder(OrderAnnotation.class)
public interface OptionalTableAttributeContract<T> extends OptionalAttributeContract<T> {
    int OPTIONAL_TABLE_ATTRIBUTE_CONTRACT_ORDER = OPTIONAL_ATTRIBUTE_CONTRACT_ORDER + 1_000;

    @Override
    OptionalTableAttribute<T> createInstance();

    T defaultValue();

    @Test
    @DisplayName("Validate default value")
    @Order(OPTIONAL_TABLE_ATTRIBUTE_CONTRACT_ORDER + 1)
    default void shouldVerifyDefaultValue() {
        final OptionalTableAttribute<T> attribute = createInstance();
        Assertions.assertThat(attribute.defaultValue()).isEqualTo(defaultValue());
    }

    @Test
    @DisplayName("Validate default value is applied")
    @Order(OPTIONAL_TABLE_ATTRIBUTE_CONTRACT_ORDER + 3)
    default void shouldApplyDefaultValue() {
        final OptionalTableAttribute<T> attribute = new OptionalTableAttribute<>(null) {
            @Override
            public String name() {
                return "optionalAttribute";
            }

            @Override
            protected T defaultValue() {
                return OptionalTableAttributeContract.this.defaultValue();
            }
        };

        Assertions.assertThat(attribute.value()).isEqualTo(defaultValue());
    }

    @TestFactory
    @DisplayName("Deserialization")
    @Order(OPTIONAL_TABLE_ATTRIBUTE_CONTRACT_ORDER + 4)
    default Stream<DynamicNode> shouldDeserialize(final TestReporter testReporter) {
        final DynamicContainer failure = DynamicContainer.dynamicContainer("Failure", deserializationCustomFailures(testReporter));

        final DynamicTest whenValueIsNull = DynamicTest.dynamicTest("When attribute value is 'null'", () -> {
            final String content = "{ \"" + name() + "\": null }";
            final Attribute<T> attribute = Serialization.deserialize(content, deduceSelfClass());

            Assertions.assertThat(attribute.value()).isEqualTo(defaultValue());
            testReporter.publishEntry(name(), content);
        });

        final DynamicContainer success = DynamicContainer.dynamicContainer("Success", Stream.concat(
                Stream.of(deserializationSuccessful(testReporter), whenValueIsNull),
                deserializationCustomSuccesses(testReporter)
        ));

        return Stream.of(failure, success);
    }

}