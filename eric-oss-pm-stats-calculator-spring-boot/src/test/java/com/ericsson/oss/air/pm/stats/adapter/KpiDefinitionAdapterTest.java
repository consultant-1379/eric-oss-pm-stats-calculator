/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.adapter;

import static com.ericsson.oss.air.pm.stats._helper.Mapper.toAggregationElements;
import static com.ericsson.oss.air.pm.stats._helper.Mapper.toComplexAggregationElements;
import static com.ericsson.oss.air.pm.stats._helper.Mapper.toComplexFilterElements;
import static com.ericsson.oss.air.pm.stats._helper.Mapper.toFilterElements;
import static com.ericsson.oss.air.pm.stats._helper.Mapper.toOnDemandAggregationElements;
import static com.ericsson.oss.air.pm.stats._helper.Mapper.toOnDemandFilterElements;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.ericsson.oss.air.pm.stats.cache.SchemaDetailCache;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;

import kpi.model.KpiDefinitionRequest;
import kpi.model.KpiDefinitionRequest.KpiDefinitionRequestBuilder;
import kpi.model.OnDemand;
import kpi.model.OnDemand.OnDemandBuilder;
import kpi.model.RetentionPeriod;
import kpi.model.ScheduledComplex;
import kpi.model.ScheduledComplex.ScheduledComplexBuilder;
import kpi.model.ScheduledSimple;
import kpi.model.ScheduledSimple.ScheduledSimpleBuilder;
import kpi.model.api.enumeration.AggregationType;
import kpi.model.api.table.definition.ComplexKpiDefinitions;
import kpi.model.api.table.definition.ComplexKpiDefinitions.ComplexKpiDefinition;
import kpi.model.api.table.definition.ComplexKpiDefinitions.ComplexKpiDefinition.ComplexKpiDefinitionBuilder;
import kpi.model.api.table.definition.OnDemandKpiDefinitions;
import kpi.model.api.table.definition.OnDemandKpiDefinitions.OnDemandKpiDefinition;
import kpi.model.api.table.definition.OnDemandKpiDefinitions.OnDemandKpiDefinition.OnDemandKpiDefinitionBuilder;
import kpi.model.api.table.definition.SimpleKpiDefinitions;
import kpi.model.api.table.definition.SimpleKpiDefinitions.SimpleKpiDefinition;
import kpi.model.api.table.definition.SimpleKpiDefinitions.SimpleKpiDefinition.SimpleKpiDefinitionBuilder;
import kpi.model.complex.ComplexTable;
import kpi.model.complex.ComplexTable.ComplexTableBuilder;
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
import kpi.model.ondemand.OnDemandTable.OnDemandTableBuilder;
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
import kpi.model.simple.SimpleTable.SimpleTableBuilder;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KpiDefinitionAdapterTest {
    @Mock
    SchemaDetailCache schemaDetailCacheMock;

    @InjectMocks
    KpiDefinitionAdapter objectUnderTest;


    @Test
    void shouldMapToEntity() {
        final OnDemandKpiDefinitionBuilder onDemandKpiDefinitionBuilder = OnDemandKpiDefinition.builder();
        onDemandKpiDefinitionBuilder.name(OnDemandDefinitionName.of("on_demand_definition"));
        onDemandKpiDefinitionBuilder.expression(OnDemandDefinitionExpression.of("FROM expression_1"));
        onDemandKpiDefinitionBuilder.objectType(OnDemandDefinitionObjectType.of("INTEGER"));
        onDemandKpiDefinitionBuilder.aggregationType(OnDemandDefinitionAggregationType.of(AggregationType.SUM));
        onDemandKpiDefinitionBuilder.aggregationElements(
                OnDemandDefinitionAggregationElements.of(toOnDemandAggregationElements("table.column1", "table.column2")));
        onDemandKpiDefinitionBuilder.exportable(OnDemandDefinitionExportable.of(true));
        onDemandKpiDefinitionBuilder.filters(OnDemandDefinitionFilters.of(toOnDemandFilterElements("filter_1", "filter_2")));

        final OnDemandTableBuilder onDemandTableBuilder = OnDemandTable.builder();
        onDemandTableBuilder.aggregationPeriod(OnDemandTableAggregationPeriod.of(60));
        onDemandTableBuilder.alias(OnDemandTableAlias.of("alias_ondemand"));
        onDemandTableBuilder.aggregationElements(OnDemandTableAggregationElements.of(toOnDemandAggregationElements("table.column1", "table.column2")));
        onDemandTableBuilder.exportable(OnDemandTableExportable.of(true));
        onDemandTableBuilder.kpiDefinitions(OnDemandKpiDefinitions.of(List.of(onDemandKpiDefinitionBuilder.build())));

        final ComplexKpiDefinitionBuilder complexKpiDefinitionBuilder = ComplexKpiDefinition.builder();
        complexKpiDefinitionBuilder.name(ComplexDefinitionName.of("complex_definition"));
        complexKpiDefinitionBuilder.expression(ComplexDefinitionExpression.of("FROM expression_1"));
        complexKpiDefinitionBuilder.objectType(ComplexDefinitionObjectType.of("INTEGER"));
        complexKpiDefinitionBuilder.aggregationType(ComplexDefinitionAggregationType.of(AggregationType.SUM));
        complexKpiDefinitionBuilder.executionGroup(ComplexDefinitionExecutionGroup.of("execution_group"));
        complexKpiDefinitionBuilder.aggregationElements(
                ComplexDefinitionAggregationElements.of(toComplexAggregationElements("table.column1", "table.column2")));
        complexKpiDefinitionBuilder.exportable(ComplexDefinitionExportable.of(true));
        complexKpiDefinitionBuilder.filters(ComplexDefinitionFilters.of(toComplexFilterElements("filter_1", "filter_2")));
        complexKpiDefinitionBuilder.dataReliabilityOffset(ComplexDefinitionDataReliabilityOffset.of(50));
        complexKpiDefinitionBuilder.dataLookBackLimit(ComplexDefinitionDataLookBackLimit.of(200));
        complexKpiDefinitionBuilder.reexportLateData(ComplexDefinitionReexportLateData.of(true));

        final ComplexTableBuilder complexTableBuilder = ComplexTable.builder();
        complexTableBuilder.aggregationPeriod(ComplexTableAggregationPeriod.of(60));
        complexTableBuilder.alias(ComplexTableAlias.of("alias"));
        complexTableBuilder.aggregationElements(ComplexTableAggregationElements.of(toComplexAggregationElements("table.column1", "table.column2")));
        complexTableBuilder.exportable(ComplexTableExportable.of(true));
        complexTableBuilder.dataReliabilityOffset(ComplexTableDataReliabilityOffset.of(100));
        complexTableBuilder.dataLookBackLimit(ComplexTableDataLookBackLimit.of(200));
        complexTableBuilder.reexportLateData(ComplexTableReexportLateData.of(true));
        complexTableBuilder.kpiDefinitions(ComplexKpiDefinitions.of(List.of(complexKpiDefinitionBuilder.build())));

        final SimpleKpiDefinitionBuilder simpleKpiDefinitionBuilder = SimpleKpiDefinition.builder();
        simpleKpiDefinitionBuilder.name(SimpleDefinitionName.of("simple_definition"));
        simpleKpiDefinitionBuilder.expression(SimpleDefinitionExpression.of("expression_1"));
        simpleKpiDefinitionBuilder.objectType(SimpleDefinitionObjectType.of("INTEGER"));
        simpleKpiDefinitionBuilder.aggregationType(SimpleDefinitionAggregationType.of(AggregationType.SUM));
        simpleKpiDefinitionBuilder.aggregationElements(SimpleDefinitionAggregationElements.of(toAggregationElements("table.column1", "table.column2")));
        simpleKpiDefinitionBuilder.exportable(SimpleDefinitionExportable.of(false));
        simpleKpiDefinitionBuilder.filters(SimpleDefinitionFilters.of(toFilterElements("filter_1", "filter_2")));
        simpleKpiDefinitionBuilder.inpDataIdentifier(SimpleDefinitionInpDataIdentifier.of("dataSpace|category|schema"));
        simpleKpiDefinitionBuilder.dataReliabilityOffset(SimpleDefinitionDataReliabilityOffset.of(50));
        simpleKpiDefinitionBuilder.dataLookBackLimit(SimpleDefinitionDataLookBackLimit.of(200));
        simpleKpiDefinitionBuilder.reexportLateData(SimpleDefinitionReexportLateData.of(true));

        final SimpleTableInpDataIdentifier simpleTableInpDataIdentifier = SimpleTableInpDataIdentifier.of("dataSpace|category|schema");
        final SimpleTableBuilder simpleTableBuilder = SimpleTable.builder();
        simpleTableBuilder.aggregationPeriod(SimpleTableAggregationPeriod.of(60));
        simpleTableBuilder.alias(SimpleTableAlias.of("alias"));
        simpleTableBuilder.aggregationElements(SimpleTableAggregationElements.of(toAggregationElements("table.column1", "table.column2")));
        simpleTableBuilder.exportable(SimpleTableExportable.of(false));
        simpleTableBuilder.inpDataIdentifier(simpleTableInpDataIdentifier);
        simpleTableBuilder.dataReliabilityOffset(SimpleTableDataReliabilityOffset.of(50));
        simpleTableBuilder.dataLookBackLimit(SimpleTableDataLookBackLimit.of(200));
        simpleTableBuilder.reexportLateData(SimpleTableReexportLateData.of(true));
        simpleTableBuilder.kpiDefinitions(SimpleKpiDefinitions.of(List.of(simpleKpiDefinitionBuilder.build())));

        final OnDemandBuilder onDemandBuilder = OnDemand.builder();
        onDemandBuilder.kpiOutputTables(List.of(onDemandTableBuilder.build()));

        final ScheduledComplexBuilder scheduledComplexBuilder = ScheduledComplex.builder();
        scheduledComplexBuilder.kpiOutputTables(List.of(complexTableBuilder.build()));

        final ScheduledSimpleBuilder scheduledSimpleBuilder = ScheduledSimple.builder();
        scheduledSimpleBuilder.kpiOutputTables(List.of(simpleTableBuilder.build()));

        final KpiDefinitionRequestBuilder kpiDefinitionBuilder = KpiDefinitionRequest.builder();
        kpiDefinitionBuilder.onDemand(onDemandBuilder.build());
        kpiDefinitionBuilder.scheduledComplex(scheduledComplexBuilder.build());
        kpiDefinitionBuilder.scheduledSimple(scheduledSimpleBuilder.build());
        kpiDefinitionBuilder.retentionPeriod(RetentionPeriod.of(null));

        final KpiDefinitionRequest kpiDefinition = kpiDefinitionBuilder.build();

        final SchemaDetail schemaDetail = SchemaDetail.builder().build();
        when(schemaDetailCacheMock.get(simpleTableInpDataIdentifier)).thenReturn(schemaDetail);

        final List<KpiDefinitionEntity> actual = objectUnderTest.toListOfEntities(kpiDefinition);

        verify(schemaDetailCacheMock).get(simpleTableInpDataIdentifier);

        assertThat(actual).satisfiesExactlyInAnyOrder(onDemandDefinition -> {
            assertThat(onDemandDefinition.name()).isEqualTo("on_demand_definition");
            assertThat(onDemandDefinition.alias()).isEqualTo("alias_ondemand");
            assertThat(onDemandDefinition.aggregationPeriod()).isEqualTo(60);
            assertThat(onDemandDefinition.expression()).isEqualTo("FROM expression_1");
            assertThat(onDemandDefinition.objectType()).isEqualTo("INTEGER");
            assertThat(onDemandDefinition.aggregationType()).isEqualTo(AggregationType.SUM.name());
            assertThat(onDemandDefinition.aggregationElements()).isEqualTo(List.of("table.column1", "table.column2"));
            assertThat(onDemandDefinition.exportable()).isTrue();
            assertThat(onDemandDefinition.filters()).isEqualTo(List.of("filter_1","filter_2"));
        }, complexDefinition -> {
            assertThat(complexDefinition.name()).isEqualTo("complex_definition");
            assertThat(complexDefinition.alias()).isEqualTo("alias");
            assertThat(complexDefinition.aggregationPeriod()).isEqualTo(60);
            assertThat(complexDefinition.expression()).isEqualTo("FROM expression_1");
            assertThat(complexDefinition.objectType()).isEqualTo("INTEGER");
            assertThat(complexDefinition.aggregationType()).isEqualTo(AggregationType.SUM.name());
            assertThat(complexDefinition.aggregationElements()).isEqualTo(List.of("table.column1", "table.column2"));
            assertThat(complexDefinition.exportable()).isTrue();
            assertThat(complexDefinition.executionGroup().name()).isEqualTo("execution_group");
            assertThat(complexDefinition.filters()).isEqualTo(List.of("filter_1", "filter_2"));
            assertThat(complexDefinition.dataReliabilityOffset()).isEqualTo(50);
            assertThat(complexDefinition.dataLookbackLimit()).isEqualTo(200);
            assertThat(complexDefinition.reexportLateData()).isTrue();
        }, simpleDefinition -> {
            assertThat(simpleDefinition.name()).isEqualTo("simple_definition");
            assertThat(simpleDefinition.alias()).isEqualTo("alias");
            assertThat(simpleDefinition.aggregationPeriod()).isEqualTo(60);
            assertThat(simpleDefinition.expression()).isEqualTo("expression_1");
            assertThat(simpleDefinition.objectType()).isEqualTo("INTEGER");
            assertThat(simpleDefinition.aggregationType()).isEqualTo(AggregationType.SUM.name());
            assertThat(simpleDefinition.aggregationElements()).isEqualTo(List.of("table.column1", "table.column2"));
            assertThat(simpleDefinition.exportable()).isFalse();
            assertThat(simpleDefinition.filters()).isEqualTo(List.of("filter_1", "filter_2"));
            assertThat(simpleDefinition.dataReliabilityOffset()).isEqualTo(50);
            assertThat(simpleDefinition.dataLookbackLimit()).isEqualTo(200);
            assertThat(simpleDefinition.reexportLateData()).isTrue();
            assertThat(simpleDefinition.schemaDataSpace()).isEqualTo("dataSpace");
            assertThat(simpleDefinition.schemaCategory()).isEqualTo("category");
            assertThat(simpleDefinition.schemaName()).isEqualTo("schema");
            assertThat(simpleDefinition.schemaDetail()).isEqualTo(schemaDetail);
        });
    }
}