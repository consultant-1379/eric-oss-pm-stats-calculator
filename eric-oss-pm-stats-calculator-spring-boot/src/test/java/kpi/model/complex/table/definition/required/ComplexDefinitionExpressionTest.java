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

import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;

import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import kpi.model._helper.Serialization;
import kpi.model.api.table.definition.RequiredDefinitionAttribute;
import kpi.model.api.table.definition.RequiredDefinitionAttributeContract;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestReporter;

class ComplexDefinitionExpressionTest implements RequiredDefinitionAttributeContract<String> {
    @Override
    public RequiredDefinitionAttribute<String> createInstance() {
        return ComplexDefinitionExpression.of("SELECT * FROM <table>");
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

        final DynamicNode whenAttributeValueContainsMisspelledFrom = dynamicTest("When attribute value contains misspelled 'FROM')", () -> {
            final String value = "SELECT * FORM <table>";
            final String content = "{ \"" + name() + "\": \"" + value + "\" }";
            Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()))
                    .isInstanceOf(ValueInstantiationException.class)
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .getRootCause()
                    .hasMessage("'%s' value '%s' must contain exactly one 'FROM'", name(), value);
            testReporter.publishEntry(name(), value);
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

        final DynamicNode whenAttributeValueHasInvalidParameter = dynamicTest("When attribute value contains not supported parameter token", () -> {
            final String value = "SELECT * FROM ${param.invalid_param}";
            final String content = "{ \"" + name() + "\": \"" + value + "\" }";
            Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()))
                    .isInstanceOf(ValueInstantiationException.class)
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .getRootCause()
                    .hasMessageContaining("The attribute 'expression' only support '${param.start_date_time}' or '${param.end_date_time}' as parameter");
            testReporter.publishEntry(name(), content);
        });

        final DynamicNode whenAttributeValueHasTabularParameterDatasource = dynamicTest("When attribute value contains 'tabular_parameter' datasource", () -> {
            final String value = "SELECT * FROM tabular_parameters://cell_configuration_test";
            final String content = "{ \"" + name() + "\": \"" + value + "\" }";
            Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()))
                    .isInstanceOf(ValueInstantiationException.class)
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .getRootCause()
                    .hasMessageContaining("'%s' value '%s' cannot contain '%s'", name(), value, Datasource.TABULAR_PARAMETERS.getName());
            testReporter.publishEntry(name(), content);
        });

        return Stream.of(
                        whenAttributeValueIsBlank,
                        whenAttributeValueContainsPostAggDatasourceAndContainsAnOther,
                        whenAttributeValueContainsInMemoryDatasourceAndContainsAnOther,
                        Stream.of(whenAttributeValueContainsMoreThanOneFrom,
                                whenAttributeValueContainsNoFrom,
                                whenAttributeValueContainsMisspelledFrom,
                                whenAttributeValueContainsFromWithLowercaseLetters,
                                whenAttributeValueHasInvalidParameter,
                                whenAttributeValueContainsOneFromExactly,
                                whenAttributeValueHasTabularParameterDatasource))
                .flatMap(stream -> stream);
    }
}
