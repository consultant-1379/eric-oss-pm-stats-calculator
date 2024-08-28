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

import kpi.model.api.RequiredAttributeContract;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.TestReporter;

@TestMethodOrder(OrderAnnotation.class)
public interface RequiredDefinitionAttributeContract<T> extends RequiredAttributeContract<T> {
    int REQUIRED_DEFINITION_ATTRIBUTE_CONTRACT_ORDER = REQUIRED_ATTRIBUTE_CONTRACT_ORDER + 1_000;

    @Override
    RequiredDefinitionAttribute<T> createInstance();

    @TestFactory
    @DisplayName("Deserialization")
    @Order(REQUIRED_DEFINITION_ATTRIBUTE_CONTRACT_ORDER + 1)
    default Stream<DynamicNode> shouldDeserialize(final TestReporter testReporter) {
        final DynamicContainer failure = DynamicContainer.dynamicContainer("Failure", Stream.concat(
                deserializationRequiredAttribute(testReporter),
                deserializationCustomFailures(testReporter)
        ));

        final DynamicContainer success = DynamicContainer.dynamicContainer("Success", Stream.concat(
                Stream.of(deserializationSuccessful(testReporter)),
                deserializationCustomSuccesses(testReporter)
        ));

        return Stream.of(failure, success);
    }
}