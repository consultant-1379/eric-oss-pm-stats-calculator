/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.start.time;

import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculation.start.time.adjuster.StartTimeTemporalAdjuster;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;
import com.ericsson.oss.air.pm.stats.repository.api.CalculationRepository;
import com.ericsson.oss.air.pm.stats.repository.api.KpiDefinitionRepository;
import com.ericsson.oss.air.pm.stats.repository.api.ReadinessLogRepository;

import lombok.NonNull;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SimpleStartTimeCalculatorTest {
    static final UUID UUID_1 = UUID.fromString("6cf32e0a-5a0f-4a4a-8d8d-3774ffcd3808");
    static final String EXECUTION_GROUP = "execution_group";

    @Mock
    ReadinessLogRepository readinessLogRepositoryMock;
    @Mock
    CalculationRepository calculationRepositoryMock;
    @Mock
    KpiDefinitionRepository kpiDefinitionRepositoryMock;
    @Spy
    StartTimeTemporalAdjuster startTimeTemporalAdjuster; /* No point on mocking */

    @InjectMocks
    SimpleStartTimeCalculator objectUnderTest;

    @Test
    void shouldCalculateStartTime() {
        final LocalDateTime testTime1 = localDateTime(19, LocalTime.NOON);
        final LocalDateTime testTime2 = localDateTime(18, LocalTime.NOON);
        final LocalDateTime testTime3 = localDateTime(18, LocalTime.of(12, 15));
        final LocalDateTime testTime4 = localDateTime(18, LocalTime.of(12, 15));

        final KpiDefinitionEntity kpiDefinition1 = definitionEntity("kpi1", 60, "identifier_1", "1", "2");
        final KpiDefinitionEntity kpiDefinition2 = definitionEntity("kpi2", 1440, "identifier_2", "1", "2");
        final KpiDefinitionEntity kpiDefinition3 = definitionEntity("kpi3", 60, "identifier_3", "1", "2");
        final KpiDefinitionEntity kpiDefinition4 = definitionEntity("kpi4", 1440, "identifier_3", "1", "2");
        final KpiDefinitionEntity kpiDefinition5 = definitionEntity("kpi5", -1, "identifier_1", "1", "2");

        final ReadinessLog readinessLog1 = readinessLog("identifier_1|1|2", testTime1);
        final ReadinessLog readinessLog2 = readinessLog("identifier_2|1|2", testTime2);
        final ReadinessLog readinessLog3 = readinessLog("identifier_3|1|2", testTime3);

        final List<KpiDefinitionEntity> kpiDefinitions = Arrays.asList(
                kpiDefinition1,
                kpiDefinition2,
                kpiDefinition3,
                kpiDefinition4,
                kpiDefinition5
        );

        when(readinessLogRepositoryMock.findByCalculationId(UUID_1)).thenReturn(List.of(readinessLog1, readinessLog2, readinessLog3));
        when(calculationRepositoryMock.forceFetchExecutionGroupByCalculationId(UUID_1)).thenReturn(EXECUTION_GROUP);
        when(kpiDefinitionRepositoryMock.findKpiDefinitionsByExecutionGroup(EXECUTION_GROUP)).thenReturn(kpiDefinitions);

        final Map<String, LocalDateTime> actual = objectUnderTest.calculateStartTime(UUID_1);

        verify(readinessLogRepositoryMock).findByCalculationId(UUID_1);
        verify(calculationRepositoryMock).forceFetchExecutionGroupByCalculationId(UUID_1);
        verify(kpiDefinitionRepositoryMock).findKpiDefinitionsByExecutionGroup(EXECUTION_GROUP);

        Assertions.assertThat(actual).containsOnly(
                entry(kpiDefinition1.name(), testTime1),
                entry(kpiDefinition2.name(), localDateTime(18, LocalTime.MIDNIGHT)),
                entry(kpiDefinition3.name(), localDateTime(18, LocalTime.NOON)),
                entry(kpiDefinition4.name(), localDateTime(18, LocalTime.MIDNIGHT)),
                entry(kpiDefinition5.name(), localDateTime(19, LocalTime.NOON))
        );
    }

    @ParameterizedTest
    @EnumSource(value = KpiType.class, names = {"SCHEDULED_COMPLEX", "ON_DEMAND"})
    void shouldNotSupports(final KpiType kpiType) {
        final boolean actual = objectUnderTest.supports(kpiType);
        Assertions.assertThat(actual).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = KpiType.class, names = "SCHEDULED_SIMPLE")
    void shouldSupports(final KpiType kpiType) {
        final boolean actual = objectUnderTest.supports(kpiType);
        Assertions.assertThat(actual).isTrue();
    }

    @NonNull
    static LocalDateTime localDateTime(final int dayOfMonth, final LocalTime localTime) {
        return LocalDateTime.of(LocalDate.of(2_022, Month.APRIL, dayOfMonth), localTime);
    }

    static KpiDefinitionEntity definitionEntity(final String name, final int aggregationPeriod, final String dataSpace, final String category, final String schemaName) {
        return KpiDefinitionEntity.builder()
                .withName(name)
                .withSchemaDataSpace(dataSpace)
                .withSchemaCategory(category)
                .withSchemaName(schemaName)
                .withAggregationPeriod(aggregationPeriod)
                .withExportable(true)
                .withFilters(List.of())
                .build();
    }

    static ReadinessLog readinessLog(final String datasource, final LocalDateTime testTime) {
        return ReadinessLog.builder()
                .withKpiCalculationId(UUID_1)
                .withDatasource(datasource)
                .withEarliestCollectedData(testTime)
                .build();
    }
}
