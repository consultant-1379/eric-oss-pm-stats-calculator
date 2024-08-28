/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.exporter.builder;

import static java.time.Month.SEPTEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.calculation.ReliabilityThresholdCalculator;
import com.ericsson.oss.air.pm.stats.calculation.reliability.threshold.registry.ReliabilityThresholdCalculatorRegistry;
import com.ericsson.oss.air.pm.stats.calculation.start.time.api.StartTimeCalculator;
import com.ericsson.oss.air.pm.stats.calculation.start.time.registry.StartTimeCalculatorRegistry;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.model.entity.Calculation;
import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.service.api.CalculationService;
import com.ericsson.oss.air.pm.stats.service.api.DatabaseService;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.service.exporter.model.ExecutionReport;
import com.ericsson.oss.air.pm.stats.service.exporter.model.Kpi;

import lombok.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExecutionReportBuilderFacadeTest {
    static final String EXECUTION_GROUP = "eric-data-message-bus-kf:9092|topic0|fact_table_0";
    static final LocalDateTime TEST_TIME_CREATED = LocalDateTime.of(2_022, SEPTEMBER, 1, 0, 0, 0);
    static final LocalDateTime TEST_TIME_COMPLETED = LocalDateTime.of(2_022, SEPTEMBER, 1, 1, 0, 0);
    static final long RELIABILITY_THRESHOLD = 1663761600;
    static final long START_TIME = 1663743600;
    static final UUID CALCULATION_ID = UUID.fromString("63515664-6a90-4aab-a033-053e8b4a659c");

    @Mock
    CalculationService calculationServiceMock;
    @Mock
    KpiDefinitionService kpiDefinitionServiceMock;
    @Mock
    DatabaseService databaseServiceMock;
    @Mock
    ReliabilityThresholdCalculator reliabilityThresholdCalculatorMock;
    @Mock
    ReliabilityThresholdCalculatorRegistry reliabilityThresholdCalculatorRegistryMock;
    @Mock
    StartTimeCalculator startTimeCalculatorMock;
    @Mock
    StartTimeCalculatorRegistry startTimeCalculatorRegistryMock;

    @InjectMocks
    ExecutionReportBuilderFacade objectUnderTest;

    @Nested
    class BuildExecutionReport {
        final List<KpiDefinitionEntity> kpiDefinitions = List.of(
                definition("kpi_1", "table_a", 60, true),
                definition("kpi_2", "table_a", 60, false),
                definition("kpi_3", "table_a", 1440, true),
                definition("kpi_4", "table_a", 1440, false),
                definition("kpi_5", "table_a", -1, true),
                definition("kpi_6", "table_a", -1, false)
        );

        @BeforeEach
        void setup() {
            doReturn(List.of("kpi_1", "kpi_2")).when(kpiDefinitionServiceMock).findAllKpiNamesFromSameTableAsKpiDefinition(any(KpiDefinitionEntity.class));
            doReturn(Set.of("kpi_1", "kpi_2", "kpi_3", "kpi_4", "kpi_5", "kpi_6")).when(kpiDefinitionServiceMock).findAllKpiNames();
            doReturn(List.of("kpi_1", "kpi_2", "dimension")).when(databaseServiceMock).findColumnNamesForTable(anyString());
            doReturn(kpiDefinitions.stream().collect(Collectors.toMap(KpiDefinitionEntity::name, kpi -> LocalDateTime.ofEpochSecond(
                    RELIABILITY_THRESHOLD, 0, ZoneOffset.UTC))))
                    .when(reliabilityThresholdCalculatorMock).calculateReliabilityThreshold(
                            CALCULATION_ID);
            doReturn(reliabilityThresholdCalculatorMock).when(reliabilityThresholdCalculatorRegistryMock).calculator(any(Collection.class));
            doReturn(kpiDefinitions.stream().collect(Collectors.toMap(KpiDefinitionEntity::name, kpi -> LocalDateTime.ofEpochSecond(
                    START_TIME, 0, ZoneOffset.UTC))))
                    .when(startTimeCalculatorMock).calculateStartTime(CALCULATION_ID);
            doReturn(startTimeCalculatorMock).when(startTimeCalculatorRegistryMock).calculator(any(Collection.class));
        }

        @Test
        void shouldExportOnDemandReport() {
            final Calculation calculation = calculation(CALCULATION_ID, "ON_DEMAND");

            doReturn(calculation).when(calculationServiceMock).findCalculationReadyToBeExported(CALCULATION_ID);
            doReturn(kpiDefinitions).when(kpiDefinitionServiceMock).findOnDemandKpiDefinitionsByCalculationId(CALCULATION_ID);

            final ExecutionReport actual = objectUnderTest.buildExecutionReport(CALCULATION_ID);

            verify(kpiDefinitionServiceMock).findOnDemandKpiDefinitionsByCalculationId(CALCULATION_ID);
            verifyAll();

            assertTable(false, "ON_DEMAND", actual);
        }

        @Test
        void shouldReturnCorrectlyBuiltExecutionReport() {
            final Calculation calculation = calculation(CALCULATION_ID, "COMPLEX");

            doReturn(calculation).when(calculationServiceMock).findCalculationReadyToBeExported(CALCULATION_ID);
            doReturn(kpiDefinitions).when(kpiDefinitionServiceMock).findKpiDefinitionsByExecutionGroup("COMPLEX");


            final ExecutionReport actual = objectUnderTest.buildExecutionReport(CALCULATION_ID);

            verify(kpiDefinitionServiceMock).findKpiDefinitionsByExecutionGroup("COMPLEX");
            verifyAll();
            assertTable(true, "COMPLEX", actual);
        }

        void verifyAll() {
            verify(calculationServiceMock).findCalculationReadyToBeExported(CALCULATION_ID);
            verify(kpiDefinitionServiceMock, times(3)).findAllKpiNamesFromSameTableAsKpiDefinition(any(KpiDefinitionEntity.class));
            verify(kpiDefinitionServiceMock, times(3)).findAllKpiNames();
            verify(databaseServiceMock, times(3)).findColumnNamesForTable(anyString());
            verify(reliabilityThresholdCalculatorMock).calculateReliabilityThreshold(CALCULATION_ID);
            verify(reliabilityThresholdCalculatorRegistryMock).calculator(anyList());
            verify(startTimeCalculatorMock).calculateStartTime(CALCULATION_ID);
            verify(startTimeCalculatorRegistryMock).calculator(anyList());
        }
    }

    @Test
    void shouldBuildReportWithOnlyCalculatedKpis() {
        final UUID calculationId = UUID.fromString("15fb6043-4669-46d0-ad63-d86099de72fd");
        final Calculation calculation = calculation(calculationId, "COMPLEX");

        final KpiDefinitionEntity calculated = definition("calculated", "table_a", 60, true);

        final List<KpiDefinitionEntity> kpiDefinitions = List.of(
                calculated,
                definition("not_calculated", "table_a", 60, false)

        );

        when(calculationServiceMock.findCalculationReadyToBeExported(calculationId)).thenReturn(calculation);
        when(kpiDefinitionServiceMock.findKpiDefinitionsByExecutionGroup("COMPLEX")).thenReturn(kpiDefinitions);
        when(reliabilityThresholdCalculatorRegistryMock.calculator(kpiDefinitions)).thenReturn(reliabilityThresholdCalculatorMock);
        when(reliabilityThresholdCalculatorMock.calculateReliabilityThreshold(calculationId)).thenReturn(Map.of("calculated", LocalDateTime.ofEpochSecond(RELIABILITY_THRESHOLD, 0, ZoneOffset.UTC)));
        when(startTimeCalculatorRegistryMock.calculator(kpiDefinitions)).thenReturn(startTimeCalculatorMock);
        when(startTimeCalculatorMock.calculateStartTime(calculationId)).thenReturn(Map.of("calculated", LocalDateTime.ofEpochSecond(START_TIME, 0, ZoneOffset.UTC)));
        when(kpiDefinitionServiceMock.findAllKpiNamesFromSameTableAsKpiDefinition(calculated)).thenReturn(List.of("calculated", "not_calculated"));
        when(kpiDefinitionServiceMock.findAllKpiNames()).thenReturn(Set.of("calculated", "not_calculated"));
        when(databaseServiceMock.findColumnNamesForTable("kpi_table_a_60")).thenReturn(List.of("calculated", "not_calculated", "dimension"));

        ExecutionReport actual = objectUnderTest.buildExecutionReport(calculationId);

        verify(calculationServiceMock).findCalculationReadyToBeExported(calculationId);
        verify(kpiDefinitionServiceMock).findKpiDefinitionsByExecutionGroup("COMPLEX");
        verify(reliabilityThresholdCalculatorRegistryMock).calculator(kpiDefinitions);
        verify(reliabilityThresholdCalculatorMock).calculateReliabilityThreshold(calculationId);
        verify(startTimeCalculatorRegistryMock).calculator(kpiDefinitions);
        verify(startTimeCalculatorMock).calculateStartTime(calculationId);
        verify(kpiDefinitionServiceMock).findAllKpiNamesFromSameTableAsKpiDefinition(calculated);
        verify(kpiDefinitionServiceMock).findAllKpiNames();
        verify(databaseServiceMock).findColumnNamesForTable("kpi_table_a_60");

        assertThat(actual.isScheduled()).isTrue();
        assertThat(actual.getExecutionStart()).isEqualTo(TEST_TIME_CREATED.toEpochSecond(ZoneOffset.UTC));
        assertThat(actual.getExecutionEnd()).isEqualTo(TEST_TIME_COMPLETED.toEpochSecond(ZoneOffset.UTC));
        assertThat(actual.getExecutionGroup()).isEqualTo("COMPLEX");
        assertThat(actual.getTables()).satisfiesExactly(
                table -> {
                    assertThat(table.getName()).isEqualTo("kpi_table_a_60");
                    assertThat(table.getAggregationPeriod()).isEqualTo(60);
                    assertThat(table.getKpis()).satisfiesExactly(
                            kpi -> assertKpi(kpi, "calculated", false, true, RELIABILITY_THRESHOLD, START_TIME)
                    );
                    assertThat(table.getListOfKpis()).containsExactlyInAnyOrder("calculated", "not_calculated");
                    assertThat(table.getListOfDimensions()).containsExactlyInAnyOrder("dimension");
                }
        );
    }

    static void assertKpi(@NonNull final Kpi kpi, final String name, final boolean reExportLateData, final boolean exportable, final long reliabilityThreshold, final long startTime) {
        assertThat(kpi.getName()).isEqualTo(name);
        assertThat(kpi.isReexportLateData()).isEqualTo(reExportLateData);
        assertThat(kpi.isExportable()).isEqualTo(exportable);
        assertThat(kpi.getReliabilityThreshold()).isEqualTo(reliabilityThreshold);
        assertThat(kpi.getCalculationStartTime()).isEqualTo(startTime);
    }

    static void assertTable(final boolean isScheduled, final String executionGroup, final ExecutionReport actual) {
        assertThat(actual.isScheduled()).isEqualTo(isScheduled);
        assertThat(actual.getExecutionStart()).isEqualTo(TEST_TIME_CREATED.toEpochSecond(ZoneOffset.UTC));
        assertThat(actual.getExecutionEnd()).isEqualTo(TEST_TIME_COMPLETED.toEpochSecond(ZoneOffset.UTC));
        assertThat(actual.getExecutionGroup()).isEqualTo(executionGroup);
        assertThat(actual.getTables()).satisfiesExactly(
                table -> {
                    assertThat(table.getName()).isEqualTo("kpi_table_a_1440");
                    assertThat(table.getAggregationPeriod()).isEqualTo(1_440);
                    assertThat(table.getKpis()).satisfiesExactly(
                            kpi -> assertKpi(kpi, "kpi_3", false, true, RELIABILITY_THRESHOLD, START_TIME),
                            kpi -> assertKpi(kpi, "kpi_4", false, false, RELIABILITY_THRESHOLD, START_TIME)
                    );
                    assertThat(table.getListOfKpis()).containsExactlyInAnyOrder("kpi_1", "kpi_2");
                    assertThat(table.getListOfDimensions()).containsExactlyInAnyOrder("dimension");
                },
                table -> {
                    assertThat(table.getName()).isEqualTo("kpi_table_a_60");
                    assertThat(table.getAggregationPeriod()).isEqualTo(60);
                    assertThat(table.getKpis()).satisfiesExactly(
                            kpi -> assertKpi(kpi, "kpi_1", false, true, RELIABILITY_THRESHOLD, START_TIME),
                            kpi -> assertKpi(kpi, "kpi_2", false, false, RELIABILITY_THRESHOLD, START_TIME)
                    );
                    assertThat(table.getListOfKpis()).containsExactlyInAnyOrder("kpi_1", "kpi_2");
                    assertThat(table.getListOfDimensions()).containsExactlyInAnyOrder("dimension");
                },
                table -> {
                    assertThat(table.getName()).isEqualTo("kpi_table_a_");
                    assertThat(table.getAggregationPeriod()).isEqualTo(-1);
                    assertThat(table.getKpis()).satisfiesExactly(
                            kpi -> assertKpi(kpi, "kpi_5", false, true, RELIABILITY_THRESHOLD, START_TIME),
                            kpi -> assertKpi(kpi, "kpi_6", false, false, RELIABILITY_THRESHOLD, START_TIME)
                    );
                    assertThat(table.getListOfKpis()).containsExactlyInAnyOrder("kpi_1", "kpi_2");
                    assertThat(table.getListOfDimensions()).containsExactlyInAnyOrder("dimension");
                }
        );
    }

    static Calculation calculation(final UUID calculationId, final String executionGroup) {
        return Calculation.builder()
                .withCalculationId(calculationId)
                .withExecutionGroup(executionGroup)
                .withKpiCalculationState(KpiCalculationState.FINALIZING)
                .withTimeCreated(TEST_TIME_CREATED)
                .withTimeCompleted(TEST_TIME_COMPLETED)
                .build();
    }

    static KpiDefinitionEntity definition(final String name, final String alias, final int aggregationPeriod, final boolean exportable) {
        return KpiDefinitionEntity.builder()
                .withName(name)
                .withAlias(alias)
                .withAggregationPeriod(aggregationPeriod)
                .withExecutionGroup(ExecutionGroup.builder().withId(1).withName(EXECUTION_GROUP).build())
                .withExportable(exportable)
                .withReexportLateData(false)
                .build();
    }
}
