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
import kpi.model.api.table.definition.SimpleKpiDefinitions.SimpleKpiDefinition;
import kpi.model.api.validation.ValidationResult;
import kpi.model.simple.SimpleTable;
import kpi.model.simple.table.definition.optional.SimpleDefinitionAggregationElements;
import kpi.model.simple.table.definition.optional.SimpleDefinitionDataLookBackLimit;
import kpi.model.simple.table.definition.optional.SimpleDefinitionDataReliabilityOffset;
import kpi.model.simple.table.definition.optional.SimpleDefinitionExportable;
import kpi.model.simple.table.definition.optional.SimpleDefinitionFilters;
import kpi.model.simple.table.definition.optional.SimpleDefinitionInpDataIdentifier;
import kpi.model.simple.table.definition.optional.SimpleDefinitionReexportLateData;
import kpi.model.simple.table.definition.required.SimpleDefinitionAggregationType;
import kpi.model.simple.table.definition.required.SimpleDefinitionExpression;
import kpi.model.simple.table.definition.required.SimpleDefinitionName;
import kpi.model.simple.table.definition.required.SimpleDefinitionObjectType;
import kpi.model.util.Attributes;
import kpi.model.util.Strings;
import kpi.model.util.ValidationResults;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

public class SimpleKpiDefinitions extends RequiredTableAttribute<List<SimpleKpiDefinition>> implements KpiDefinitions<SimpleKpiDefinition> {

    private SimpleKpiDefinitions(final List<SimpleKpiDefinition> value) {
        super(value);
    }

    @JsonCreator
    public static SimpleKpiDefinitions of(
            @JsonSetter(contentNulls = Nulls.FAIL)
            @JsonProperty(value = Attributes.ATTRIBUTE_KPI_DEFINITIONS, required = true) final List<SimpleKpiDefinition> value
    ) {
        return new SimpleKpiDefinitions(value);
    }

    public SimpleKpiDefinitions withInheritedValuesFrom(final SimpleTable table) {
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
    protected ValidationResult validateValue(final List<SimpleKpiDefinition> value) {
        return super.validateValue(value)
                .andThen(() -> ValidationResults.validateKpiDefinitionExist(value));
    }

    @Data
    @Builder
    @Jacksonized
    @Accessors(fluent = true)
    @ToString(includeFieldNames = false)
    public static class SimpleKpiDefinition implements KpiDefinition {
        @JsonUnwrapped
        private final SimpleDefinitionName name;
        @JsonUnwrapped
        private final SimpleDefinitionExpression expression;
        @JsonUnwrapped
        private final SimpleDefinitionObjectType objectType;
        @JsonUnwrapped
        private final SimpleDefinitionAggregationType aggregationType;
        @JsonUnwrapped
        private final SimpleDefinitionAggregationElements aggregationElements;
        @JsonUnwrapped
        private final SimpleDefinitionExportable exportable;
        @JsonUnwrapped
        private final SimpleDefinitionFilters filters;
        @JsonUnwrapped
        private final SimpleDefinitionDataReliabilityOffset dataReliabilityOffset;
        @JsonUnwrapped
        private final SimpleDefinitionDataLookBackLimit dataLookBackLimit;
        @JsonUnwrapped
        private final SimpleDefinitionReexportLateData reexportLateData;
        @JsonUnwrapped
        private final SimpleDefinitionInpDataIdentifier inpDataIdentifier;

        private void inheritFrom(@NonNull final SimpleTable table) {
            exportable.overrideBy(table.exportable());
            aggregationElements.overrideBy(table.aggregationElements());
            dataReliabilityOffset.overrideBy(table.dataReliabilityOffset());
            dataLookBackLimit.overrideBy(table.dataLookBackLimit());
            reexportLateData.overrideBy(table.reexportLateData());
            inpDataIdentifier.overrideBy(table.inpDataIdentifier());
        }
    }
}
