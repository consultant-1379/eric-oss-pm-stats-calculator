/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition.KpiDefinitionBuilder;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.KpiExecutionGroup;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.SchemaDetail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KpiDefinitionAdapterImplTest {

    static final Boolean EXPORTABLE = false;
    static final Integer AGGREGATION_PERIOD = 60;
    static final String FILTER_1 = "filter1";
    static final String FILTER_2 = "filter2";
    static final String EXPRESSION = "expression";
    static final String AGGREGATION_ELEMENT_1 = "element1";
    static final String AGGREGATION_ELEMENT_2 = "element2";
    static final String ALIAS = "alias";
    static final String AGGREGATION_TYPE = "aggregation_type";
    static final Integer DATA_LOOKBACK_LIMIT = 30;
    static final Integer DATA_RELIABILITY_OFFSET = 90;
    static final String EXECUTION_GROUP = "execution_group";
    static final String SCHEMA_DATA_SPACE = "dataSpace";
    static final String SCHEMA_CATEGORY = "category";
    static final String SCHEMA_NAME = "schema_name";
    static final String NAME = "name";
    static final String OBJECT_TYPE = "object_type";
    static final String TOPIC = "topic";
    static final String NAMESPACE = "namespace";

    KpiDefinitionAdapterImpl objectUnderTest = new KpiDefinitionAdapterImpl();

    KpiDefinition simpleKpiDefinitionSpark;
    com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition simpleKpiDefinitionApi;
    SchemaDetail schemaDetailSpark;
    com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail schemaDetailApi;

    @BeforeEach
    void setUp() {

        schemaDetailSpark = SchemaDetail.builder().id(1).topic(TOPIC).namespace(NAMESPACE).build();
        schemaDetailApi = com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail.builder().withId(1).withTopic(TOPIC).withNamespace(NAMESPACE).build();

        final KpiDefinition.KpiDefinitionBuilder entityBuilder = KpiDefinition.builder();
        entityBuilder.exportable(EXPORTABLE);
        entityBuilder.aggregationPeriod(AGGREGATION_PERIOD);
        entityBuilder.filters(Arrays.asList(FILTER_1, FILTER_2));
        entityBuilder.expression(EXPRESSION);
        entityBuilder.aggregationElements(Arrays.asList(AGGREGATION_ELEMENT_1, AGGREGATION_ELEMENT_2));
        entityBuilder.alias(ALIAS);
        entityBuilder.aggregationType(AGGREGATION_TYPE);
        entityBuilder.dataLookbackLimit(DATA_LOOKBACK_LIMIT);
        entityBuilder.dataReliabilityOffset(DATA_RELIABILITY_OFFSET);
        entityBuilder.executionGroup(KpiExecutionGroup.builder().executionGroup(EXECUTION_GROUP).build());
        entityBuilder.name(NAME);
        entityBuilder.objectType(OBJECT_TYPE);
        entityBuilder.schemaDataSpace(SCHEMA_DATA_SPACE);
        entityBuilder.schemaCategory(SCHEMA_CATEGORY);
        entityBuilder.schemaName(SCHEMA_NAME);
        entityBuilder.schemaDetail(schemaDetailSpark);
        entityBuilder.reexportLateData(true);
        entityBuilder.collectionId(UUID.fromString("040dabe6-f106-4e7b-833d-0c40dea01841"));
        simpleKpiDefinitionSpark = entityBuilder.build();

        final KpiDefinitionBuilder builder = com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition.builder();
        builder.withName(NAME);
        builder.withAlias(ALIAS);
        builder.withExpression(EXPRESSION);
        builder.withObjectType(OBJECT_TYPE);
        builder.withAggregationType(AGGREGATION_TYPE);
        builder.withAggregationPeriod(String.valueOf(AGGREGATION_PERIOD));
        builder.withAggregationElements(Arrays.asList(AGGREGATION_ELEMENT_1, AGGREGATION_ELEMENT_2));
        builder.withExportable(EXPORTABLE);
        builder.withFilter(Arrays.asList(Filter.of(FILTER_1), Filter.of(FILTER_2)));
        builder.withInpDataIdentifier(DataIdentifier.of(SCHEMA_DATA_SPACE, SCHEMA_CATEGORY, SCHEMA_NAME));
        builder.withExecutionGroup(EXECUTION_GROUP);
        builder.withDataReliabilityOffset(DATA_RELIABILITY_OFFSET);
        builder.withDataLookbackLimit(DATA_LOOKBACK_LIMIT);
        builder.withSchemaDetail(schemaDetailApi);
        builder.withReexportLateData(true);
        builder.withCollectionId(UUID.fromString("040dabe6-f106-4e7b-833d-0c40dea01841"));
        simpleKpiDefinitionApi = builder.build();
    }

    @Test
    void shouldConvertKpiDefinition() {
        final com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition actual = objectUnderTest.convertKpiDefinition(simpleKpiDefinitionSpark);
        assertThat(actual).isEqualTo(simpleKpiDefinitionApi);
    }

    @Test
    void shouldReturnEmptyList() {
        assertThat(objectUnderTest.convertKpiDefinitions(new ArrayList<>())).isEmpty();
    }

    @Test
    void shouldReturnSameLengthOfKpisWithSameValueOfInputs() {
        final KpiDefinition.KpiDefinitionBuilder entityBuilder = KpiDefinition.builder();
        entityBuilder.exportable(EXPORTABLE);
        entityBuilder.aggregationPeriod(AGGREGATION_PERIOD);
        entityBuilder.filters(Arrays.asList(FILTER_1, FILTER_2));
        entityBuilder.expression(EXPRESSION);
        entityBuilder.aggregationElements(Arrays.asList(AGGREGATION_ELEMENT_1, AGGREGATION_ELEMENT_2));
        entityBuilder.alias(ALIAS);
        entityBuilder.aggregationType(AGGREGATION_TYPE);
        entityBuilder.dataLookbackLimit(DATA_LOOKBACK_LIMIT);
        entityBuilder.dataReliabilityOffset(DATA_RELIABILITY_OFFSET);
        entityBuilder.name(NAME);
        entityBuilder.objectType(OBJECT_TYPE);
        entityBuilder.schemaDetail(null);
        entityBuilder.reexportLateData(false);
        final KpiDefinition complexKpiDefinitionSpark = entityBuilder.build();

        final KpiDefinitionBuilder builder = com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition.builder();
        builder.withName(NAME);
        builder.withAlias(ALIAS);
        builder.withExpression(EXPRESSION);
        builder.withObjectType(OBJECT_TYPE);
        builder.withAggregationType(AGGREGATION_TYPE);
        builder.withAggregationPeriod(String.valueOf(AGGREGATION_PERIOD));
        builder.withAggregationElements(Arrays.asList(AGGREGATION_ELEMENT_1, AGGREGATION_ELEMENT_2));
        builder.withExportable(EXPORTABLE);
        builder.withFilter(Arrays.asList(Filter.of(FILTER_1), Filter.of(FILTER_2)));
        builder.withDataReliabilityOffset(DATA_RELIABILITY_OFFSET);
        builder.withDataLookbackLimit(DATA_LOOKBACK_LIMIT);
        builder.withSchemaDetail(null);
        builder.withReexportLateData(false);
        final com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition complexKpiDefinitionApi = builder.build();

        final List<com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition> actual = objectUnderTest.convertKpiDefinitions(Arrays.asList(simpleKpiDefinitionSpark, complexKpiDefinitionSpark));

        assertThat(actual).hasSize(2).containsExactlyInAnyOrder(simpleKpiDefinitionApi, complexKpiDefinitionApi);
    }
}