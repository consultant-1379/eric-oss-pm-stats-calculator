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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.common.model.attribute.Attribute;

import kpi.model._helper.Serialization;
import kpi.model.api._helper.Testable;
import kpi.model.api.element.ElementBase;
import kpi.model.api.enumeration.AggregationType;
import kpi.model.api.enumeration.ObjectType;
import lombok.NonNull;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.TestReporter;

@TestMethodOrder(OrderAnnotation.class)
public interface AttributeContract<T> extends Testable<Attribute<T>> {
    int ATTRIBUTE_CONTRACT_ORDER = Integer.MIN_VALUE;

    String name();

    boolean isRequired();

    String representation();

    @Test
    @Order(ATTRIBUTE_CONTRACT_ORDER + 1)
    @DisplayName("Validate 'name' attribute")
    default void shouldValidateAttributeName() {
        final Attribute<T> attribute = createInstance();
        Assertions.assertThat(attribute.name()).isEqualTo(name());
    }

    @Test
    @Order(ATTRIBUTE_CONTRACT_ORDER + 2)
    @DisplayName("Validate 'isRequired' attribute")
    default void shouldValidateIsRequired() {
        final Attribute<T> attribute = createInstance();
        Assertions.assertThat(attribute.isRequired()).as("check 'isRequired' attribute").isEqualTo(isRequired());
    }

    @Test
    @Order(ATTRIBUTE_CONTRACT_ORDER + 3)
    @DisplayName("Validate 'isOptional' attribute")
    default void shouldValidateIsOptional() {
        final Attribute<T> attribute = createInstance();
        Assertions.assertThat(attribute.isOptional()).as("check 'isOptional' attribute").isEqualTo(!isRequired());
    }

    @Test
    @Order(ATTRIBUTE_CONTRACT_ORDER + 4)
    @DisplayName("Validate 'toString()' representation")
    default void shouldValidateRepresentation() {
        final Attribute<T> attribute = createInstance();
        Assertions.assertThat(attribute).hasToString(representation());
    }

    default Stream<DynamicNode> deserializationCustomFailures(final TestReporter testReporter) {
        return Stream.of();
    }

    default Stream<DynamicNode> deserializationCustomSuccesses(final TestReporter testReporter) {
        return Stream.of();
    }

    default DynamicNode deserializationSuccessful(final TestReporter testReporter) {
        final T value = createInstance().value();

        return dynamicTest(String.format("When attribute value is '%s'", value), () -> {
            final String content = "{ \"" + name() + "\": " + toJsonValue(value) + " }";

            final Attribute<T> deserializedAttribute = Serialization.deserialize(content, deduceSelfClass());
            Assertions.assertThat(deserializedAttribute.value()).isEqualTo(value);

            testReporter.publishEntry(name(), content);
        });
    }

    default String toJsonValue(@NonNull final T value) {
        final Class<?> valueClass = value.getClass();
        if (Objects.equals(valueClass, Integer.class) || Objects.equals(valueClass, Boolean.class)) {
            return String.valueOf(value);
        }

        if (Objects.equals(valueClass, String.class)) {
            return String.format("\"%s\"", value);
        }

        if (Objects.equals(valueClass, ArrayList.class)) {
            @SuppressWarnings("unchecked") final List<ElementBase<String>> values = (List<ElementBase<String>>) value;
            @SuppressWarnings("unchecked") final Function<ElementBase<String>, String> toJsonValue = v -> toJsonValue((T) v.value());
            return values.stream().map(toJsonValue).collect(Collectors.joining(", ", "[", "]"));
        }

        if (Objects.equals(valueClass, AggregationType.class)) {
            @SuppressWarnings("unchecked") final T name = (T) AggregationType.SUM.name();
            return toJsonValue(name);
        }

        if (Objects.equals(valueClass, ObjectType.class)) {
            @SuppressWarnings("unchecked") final T name = (T) ObjectType.POSTGRES_INTEGER.name();
            return toJsonValue(name);
        }

        throw new IllegalStateException(String.format("Could not convert value type '%s' to JSON", valueClass.getSimpleName()));
    }

    @SuppressWarnings("unchecked")
    default Class<Attribute<T>> deduceSelfClass() {
        return (Class<Attribute<T>>) createInstance().getClass();
    }
}
