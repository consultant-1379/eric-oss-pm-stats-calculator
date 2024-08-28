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

import java.util.List;

import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity.KpiDefinitionEntityBuilder;

import kpi.model.api.enumeration.AggregationType;
import kpi.model.api.enumeration.ObjectType;
import kpi.model.api.table.definition.ComplexKpiDefinitions.ComplexKpiDefinition;
import kpi.model.api.table.definition.OnDemandKpiDefinitions.OnDemandKpiDefinition;
import kpi.model.api.table.definition.SimpleKpiDefinitions.SimpleKpiDefinition;
import kpi.model.complex.element.ComplexAggregationElement;
import kpi.model.complex.element.ComplexFilterElement;
import kpi.model.ondemand.element.OnDemandAggregationElement;
import kpi.model.ondemand.element.OnDemandFilterElement;
import kpi.model.simple.element.SimpleAggregationElement;
import kpi.model.simple.element.SimpleFilterElement;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class EntityTransformersTest {

    @Test
    void shouldTestOnDemand() {
        final OnDemandKpiDefinition actual = EntityTransformers.toOnDemand(entity(
                "dummy_definition", "FROM dummy_expression", "INTEGER", "SUM",
                List.of("table.column"), true,
                null, null, null, null, null, null,
                List.of("dummy_filter"), null
        ));

        Assertions.assertThat(actual.name().value()).isEqualTo("dummy_definition");
        Assertions.assertThat(actual.expression().value()).isEqualTo("FROM dummy_expression");
        Assertions.assertThat(actual.objectType().value()).isEqualTo(ObjectType.POSTGRES_INTEGER);
        Assertions.assertThat(actual.aggregationType().value()).isEqualTo(AggregationType.SUM);
        Assertions.assertThat(actual.aggregationElements().value()).containsExactlyInAnyOrder(OnDemandAggregationElement.of("table.column"));
        Assertions.assertThat(actual.exportable().value()).isTrue();
        Assertions.assertThat(actual.filters().value()).containsExactlyInAnyOrder(OnDemandFilterElement.of("dummy_filter"));
    }

    @Test
    void shouldTestComplex() {
        final ComplexKpiDefinition actual = EntityTransformers.toComplex(entity(
                "dummy_definition", "FROM dummy_expression", "INTEGER", "SUM",
                List.of("table.column"), true,
                null, null, null, ExecutionGroup.builder().withName("COMPLEX1").build(), 30, 45,
                List.of("dummy_filter"), true
        ));

        Assertions.assertThat(actual.name().value()).isEqualTo("dummy_definition");
        Assertions.assertThat(actual.expression().value()).isEqualTo("FROM dummy_expression");
        Assertions.assertThat(actual.objectType().value()).isEqualTo(ObjectType.POSTGRES_INTEGER);
        Assertions.assertThat(actual.aggregationType().value()).isEqualTo(AggregationType.SUM);
        Assertions.assertThat(actual.executionGroup().value()).isEqualTo("COMPLEX1");
        Assertions.assertThat(actual.aggregationElements().value()).containsExactlyInAnyOrder(ComplexAggregationElement.of("table.column"));
        Assertions.assertThat(actual.exportable().value()).isTrue();
        Assertions.assertThat(actual.filters().value()).containsExactlyInAnyOrder(ComplexFilterElement.of("dummy_filter"));
        Assertions.assertThat(actual.dataReliabilityOffset().value()).isEqualTo(30);
        Assertions.assertThat(actual.dataLookBackLimit().value()).isEqualTo(45);
        Assertions.assertThat(actual.reexportLateData().value()).isTrue();
    }

    @Test
    void shouldTestSimple() {
        final SimpleKpiDefinition actual = EntityTransformers.toSimple(entity(
                "dummy_definition", "dummy_expression", "INTEGER", "SUM",
                List.of("table.column"), true,
                "space", "category", "schema", null, 30, 45,
                List.of("dummy_filter"), true
        ));

        Assertions.assertThat(actual.name().value()).isEqualTo("dummy_definition");
        Assertions.assertThat(actual.expression().value()).isEqualTo("dummy_expression");
        Assertions.assertThat(actual.objectType().value()).isEqualTo(ObjectType.POSTGRES_INTEGER);
        Assertions.assertThat(actual.aggregationType().value()).isEqualTo(AggregationType.SUM);
        Assertions.assertThat(actual.aggregationElements().value()).containsExactlyInAnyOrder(SimpleAggregationElement.of("table.column"));
        Assertions.assertThat(actual.exportable().value()).isTrue();
        Assertions.assertThat(actual.filters().value()).containsExactlyInAnyOrder(SimpleFilterElement.of("dummy_filter"));
        Assertions.assertThat(actual.dataReliabilityOffset().value()).isEqualTo(30);
        Assertions.assertThat(actual.dataLookBackLimit().value()).isEqualTo(45);
        Assertions.assertThat(actual.reexportLateData().value()).isTrue();
        Assertions.assertThat(actual.inpDataIdentifier().value()).isEqualTo("space|category|schema");
    }

    static KpiDefinitionEntity entity(
            final String name, final String expression, final String objectType, final String aggregationType,
            final List<String> aggregationElements, final Boolean exportable, final String dataSpace, final String category,
            final String schemaName, final ExecutionGroup executionGroup, final Integer reliabilityOffset,
            final Integer lookbackLimit, final List<String> filters, final Boolean reexportLateData
    ) {
        final KpiDefinitionEntityBuilder builder = KpiDefinitionEntity.builder();
        builder.withName(name);
        builder.withExpression(expression);
        builder.withObjectType(objectType);
        builder.withAggregationType(aggregationType);
        builder.withAggregationElements(aggregationElements);
        builder.withExportable(exportable);
        builder.withSchemaDataSpace(dataSpace);
        builder.withSchemaCategory(category);
        builder.withSchemaName(schemaName);
        builder.withExecutionGroup(executionGroup);
        builder.withDataReliabilityOffset(reliabilityOffset);
        builder.withDataLookbackLimit(lookbackLimit);
        builder.withFilters(filters);
        builder.withReexportLateData(reexportLateData);
        return builder.build();
    }
}