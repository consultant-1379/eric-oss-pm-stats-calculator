/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.ondemand.table.definition.optional;

import static kpi.model._helper.Mapper.toOnDemandFilterElements;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.List;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.common.model.attribute.Attribute;
import com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants;

import com.fasterxml.jackson.databind.exc.InvalidNullException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import kpi.model._helper.Serialization;
import kpi.model.api._helper.annotation.DisableForNoParent;
import kpi.model.api.table.api.TableAttribute;
import kpi.model.api.table.definition.OptionalDefinitionAttribute;
import kpi.model.api.table.definition.OptionalDefinitionAttributeContract;
import kpi.model.ondemand.element.OnDemandFilterElement;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;

class OnDemandDefinitionFiltersTest implements OptionalDefinitionAttributeContract<List<OnDemandFilterElement>> {
    @Override
    public OptionalDefinitionAttribute<List<OnDemandFilterElement>> createInstance() {
        return OnDemandDefinitionFilters.of(toOnDemandFilterElements("filter_1", "filter_2://source"));
    }

    @Override
    public TableAttribute<List<OnDemandFilterElement>> createParentInstance() {
        throw new UnsupportedOperationException(String.format("'%s' has no parent value", name()));
    }

    @Override
    public String name() {
        return "filters";
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public String representation() {
        return "filters = [filter_1, filter_2://source]";
    }

    @Override
    public Class<? extends TableAttribute<List<OnDemandFilterElement>>> parentClass() {
        throw new UnsupportedOperationException(String.format("'%s' has no parent value", name()));
    }

    @Test
    @Override
    @DisplayName("Validate attribute has no parent")
    @Order(OPTIONAL_DEFINITION_ATTRIBUTE_CONTRACT_ORDER + 1)
    public void shouldVerifyParentClass() {
        final OptionalDefinitionAttribute<List<OnDemandFilterElement>> attribute = createInstance();
        Assertions.assertThatThrownBy(attribute::parentClass)
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("Attribute '%s' has no parent", name());
    }

    @Override
    @DisableForNoParent
    public void shouldRaiseException_whenDifferentParentClass() {
    }

    @Override
    @DisableForNoParent
    public void shouldOverrideBy() {
    }

    @Override
    @DisableForNoParent
    public void shouldNotOverrideBy() {
    }

    @Override
    public Stream<DynamicNode> deserializationCustomSuccesses(final TestReporter testReporter) {
        return Stream.of(dynamicTest("When attribute values are missing", () -> {
            final String content = "{ \"" + name() + "\": [] }";
            final Attribute<List<OnDemandFilterElement>> attribute = Serialization.deserialize(content, deduceSelfClass());
            Assertions.assertThat(attribute.value()).isEmpty();
            testReporter.publishEntry(name(), content);
        }));
    }

    @Override
    public Stream<DynamicNode> deserializationCustomFailures(final TestReporter testReporter) {
        final DynamicNode whenAnyValueIsNull = dynamicTest("When any attribute value is null", () -> {
            final String content = "{ \"" + name() + "\": [null] }";
            Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()))
                    .isInstanceOf(InvalidNullException.class)
                    .hasMessageContaining("Invalid `null` value encountered for property \"%s\"", name());
            testReporter.publishEntry(name(), content);
        });

        final Stream<DynamicNode> whenAnyValueIsInvalid = Stream.of("\"\"", "\"  \"").map(value -> {
            return dynamicTest(String.format("When any attribute value is '%s'", value), () -> {
                final String content = "{ \"" + name() + "\": [" + value + "] }";
                Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()))
                        .isInstanceOf(ValueInstantiationException.class)
                        .hasRootCauseInstanceOf(IllegalArgumentException.class)
                        .getRootCause()
                        .hasMessage("'filter' value '%s' is blank, but this attribute is \"required\", must not be empty", value.replaceAll("\"", ""));
                testReporter.publishEntry(name(), content);
            });
        });
/*,
        final DynamicNode whenAnyValueIsParameterized = dynamicTest("When any attribute value contains parameter token", () -> {
            final String content = "{ \"" + name() + "\": [\"${<parameter>}\"] }";
            Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()))
                    .isInstanceOf(ValueInstantiationException.class)
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .getRootCause()
                    .hasMessageContaining("The attribute 'filter' does not support parameters");
            testReporter.publishEntry(name(), content);
        });
*/
        final DynamicNode whenAnyValueDoesNotMatchPattern =
                dynamicTest("When any attribute value is [\"datasource_1://table.value = datasource_2://table.value\"]", () -> {
                    final String content = "{ \"" + name() + "\": [\"datasource_1://table.value = datasource_2://table.value\"] }";
                    Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()))
                            .isInstanceOf(ValueInstantiationException.class)
                            .hasRootCauseInstanceOf(IllegalArgumentException.class)
                            .getRootCause()
                            .hasMessage("'filter' value 'datasource_1://table.value = datasource_2://table.value' has invalid format. Format must follow the \"^(?!.*://.*://).*$\" pattern");
                    testReporter.publishEntry(name(), content);
                });

        final DynamicNode whenAttributeValueContainsFrom =
                dynamicTest("When attribute value contains 'FROM'", () -> {
                    final String value = "kpi_db://kpi_sector_60.TO_DATE(aggregation_begin_time) = '${param.date_for_filter}' "  + KpiCalculatorConstants.SQL_FROM;
                    final String content = "{ \"" + name() + "\": [\"" + value + "\"] }";
                    Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()))
                            .isInstanceOf(ValueInstantiationException.class)
                            .hasRootCauseInstanceOf(IllegalArgumentException.class)
                            .getRootCause()
                            .hasMessage("'filter' value '%s' cannot contain '" + KpiCalculatorConstants.SQL_FROM + "'", value);
                    testReporter.publishEntry(name(), content);
                });

        final DynamicNode whenAttributeValueContainsWhere =
                dynamicTest("When attribute value contains 'WHERE'", () -> {
                    final String value = "kpi_db://kpi_sector_60.TO_DATE(aggregation_begin_time) = '${param.date_for_filter}' "  + KpiCalculatorConstants.SQL_WHERE;
                    final String content = "{ \"" + name() + "\": [\"" + value + "\"] }";
                    Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, deduceSelfClass()))
                            .isInstanceOf(ValueInstantiationException.class)
                            .hasRootCauseInstanceOf(IllegalArgumentException.class)
                            .getRootCause()
                            .hasMessage("'filter' value '%s' cannot contain '" + KpiCalculatorConstants.SQL_WHERE + "'", value);
                    testReporter.publishEntry(name(), content);
                });

        return Stream.concat(
                Stream.of(whenAnyValueIsNull, whenAnyValueDoesNotMatchPattern, whenAttributeValueContainsFrom, whenAttributeValueContainsWhere),
                whenAnyValueIsInvalid
        );
    }
}
