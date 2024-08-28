/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model._helper;

import static kpi.model._helper.Mapper.toAggregationElements;
import static kpi.model._helper.Mapper.toComplexAggregationElements;
import static kpi.model._helper.Mapper.toOnDemandAggregationElements;

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
import kpi.model.complex.element.ComplexAggregationElement;
import kpi.model.complex.element.ComplexFilterElement;
import kpi.model.complex.table.definition.optional.ComplexDefinitionAggregationElements;
import kpi.model.complex.table.definition.optional.ComplexDefinitionDataLookBackLimit;
import kpi.model.complex.table.definition.optional.ComplexDefinitionDataReliabilityOffset;
import kpi.model.complex.table.definition.optional.ComplexDefinitionExportable;
import kpi.model.complex.table.definition.optional.ComplexDefinitionFilters;
import kpi.model.complex.table.definition.optional.ComplexDefinitionReexportLateData;
import kpi.model.complex.table.definition.optional.ComplexTableRetentionPeriod;
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
import kpi.model.ondemand.OnDemandParameter;
import kpi.model.ondemand.OnDemandTable;
import kpi.model.ondemand.ParameterType;
import kpi.model.ondemand.element.OnDemandAggregationElement;
import kpi.model.ondemand.element.OnDemandFilterElement;
import kpi.model.ondemand.table.definition.optional.OnDemandDefinitionAggregationElements;
import kpi.model.ondemand.table.definition.optional.OnDemandDefinitionExportable;
import kpi.model.ondemand.table.definition.optional.OnDemandDefinitionFilters;
import kpi.model.ondemand.table.definition.required.OnDemandDefinitionAggregationType;
import kpi.model.ondemand.table.definition.required.OnDemandDefinitionExpression;
import kpi.model.ondemand.table.definition.required.OnDemandDefinitionName;
import kpi.model.ondemand.table.definition.required.OnDemandDefinitionObjectType;
import kpi.model.ondemand.table.optional.OnDemandTableExportable;
import kpi.model.ondemand.table.optional.OnDemandTableRetentionPeriod;
import kpi.model.ondemand.table.required.OnDemandTableAggregationElements;
import kpi.model.ondemand.table.required.OnDemandTableAggregationPeriod;
import kpi.model.ondemand.table.required.OnDemandTableAlias;
import kpi.model.simple.SimpleTable;
import kpi.model.simple.element.SimpleAggregationElement;
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
import kpi.model.simple.table.optional.SimpleTableRetentionPeriod;
import kpi.model.simple.table.required.SimpleTableAggregationElements;
import kpi.model.simple.table.required.SimpleTableAlias;
import kpi.model.simple.table.required.SimpleTableInpDataIdentifier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MotherObject {

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

    public static ComplexTable kpiTable(final ComplexTableAlias alias, final int aggregationPeriod,
                                        final Integer retentionPeriod,
                                        final List<ComplexAggregationElement> aggregationElements,
                                        final List<ComplexKpiDefinition> complexKpiDefinitions) {
        return ComplexTable.builder()
                .alias(alias)
                .aggregationPeriod(ComplexTableAggregationPeriod.of(aggregationPeriod))
                .retentionPeriod(ComplexTableRetentionPeriod.of(retentionPeriod))
                .aggregationElements(ComplexTableAggregationElements.of(aggregationElements))
                .kpiDefinitions(ComplexKpiDefinitions.of(complexKpiDefinitions))
                .exportable(ComplexTableExportable.of(false))
                .dataReliabilityOffset(ComplexTableDataReliabilityOffset.of(15))
                .dataLookBackLimit(ComplexTableDataLookBackLimit.of(7_200))
                .reexportLateData(ComplexTableReexportLateData.of(false))
                .build();
    }

    public static SimpleTable kpiTable(final SimpleTableAlias alias, final int aggregationPeriod,
                                       final Integer retentionPeriod,
                                       final List<SimpleAggregationElement> aggregationElements,
                                       final SimpleTableInpDataIdentifier inpDataIdentifier,
                                       List<SimpleKpiDefinition> simpleKpiDefinitions) {
        return SimpleTable.builder()
                .alias(alias)
                .aggregationPeriod(SimpleTableAggregationPeriod.of(aggregationPeriod))
                .retentionPeriod(SimpleTableRetentionPeriod.of(retentionPeriod))
                .aggregationElements(SimpleTableAggregationElements.of(aggregationElements))
                .inpDataIdentifier(inpDataIdentifier)
                .kpiDefinitions(SimpleKpiDefinitions.of(simpleKpiDefinitions))
                .exportable(SimpleTableExportable.of(false))
                .dataReliabilityOffset(SimpleTableDataReliabilityOffset.of(15))
                .dataLookBackLimit(SimpleTableDataLookBackLimit.of(7_200))
                .reexportLateData(SimpleTableReexportLateData.of(false))
                .inpDataIdentifier(SimpleTableInpDataIdentifier.of("dataSpace|category|parent_schema"))
                .build();
    }

    public static OnDemandTable kpiTable(final OnDemandTableAlias alias, final int aggregationPeriod,
                                         final Integer retentionPeriod,
                                         final List<OnDemandAggregationElement> aggregationElements,
                                         List<OnDemandKpiDefinition> onDemandKpiDefinitions) {
        return OnDemandTable.builder()
                .alias(alias)
                .aggregationPeriod(OnDemandTableAggregationPeriod.of(aggregationPeriod))
                .retentionPeriod(OnDemandTableRetentionPeriod.of(retentionPeriod))
                .aggregationElements(OnDemandTableAggregationElements.of(aggregationElements))
                .kpiDefinitions(OnDemandKpiDefinitions.of(onDemandKpiDefinitions))
                .exportable(OnDemandTableExportable.of(false))
                .build();
    }

    public static <T> List<T> mapTo(final List<String> values, final Function<? super String, ? extends T> mapper) {
        return values.stream().map(mapper).collect(Collectors.toList());
    }

    public static OnDemandParameter parameter(final String name, final ParameterType type) {
        return OnDemandParameter.builder().name(name).type(type).build();
    }
}
