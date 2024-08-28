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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.Nulls;
import kpi.model.api.table.RequiredTableAttribute;
import kpi.model.api.table.definition.OnDemandKpiDefinitions.OnDemandKpiDefinition;
import kpi.model.api.validation.ValidationResult;
import kpi.model.ondemand.OnDemandTable;
import kpi.model.ondemand.table.definition.optional.OnDemandDefinitionAggregationElements;
import kpi.model.ondemand.table.definition.optional.OnDemandDefinitionExportable;
import kpi.model.ondemand.table.definition.optional.OnDemandDefinitionFilters;
import kpi.model.ondemand.table.definition.required.OnDemandDefinitionAggregationType;
import kpi.model.ondemand.table.definition.required.OnDemandDefinitionExpression;
import kpi.model.ondemand.table.definition.required.OnDemandDefinitionName;
import kpi.model.ondemand.table.definition.required.OnDemandDefinitionObjectType;
import kpi.model.util.Attributes;
import kpi.model.util.Strings;
import kpi.model.util.ValidationResults;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

public class OnDemandKpiDefinitions extends RequiredTableAttribute<List<OnDemandKpiDefinition>> implements KpiDefinitions<OnDemandKpiDefinition> {

    private OnDemandKpiDefinitions(final List<OnDemandKpiDefinition> value) {
        super(value);
    }

    @JsonCreator
    public static OnDemandKpiDefinitions of(
            @JsonSetter(contentNulls = Nulls.FAIL)
            @JsonProperty(value = Attributes.ATTRIBUTE_KPI_DEFINITIONS, required = true) final List<OnDemandKpiDefinition> value
    ) {
        return new OnDemandKpiDefinitions(value);
    }

    public OnDemandKpiDefinitions withInheritedValuesFrom(final OnDemandTable table) {
        value.forEach(kpiDefinition -> kpiDefinition.inheritFrom(table));
        return this;
    }

    @Override
    public String name() {
        return Attributes.ATTRIBUTE_KPI_DEFINITIONS;
    }

    @Override
    public String toString() {
        return Strings.stringifyIterable(this);
    }

    @Override
    protected ValidationResult validateValue(final List<OnDemandKpiDefinition> value) {
        return super.validateValue(value)
                .andThen(() -> ValidationResults.validateKpiDefinitionExist(value))
                .andThen(() -> ValidationResults.validateFiltersAreEmptyWhenExpressionContainsPostAggregationOrInMemoryDatasource(value));
    }

    @Data
    @Builder
    @Jacksonized
    @Accessors(fluent = true)
    @ToString(includeFieldNames = false)
    public static class OnDemandKpiDefinition implements KpiDefinition {
        @JsonUnwrapped
        private final OnDemandDefinitionName name;
        @JsonUnwrapped
        private final OnDemandDefinitionExpression expression;
        @JsonUnwrapped
        private final OnDemandDefinitionObjectType objectType;
        @JsonUnwrapped
        private final OnDemandDefinitionAggregationType aggregationType;
        @JsonUnwrapped
        private final OnDemandDefinitionAggregationElements aggregationElements;
        @JsonUnwrapped
        private final OnDemandDefinitionExportable exportable;
        @JsonUnwrapped
        private final OnDemandDefinitionFilters filters;

        private void inheritFrom(@NonNull final OnDemandTable table) {
            exportable.overrideBy(table.exportable());
            aggregationElements.overrideBy(table.aggregationElements());
        }
    }
}
