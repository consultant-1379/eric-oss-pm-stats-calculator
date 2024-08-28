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
import kpi.model.api.table.definition.ComplexKpiDefinitions.ComplexKpiDefinition;
import kpi.model.api.validation.ValidationResult;
import kpi.model.complex.ComplexTable;
import kpi.model.complex.table.definition.optional.ComplexDefinitionAggregationElements;
import kpi.model.complex.table.definition.optional.ComplexDefinitionDataLookBackLimit;
import kpi.model.complex.table.definition.optional.ComplexDefinitionDataReliabilityOffset;
import kpi.model.complex.table.definition.optional.ComplexDefinitionExportable;
import kpi.model.complex.table.definition.optional.ComplexDefinitionFilters;
import kpi.model.complex.table.definition.optional.ComplexDefinitionReexportLateData;
import kpi.model.complex.table.definition.required.ComplexDefinitionAggregationType;
import kpi.model.complex.table.definition.required.ComplexDefinitionExecutionGroup;
import kpi.model.complex.table.definition.required.ComplexDefinitionExpression;
import kpi.model.complex.table.definition.required.ComplexDefinitionName;
import kpi.model.complex.table.definition.required.ComplexDefinitionObjectType;
import kpi.model.util.Attributes;
import kpi.model.util.Strings;
import kpi.model.util.ValidationResults;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;


public class ComplexKpiDefinitions extends RequiredTableAttribute<List<ComplexKpiDefinition>> implements KpiDefinitions<ComplexKpiDefinition> {

    private ComplexKpiDefinitions(final List<ComplexKpiDefinition> value) {
        super(value);
    }

    @JsonCreator
    public static ComplexKpiDefinitions of(
            @JsonSetter(contentNulls = Nulls.FAIL)
            @JsonProperty(value = Attributes.ATTRIBUTE_KPI_DEFINITIONS, required = true) final List<ComplexKpiDefinition> value
    ) {
        return new ComplexKpiDefinitions(value);
    }

    public ComplexKpiDefinitions withInheritedValuesFrom(final ComplexTable table) {
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
    protected ValidationResult validateValue(final List<ComplexKpiDefinition> value) {
        return super.validateValue(value)
                .andThen(() -> ValidationResults.validateKpiDefinitionExist(value))
                .andThen(() -> ValidationResults.validateFiltersAreEmptyWhenExpressionContainsPostAggregationOrInMemoryDatasource(value));
    }

    @Data
    @Builder
    @Jacksonized
    @Accessors(fluent = true)
    @ToString(includeFieldNames = false)
    public static class ComplexKpiDefinition implements KpiDefinition {
        @JsonUnwrapped
        private final ComplexDefinitionName name;
        @JsonUnwrapped
        private final ComplexDefinitionExpression expression;
        @JsonUnwrapped
        private final ComplexDefinitionObjectType objectType;
        @JsonUnwrapped
        private final ComplexDefinitionAggregationType aggregationType;
        @JsonUnwrapped
        private final ComplexDefinitionExecutionGroup executionGroup;
        @JsonUnwrapped
        private final ComplexDefinitionAggregationElements aggregationElements;
        @JsonUnwrapped
        private final ComplexDefinitionExportable exportable;
        @JsonUnwrapped
        private final ComplexDefinitionFilters filters;
        @JsonUnwrapped
        private final ComplexDefinitionDataReliabilityOffset dataReliabilityOffset;
        @JsonUnwrapped
        private final ComplexDefinitionDataLookBackLimit dataLookBackLimit;
        @JsonUnwrapped
        private final ComplexDefinitionReexportLateData reexportLateData;

        public void inheritFrom(@NonNull final ComplexTable table) {
            aggregationElements.overrideBy(table.aggregationElements());
            exportable.overrideBy(table.exportable());
            dataReliabilityOffset.overrideBy(table.dataReliabilityOffset());
            dataLookBackLimit.overrideBy(table.dataLookBackLimit());
            reexportLateData.overrideBy(table.reexportLateData());
        }
    }
}
