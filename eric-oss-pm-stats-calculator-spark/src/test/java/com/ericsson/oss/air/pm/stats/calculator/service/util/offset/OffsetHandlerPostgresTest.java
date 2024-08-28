/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.offset;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatasourceTables;
import com.ericsson.oss.air.pm.stats.calculator.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiCalculationPeriodUtils;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.OffsetHandlerPostgres.AggregationTimestampCache;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.AggregationPeriodWindow;

import com.google.common.collect.Maps;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OffsetHandlerPostgresTest {
    static final LocalDateTime TEST_TIME = LocalDateTime.of(LocalDate.of(2_022, Month.JULY, 16), LocalTime.NOON);

    @Mock KpiDefinitionService kpiDefinitionServiceMock;
    @Mock KpiCalculationPeriodUtils kpiCalculationPeriodUtilsMock;
    @Mock KpiDefinitionHelperImpl kpiDefinitionHelperMock;
    @Mock SparkService sparkServiceMock;
    @Mock AggregationTimestampCache aggregationTimestampStartMock;
    @Mock AggregationTimestampCache aggregationTimestampEndMock;

    OffsetHandlerPostgres objectUnderTest;

    Set<KpiDefinition> kpiDefinitions = emptySet();

    @BeforeEach
    void setUp() {
        objectUnderTest = new OffsetHandlerPostgres(
                kpiDefinitionServiceMock,
                kpiCalculationPeriodUtilsMock,
                kpiDefinitionHelperMock,
                sparkServiceMock,
                aggregationTimestampStartMock,
                aggregationTimestampEndMock
        );
    }

    @Test
    void shouldVerifyKpiDefinitionSupport() {
        objectUnderTest.supports(kpiDefinitions);

        verify(kpiDefinitionServiceMock).areNonScheduledSimple(kpiDefinitions);
    }

    @Nested
    @DisplayName("verify getKpisForAggregationPeriodWithTimestampParameters")
    class GetKpisForAggregationPeriod {
        @Test
        void shouldHandleNonScheduledKpiDefinitions() {
            final List<KpiDefinition> filteredKpiDefinitions = emptyList();

            when(kpiDefinitionServiceMock.areScheduled(kpiDefinitions)).thenReturn(false);
            when(kpiDefinitionHelperMock.filterByAggregationPeriod(60, kpiDefinitions)).thenReturn(filteredKpiDefinitions);

            final List<KpiDefinition> actual = objectUnderTest.getKpisForAggregationPeriodWithTimestampParameters(60, kpiDefinitions);

            verify(kpiDefinitionServiceMock).areScheduled(kpiDefinitions);
            verify(kpiDefinitionHelperMock).filterByAggregationPeriod(60, kpiDefinitions);

            Assertions.assertThat(actual).isEqualTo(filteredKpiDefinitions);
        }

        @Test
        void shouldReplaceParameters() {
            KpiDefinition parameterizedDefinition = KpiDefinition.builder()
                    .withFilter(List.of(Filter.of(
                            "kpi_db://kpi_simple_60.aggregation_begin_time BETWEEN (date_trunc('hour', TO_TIMESTAMP('${param.start_date_time}')) - interval 1 day) and date_trunc('hour', TO_TIMESTAMP('${param.end_date_time}'))")))
                    .withExpression(
                            "SUM(kpi_simple_60.integer_simple) + SUM(kpi_simple_60.float_simple) FROM kpi_db://kpi_simple_60")
                    .withExecutionGroup("COMPLEX1")
                    .withAggregationType("SUM")
                    .withAggregationPeriod("60")
                    .withAlias("Complex")
                    .withName("Parameter")
                    .withObjectType("FLOAT")
                    .withAggregationElements(List.of("kpi_simple_60.agg_column_0", "kpi_simple_60.agg_column_1"))
                    .build();

            List<KpiDefinition> definitions = List.of(parameterizedDefinition);

            when(kpiDefinitionServiceMock.areScheduled(definitions)).thenReturn(true);
            when(aggregationTimestampStartMock.get(60)).thenReturn(Timestamp.valueOf(TEST_TIME.minusMinutes(5)));
            when(aggregationTimestampEndMock.get(60)).thenReturn(Timestamp.valueOf(TEST_TIME));
            when(kpiDefinitionHelperMock.filterByAggregationPeriod(60, definitions)).thenReturn(definitions);


            List<KpiDefinition> actual = objectUnderTest.getKpisForAggregationPeriodWithTimestampParameters(
                    60, definitions);
            Filter expected = Filter.of("kpi_db://kpi_simple_60.aggregation_begin_time BETWEEN (date_trunc('hour', TO_TIMESTAMP('2022-07-16 11:55:00.0')) - interval 1 day) and date_trunc('hour', TO_TIMESTAMP('2022-07-16 12:00:00.0'))");

            verify(kpiDefinitionServiceMock).areScheduled(definitions);
            verify(kpiDefinitionHelperMock).filterByAggregationPeriod(60, definitions);
            verify(aggregationTimestampStartMock, times(2)).get(60);
            verify(aggregationTimestampEndMock, times(2)).get(60);

            Assertions.assertThat(actual.get(0).getFilter()).containsAnyOf(expected);
        }

        @Nested
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("when KPI Definitions are scheduled")
        class ScheduledKpiDefinition {
            @MethodSource("provideGetKpisForAggregationPeriodData")
            @ParameterizedTest(name = "[{index}] Start timestamp: ''{0}'' End timestamp: ''{1}'' ==> returns ''{2}''")
            void shouldReturnEmpty_whenStartTimestamp_orEndTimestampIsNull(final Timestamp start, final Timestamp end, final List<KpiDefinition> expected) {
                when(kpiDefinitionServiceMock.areScheduled(kpiDefinitions)).thenReturn(true);
                when(aggregationTimestampStartMock.get(60)).thenReturn(start);
                when(aggregationTimestampEndMock.get(60)).thenReturn(end);

                final List<KpiDefinition> actual = objectUnderTest.getKpisForAggregationPeriodWithTimestampParameters(60, kpiDefinitions);

                verify(kpiDefinitionServiceMock).areScheduled(kpiDefinitions);
                verify(aggregationTimestampStartMock).get(60);
                verify(aggregationTimestampEndMock).get(60);

                Assertions.assertThat(actual).isEqualTo(expected);
            }

            @Test
            void shouldReturnEmpty_whenStartStartTimestampIsAfterEndTimestamp() {
                when(kpiDefinitionServiceMock.areScheduled(kpiDefinitions)).thenReturn(true);
                when(aggregationTimestampStartMock.get(60)).thenReturn(Timestamp.valueOf(TEST_TIME.plusMinutes(5)));
                when(aggregationTimestampEndMock.get(60)).thenReturn(Timestamp.valueOf(TEST_TIME));

                final List<KpiDefinition> actual = objectUnderTest.getKpisForAggregationPeriodWithTimestampParameters(60, kpiDefinitions);

                verify(kpiDefinitionServiceMock).areScheduled(kpiDefinitions);
                verify(aggregationTimestampStartMock).get(60);
                verify(aggregationTimestampEndMock).get(60);

                Assertions.assertThat(actual).isEmpty();
            }

            Stream<Arguments> provideGetKpisForAggregationPeriodData() {
                return Stream.of(
                        Arguments.of(null, Timestamp.valueOf(TEST_TIME), emptyList()),
                        Arguments.of(Timestamp.valueOf(TEST_TIME), null, emptyList())
                );
            }
        }
    }

    @Nested
    @DisplayName("verify calculateOffsets")
    class CalculateOffsets {

        @Nested
        @DisplayName("when kpiDefinition Exists")
        class AvailableKpiDefinitions {
            final List<KpiDefinition> kpiDefinitions_default = singletonList(KpiDefinition.builder().build());
            final List<KpiDefinition> kpiDefinitions60 = singletonList(KpiDefinition.builder().build());
            final List<KpiDefinition> kpiDefinitions1_440 = singletonList(KpiDefinition.builder().build());
            final DatasourceTables datasourceTables_default = DatasourceTables.newInstance();
            final DatasourceTables datasourceTables60 = DatasourceTables.newInstance();
            final DatasourceTables datasourceTables1_440 = DatasourceTables.newInstance();
            final Map<Integer, List<KpiDefinition>> aggregationPeriodGroups = Maps.newLinkedHashMapWithExpectedSize(3);

            @BeforeEach
            void setUp() {
                aggregationPeriodGroups.put(-1, kpiDefinitions_default);
                aggregationPeriodGroups.put(60, kpiDefinitions60);
                aggregationPeriodGroups.put(1_440, kpiDefinitions1_440);

                when(kpiDefinitionHelperMock.groupByAggregationPeriod(kpiDefinitions)).thenReturn(aggregationPeriodGroups);
            }

            @AfterEach
            void verifyCalls() {
                verify(sparkServiceMock, times(3)).isOnDemand();
                verify(kpiDefinitionHelperMock).groupByAggregationPeriod(kpiDefinitions);
            }

            @Test
            void shouldCalculateOffsets_andPopulateMaps_whenOnDemandKpis() {
                when(kpiDefinitionHelperMock.extractNonInMemoryDatasourceTables(same(kpiDefinitions_default))).thenReturn(datasourceTables_default);
                when(kpiDefinitionHelperMock.extractNonInMemoryDatasourceTables(same(kpiDefinitions60))).thenReturn(datasourceTables60);
                when(kpiDefinitionHelperMock.extractNonInMemoryDatasourceTables(same(kpiDefinitions1_440))).thenReturn(datasourceTables1_440);

                when(kpiCalculationPeriodUtilsMock.getKpiCalculationStartTimeStampInUtc(eq(-1), same(datasourceTables_default), eq(kpiDefinitions))).thenReturn(testTime(11));
                when(kpiCalculationPeriodUtilsMock.getKpiCalculationStartTimeStampInUtc(eq(60), same(datasourceTables60), eq(kpiDefinitions))).thenReturn(testTime(12));
                when(kpiCalculationPeriodUtilsMock.getKpiCalculationStartTimeStampInUtc(eq(1_440), same(datasourceTables1_440), eq(kpiDefinitions))).thenReturn(testTime(13));

                when(kpiCalculationPeriodUtilsMock.getEndTimestampInUtc(same(datasourceTables_default), eq(kpiDefinitions))).thenReturn(testTime(12));
                when(kpiCalculationPeriodUtilsMock.getEndTimestampInUtc(same(datasourceTables60), eq(kpiDefinitions))).thenReturn(testTime(13));
                when(kpiCalculationPeriodUtilsMock.getEndTimestampInUtc(same(datasourceTables1_440), eq(kpiDefinitions))).thenReturn(testTime(14));

                when(sparkServiceMock.isOnDemand()).thenReturn(true);

                objectUnderTest.calculateOffsets(kpiDefinitions);

                verify(kpiDefinitionHelperMock, atLeastOnce()).extractNonInMemoryDatasourceTables(kpiDefinitions_default);
                verify(kpiDefinitionHelperMock, atLeastOnce()).extractNonInMemoryDatasourceTables(kpiDefinitions60);
                verify(kpiDefinitionHelperMock, atLeastOnce()).extractNonInMemoryDatasourceTables(kpiDefinitions1_440);

                verify(kpiCalculationPeriodUtilsMock).getKpiCalculationStartTimeStampInUtc(-1, datasourceTables_default, kpiDefinitions);
                verify(kpiCalculationPeriodUtilsMock).getKpiCalculationStartTimeStampInUtc(60, datasourceTables60, kpiDefinitions);
                verify(kpiCalculationPeriodUtilsMock).getKpiCalculationStartTimeStampInUtc(1_440, datasourceTables1_440, kpiDefinitions);

                verify(kpiCalculationPeriodUtilsMock, atLeastOnce()).getEndTimestampInUtc(datasourceTables_default, kpiDefinitions);
                verify(kpiCalculationPeriodUtilsMock, atLeastOnce()).getEndTimestampInUtc(datasourceTables60, kpiDefinitions);
                verify(kpiCalculationPeriodUtilsMock, atLeastOnce()).getEndTimestampInUtc(datasourceTables1_440, kpiDefinitions);

                verify(aggregationTimestampStartMock).put(-1, testTime(11));
                verify(aggregationTimestampEndMock).put(-1, testTime(12));
                verify(aggregationTimestampStartMock).put(60, testTime(12));
                verify(aggregationTimestampEndMock).put(60, testTime(13));
                verify(aggregationTimestampStartMock).put(1_440, testTime(13));
                verify(aggregationTimestampEndMock).put(1_440, testTime(14));
            }

            @Test
            void shouldCalculateOffsets_andPopulateMaps_whenComplexKpis() {
                when(sparkServiceMock.isOnDemand()).thenReturn(false);
                when(sparkServiceMock.getComplexAggregationPeriodWindow()).thenReturn(AggregationPeriodWindow.of(
                        testTime(12, 45),
                        testTime(13)
                ));

                objectUnderTest.calculateOffsets(kpiDefinitions);

                verify(sparkServiceMock, times(3)).getComplexAggregationPeriodWindow();
                verify(aggregationTimestampStartMock).put(-1, testTime(12, 45));
                verify(aggregationTimestampEndMock).put(-1, testTime(13));
                verify(aggregationTimestampStartMock).put(60, testTime(12));
                verify(aggregationTimestampEndMock).put(60, testTime(13));
                verify(aggregationTimestampStartMock).put(1_440, testTime(0));
                verify(aggregationTimestampEndMock).put(1_440, testTime(13));
            }
        }
    }

    static Timestamp testTime(final int hour) {
        return testTime(hour, 0);
    }

    static Timestamp testTime(final int hour, final int minutes) {
        return Timestamp.valueOf(LocalDateTime.of(2_022, Month.NOVEMBER, 11, hour, minutes));
    }
}