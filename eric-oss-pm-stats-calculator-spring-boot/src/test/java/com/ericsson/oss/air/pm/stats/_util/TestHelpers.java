/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats._util;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculation.limits.model.ReadinessBound;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail;
import com.ericsson.oss.air.pm.stats.model.entity.Calculation;
import com.ericsson.oss.air.pm.stats.model.entity.CalculationReliability;
import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;
import com.ericsson.oss.air.pm.stats.repository._util.RepositoryHelpers.ComplexReadiness;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestHelpers {
    public static UUID uuid(final String uuid) {
        return UUID.fromString(uuid);
    }

    public static LocalDateTime testTime(final int hour, final int minute) {
        return LocalDateTime.of(2_022, Month.NOVEMBER, 20, hour, minute);
    }

    public static LocalDateTime testTime(final int day, final int hour, final int minute) {
        return LocalDateTime.of(2_022, Month.AUGUST, day, hour, minute);
    }

    public static LocalDateTime lowerReadinessBound(final int day, final int hour, final int minute) {
        return testTime(day, hour, minute);
    }

    public static LocalDateTime upperReadinessBound(final int day, final int hour, final int minute) {
        return testTime(day, hour, minute);
    }

    public static LocalDateTime testMinute(final int minute) {
        return testTime(12, minute);
    }

    public static ReadinessLog readinessLog(final Integer id,
                                            final String datasource,
                                            final Long collectedRowsCount,
                                            final LocalDateTime earliestCollectedData,
                                            final LocalDateTime latestCollectedData,
                                            final UUID kpiCalculationId) {
        return ReadinessLog.builder()
                .withId(id)
                .withDatasource(datasource)
                .withCollectedRowsCount(collectedRowsCount)
                .withEarliestCollectedData(earliestCollectedData)
                .withLatestCollectedData(latestCollectedData)
                .withKpiCalculationId(kpiCalculationId)
                .build();
    }

    public static Calculation complexCalculation(final UUID id,
                                                 final LocalDateTime timeCreated,
                                                 final LocalDateTime timeCompleted,
                                                 final KpiCalculationState state,
                                                 final String executionGroup) {
        return calculation(id, timeCreated, timeCompleted, state, executionGroup, KpiType.SCHEDULED_COMPLEX);
    }

    public static Calculation simpleCalculation(final UUID id,
                                                final LocalDateTime timeCreated,
                                                final LocalDateTime timeCompleted,
                                                final KpiCalculationState state,
                                                final String executionGroup) {
        return calculation(id, timeCreated, timeCompleted, state, executionGroup, KpiType.SCHEDULED_SIMPLE);
    }

    public static Calculation calculation(final UUID id,
                                          final LocalDateTime timeCreated,
                                          final LocalDateTime timeCompleted,
                                          final KpiCalculationState state,
                                          final String executionGroup,
                                          final KpiType kpiType) {
        return Calculation.builder()
                .withCalculationId(id)
                .withTimeCreated(timeCreated)
                .withTimeCompleted(timeCompleted)
                .withKpiCalculationState(state)
                .withExecutionGroup(executionGroup)
                .withKpiType(kpiType)
                .build();
    }

    public static KpiDefinitionEntity kpiDefinitionEntity(final String name,
                                                          final String executionGroup,
                                                          final int executionGroupId,
                                                          final int aggregationPeriod,
                                                          final int id) {
        return KpiDefinitionEntity.builder()
                .withAggregationPeriod(aggregationPeriod)
                .withName(name)
                .withExecutionGroup(ExecutionGroup.builder().withId(executionGroupId).withName(executionGroup).build())
                .withId(id)
                .build();
    }

    public static SchemaDetail schemaDetail(final int id, final String namespace, final String topic) {
        return SchemaDetail.builder().withId(id).withNamespace(namespace).withTopic(topic).build();
    }

    public static ReadinessBound readinessBound(final LocalDateTime lowerReadinessBound,
                                                final LocalDateTime upperReadinessBound) {
        return ReadinessBound.builder()
                .lowerReadinessBound(lowerReadinessBound)
                .upperReadinessBound(upperReadinessBound)
                .build();
    }

    public static CalculationReliability calculationReliability(final LocalDateTime calculationStartTime,
                                                                final LocalDateTime reliabilityThreshold,
                                                                final UUID kpiCalculationId,
                                                                final Integer kpiDefinitionId) {
        return CalculationReliability.builder()
                .withCalculationStartTime(calculationStartTime)
                .withReliabilityThreshold(reliabilityThreshold)
                .withKpiCalculationId(kpiCalculationId)
                .withKpiDefinitionId(kpiDefinitionId)
                .build();
    }

    public static List<Calculation> calculations(final Calculation... calculations) {
        return List.of(calculations);
    }

    public static List<ReadinessLog> readinessLogs(final ReadinessLog... readinessLogs) {
        return List.of(readinessLogs);
    }

    public static List<ComplexReadiness> complexReadiness(final ComplexReadiness... complexReadiness) {
        return List.of(complexReadiness);
    }

    public static List<CalculationReliability> calculationReliabilities(final CalculationReliability... calculationReliabilities) {
        return List.of(calculationReliabilities);
    }

    public static List<KpiDefinitionEntity> definitions(final Map<KpiDefinitionEntity, ReadinessBound> calculatedReadinessBoundMap) {
        return List.copyOf(calculatedReadinessBoundMap.keySet());
    }
}
