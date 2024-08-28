/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import com.ericsson.oss.air.pm.stats.calculation.limits.CalculationLimitCalculator;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation.CalculationLimit;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation.KpiCalculationJob;
import com.ericsson.oss.air.pm.stats.scheduler.execution.order.ComplexExecutionGroupOrderFacade;
import com.ericsson.oss.air.pm.stats.scheduler.heartbeatmanager.HeartbeatManager;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.service.facade.KafkaOffsetCheckerFacade;

import lombok.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KpiCalculationJobSchedulerTest {
    @Mock
    ComplexExecutionGroupOrderFacade complexExecutionGroupOrderFacadeMock;
    @Mock
    KpiCalculationExecutionController kpiCalculationExecutionControllerMock;
    @Mock
    KpiDefinitionService kpiDefinitionServiceMock;
    @Mock
    CalculationLimitCalculator calculationLimitCalculatorMock;
    @Mock
    HeartbeatManager heartbeatManagerMock;
    @Mock
    KafkaOffsetCheckerFacade kafkaOffsetCheckerFacadeMock;
    @Captor
    ArgumentCaptor<KpiCalculationJob> kpiCalculationJobCaptor;

    @InjectMocks
    KpiCalculationJobScheduler objectUnderTest;

    @Nested
    class SimplesCanWait {
        @BeforeEach
        void setUp() {
            when(heartbeatManagerMock.canSimplesWait()).thenReturn(true);
        }

        @Test
        void whenComplexQueueEmpty_andNoComplexKpisExist_shouldStartOnlySimpleCalculations() {
            when(kpiDefinitionServiceMock.countComplexKpi()).thenReturn(0L);
            when(heartbeatManagerMock.getHeartbeatCounter()).thenReturn(new AtomicInteger(5));
            when(kpiCalculationExecutionControllerMock.isComplexQueueEmpty()).thenReturn(true);
            when(kpiDefinitionServiceMock.findAllSimpleKpiNamesGroupedByExecGroups()).thenReturn(Map.of(
                    "simple_execution_group", List.of("simple_definition")
            ));
            when(kafkaOffsetCheckerFacadeMock.hasNewMessage("simple_execution_group")).thenReturn(true);

            objectUnderTest.scheduleCalculations();

            verify(kpiCalculationExecutionControllerMock).checkHangingCalculations();
            verify(kpiDefinitionServiceMock).countComplexKpi();
            verify(kpiDefinitionServiceMock).findAllSimpleKpiNamesGroupedByExecGroups();
            verify(kpiCalculationExecutionControllerMock).isSimpleQueuedOrRunning("simple_execution_group");
            verify(calculationLimitCalculatorMock, never()).calculateLimit("execution_group");
            verify(kpiCalculationExecutionControllerMock).scheduleCalculation(kpiCalculationJobCaptor.capture());


            assertThat(kpiCalculationJobCaptor.getValue()).satisfies(kpiCalculationJob -> {
                assertThat(kpiCalculationJob.isSimple()).isTrue();
                assertThat(kpiCalculationJob.getKpiDefinitionNames()).containsExactlyInAnyOrder("simple_definition");
                assertThat(kpiCalculationJob.getCalculationLimit().calculationStartTime()).isEqualTo(LocalDateTime.MIN);
                assertThat(kpiCalculationJob.getCalculationLimit().calculationEndTime()).isEqualTo(LocalDateTime.MIN);
            });
        }

        @Test
        void whenComplexQueueEmpty_andComplexKpisExist_shouldStartComplexCalculations() {
            when(kpiDefinitionServiceMock.countComplexKpi()).thenReturn(1L);
            when(heartbeatManagerMock.getHeartbeatCounter()).thenReturn(new AtomicInteger(5));
            when(kpiCalculationExecutionControllerMock.isComplexQueueEmpty()).thenReturn(true);
            when(kpiDefinitionServiceMock.findAllSimpleKpiNamesGroupedByExecGroups()).thenReturn(Collections.emptyMap());
            when(kpiDefinitionServiceMock.findAllComplexKpiNamesGroupedByExecGroups()).thenReturn(Map.of(
                    "complex_execution_group", List.of("complex_definition")
            ));
            when(complexExecutionGroupOrderFacadeMock.sortCalculableComplexExecutionGroups()).thenReturn(order("complex_execution_group"));
            when(calculationLimitCalculatorMock.calculateLimit("complex_execution_group")).thenReturn(
                    createTestCalculationLimit(createTestTime(16), createTestTime(17))
            );

            objectUnderTest.scheduleCalculations();

            verify(kpiCalculationExecutionControllerMock).checkHangingCalculations();
            verify(kpiDefinitionServiceMock).countComplexKpi();
            verify(calculationLimitCalculatorMock).calculateLimit("complex_execution_group");
            verify(kpiDefinitionServiceMock).findAllSimpleKpiNamesGroupedByExecGroups();
            verify(kpiDefinitionServiceMock).findAllComplexKpiNamesGroupedByExecGroups();
            verify(complexExecutionGroupOrderFacadeMock).sortCalculableComplexExecutionGroups();
            verify(calculationLimitCalculatorMock).calculateLimit("complex_execution_group");
            verify(kpiCalculationExecutionControllerMock).scheduleCalculation(kpiCalculationJobCaptor.capture());

            assertThat(kpiCalculationJobCaptor.getValue()).satisfies(kpiCalculationJob -> {
                assertThat(kpiCalculationJob.isComplex()).isTrue();
                assertThat(kpiCalculationJob.getKpiDefinitionNames()).containsExactlyInAnyOrder("complex_definition");
                assertThat(kpiCalculationJob.getCalculationLimit().calculationStartTime()).isEqualTo(createTestTime(16));
                assertThat(kpiCalculationJob.getCalculationLimit().calculationEndTime()).isEqualTo(createTestTime(17));
            });
        }

        @Test
        void whenComplexQueueNotEmpty_shouldNotStartCalculations_andIncrementHeartbeat() {
            when(heartbeatManagerMock.getHeartbeatCounter()).thenReturn(new AtomicInteger(5));
            when(kpiCalculationExecutionControllerMock.isComplexQueueEmpty()).thenReturn(false);

            objectUnderTest.scheduleCalculations();

            verify(kpiCalculationExecutionControllerMock).checkHangingCalculations();
            verify(kpiDefinitionServiceMock, never()).findAllSimpleKpiNamesGroupedByExecGroups();
            verify(kpiDefinitionServiceMock, never()).findAllComplexKpiNamesGroupedByExecGroups();
            verify(kpiCalculationExecutionControllerMock, never()).scheduleCalculation(any());
            verify(heartbeatManagerMock).incrementHeartbeatCounter();
        }

        @Test
        void whenNoDataReadFromKafka_andNoComplexKpisExistOrInAQue_shouldNotStartAnyCalculation() {
            when(kpiDefinitionServiceMock.countComplexKpi()).thenReturn(0L);
            when(heartbeatManagerMock.getHeartbeatCounter()).thenReturn(new AtomicInteger(5));
            when(kpiCalculationExecutionControllerMock.isComplexQueueEmpty()).thenReturn(true);
            when(kpiDefinitionServiceMock.findAllSimpleKpiNamesGroupedByExecGroups()).thenReturn(Map.of(
                    "simple_execution_group", List.of("simple_definition")
            ));
            when(kafkaOffsetCheckerFacadeMock.hasNewMessage("simple_execution_group")).thenReturn(false);

            objectUnderTest.scheduleCalculations();

            verify(kpiDefinitionServiceMock).countComplexKpi();
            verify(kpiDefinitionServiceMock).findAllSimpleKpiNamesGroupedByExecGroups();
            verify(kpiCalculationExecutionControllerMock).isSimpleQueuedOrRunning("simple_execution_group");
            verify(kpiCalculationExecutionControllerMock, never()).scheduleCalculation(kpiCalculationJobCaptor.capture());
        }
    }

    @Nested
    class SimplesCanNotWait {

        @Test
        void shouldComplexQueueCleared_andSimpleCalculationLaunched_andHeartbeatSetToZero() {
            when(heartbeatManagerMock.getHeartbeatCounter()).thenReturn(new AtomicInteger(5));
            when(heartbeatManagerMock.canSimplesWait()).thenReturn(false);
            when(kpiDefinitionServiceMock.findAllSimpleKpiNamesGroupedByExecGroups()).thenReturn(Map.of(
                    "simple_execution_group", List.of("simple_definition"),
                    "filtered_out", List.of("simple_definition")
            ));
            when(kpiCalculationExecutionControllerMock.isSimpleQueuedOrRunning("simple_execution_group")).thenReturn(false);
            when(kpiCalculationExecutionControllerMock.isSimpleQueuedOrRunning("filtered_out")).thenReturn(true);
            when(kafkaOffsetCheckerFacadeMock.hasNewMessage("simple_execution_group")).thenReturn(true);

            objectUnderTest.scheduleCalculations();

            verify(kpiCalculationExecutionControllerMock).checkHangingCalculations();
            verify(heartbeatManagerMock).getHeartbeatCounter();
            verify(heartbeatManagerMock).canSimplesWait();
            verify(kpiCalculationExecutionControllerMock).clearComplexCalculationQueue();

            verify(kpiDefinitionServiceMock).findAllSimpleKpiNamesGroupedByExecGroups();
            verify(kpiCalculationExecutionControllerMock).isSimpleQueuedOrRunning("simple_execution_group");
            verify(kpiCalculationExecutionControllerMock).isSimpleQueuedOrRunning("filtered_out");
            verify(kpiCalculationExecutionControllerMock).scheduleCalculation(kpiCalculationJobCaptor.capture());
            verify(heartbeatManagerMock).resetHeartBeatCounter();

            assertThat(kpiCalculationJobCaptor.getAllValues()).hasSize(1);
            assertThat(kpiCalculationJobCaptor.getValue()).satisfies(kpiCalculationJob -> {
                assertThat(kpiCalculationJob.isSimple()).isTrue();
                assertThat(kpiCalculationJob.getExecutionGroup()).isEqualTo("simple_execution_group");
            });
        }
    }

    @NonNull
    LocalDateTime createTestTime(final int hour) {
        return LocalDateTime.of(2_022, Month.OCTOBER, 25, hour, 0);
    }

    private CalculationLimit createTestCalculationLimit(final LocalDateTime calculationStartTime, final LocalDateTime calculationEndTime) {
        return CalculationLimit.builder().calculationStartTime(calculationStartTime).calculationEndTime(calculationEndTime).build();
    }

    static Queue<String> order(final String... orders) {
        return new LinkedList<>(List.of(orders));
    }
}
