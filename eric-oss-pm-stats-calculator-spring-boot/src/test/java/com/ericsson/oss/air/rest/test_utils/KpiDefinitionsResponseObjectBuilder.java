/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.test_utils;

import java.util.List;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.rest.output.KpiDefinitionDto;
import com.ericsson.oss.air.rest.output.KpiDefinitionUpdateResponse;
import com.ericsson.oss.air.rest.output.KpiDefinitionsResponse.ComplexKpiDefinitionDto;
import com.ericsson.oss.air.rest.output.KpiDefinitionsResponse.OnDemandKpiDefinitionDto;
import com.ericsson.oss.air.rest.output.KpiDefinitionsResponse.SimpleKpiDefinitionDto;
import com.ericsson.oss.air.rest.output.KpiDefinitionsResponse.TableDto;

import org.assertj.core.api.Assertions;

public final class KpiDefinitionsResponseObjectBuilder {
    public static final UUID  COLLECTION_ID = UUID.fromString("29dc1bbf-7cdf-421b-8fc9-e363889ada79");
    public static final String EXPRESSION = "EXPRESSION";
    public static final List<String> AGGREGATION_ELEMENTS = List.of("table.column1", "table.column2");
    public static final String OBJECT_TYPE = "INTEGER";
    public static final String AGGREGATION_TYPE = "SUM";
    public static final int AGGREGATION_PERIOD = 60;
    public static final boolean EXPORTABLE = true;
    public static final boolean REEXPORT_LATE_DATA = true;
    public static final String SCHEMA_DATA_SPACE = "dataSpace";
    public static final String SCHEMA_CATEGORY = "category";
    public static final String SCHEMA_NAME = "name";
    public static final String INPUT_DATA_IDENTIFIER = String.format("%s|%s|%s", SCHEMA_DATA_SPACE, SCHEMA_CATEGORY, SCHEMA_NAME);
    public static final int DATA_RELIABILITY_OFFSET = 1;
    public static final int DATA_LOOKBACK_LIMIT = 1;
    public static final int RETENTION_PERIOD_IN_DAYS = 7;
    public static final List<String> FILTERS = List.of("f1", "f2");
    public static final ExecutionGroup EXECUTION_GROUP = ExecutionGroup.builder().withId(1).withName("name").build();

    public static KpiDefinitionEntity simpleKpiDefinitionEntity(final String name, final String alias) {
        return scheduledEntityBuilder(name, alias)
                .withSchemaDataSpace(SCHEMA_DATA_SPACE)
                .withSchemaCategory(SCHEMA_CATEGORY)
                .withSchemaName(SCHEMA_NAME)
                .build();
    }

    public static KpiDefinitionEntity complexKpiDefinitionEntity(final String name, final String alias) {
        return scheduledEntityBuilder(name, alias)
                .build();
    }

    public static KpiDefinitionEntity onDemandKpiDefinitionEntity(final String name, final String alias) {
        return entityBuilder(name, alias)
                .build();
    }

    public static TableDto tableDto(final String alias, final List<KpiDefinitionDto> kpiDefinitions) {
        return new TableDto(alias, AGGREGATION_PERIOD, RETENTION_PERIOD_IN_DAYS, kpiDefinitions);
    }

    public static SimpleKpiDefinitionDto simpleKpiDefinitionDto(final String name) {
        return SimpleKpiDefinitionDto.builder()
                                     .name(name)
                                     .expression(EXPRESSION)
                                     .objectType(OBJECT_TYPE)
                                     .aggregationType(AGGREGATION_TYPE)
                                     .aggregationElements(AGGREGATION_ELEMENTS)
                                     .exportable(EXPORTABLE)
                                     .inpDataIdentifier(INPUT_DATA_IDENTIFIER)
                                     .dataReliabilityOffset(DATA_RELIABILITY_OFFSET)
                                     .dataLookbackLimit(DATA_LOOKBACK_LIMIT)
                                     .filters(FILTERS)
                                     .reexportLateData(REEXPORT_LATE_DATA)
                                     .build();
    }

    public static ComplexKpiDefinitionDto complexKpiDefinitionDto(final String name) {
        return ComplexKpiDefinitionDto.builder()
                                      .name(name)
                                      .expression(EXPRESSION)
                                      .objectType(OBJECT_TYPE)
                                      .aggregationType(AGGREGATION_TYPE)
                                      .aggregationElements(AGGREGATION_ELEMENTS)
                                      .exportable(EXPORTABLE)
                                      .dataReliabilityOffset(DATA_RELIABILITY_OFFSET)
                                      .dataLookbackLimit(DATA_LOOKBACK_LIMIT)
                                      .filters(FILTERS)
                                      .reexportLateData(REEXPORT_LATE_DATA)
                                      .executionGroup(EXECUTION_GROUP.name())
                                      .build();
    }

    public static OnDemandKpiDefinitionDto onDemandKpiDefinitionDto(final String name) {
        return OnDemandKpiDefinitionDto.builder()
                                       .name(name)
                                       .expression(EXPRESSION)
                                       .objectType(OBJECT_TYPE)
                                       .aggregationType(AGGREGATION_TYPE)
                                       .aggregationElements(AGGREGATION_ELEMENTS)
                                       .exportable(EXPORTABLE)
                                       .filters(FILTERS)
                                       .build();
    }

    private static KpiDefinitionEntity.KpiDefinitionEntityBuilder entityBuilder(final String name, final String alias) {
        return KpiDefinitionEntity.builder()
                                  .withCollectionId(COLLECTION_ID)
                                  .withName(name)
                                  .withAlias(alias)
                                  .withExpression(EXPRESSION)
                                  .withObjectType(OBJECT_TYPE)
                                  .withAggregationType(AGGREGATION_TYPE)
                                  .withAggregationPeriod(AGGREGATION_PERIOD)
                                  .withAggregationElements(AGGREGATION_ELEMENTS)
                                  .withExportable(EXPORTABLE)
                                  .withFilters(FILTERS);
    }

    private static KpiDefinitionEntity.KpiDefinitionEntityBuilder scheduledEntityBuilder(final String name, final String alias) {
        return entityBuilder(name, alias)
                .withDataReliabilityOffset(DATA_RELIABILITY_OFFSET)
                .withDataLookbackLimit(DATA_LOOKBACK_LIMIT)
                .withReexportLateData(REEXPORT_LATE_DATA)
                .withExecutionGroup(EXECUTION_GROUP);
    }

    public static void assertSimpleDefinition(final KpiDefinitionDto simpleKpiDefinitionDto, final String name) {
        Assertions.assertThat(simpleKpiDefinitionDto.name()).isEqualTo(name);
        Assertions.assertThat(simpleKpiDefinitionDto.expression()).isEqualTo(EXPRESSION);
        Assertions.assertThat(simpleKpiDefinitionDto.objectType()).isEqualTo(OBJECT_TYPE);
        Assertions.assertThat(simpleKpiDefinitionDto.aggregationType()).isEqualTo(AGGREGATION_TYPE);
        Assertions.assertThat(simpleKpiDefinitionDto.aggregationElements()).isEqualTo(AGGREGATION_ELEMENTS);
        Assertions.assertThat(simpleKpiDefinitionDto.exportable()).isEqualTo(EXPORTABLE);
        Assertions.assertThat(simpleKpiDefinitionDto.filters()).isEqualTo(FILTERS);
        Assertions.assertThat(simpleKpiDefinitionDto.reexportLateData()).isEqualTo(REEXPORT_LATE_DATA);
        Assertions.assertThat(simpleKpiDefinitionDto.dataReliabilityOffset()).isEqualTo(DATA_RELIABILITY_OFFSET);
        Assertions.assertThat(simpleKpiDefinitionDto.dataLookbackLimit()).isEqualTo(DATA_LOOKBACK_LIMIT);
        Assertions.assertThat(simpleKpiDefinitionDto.inpDataIdentifier()).isEqualTo(INPUT_DATA_IDENTIFIER);
    }

    public static void assertComplexDefinition(final KpiDefinitionDto complexKpiDefinitionDto, final String name) {
        Assertions.assertThat(complexKpiDefinitionDto.name()).isEqualTo(name);
        Assertions.assertThat(complexKpiDefinitionDto.expression()).isEqualTo(EXPRESSION);
        Assertions.assertThat(complexKpiDefinitionDto.objectType()).isEqualTo(OBJECT_TYPE);
        Assertions.assertThat(complexKpiDefinitionDto.aggregationType()).isEqualTo(AGGREGATION_TYPE);
        Assertions.assertThat(complexKpiDefinitionDto.aggregationElements()).isEqualTo(AGGREGATION_ELEMENTS);
        Assertions.assertThat(complexKpiDefinitionDto.exportable()).isEqualTo(EXPORTABLE);
        Assertions.assertThat(complexKpiDefinitionDto.filters()).isEqualTo(FILTERS);
        Assertions.assertThat(complexKpiDefinitionDto.reexportLateData()).isEqualTo(REEXPORT_LATE_DATA);
        Assertions.assertThat(complexKpiDefinitionDto.dataReliabilityOffset()).isEqualTo(DATA_RELIABILITY_OFFSET);
        Assertions.assertThat(complexKpiDefinitionDto.dataLookbackLimit()).isEqualTo(DATA_LOOKBACK_LIMIT);
        Assertions.assertThat(complexKpiDefinitionDto.executionGroup()).isEqualTo("name");
    }

    public static void assertOnDemandDefinition(final KpiDefinitionDto onDemandKpiDefinitionDto, final String name) {
        Assertions.assertThat(onDemandKpiDefinitionDto.name()).isEqualTo(name);
        Assertions.assertThat(onDemandKpiDefinitionDto.expression()).isEqualTo(EXPRESSION);
        Assertions.assertThat(onDemandKpiDefinitionDto.objectType()).isEqualTo(OBJECT_TYPE);
        Assertions.assertThat(onDemandKpiDefinitionDto.aggregationType()).isEqualTo(AGGREGATION_TYPE);
        Assertions.assertThat(onDemandKpiDefinitionDto.aggregationElements()).isEqualTo(AGGREGATION_ELEMENTS);
        Assertions.assertThat(onDemandKpiDefinitionDto.exportable()).isEqualTo(EXPORTABLE);
        Assertions.assertThat(onDemandKpiDefinitionDto.filters()).isEqualTo(FILTERS);
    }

    public static void assertSimpleKpiDefinitionUpdateResponse(final KpiDefinitionUpdateResponse kpiDefinitionUpdateResponse,
                                                               final String name,
                                                               final String alias) {
        Assertions.assertThat(kpiDefinitionUpdateResponse.getName()).isEqualTo(name);
        Assertions.assertThat(kpiDefinitionUpdateResponse.getAlias()).isEqualTo(alias);
        Assertions.assertThat(kpiDefinitionUpdateResponse.getAggregationPeriod()).isEqualTo(AGGREGATION_PERIOD);
        Assertions.assertThat(kpiDefinitionUpdateResponse.getExpression()).isEqualTo(EXPRESSION);
        Assertions.assertThat(kpiDefinitionUpdateResponse.getObjectType()).isEqualTo(OBJECT_TYPE);
        Assertions.assertThat(kpiDefinitionUpdateResponse.getAggregationType()).isEqualTo(AGGREGATION_TYPE);
        Assertions.assertThat(kpiDefinitionUpdateResponse.getAggregationElements()).isEqualTo(AGGREGATION_ELEMENTS);
        Assertions.assertThat(kpiDefinitionUpdateResponse.getFilters()).isEqualTo(FILTERS);
        Assertions.assertThat(kpiDefinitionUpdateResponse.getDataReliabilityOffset()).isEqualTo(DATA_RELIABILITY_OFFSET);
        Assertions.assertThat(kpiDefinitionUpdateResponse.getDataLookbackLimit()).isEqualTo(DATA_LOOKBACK_LIMIT);
        Assertions.assertThat(kpiDefinitionUpdateResponse.getInpDataIdentifier()).isEqualTo(INPUT_DATA_IDENTIFIER);
        Assertions.assertThat(kpiDefinitionUpdateResponse.getReexportLateData()).isTrue();
        Assertions.assertThat(kpiDefinitionUpdateResponse.getExportable()).isTrue();
    }

    public static void assertComplexKpiDefinitionUpdateResponse(final KpiDefinitionUpdateResponse kpiDefinitionUpdateResponse,
                                                                final String name,
                                                                final String alias) {
        Assertions.assertThat(kpiDefinitionUpdateResponse.getName()).isEqualTo(name);
        Assertions.assertThat(kpiDefinitionUpdateResponse.getAlias()).isEqualTo(alias);
        Assertions.assertThat(kpiDefinitionUpdateResponse.getAggregationPeriod()).isEqualTo(AGGREGATION_PERIOD);
        Assertions.assertThat(kpiDefinitionUpdateResponse.getExpression()).isEqualTo(EXPRESSION);
        Assertions.assertThat(kpiDefinitionUpdateResponse.getObjectType()).isEqualTo(OBJECT_TYPE);
        Assertions.assertThat(kpiDefinitionUpdateResponse.getAggregationType()).isEqualTo(AGGREGATION_TYPE);
        Assertions.assertThat(kpiDefinitionUpdateResponse.getAggregationElements()).isEqualTo(AGGREGATION_ELEMENTS);
        Assertions.assertThat(kpiDefinitionUpdateResponse.getFilters()).isEqualTo(FILTERS);
        Assertions.assertThat(kpiDefinitionUpdateResponse.getDataReliabilityOffset()).isEqualTo(DATA_RELIABILITY_OFFSET);
        Assertions.assertThat(kpiDefinitionUpdateResponse.getDataLookbackLimit()).isEqualTo(DATA_LOOKBACK_LIMIT);
        Assertions.assertThat(kpiDefinitionUpdateResponse.getExecutionGroup()).isEqualTo(EXECUTION_GROUP.name());
        Assertions.assertThat(kpiDefinitionUpdateResponse.getReexportLateData()).isTrue();
        Assertions.assertThat(kpiDefinitionUpdateResponse.getExportable()).isTrue();
    }

    public static void assertOnDemandKpiDefinitionUpdateResponse(final KpiDefinitionUpdateResponse kpiDefinitionUpdateResponse,
                                                                 final String name,
                                                                 final String alias) {
        Assertions.assertThat(kpiDefinitionUpdateResponse.getName()).isEqualTo(name);
        Assertions.assertThat(kpiDefinitionUpdateResponse.getAlias()).isEqualTo(alias);
        Assertions.assertThat(kpiDefinitionUpdateResponse.getAggregationPeriod()).isEqualTo(AGGREGATION_PERIOD);
        Assertions.assertThat(kpiDefinitionUpdateResponse.getExpression()).isEqualTo(EXPRESSION);
        Assertions.assertThat(kpiDefinitionUpdateResponse.getObjectType()).isEqualTo(OBJECT_TYPE);
        Assertions.assertThat(kpiDefinitionUpdateResponse.getAggregationType()).isEqualTo(AGGREGATION_TYPE);
        Assertions.assertThat(kpiDefinitionUpdateResponse.getAggregationElements()).isEqualTo(AGGREGATION_ELEMENTS);
        Assertions.assertThat(kpiDefinitionUpdateResponse.getFilters()).isEqualTo(FILTERS);
        Assertions.assertThat(kpiDefinitionUpdateResponse.getExportable()).isTrue();
    }
}
