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

import java.util.stream.Stream;

import kpi.model.api._helper.Any;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(OrderAnnotation.class)
public interface AttributeBaseContract<T> extends AttributeContract<T> {
    int ATTRIBUTE_BASE_CONTRACT_ORDER = ATTRIBUTE_CONTRACT_ORDER + 1_000;

    @Override
    AttributeBase<T> createInstance();

    @Test
    @Order(ATTRIBUTE_BASE_CONTRACT_ORDER + 1)
    @DisplayName("Validate contained value is overridable")
    default void shouldOverrideValue() {
        final AttributeBase<T> attribute = createInstance();

        final T any = Any.any();
        final T oldValue = attribute.value();

        attribute.overrideValue(any);

        Assertions.assertThat(attribute.value()).isNotSameAs(oldValue).isEqualTo(any);
    }

    default Stream<DynamicNode> provideCustomValueValidations() {
        return Stream.of();
    }

}