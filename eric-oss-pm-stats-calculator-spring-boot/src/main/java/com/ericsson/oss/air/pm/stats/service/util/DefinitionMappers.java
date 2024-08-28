/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.util;

import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.DEFINITION_AGGREGATION_ELEMENTS_KEY;
import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.DEFINITION_AGGREGATION_PERIOD_KEY;
import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.DEFINITION_AGGREGATION_TYPE_KEY;
import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.DEFINITION_ALIAS_KEY;
import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.DEFINITION_EXECUTION_GROUP_KEY;
import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.DEFINITION_EXPORTABLE_KEY;
import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.DEFINITION_EXPRESSION_KEY;
import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.DEFINITION_FILTER_KEY;
import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.DEFINITION_IDENTIFIER_KEY;
import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.DEFINITION_LOOKBACK_LIMIT_KEY;
import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.DEFINITION_NAME_KEY;
import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.DEFINITION_OBJECT_TYPE_KEY;
import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.DEFINITION_REEXPORT_LATE_DATA;
import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.DEFINITION_RELIABILITY_OFFSET_KEY;
import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.DEFINITION_SCHEMA_CATEGORY_KEY;
import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.DEFINITION_SCHEMA_DATA_SPACE_KEY;
import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.DEFINITION_SCHEMA_DETAIL;
import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.DEFINITION_SCHEMA_NAME_KEY;
import static kpi.model.util.Attributes.ATTRIBUTE_REEXPORT_LATE_DATA;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.ericsson.oss.air.pm.stats.calculator.api.model.Definition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition.KpiDefinitionBuilder;
import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity.KpiDefinitionEntityBuilder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefinitionMappers {

    public static KpiDefinitionEntity toEntity(@NonNull final Definition definition) {

        final Boolean exportable = checkBooleanValue(definition, DEFINITION_EXPORTABLE_KEY);

        final KpiDefinitionEntityBuilder builder = KpiDefinitionEntity.builder();

        /* Common attributes */
        builder.withName(definition.getName());
        builder.withAlias(definition.attribute(DEFINITION_ALIAS_KEY, String.class));
        builder.withExpression(definition.getExpression());
        builder.withObjectType(definition.attribute(DEFINITION_OBJECT_TYPE_KEY, String.class));
        builder.withAggregationType(definition.attribute(DEFINITION_AGGREGATION_TYPE_KEY, String.class));
        builder.withAggregationPeriod(checkInteger(definition, DEFINITION_AGGREGATION_PERIOD_KEY));
        builder.withAggregationElements(definition.getAggregationElements());
        builder.withExportable(exportable);
        builder.withFilters(definition.getFilters().stream().map(Filter::getName).collect(Collectors.toList()));

        /* Simple attributes */
        if (definition.isSimpleKpiDefinition()) {
            DataIdentifier inputDataIdentifier = definition.getInputDataIdentifier();
            builder.withSchemaDataSpace(inputDataIdentifier.dataSpace());
            builder.withSchemaCategory(inputDataIdentifier.category());
            builder.withSchemaName(inputDataIdentifier.schema());
            builder.withSchemaDetail(definition.getSchemaDetail());
        }

        /* Scheduled attributes [Complex, Simple] */
        if (definition.isSimpleKpiDefinition() || definition.isComplexDefinition()) {
            final Integer dataReliabilityOffset = checkInteger(definition, DEFINITION_RELIABILITY_OFFSET_KEY);
            final Integer dataLookBackLimit = checkInteger(definition, DEFINITION_LOOKBACK_LIMIT_KEY);

            builder.withDataReliabilityOffset(dataReliabilityOffset);
            builder.withDataLookbackLimit(dataLookBackLimit);
            builder.withReexportLateData(checkBooleanValue(definition, DEFINITION_REEXPORT_LATE_DATA));
            builder.withExecutionGroup(ExecutionGroup.builder().withName(definition.attribute(DEFINITION_EXECUTION_GROUP_KEY, String.class)).build());
        }

        return builder.build();
    }

    @Nullable
    public static Boolean checkBooleanValue(@NonNull final Definition definition, final String attributeByName) {
        final Object attribute = definition.getAttributeByName(attributeByName);
        if (attribute instanceof Boolean) {
            return (Boolean) attribute;
        }
        if (attribute instanceof String) {
            return Boolean.parseBoolean((String) attribute);
        }
        return null; //NOSONAR
    }

    @Nullable
    public static Integer checkInteger(@NonNull final Definition definition, final String attributeByName) {
        final Object attribute = definition.getAttributeByName(attributeByName);
        if (attribute instanceof Integer) {
            return (Integer) attribute;
        }
        if (attribute instanceof String) {
            return Integer.parseInt((String) attribute);
        }
        return null; //NOSONAR
    }

    public static KpiDefinitionEntity toEntity(@NonNull final KpiDefinition kpiDefinition) {
        final KpiDefinitionEntityBuilder builder = KpiDefinitionEntity.builder();

        /* Common attributes */
        builder.withName(kpiDefinition.getName());
        builder.withAlias(kpiDefinition.getAlias());
        builder.withExpression(kpiDefinition.getExpression());
        builder.withObjectType(kpiDefinition.getObjectType());
        builder.withAggregationType(kpiDefinition.getAggregationType());
        builder.withAggregationPeriod(Integer.parseInt(kpiDefinition.getAggregationPeriod()));
        builder.withAggregationElements(kpiDefinition.getAggregationElements());
        builder.withExportable(kpiDefinition.getExportable());
        builder.withFilters(kpiDefinition.getFilter().stream().map(Filter::getName).collect(Collectors.toList()));

        /* Simple attributes */
        if (kpiDefinition.isSimple()) {
            final DataIdentifier dataIdentifier = kpiDefinition.getInpDataIdentifier();
            builder.withSchemaDataSpace(dataIdentifier.dataSpace());
            builder.withSchemaCategory(dataIdentifier.category());
            builder.withSchemaName(dataIdentifier.schema());
            builder.withSchemaDetail(kpiDefinition.getSchemaDetail());
        }

        /* Scheduled attributes [Complex, Simple] */
        if (kpiDefinition.isSimple() || kpiDefinition.isScheduled()) {
            builder.withDataReliabilityOffset(kpiDefinition.getDataReliabilityOffset());
            builder.withDataLookbackLimit(kpiDefinition.getDataLookbackLimit());
            builder.withReexportLateData(kpiDefinition.getReexportLateData());
            builder.withExecutionGroup(ExecutionGroup.builder().withName(kpiDefinition.getExecutionGroup()).build());
        }

        return builder.build();
    }

    public static Definition toDefinition(@NonNull final KpiDefinitionEntity kpiDefinitionEntity) {
        final Definition definition = new Definition();

        /* Common attributes */
        definition.setAttribute(DEFINITION_NAME_KEY, kpiDefinitionEntity.name());
        definition.setAttribute(DEFINITION_ALIAS_KEY, kpiDefinitionEntity.alias());
        definition.setAttribute(DEFINITION_EXPRESSION_KEY, kpiDefinitionEntity.expression());
        definition.setAttribute(DEFINITION_OBJECT_TYPE_KEY, kpiDefinitionEntity.objectType());
        definition.setAttribute(DEFINITION_AGGREGATION_TYPE_KEY, kpiDefinitionEntity.aggregationType());
        definition.setAttribute(DEFINITION_AGGREGATION_PERIOD_KEY, kpiDefinitionEntity.aggregationPeriod());
        definition.setAttribute(DEFINITION_EXPORTABLE_KEY, kpiDefinitionEntity.exportable());
        definition.setAttribute(DEFINITION_FILTER_KEY, kpiDefinitionEntity.filters().stream().map(Filter::new).collect(Collectors.toList()));
        definition.setAttribute(DEFINITION_AGGREGATION_ELEMENTS_KEY, kpiDefinitionEntity.aggregationElements());
        /* Because of the KpiDefinitionMapper add these two attributes as well */
        definition.setAttribute(DEFINITION_IDENTIFIER_KEY, null);
        definition.setAttribute(DEFINITION_SCHEMA_DETAIL, null);

        /* Simple attributes */
        if (kpiDefinitionEntity.isSimple()) {
            final String dataSpace = kpiDefinitionEntity.schemaDataSpace();
            final String category = kpiDefinitionEntity.schemaCategory();
            final String name = kpiDefinitionEntity.schemaName();

            definition.setAttribute(DEFINITION_SCHEMA_DATA_SPACE_KEY, kpiDefinitionEntity.schemaDataSpace());
            definition.setAttribute(DEFINITION_SCHEMA_CATEGORY_KEY, kpiDefinitionEntity.schemaCategory());
            definition.setAttribute(DEFINITION_SCHEMA_NAME_KEY, kpiDefinitionEntity.schemaName());
            definition.setAttribute(DEFINITION_IDENTIFIER_KEY, DataIdentifier.of(dataSpace, category, name));
            definition.setAttribute(DEFINITION_SCHEMA_DETAIL, kpiDefinitionEntity.schemaDetail());
        }

        /* Scheduled attributes [Complex, Simple] */
        if (kpiDefinitionEntity.isComplex() || kpiDefinitionEntity.isSimple()) {
            final ExecutionGroup executionGroup = kpiDefinitionEntity.executionGroup();

            definition.setAttribute(DEFINITION_RELIABILITY_OFFSET_KEY, kpiDefinitionEntity.dataReliabilityOffset());
            definition.setAttribute(DEFINITION_LOOKBACK_LIMIT_KEY, kpiDefinitionEntity.dataLookbackLimit());
            definition.setAttribute(ATTRIBUTE_REEXPORT_LATE_DATA, kpiDefinitionEntity.reexportLateData());
            definition.setAttribute(DEFINITION_EXECUTION_GROUP_KEY, executionGroup == null ? null : executionGroup.name());
        }

        return definition;
    }

    public static KpiDefinition toKpiDefinition(@NonNull final KpiDefinitionEntity kpiDefinitionEntity) {
        final KpiDefinitionBuilder builder = KpiDefinition.builder();

        /* Common attributes */
        builder.withName(kpiDefinitionEntity.name());
        builder.withAlias(kpiDefinitionEntity.alias());
        builder.withExpression(kpiDefinitionEntity.expression());
        builder.withObjectType(kpiDefinitionEntity.objectType());
        builder.withAggregationType(kpiDefinitionEntity.aggregationType());
        builder.withAggregationPeriod(String.valueOf(kpiDefinitionEntity.aggregationPeriod()));
        builder.withAggregationElements(kpiDefinitionEntity.aggregationElements());
        builder.withExportable(kpiDefinitionEntity.exportable());
        builder.withFilter(kpiDefinitionEntity.filters().stream().map(Filter::new).collect(Collectors.toList()));

        /* Simple attributes */
        if (kpiDefinitionEntity.isSimple()) {
            final String dataSpace = kpiDefinitionEntity.schemaDataSpace();
            final String category = kpiDefinitionEntity.schemaCategory();
            final String name = kpiDefinitionEntity.schemaName();

            builder.withInpDataIdentifier(DataIdentifier.of(dataSpace, category, name));
            builder.withSchemaDetail(kpiDefinitionEntity.schemaDetail());
        }

        /* Scheduled attributes [Complex, Simple] */
        if (kpiDefinitionEntity.isComplex() || kpiDefinitionEntity.isSimple()) {
            final ExecutionGroup executionGroup = kpiDefinitionEntity.executionGroup();

            builder.withDataReliabilityOffset(kpiDefinitionEntity.dataReliabilityOffset());
            builder.withDataLookbackLimit(kpiDefinitionEntity.dataLookbackLimit());
            builder.withReexportLateData(kpiDefinitionEntity.reexportLateData());
            builder.withExecutionGroup(executionGroup == null ? null : executionGroup.name());
        }

        return builder.build();
    }

    public static List<KpiDefinitionEntity> definitionsToEntities(@NonNull final List<? extends Definition> definitions) {
        return definitions.stream().map(DefinitionMappers::toEntity).collect(Collectors.toList());
    }

    public static List<KpiDefinitionEntity> kpiDefinitionsToEntities(@NonNull final List<? extends KpiDefinition> definitions) {
        return definitions.stream().map(DefinitionMappers::toEntity).collect(Collectors.toList());
    }

    public static List<Definition> toDefinitions(@NonNull final Collection<? extends KpiDefinitionEntity> entities) {
        return entities.stream().map(DefinitionMappers::toDefinition).collect(Collectors.toList());
    }

    //TODO remove only when Definition class is eliminated
    public static Set<Definition> toDefinitionsSet(@NonNull final Collection<? extends KpiDefinitionEntity> entities) {
        return entities.stream().map(DefinitionMappers::toDefinition).collect(Collectors.toSet());
    }

    public static List<KpiDefinition> toKpiDefinitions(@NonNull final List<? extends KpiDefinitionEntity> entities) {
        return entities.stream().map(DefinitionMappers::toKpiDefinition).collect(Collectors.toList());
    }

}
