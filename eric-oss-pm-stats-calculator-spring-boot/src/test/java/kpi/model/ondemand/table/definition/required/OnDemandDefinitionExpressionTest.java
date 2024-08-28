/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.ondemand.table.definition.required;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.stream.Stream;

import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import kpi.model._helper.Serialization;
import kpi.model.api.table.definition.RequiredDefinitionAttribute;
import kpi.model.api.table.definition.RequiredDefinitionAttributeContract;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestReporter;

class OnDemandDefinitionExpressionTest implements RequiredDefinitionAttributeContract<String> {
    @Override
    public RequiredDefinitionAttribute<String> createInstance() {
        return OnDemandDefinitionExpression.of("SELECT * FROM <table>");
    }

    @Override
    public String name() {
        return "expression";
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public String representation() {
        return "expression = SELECT * FROM <table>";
    }

    @Override
    public Stream<DynamicNode> deserializationCustomFailures(final TestReporter testReporter) {
        final Stream<DynamicNode> whenAttributeValueIsBlank = Stream.of("\"\"", "\"  \"")
                .map(value -> dynamicTest(String.format("When attribute value is '%s'", value), () -> {
                    final String content = "{ \"" + name() + "\": " + value + " }";
                    Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()))
                            .isInstanceOf(ValueInstantiationException.class)
                            .hasRootCauseInstanceOf(IllegalArgumentException.class)
                            .getRootCause()
                            .hasMessage("'%s' value '%s' is blank, but this attribute is \"required\", must not be empty", name(), value.replaceAll("\"", ""));
                    testReporter.publishEntry(name(), content);
                }));

        final DynamicNode whenAttributeValueContainsMoreThanOneFrom = dynamicTest("When attribute value contains more than one 'FROM'", () -> {
            final String value = "SELECT * FROM <table> FROM <table>";
            final String content = "{ \"" + name() + "\": \"" + value + "\" }";
            Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()))
                    .isInstanceOf(ValueInstantiationException.class)
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .getRootCause()
                    .hasMessage("'%s' value '%s' must contain exactly one 'FROM'", name(), value);
            testReporter.publishEntry(name(), content);
        });

        final DynamicNode whenAttributeValueContainsNoFrom = dynamicTest("When attribute value contains no 'FROM'", () -> {
            final String value = "SELECT * <table>";
            final String content = "{ \"" + name() + "\": \"" + value + "\" }";
            Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()))
                    .isInstanceOf(ValueInstantiationException.class)
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .getRootCause()
                    .hasMessage("'%s' value '%s' must contain exactly one 'FROM'", name(), value);
            testReporter.publishEntry(name(), content);
        });

        final DynamicNode whenAttributeValueContainsOneFromExactly = dynamicTest("When attribute value contains one 'FROM' exactly", () -> {
            final String value = "SELECT * FROM <table>";
            final String content = "{ \"" + name() + "\": \"" + value + "\" }";
            Assertions.assertThatNoException().isThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()));
        });

        final DynamicNode whenAttributeValueContainsFromWithLowercaseLetters = dynamicTest("When 'FROM' contains lowercase letters", () -> {
            final String value = "SELECT * from <table>";
            final String content = "{ \"" + name() + "\": \"" + value + "\" }";
            Assertions.assertThatNoException().isThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()));
        });

        final Stream<DynamicNode> whenAttributeValueContainsPostAggDatasourceAndContainsAnOther = Stream.of("kpi_db://", "kpi_inmemory://", "tabular_parameters://")
                .map(value -> dynamicTest(String.format("When attribute value contains 'kpi_post_agg://' cannot contain %s", value), () -> {
                    final String newValue = "SELECT * FROM kpi_post_agg:// " + value;
                    final String content = "{ \"" + name() + "\": \"" + newValue + "\" }";
                    Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()))
                            .isInstanceOf(ValueInstantiationException.class)
                            .hasRootCauseInstanceOf(IllegalArgumentException.class)
                            .getRootCause()
                            .hasMessage("'%s' value '%s' contains kpi_post_agg:// datasource cannot contain %s datasource", name(), newValue, value);
                    testReporter.publishEntry(name(), content);
                }));
        final Stream<DynamicNode> whenAttributeValueContainsInMemoryDatasourceAndContainsAnOther = Stream.of("kpi_db://", "tabular_parameters://")
                .map(value -> dynamicTest(String.format("When attribute value contains 'in_memory://' cannot contain %s", value), () -> {
                    final String newValue = "SELECT * FROM kpi_inmemory:// " + value;
                    final String content = "{ \"" + name() + "\": \"" + newValue + "\" }";
                    Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()))
                            .isInstanceOf(ValueInstantiationException.class)
                            .hasRootCauseInstanceOf(IllegalArgumentException.class)
                            .getRootCause()
                            .hasMessage("'%s' value '%s' contains kpi_inmemory:// datasource cannot contain %s datasource", name(), newValue, value);
                    testReporter.publishEntry(name(), content);
                }));


        return Stream.of(
                        whenAttributeValueIsBlank,
                        whenAttributeValueContainsPostAggDatasourceAndContainsAnOther,
                        whenAttributeValueContainsInMemoryDatasourceAndContainsAnOther,
                        Stream.of(whenAttributeValueContainsMoreThanOneFrom,
                                whenAttributeValueContainsNoFrom,
                                whenAttributeValueContainsFromWithLowercaseLetters,
                                whenAttributeValueContainsOneFromExactly))
                .flatMap(stream -> stream);
    }
}
