/*******************************************************************************
 * COPYRIGHT Ericsson 2024
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
import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.DEFINITION_LOOKBACK_LIMIT_KEY;
import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.DEFINITION_NAME_KEY;
import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.DEFINITION_OBJECT_TYPE_KEY;
import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.DEFINITION_REEXPORT_LATE_DATA;
import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.DEFINITION_RELIABILITY_OFFSET_KEY;
import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.DEFINITION_SCHEMA_CATEGORY_KEY;
import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.DEFINITION_SCHEMA_DATA_SPACE_KEY;
import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.DEFINITION_SCHEMA_DETAIL;
import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.DEFINITION_SCHEMA_NAME_KEY;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.Definition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition.KpiDefinitionBuilder;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail;
import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity.KpiDefinitionEntityBuilder;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DefinitionMapperTest {
    DefinitionMapper objectUnderTest = new DefinitionMapper();

    @Test
    void definitionToKpiDefinitionEntity() {
        final KpiDefinitionEntity actual = objectUnderTest.toEntity(definition());
        Assertions.assertThat(actual).usingRecursiveComparison().isEqualTo(entity());
    }

    @Test
    void kpiDefinitionToKpiDefinitionEntity() {
        final KpiDefinitionEntity actual = objectUnderTest.toEntity(kpiDefinition());
        Assertions.assertThat(actual).usingRecursiveComparison().isEqualTo(entity());
    }

    @Test
    void kpiDefinitionEntityToDefinition() {
        final Definition actual = objectUnderTest.toDefinition(entity());
        Assertions.assertThat(actual).usingRecursiveComparison().isEqualTo(definition());
    }

    @Test
    void kpiDefinitionEntityToKpiDefinition() {
        final KpiDefinition actual = objectUnderTest.toKpiDefinition(entity());
        Assertions.assertThat(actual).usingRecursiveComparison().isEqualTo(kpiDefinition());
    }

    @Nested
    class MappingCollections {
        @Test
        void shouldMapDefinitionsToEntities() {
            final List<KpiDefinitionEntity> actual = objectUnderTest.definitionsToEntities(List.of(definition()));
            Assertions.assertThat(actual).first().usingRecursiveComparison().isEqualTo(entity());
        }

        @Test
        void shouldMapKpiDefinitionsToEntities() {
            final List<KpiDefinitionEntity> actual = objectUnderTest.kpiDefinitionsToEntities(List.of(kpiDefinition()));
            Assertions.assertThat(actual).first().usingRecursiveComparison().isEqualTo(entity());
        }

        @Test
        void shouldMapToDefinitions() {
            final List<Definition> actual = objectUnderTest.toDefinitions(List.of(entity()));
            Assertions.assertThat(actual).first().usingRecursiveComparison().isEqualTo(definition());
        }

        @Test
        void shouldMapToKpiDefinitions() {
            final List<KpiDefinition> actual = objectUnderTest.toKpiDefinitions(List.of(entity()));
            Assertions.assertThat(actual).first().usingRecursiveComparison().isEqualTo(kpiDefinition());
        }

        @Test
        void shouldMapEntitiesSetToDefinitionSet() {
            final Set<Definition> actual = objectUnderTest.toDefinitions(Set.of(entity()));
            Assertions.assertThat(actual).first().usingRecursiveComparison().isEqualTo(definition());
        }

        @Test
        void shouldMapDefinitionsSetToEntitiesSet() {
            final Set<KpiDefinitionEntity> actual = objectUnderTest.toEntities(Set.of(definition()));
            Assertions.assertThat(actual).first().usingRecursiveComparison().isEqualTo(entity());
        }
    }

    static Definition definition() {
        final Definition definition = new Definition();

        definition.setAttribute(DEFINITION_NAME_KEY, "test");
        definition.setAttribute(DEFINITION_ALIAS_KEY, "alias");
        definition.setAttribute(DEFINITION_EXPRESSION_KEY, "expression");
        definition.setAttribute(DEFINITION_OBJECT_TYPE_KEY, "Integer");
        definition.setAttribute(DEFINITION_AGGREGATION_TYPE_KEY, "SUM");
        definition.setAttribute(DEFINITION_AGGREGATION_PERIOD_KEY, 1_440);
        definition.setAttribute(DEFINITION_AGGREGATION_ELEMENTS_KEY, Arrays.asList("agg_column_0", "agg_column_1"));
        definition.setAttribute(DEFINITION_EXPORTABLE_KEY, true);
        definition.setAttribute(Definition.INP_DATA_IDENTIFIER, DataIdentifier.of("dataSpace", "category", "schema"));
        definition.setAttribute(DEFINITION_SCHEMA_DATA_SPACE_KEY, "dataSpace");
        definition.setAttribute(DEFINITION_SCHEMA_CATEGORY_KEY, "category");
        definition.setAttribute(DEFINITION_SCHEMA_NAME_KEY, "schema");
        definition.setAttribute(DEFINITION_EXECUTION_GROUP_KEY, "executionGroup");
        definition.setAttribute(DEFINITION_RELIABILITY_OFFSET_KEY, 15);
        definition.setAttribute(DEFINITION_LOOKBACK_LIMIT_KEY, 5);
        definition.setAttribute(DEFINITION_FILTER_KEY, List.of(new Filter("filter1")));
        definition.setAttribute(DEFINITION_REEXPORT_LATE_DATA, true);
        definition.setAttribute(DEFINITION_SCHEMA_DETAIL, new SchemaDetail(1, "test", "test"));

        return definition;
    }

    static KpiDefinition kpiDefinition() {
        final KpiDefinitionBuilder builder = KpiDefinition.builder();

        builder.withName("test");
        builder.withAlias("alias");
        builder.withExpression("expression");
        builder.withObjectType("Integer");
        builder.withAggregationType("SUM");
        builder.withAggregationPeriod("1440");
        builder.withAggregationElements(Arrays.asList("agg_column_0", "agg_column_1"));
        builder.withFilter(List.of(new Filter("filter1")));
        builder.withInpDataIdentifier(DataIdentifier.of("dataSpace", "category", "schema"));
        builder.withExportable(true);
        builder.withExecutionGroup("executionGroup");
        builder.withDataReliabilityOffset(15);
        builder.withDataLookbackLimit(5);
        builder.withReexportLateData(true);
        builder.withSchemaDetail(new SchemaDetail(1, "test", "test"));

        return builder.build();
    }

    static KpiDefinitionEntity entity() {
        final KpiDefinitionEntityBuilder builder = KpiDefinitionEntity.builder();

        builder.withName("test");
        builder.withAlias("alias");
        builder.withExpression("expression");
        builder.withObjectType("Integer");
        builder.withAggregationType("SUM");
        builder.withAggregationPeriod(1_440);
        builder.withAggregationElements(Arrays.asList("agg_column_0", "agg_column_1"));
        builder.withExportable(true);
        builder.withSchemaDataSpace("dataSpace");
        builder.withSchemaCategory("category");
        builder.withSchemaName("schema");
        builder.withExecutionGroup(ExecutionGroup.builder().withName("executionGroup").build());
        builder.withDataReliabilityOffset(15);
        builder.withDataLookbackLimit(5);
        builder.withFilters(List.of("filter1"));
        builder.withReexportLateData(true);
        builder.withSchemaDetail(new SchemaDetail(1, "test", "test"));

        return builder.build();
    }
}