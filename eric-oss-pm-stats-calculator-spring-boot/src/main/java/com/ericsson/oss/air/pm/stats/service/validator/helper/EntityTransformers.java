/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.helper;

import static kpi.model.api.table.definition.OnDemandKpiDefinitions.OnDemandKpiDefinition;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;

import kpi.model.api.enumeration.AggregationType;
import kpi.model.api.table.definition.ComplexKpiDefinitions.ComplexKpiDefinition;
import kpi.model.api.table.definition.ComplexKpiDefinitions.ComplexKpiDefinition.ComplexKpiDefinitionBuilder;
import kpi.model.api.table.definition.OnDemandKpiDefinitions.OnDemandKpiDefinition.OnDemandKpiDefinitionBuilder;
import kpi.model.api.table.definition.SimpleKpiDefinitions.SimpleKpiDefinition;
import kpi.model.api.table.definition.SimpleKpiDefinitions.SimpleKpiDefinition.SimpleKpiDefinitionBuilder;
import kpi.model.complex.element.ComplexAggregationElement;
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
import kpi.model.ondemand.element.OnDemandAggregationElement;
import kpi.model.ondemand.element.OnDemandFilterElement;
import kpi.model.ondemand.table.definition.optional.OnDemandDefinitionAggregationElements;
import kpi.model.ondemand.table.definition.optional.OnDemandDefinitionExportable;
import kpi.model.ondemand.table.definition.optional.OnDemandDefinitionFilters;
import kpi.model.ondemand.table.definition.required.OnDemandDefinitionAggregationType;
import kpi.model.ondemand.table.definition.required.OnDemandDefinitionExpression;
import kpi.model.ondemand.table.definition.required.OnDemandDefinitionName;
import kpi.model.ondemand.table.definition.required.OnDemandDefinitionObjectType;
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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EntityTransformers {

    public static OnDemandKpiDefinition toOnDemand(@NonNull final KpiDefinitionEntity entity) {
        final OnDemandDefinitionName name = OnDemandDefinitionName.of(entity.name());
        final OnDemandDefinitionExpression expression = OnDemandDefinitionExpression.of(entity.expression());
        final OnDemandDefinitionObjectType objectType = OnDemandDefinitionObjectType.of(entity.objectType());
        final OnDemandDefinitionAggregationType aggregationType = OnDemandDefinitionAggregationType.of(aggregationType(entity));
        final OnDemandDefinitionAggregationElements aggregationElements = OnDemandDefinitionAggregationElements.of(toAggregationElements(entity, OnDemandAggregationElement::of));
        final OnDemandDefinitionExportable exportable = OnDemandDefinitionExportable.of(entity.exportable());
        final OnDemandDefinitionFilters filters = OnDemandDefinitionFilters.of(toFilters(entity, OnDemandFilterElement::of));

        final OnDemandKpiDefinitionBuilder builder = OnDemandKpiDefinition.builder();
        builder.name(name);
        builder.expression(expression);
        builder.objectType(objectType);
        builder.aggregationType(aggregationType);
        builder.aggregationElements(aggregationElements);
        builder.exportable(exportable);
        builder.filters(filters);
        return builder.build();
    }

    public static ComplexKpiDefinition toComplex(@NonNull final KpiDefinitionEntity entity) {
        final ComplexDefinitionName name = ComplexDefinitionName.of(entity.name());
        final ComplexDefinitionExpression expression = ComplexDefinitionExpression.of(entity.expression());
        final ComplexDefinitionObjectType objectType = ComplexDefinitionObjectType.of(entity.objectType());
        final ComplexDefinitionAggregationType aggregationType = ComplexDefinitionAggregationType.of(aggregationType(entity));
        final ComplexDefinitionExecutionGroup executionGroup = ComplexDefinitionExecutionGroup.of(entity.executionGroup().name());
        final ComplexDefinitionAggregationElements aggregationElements = ComplexDefinitionAggregationElements.of(toAggregationElements(entity, ComplexAggregationElement::of));
        final ComplexDefinitionExportable exportable = ComplexDefinitionExportable.of(entity.exportable());
        final ComplexDefinitionFilters filters = ComplexDefinitionFilters.of(toFilters(entity, ComplexFilterElement::of));
        final ComplexDefinitionDataReliabilityOffset dataReliabilityOffset = ComplexDefinitionDataReliabilityOffset.of(entity.dataReliabilityOffset());
        final ComplexDefinitionDataLookBackLimit dataLookBackLimit = ComplexDefinitionDataLookBackLimit.of(entity.dataLookbackLimit());
        final ComplexDefinitionReexportLateData reexportLateData = ComplexDefinitionReexportLateData.of(entity.reexportLateData());

        final ComplexKpiDefinitionBuilder builder = ComplexKpiDefinition.builder();
        builder.name(name);
        builder.expression(expression);
        builder.objectType(objectType);
        builder.aggregationType(aggregationType);
        builder.executionGroup(executionGroup);
        builder.aggregationElements(aggregationElements);
        builder.exportable(exportable);
        builder.filters(filters);
        builder.dataReliabilityOffset(dataReliabilityOffset);
        builder.dataLookBackLimit(dataLookBackLimit);
        builder.reexportLateData(reexportLateData);
        return builder.build();
    }

    public static SimpleKpiDefinition toSimple(@NonNull final KpiDefinitionEntity entity) {
        final SimpleDefinitionName name = SimpleDefinitionName.of(entity.name());
        final SimpleDefinitionExpression expression = SimpleDefinitionExpression.of(entity.expression());
        final SimpleDefinitionObjectType objectType = SimpleDefinitionObjectType.of(entity.objectType());
        final SimpleDefinitionAggregationType aggregationType = SimpleDefinitionAggregationType.of(aggregationType(entity));
        final SimpleDefinitionAggregationElements aggregationElements = SimpleDefinitionAggregationElements.of(toAggregationElements(entity, SimpleAggregationElement::of));
        final SimpleDefinitionExportable exportable = SimpleDefinitionExportable.of(entity.exportable());
        final SimpleDefinitionFilters filters = SimpleDefinitionFilters.of(toFilters(entity, SimpleFilterElement::of));
        final SimpleDefinitionDataReliabilityOffset dataReliabilityOffset = SimpleDefinitionDataReliabilityOffset.of(entity.dataReliabilityOffset());
        final SimpleDefinitionDataLookBackLimit dataLookBackLimit = SimpleDefinitionDataLookBackLimit.of(entity.dataLookbackLimit());
        final SimpleDefinitionReexportLateData reexportLateData = SimpleDefinitionReexportLateData.of(entity.reexportLateData());
        final SimpleDefinitionInpDataIdentifier inpDataIdentifier = SimpleDefinitionInpDataIdentifier.of(entity.dataIdentifier().toString());

        final SimpleKpiDefinitionBuilder builder = SimpleKpiDefinition.builder();
        builder.name(name);
        builder.expression(expression);
        builder.objectType(objectType);
        builder.aggregationType(aggregationType);
        builder.aggregationElements(aggregationElements);
        builder.exportable(exportable);
        builder.filters(filters);
        builder.dataReliabilityOffset(dataReliabilityOffset);
        builder.dataLookBackLimit(dataLookBackLimit);
        builder.reexportLateData(reexportLateData);
        builder.inpDataIdentifier(inpDataIdentifier);
        return builder.build();
    }

    private static AggregationType aggregationType(@NonNull final KpiDefinitionEntity entity) {
        return AggregationType.valueOf(entity.aggregationType());
    }

    private static <T> List<T> toFilters(@NonNull final KpiDefinitionEntity entity, final Function<String, T> mapper) {
        return entity.filters().stream().map(mapper).collect(Collectors.toList());
    }

    private static <T> List<T> toAggregationElements(@NonNull final KpiDefinitionEntity entity, final Function<String, T> mapper) {
        return entity.aggregationElements().stream().map(mapper).collect(Collectors.toList());
    }
}
