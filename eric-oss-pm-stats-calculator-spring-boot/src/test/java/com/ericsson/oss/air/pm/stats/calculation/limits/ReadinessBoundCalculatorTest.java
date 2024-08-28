/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.limits;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.enterprise.inject.Instance;

import com.ericsson.oss.air.pm.stats.calculation.limits.model.ReadinessBound;
import com.ericsson.oss.air.pm.stats.calculation.limits.period.AggregationPeriodSupporter;
import com.ericsson.oss.air.pm.stats.calculation.limits.period.creator.HourlyAggregationPeriodCreator;
import com.ericsson.oss.air.pm.stats.calculation.limits.period.creator.api.AggregationPeriodCreator;
import com.ericsson.oss.air.pm.stats.calculation.limits.period.creator.registry.AggregationPeriodCreatorRegistry;
import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.service.KpiDefinitionServiceImpl;
import com.ericsson.oss.air.pm.stats.service.api.CalculationReliabilityService;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReadinessBoundCalculatorTest {
    @Mock
    Instance<AggregationPeriodCreator> aggregationPeriodCreatorsMock;

    @InjectMocks
    AggregationPeriodCreatorRegistry aggregationPeriodCreatorRegistrySpy = spy(new AggregationPeriodCreatorRegistry()); /* Intentional spy */
    @InjectMocks
    AggregationPeriodSupporter aggregationPeriodSupporterSpy = spy(new AggregationPeriodSupporter()); /* Intentional spy */

    @Mock
    KpiDefinitionServiceImpl kpiDefinitionServiceMock;
    @Mock
    ReadinessBoundCollector readinessBoundCollectorMock;
    @Mock
    CalculationReliabilityService calculationReliabilityServiceMock;

    @InjectMocks
    ReadinessBoundCalculator objectUnderTest;

    @Nested
    class DefineCalculable {
        @BeforeEach
        void setUp() {
            Mockito.lenient().when(aggregationPeriodCreatorsMock.stream()).thenAnswer(invocation -> Stream.of(new HourlyAggregationPeriodCreator()));
        }

        @Nested
        @TestInstance(Lifecycle.PER_CLASS)
        class Calculable {
            @ParameterizedTest
            @MethodSource("provideAnyBoundIsCalculableData")
            void shouldVerifyCalculable(final ReadinessBound readinessBound1,
                                        final ReadinessBound readinessBound2,
                                        final ReadinessBound readinessBound3,
                                        final Map<String, LocalDateTime> reliabilities,
                                        final boolean expected) {
                final KpiDefinitionEntity kpiDefinition1 = kpiDefinition("A", 60);
                final KpiDefinitionEntity kpiDefinition2 = kpiDefinition("B", 60);
                final KpiDefinitionEntity kpiDefinition3 = kpiDefinition("C", 60);

                final List<KpiDefinitionEntity> definitions = List.of(kpiDefinition1, kpiDefinition2, kpiDefinition3);

                when(kpiDefinitionServiceMock.findKpiDefinitionsByExecutionGroup("executionGroup")).thenReturn(definitions);
                when(calculationReliabilityServiceMock.findMaxReliabilityThresholdByKpiName()).thenReturn(reliabilities);
                when(readinessBoundCollectorMock.calculateReadinessBounds("executionGroup", definitions)).thenReturn(Map.of(
                        kpiDefinition1, readinessBound1,
                        kpiDefinition2, readinessBound2,
                        kpiDefinition3, readinessBound3
                ));

                final boolean actual = objectUnderTest.isReliablyCalculableGroup("executionGroup");

                verify(kpiDefinitionServiceMock).findKpiDefinitionsByExecutionGroup("executionGroup");
                verify(calculationReliabilityServiceMock).findMaxReliabilityThresholdByKpiName();
                verify(readinessBoundCollectorMock).calculateReadinessBounds("executionGroup", definitions);

                Assertions.assertThat(actual).isEqualTo(expected);
            }

            @ParameterizedTest
            @MethodSource("provideDataForComplexWithoutAggregationPeriod")
            void shouldVerifyCalculableWhenComplexHasNoAggregationPeriod(final ReadinessBound readinessBound1,
                                                                         final ReadinessBound readinessBound2,
                                                                         final ReadinessBound readinessBound3,
                                                                         final boolean expected) {
                final KpiDefinitionEntity kpiDefinition1 = kpiDefinition("A", 60);
                final KpiDefinitionEntity kpiDefinition2 = kpiDefinition("B", -1);
                final KpiDefinitionEntity kpiDefinition3 = kpiDefinition("C", 60);

                final List<KpiDefinitionEntity> definitions = List.of(kpiDefinition1, kpiDefinition2, kpiDefinition3);

                when(kpiDefinitionServiceMock.findKpiDefinitionsByExecutionGroup("executionGroup")).thenReturn(definitions);

                when(readinessBoundCollectorMock.calculateReadinessBounds("executionGroup", definitions)).thenReturn(Map.of(
                        kpiDefinition1, readinessBound1,
                        kpiDefinition2, readinessBound2,
                        kpiDefinition3, readinessBound3
                ));

                final boolean actual = objectUnderTest.isReliablyCalculableGroup("executionGroup");

                verify(kpiDefinitionServiceMock).findKpiDefinitionsByExecutionGroup("executionGroup");
                verify(calculationReliabilityServiceMock).findMaxReliabilityThresholdByKpiName();
                verify(readinessBoundCollectorMock).calculateReadinessBounds("executionGroup", definitions);

                Assertions.assertThat(actual).isEqualTo(expected);
            }

            Stream<Arguments> provideAnyBoundIsCalculableData() {
                return Stream.of(
                        Arguments.of(
                                readinessBound(lowerReadinessBound(12, 0), upperReadinessBound(11, 59)),
                                readinessBound(lowerReadinessBound(12, 0), upperReadinessBound(12, 30)),
                                readinessBound(lowerReadinessBound(12, 0), upperReadinessBound(12, 59)),
                                Collections.emptyMap(),
                                false
                        ),
                        Arguments.of(
                                readinessBound(lowerReadinessBound(12, 0), upperReadinessBound(12, 0)),
                                readinessBound(lowerReadinessBound(12, 0), upperReadinessBound(12, 30)),
                                readinessBound(lowerReadinessBound(12, 0), upperReadinessBound(12, 59)),
                                Collections.emptyMap(),
                                false
                        ),
                        Arguments.of(
                                readinessBound(lowerReadinessBound(12, 0), upperReadinessBound(12, 0)),
                                readinessBound(lowerReadinessBound(12, 0), upperReadinessBound(12, 30)),
                                readinessBound(lowerReadinessBound(12, 0), upperReadinessBound(13, 0)), /* Calculable RIGHT */
                                Collections.emptyMap(),
                                true
                        ),
                        Arguments.of(
                                readinessBound(lowerReadinessBound(12, 0), upperReadinessBound(12, 0)),
                                readinessBound(lowerReadinessBound(12, 0), upperReadinessBound(12, 30)),
                                readinessBound(lowerReadinessBound(12, 0), upperReadinessBound(13, 1)), /* Calculable RIGHT */
                                Collections.emptyMap(),
                                true
                        ),
                        Arguments.of(
                                readinessBound(lowerReadinessBound(12, 0), upperReadinessBound(12, 0)),
                                readinessBound(lowerReadinessBound(12, 0), upperReadinessBound(12, 30)),
                                readinessBound(lowerReadinessBound(12, 0), upperReadinessBound(12, 59)),
                                Map.of("A", testTime(13, 0)), /* late data case: upper readiness before, lower readiness + lookback after */
                                true
                        ),
                        Arguments.of(
                                readinessBound(lowerReadinessBound(12, 0), upperReadinessBound(12, 0)),
                                readinessBound(lowerReadinessBound(12, 0), upperReadinessBound(12, 30)),
                                readinessBound(lowerReadinessBound(12, 0), upperReadinessBound(12, 59)),
                                Map.of("A", testTime(14, 0)), /* late data case: upper readiness before, lower readiness + lookback equals */
                                true
                        ),
                        Arguments.of(
                                readinessBound(lowerReadinessBound(12, 0), upperReadinessBound(12, 0)),
                                readinessBound(lowerReadinessBound(12, 0), upperReadinessBound(12, 30)),
                                readinessBound(lowerReadinessBound(12, 0), upperReadinessBound(12, 59)),
                                Map.of("A", testTime(15, 0)), /* late data case: upper readiness before, lower readiness + lookback before */
                                false
                        )
                );
            }

            Stream<Arguments> provideDataForComplexWithoutAggregationPeriod() {
                return Stream.of(
                        Arguments.of(
                                readinessBound(lowerReadinessBound(12, 0), upperReadinessBound(11, 59)),
                                readinessBound(lowerReadinessBound(12, 0), upperReadinessBound(12, 30)), /* KPI without aggregation period */
                                readinessBound(lowerReadinessBound(12, 0), upperReadinessBound(12, 59)),
                                true
                        ),
                        Arguments.of(
                                readinessBound(lowerReadinessBound(12, 0), upperReadinessBound(12, 0)),
                                readinessBound(lowerReadinessBound(12, 0), upperReadinessBound(12, 30)), /* KPI without aggregation period */
                                readinessBound(lowerReadinessBound(12, 0), upperReadinessBound(13, 0)), /* Calculable RIGHT */
                                true
                        )
                );
            }
        }
    }

    static LocalDateTime lowerReadinessBound(final int hour, final int minute) {
        return testTime(hour, minute);
    }

    static LocalDateTime upperReadinessBound(final int hour, final int minute) {
        return testTime(hour, minute);
    }

    static LocalDateTime testTime(final int hour, final int minute) {
        return LocalDateTime.of(2_022, Month.OCTOBER, 11, hour, minute);
    }

    static KpiDefinitionEntity kpiDefinition(final String name, final int aggregationPeriod) {
        final ExecutionGroup executionGroup = ExecutionGroup.builder().withId(1).withName("executionGroup").build();
        return KpiDefinitionEntity.builder().withAggregationPeriod(aggregationPeriod).withDataLookbackLimit(120).withName(name).withExecutionGroup(executionGroup).build();
    }

    static ReadinessBound readinessBound(final LocalDateTime lowerReadinessBound, final LocalDateTime upperReadinessBound) {
        return ReadinessBound.builder().lowerReadinessBound(lowerReadinessBound).upperReadinessBound(upperReadinessBound).build();
    }
}
