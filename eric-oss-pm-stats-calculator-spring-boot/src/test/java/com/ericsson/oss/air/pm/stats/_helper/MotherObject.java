/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats._helper;

import static com.ericsson.oss.air.pm.stats._helper.Mapper.toAggregationElements;
import static com.ericsson.oss.air.pm.stats._helper.Mapper.toComplexAggregationElements;
import static com.ericsson.oss.air.pm.stats._helper.Mapper.toOnDemandAggregationElements;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import kpi.model.api.enumeration.AggregationType;
import kpi.model.api.table.definition.ComplexKpiDefinitions;
import kpi.model.api.table.definition.ComplexKpiDefinitions.ComplexKpiDefinition;
import kpi.model.api.table.definition.OnDemandKpiDefinitions;
import kpi.model.api.table.definition.OnDemandKpiDefinitions.OnDemandKpiDefinition;
import kpi.model.api.table.definition.SimpleKpiDefinitions;
import kpi.model.api.table.definition.SimpleKpiDefinitions.SimpleKpiDefinition;
import kpi.model.complex.ComplexTable;
import kpi.model.complex.element.ComplexFilterElement;
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
import kpi.model.complex.table.optional.ComplexTableAggregationPeriod;
import kpi.model.complex.table.optional.ComplexTableDataLookBackLimit;
import kpi.model.complex.table.optional.ComplexTableDataReliabilityOffset;
import kpi.model.complex.table.optional.ComplexTableExportable;
import kpi.model.complex.table.optional.ComplexTableReexportLateData;
import kpi.model.complex.table.required.ComplexTableAggregationElements;
import kpi.model.complex.table.required.ComplexTableAlias;
import kpi.model.ondemand.OnDemandTable;
import kpi.model.ondemand.element.OnDemandFilterElement;
import kpi.model.ondemand.table.definition.optional.OnDemandDefinitionAggregationElements;
import kpi.model.ondemand.table.definition.optional.OnDemandDefinitionExportable;
import kpi.model.ondemand.table.definition.optional.OnDemandDefinitionFilters;
import kpi.model.ondemand.table.definition.required.OnDemandDefinitionAggregationType;
import kpi.model.ondemand.table.definition.required.OnDemandDefinitionExpression;
import kpi.model.ondemand.table.definition.required.OnDemandDefinitionName;
import kpi.model.ondemand.table.definition.required.OnDemandDefinitionObjectType;
import kpi.model.ondemand.table.optional.OnDemandTableExportable;
import kpi.model.ondemand.table.required.OnDemandTableAggregationElements;
import kpi.model.ondemand.table.required.OnDemandTableAggregationPeriod;
import kpi.model.ondemand.table.required.OnDemandTableAlias;
import kpi.model.simple.SimpleTable;
import kpi.model.simple.element.SimpleFilterElement;
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
import kpi.model.simple.table.optional.SimpleTableAggregationPeriod;
import kpi.model.simple.table.optional.SimpleTableDataLookBackLimit;
import kpi.model.simple.table.optional.SimpleTableDataReliabilityOffset;
import kpi.model.simple.table.optional.SimpleTableExportable;
import kpi.model.simple.table.optional.SimpleTableReexportLateData;
import kpi.model.simple.table.required.SimpleTableAggregationElements;
import kpi.model.simple.table.required.SimpleTableAlias;
import kpi.model.simple.table.required.SimpleTableInpDataIdentifier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MotherObject {

    public static SimpleTable table(
            final Integer aggregationPeriod, final String alias,
            final List<String> aggregationElements, final Boolean exportable,
            final String inpDataIdentifier, final Integer dataReliabilityOffset,
            final Integer dataLookBackLimit, final Boolean reexportLateData,
            final List<SimpleKpiDefinition> kpiDefinitions
    ) {
        return SimpleTable.builder()
                .aggregationPeriod(SimpleTableAggregationPeriod.of(aggregationPeriod))
                .alias(SimpleTableAlias.of(alias))
                .aggregationElements(SimpleTableAggregationElements.of(toAggregationElements(aggregationElements)))
                .exportable(SimpleTableExportable.of(exportable))
                .dataReliabilityOffset(SimpleTableDataReliabilityOffset.of(dataReliabilityOffset))
                .dataLookBackLimit(SimpleTableDataLookBackLimit.of(dataLookBackLimit))
                .reexportLateData(SimpleTableReexportLateData.of(reexportLateData))
                .inpDataIdentifier(SimpleTableInpDataIdentifier.of(inpDataIdentifier))
                .kpiDefinitions(SimpleKpiDefinitions.of(kpiDefinitions))
                .build();
    }

    public static ComplexTable table(
            final Integer aggregationPeriod, final String alias,
            final List<String> aggregationElements, final Boolean exportable,
            final Integer dataReliabilityOffset, final Integer dataLookBackLimit,
            final Boolean reexportLateData, final List<ComplexKpiDefinition> kpiDefinitions
    ) {
        return ComplexTable.builder()
                .aggregationPeriod(ComplexTableAggregationPeriod.of(aggregationPeriod))
                .alias(ComplexTableAlias.of(alias))
                .aggregationElements(ComplexTableAggregationElements.of(toComplexAggregationElements(aggregationElements)))
                .exportable(ComplexTableExportable.of(exportable))
                .dataReliabilityOffset(ComplexTableDataReliabilityOffset.of(dataReliabilityOffset))
                .dataLookBackLimit(ComplexTableDataLookBackLimit.of(dataLookBackLimit))
                .reexportLateData(ComplexTableReexportLateData.of(reexportLateData))
                .kpiDefinitions(ComplexKpiDefinitions.of(kpiDefinitions))
                .build();
    }

    public static OnDemandTable table(
            final Integer aggregationPeriod, final String alias,
            final List<String> aggregationElements, final Boolean exportable,
            final List<OnDemandKpiDefinition> kpiDefinitions
    ) {
        return OnDemandTable.builder()
                .aggregationPeriod(OnDemandTableAggregationPeriod.of(aggregationPeriod))
                .alias(OnDemandTableAlias.of(alias))
                .aggregationElements(OnDemandTableAggregationElements.of(toOnDemandAggregationElements(aggregationElements)))
                .exportable(OnDemandTableExportable.of(exportable))
                .kpiDefinitions(OnDemandKpiDefinitions.of(kpiDefinitions))
                .build();
    }

    public static SimpleKpiDefinition kpiDefinition(
            final String name, final String expression, final String objectType, final AggregationType aggregationType,
            final List<String> aggregationElements, final Boolean exportable, final List<String> filters, final Integer dataReliabilityOffset,
            final Integer dataLookBackLimit, final Boolean reexportLateData, final String identifier
    ) {
        return SimpleKpiDefinition.builder()
                .name(SimpleDefinitionName.of(name))
                .expression(SimpleDefinitionExpression.of(expression))
                .objectType(SimpleDefinitionObjectType.of(objectType))
                .aggregationType(SimpleDefinitionAggregationType.of(aggregationType))
                .aggregationElements(SimpleDefinitionAggregationElements.of(toAggregationElements(aggregationElements)))
                .exportable(SimpleDefinitionExportable.of(exportable))
                .filters(SimpleDefinitionFilters.of(mapTo(filters, SimpleFilterElement::of)))
                .dataReliabilityOffset(SimpleDefinitionDataReliabilityOffset.of(dataReliabilityOffset))
                .dataLookBackLimit(SimpleDefinitionDataLookBackLimit.of(dataLookBackLimit))
                .reexportLateData(SimpleDefinitionReexportLateData.of(reexportLateData))
                .inpDataIdentifier(SimpleDefinitionInpDataIdentifier.of(identifier))
                .build();
    }

    public static ComplexKpiDefinition kpiDefinition(
            final String name, final String expression, final String objectType, final AggregationType aggregationType,
            final String executionGroup, final List<String> aggregationElements, final Boolean exportable, final List<String> filters,
            final Integer dataReliabilityOffset, final Integer dataLookBackLimit, final Boolean reexportLateData
    ) {
        return ComplexKpiDefinition.builder()
                .name(ComplexDefinitionName.of(name))
                .expression(ComplexDefinitionExpression.of(expression))
                .objectType(ComplexDefinitionObjectType.of(objectType))
                .aggregationType(ComplexDefinitionAggregationType.of(aggregationType))
                .executionGroup(ComplexDefinitionExecutionGroup.of(executionGroup))
                .aggregationElements(ComplexDefinitionAggregationElements.of(toComplexAggregationElements(aggregationElements)))
                .exportable(ComplexDefinitionExportable.of(exportable))
                .filters(ComplexDefinitionFilters.of(mapTo(filters, ComplexFilterElement::of)))
                .dataReliabilityOffset(ComplexDefinitionDataReliabilityOffset.of(dataReliabilityOffset))
                .dataLookBackLimit(ComplexDefinitionDataLookBackLimit.of(dataLookBackLimit))
                .reexportLateData(ComplexDefinitionReexportLateData.of(reexportLateData))
                .build();
    }

    public static OnDemandKpiDefinition kpiDefinition(
            final String name, final String expression, final String objectType, final AggregationType aggregationType,
            final List<String> aggregationElements, final Boolean exportable, final List<String> filters
    ) {
        return OnDemandKpiDefinition.builder()
                .name(OnDemandDefinitionName.of(name))
                .expression(OnDemandDefinitionExpression.of(expression))
                .objectType(OnDemandDefinitionObjectType.of(objectType))
                .aggregationType(OnDemandDefinitionAggregationType.of(aggregationType))
                .aggregationElements(OnDemandDefinitionAggregationElements.of(toOnDemandAggregationElements(aggregationElements)))
                .exportable(OnDemandDefinitionExportable.of(exportable))
                .filters(OnDemandDefinitionFilters.of(mapTo(filters, OnDemandFilterElement::of)))
                .build();
    }

    public static <T> List<T> mapTo(final List<String> values, final Function<? super String, ? extends T> mapper) {
        return values.stream().map(mapper).collect(Collectors.toList());
    }
}
