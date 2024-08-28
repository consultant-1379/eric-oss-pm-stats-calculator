/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collection;
import java.util.List;

import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.ReadinessLogResponse;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;
import com.ericsson.oss.air.pm.stats.service.validator.retention.RetentionPeriodManager;
import com.ericsson.oss.air.pm.stats.service.validator.retention.RetentionPeriodMemoizer;
import com.ericsson.oss.air.rest.output.KpiDefinitionUpdateResponse;
import com.ericsson.oss.air.rest.output.KpiDefinitionsResponse;
import com.ericsson.oss.air.rest.test_utils.KpiDefinitionsResponseObjectBuilder;

import kpi.model.ondemand.OnDemandParameter;
import kpi.model.ondemand.OnDemandParameter.OnDemandParameterBuilder;
import kpi.model.ondemand.OnDemandTabularParameter;
import kpi.model.ondemand.OnDemandTabularParameter.OnDemandTabularParameterBuilder;
import kpi.model.ondemand.ParameterType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EntityMapperTest {
    @Mock RetentionPeriodManager retentionPeriodManagerMock;

    @InjectMocks EntityMapper objectUnderTest;

    @Test
    void shouldMapReadinessLog() {
        final ReadinessLogResponse actual = objectUnderTest.mapReadinessLog(readinessLog("datasource", 10L, testTime(10, 30), testTime(10, 45)));

        assertThat(actual).isEqualTo(readinessLogResponse("datasource", 10L, testTime(10, 30), testTime(10, 45)));
    }

    @Test
    void shouldMapReadinessLogs() {
        final Collection<ReadinessLogResponse> actual = objectUnderTest.mapReadinessLogs(List.of(
                readinessLog("datasource-1", 10L, testTime(10, 30), testTime(10, 45)),
                readinessLog("datasource-2", 15L, testTime(10, 45), testTime(11, 0))
        ));

        assertThat(actual).containsExactlyInAnyOrder(
                readinessLogResponse("datasource-1", 10L, testTime(10, 30), testTime(10, 45)),
                readinessLogResponse("datasource-2", 15L, testTime(10, 45), testTime(11, 0))
        );
    }

    @Test
    void shouldMapKpiDefinitionResponse() {
        final KpiDefinitionEntity simpleKpiDefinition1 = KpiDefinitionsResponseObjectBuilder.simpleKpiDefinitionEntity(
                "simple_one",
                "alias_simple_table_1");

        final KpiDefinitionEntity simpleKpiDefinition2 = KpiDefinitionsResponseObjectBuilder.simpleKpiDefinitionEntity(
                "simple_two",
                "alias_simple_table_1");

        final KpiDefinitionEntity simpleKpiDefinition3 = KpiDefinitionsResponseObjectBuilder.simpleKpiDefinitionEntity(
                "simple_three",
                "alias_simple_table_2");

        final KpiDefinitionEntity onDemandKpiDefinition1 = KpiDefinitionsResponseObjectBuilder.onDemandKpiDefinitionEntity(
                "on_demand_one",
                "alias_on_demand_table_1");

        final KpiDefinitionEntity onDemandKpiDefinition2 = KpiDefinitionsResponseObjectBuilder.onDemandKpiDefinitionEntity(
                "on_demand_two",
                "alias_on_demand_table_1");

        final KpiDefinitionEntity complexKpiDefinition1 = KpiDefinitionsResponseObjectBuilder.complexKpiDefinitionEntity(
                "complex_one",
                "alias_complex_table_1");

        final KpiDefinitionEntity complexKpiDefinition2 = KpiDefinitionsResponseObjectBuilder.complexKpiDefinitionEntity(
                "complex_two",
                "alias_complex_table_1");

        final List<OnDemandTabularParameter> onDemandTabularParameters = List.of(onDemandTabularParameter("cell_configuration", List.of(
                onDemandParameter("target_throughput_r", ParameterType.DOUBLE),
                onDemandParameter("min_rops_for_reliability", ParameterType.INTEGER))
        ));

        final List<OnDemandParameter> onDemandParameters = List.of(onDemandParameter("execution_id", ParameterType.STRING));

        final RetentionPeriodMemoizer retentionPeriodMemoizerMock = mock(RetentionPeriodMemoizer.class);

        when(retentionPeriodManagerMock.retentionPeriodMemoizer()).thenReturn(retentionPeriodMemoizerMock);
        when(retentionPeriodMemoizerMock.computeRetentionPeriod(any(KpiDefinitionEntity.class))).thenReturn(5);

        final KpiDefinitionsResponse actual = objectUnderTest.mapToKpiDefinitionResponse(List.of(
                simpleKpiDefinition1,
                simpleKpiDefinition2,
                simpleKpiDefinition3,
                onDemandKpiDefinition1,
                onDemandKpiDefinition2,
                complexKpiDefinition1,
                complexKpiDefinition2
        ), onDemandTabularParameters, onDemandParameters);

        assertThat(actual.getOnDemand().getKpiOutputTables()).satisfiesExactly(table -> {
            assertThat(table.getRetentionPeriodInDays()).isEqualTo(5);
            assertThat(table.getAggregationPeriod()).isEqualTo(60);
            assertThat(table.getAlias()).isEqualTo("alias_on_demand_table_1");
            assertThat(table.getKpiDefinitions()).satisfiesExactly(
                    def -> KpiDefinitionsResponseObjectBuilder.assertOnDemandDefinition(def, "on_demand_one"),
                    def -> KpiDefinitionsResponseObjectBuilder.assertOnDemandDefinition(def, "on_demand_two"));
        });

        assertThat(actual.getOnDemand().getParameters()).satisfiesExactly(parameters -> {
            assertThat(parameters.getName()).isEqualTo("execution_id");
            assertThat(parameters.getType()).isEqualTo("STRING");
        });

        assertThat(actual.getOnDemand().getTabularParameters()).satisfiesExactly(tabularParameters -> {
            assertThat(tabularParameters.getName()).isEqualTo("cell_configuration");
            assertThat(tabularParameters.getColumns()).hasSize(2);

            assertThat(tabularParameters.getColumns().get(0).getName()).isEqualTo("target_throughput_r");
            assertThat(tabularParameters.getColumns().get(0).getType()).isEqualTo("DOUBLE");
            assertThat(tabularParameters.getColumns().get(1).getName()).isEqualTo("min_rops_for_reliability");
            assertThat(tabularParameters.getColumns().get(1).getType()).isEqualTo("INTEGER");

        });

        assertThat(actual.getScheduledComplex().getKpiOutputTables()).satisfiesExactly(table -> {
            assertThat(table.getRetentionPeriodInDays()).isEqualTo(5);
            assertThat(table.getAggregationPeriod()).isEqualTo(60);
            assertThat(table.getAlias()).isEqualTo("alias_complex_table_1");
            assertThat(table.getKpiDefinitions()).satisfiesExactly(
                    def -> KpiDefinitionsResponseObjectBuilder.assertComplexDefinition(def, "complex_one"),
                    def -> KpiDefinitionsResponseObjectBuilder.assertComplexDefinition(def, "complex_two"));
        });

        assertThat(actual.getScheduledSimple().getKpiOutputTables()).satisfiesExactly(
                table -> {
                    assertThat(table.getRetentionPeriodInDays()).isEqualTo(5);
                    assertThat(table.getAggregationPeriod()).isEqualTo(60);
                    assertThat(table.getAlias()).isEqualTo("alias_simple_table_1");
                    assertThat(table.getKpiDefinitions()).satisfiesExactly(
                            def -> KpiDefinitionsResponseObjectBuilder.assertSimpleDefinition(def, "simple_one"),
                            def -> KpiDefinitionsResponseObjectBuilder.assertSimpleDefinition(def, "simple_two"));
                },
                table -> {
                    assertThat(table.getAggregationPeriod()).isEqualTo(60);
                    assertThat(table.getAlias()).isEqualTo("alias_simple_table_2");
                    assertThat(table.getKpiDefinitions()).satisfiesExactly(
                            def -> KpiDefinitionsResponseObjectBuilder.assertSimpleDefinition(def, "simple_three"));
                });
    }

    @Test
    void shouldMapKpiDefinitionUpdateResponse_whenEntityIsSimple() {
        KpiDefinitionEntity simpleEntity = KpiDefinitionsResponseObjectBuilder.simpleKpiDefinitionEntity(
                "simple_one",
                "alias_simple_table_one");

        KpiDefinitionUpdateResponse actual = objectUnderTest.mapToKpiDefinitionUpdateResponse(simpleEntity);
        KpiDefinitionsResponseObjectBuilder.assertSimpleKpiDefinitionUpdateResponse(actual, "simple_one", "alias_simple_table_one");
    }

    @Test
    void shouldMapKpiDefinitionUpdateResponse_whenEntityIsComplex() {
        KpiDefinitionEntity complexEntity = KpiDefinitionsResponseObjectBuilder.complexKpiDefinitionEntity(
                "complex_one",
                "alias_complex_table_one");

        KpiDefinitionUpdateResponse actual = objectUnderTest.mapToKpiDefinitionUpdateResponse(complexEntity);
        KpiDefinitionsResponseObjectBuilder.assertComplexKpiDefinitionUpdateResponse(actual, "complex_one", "alias_complex_table_one");
    }

    @Test
    void shouldMapKpiDefinitionUpdateResponse_whenEntityIsOnDemand() {
        KpiDefinitionEntity onDemandEntity = KpiDefinitionsResponseObjectBuilder.onDemandKpiDefinitionEntity(
                "onDemand_one",
                "alias_onDemand_table_one");

        KpiDefinitionUpdateResponse actual = objectUnderTest.mapToKpiDefinitionUpdateResponse(onDemandEntity);
        KpiDefinitionsResponseObjectBuilder.assertOnDemandKpiDefinitionUpdateResponse(actual, "onDemand_one", "alias_onDemand_table_one");
    }

    static OnDemandTabularParameter onDemandTabularParameter(final String name, final List<OnDemandParameter> columns) {
        final OnDemandTabularParameterBuilder builder = OnDemandTabularParameter.builder();
        return builder.name(name).columns(columns).build();
    }

    static OnDemandParameter onDemandParameter(final String name, final ParameterType type) {
        final OnDemandParameterBuilder builder = OnDemandParameter.builder();
        builder.name(name);
        builder.type(type);
        return builder .build();
    }

    static LocalDateTime testTime(final int hour, final int minute) {
        return LocalDateTime.of(2_022, Month.DECEMBER, 3, hour, minute);
    }

    static ReadinessLogResponse readinessLogResponse(final String datasource,
                                                     final Long collectedRowsCount,
                                                     final LocalDateTime earliestCollectedData,
                                                     final LocalDateTime latestCollectedData) {
        return ReadinessLogResponse.builder()
                .withDatasource(datasource)
                .withCollectedRowsCount(collectedRowsCount)
                .withEarliestCollectedData(earliestCollectedData)
                .withLatestCollectedData(latestCollectedData)
                .build();
    }

    static ReadinessLog readinessLog(final String datasource,
                                     final Long collectedRowsCount,
                                     final LocalDateTime earliestCollectedData,
                                     final LocalDateTime latestCollectedData) {
        return ReadinessLog.builder()
                .withDatasource(datasource)
                .withCollectedRowsCount(collectedRowsCount)
                .withEarliestCollectedData(earliestCollectedData)
                .withLatestCollectedData(latestCollectedData)
                .build();
    }
}