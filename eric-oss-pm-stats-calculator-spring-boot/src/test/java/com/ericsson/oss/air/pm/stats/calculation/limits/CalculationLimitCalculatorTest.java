/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.limits;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculation.limits.model.ReadinessBound;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation.CalculationLimit;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.repository.CalculationRepositoryImpl;
import com.ericsson.oss.air.pm.stats.service.KpiDefinitionServiceImpl;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CalculationLimitCalculatorTest {
    @Mock
    KpiDefinitionServiceImpl kpiDefinitionServiceMock;
    @Mock
    ReadinessBoundCollector readinessBoundCollectorMock;
    @Mock
    CalculationRepositoryImpl calculationRepositoryMock;
    @InjectMocks
    CalculationLimitCalculator objectUnderTest;

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class ComplexExecutionGroupWithoutPredecessors {
        @MethodSource("provideData")
        @ParameterizedTest(name = "[{index}] Readiness bounds ''{1}'', completed time ''{2}'' ==> ''{0}''")
        void whenThisGroupNotHasPredecessors_theCalculatedTimesWillBeTheStartAndEndTimes(final CalculationLimit expected,
                                                                                         final Map<Integer, ReadinessBound> readinessBounds,
                                                                                         final LocalDateTime complexCompleted) {
            final Map<KpiDefinitionEntity, ReadinessBound> transformedReadinessBound = convertKeysToDefinition(readinessBounds);

            final List<KpiDefinitionEntity> kpiDefinitionEntities = List.copyOf(transformedReadinessBound.keySet());
            when(kpiDefinitionServiceMock.findKpiDefinitionsByExecutionGroup("executionGroup")).thenReturn(kpiDefinitionEntities);
            when(readinessBoundCollectorMock.calculateReadinessBounds("executionGroup", kpiDefinitionEntities)).thenReturn(transformedReadinessBound);
            when(calculationRepositoryMock.getLastComplexCalculationReliability("executionGroup")).thenReturn(complexCompleted);

            final CalculationLimit actual = objectUnderTest.calculateLimit("executionGroup");

            verify(kpiDefinitionServiceMock).findKpiDefinitionsByExecutionGroup("executionGroup");
            verify(readinessBoundCollectorMock).calculateReadinessBounds("executionGroup", kpiDefinitionEntities);
            verify(calculationRepositoryMock).getLastComplexCalculationReliability("executionGroup");

            assertThat(actual).isEqualTo(expected);
        }

        Stream<Arguments> provideData() {
            return Stream.of(
                    Arguments.of( /* When calculation start time is the  earliestAllowedStartTime */
                            calculationLimit(calculationStartTime(12, 59), calculationEndTime(14, 0)),
                            Map.of(
                                    dataLookBackLimit(15), readinessBound(lowerReadinessBound(12, 59), upperReadinessBound(13, 25)),
                                    dataLookBackLimit(30), readinessBound(lowerReadinessBound(13, 20), upperReadinessBound(13, 30)),
                                    dataLookBackLimit(45), readinessBound(lowerReadinessBound(12, 59), upperReadinessBound(13, 30)),
                                    dataLookBackLimit(60), readinessBound(lowerReadinessBound(13, 40), upperReadinessBound(14, 0))
                            ),
                            complexCompleted(12, 0)
                    ),
                    Arguments.of( /* When calculation start time is the min(lowerReadinessBounds) */
                            calculationLimit(calculationStartTime(13, 1), calculationEndTime(14, 0)),
                            Map.of(
                                    dataLookBackLimit(15), readinessBound(lowerReadinessBound(13, 1), upperReadinessBound(13, 25)),
                                    dataLookBackLimit(30), readinessBound(lowerReadinessBound(13, 2), upperReadinessBound(14, 0)),
                                    dataLookBackLimit(45), readinessBound(lowerReadinessBound(13, 30), upperReadinessBound(13, 45)),
                                    dataLookBackLimit(60), readinessBound(lowerReadinessBound(13, 45), upperReadinessBound(14, 0))
                            ),
                            complexCompleted(12, 0)
                    ),
                    Arguments.of( /* When late data is present */
                            calculationLimit(calculationStartTime(10, 0), calculationEndTime(14, 0)),
                            Map.of(
                                    dataLookBackLimit(15), readinessBound(lowerReadinessBound(10, 0), upperReadinessBound(10, 25)),
                                    dataLookBackLimit(30), readinessBound(lowerReadinessBound(13, 2), upperReadinessBound(14, 0)),
                                    dataLookBackLimit(45), readinessBound(lowerReadinessBound(13, 30), upperReadinessBound(13, 45)),
                                    dataLookBackLimit(180), readinessBound(lowerReadinessBound(13, 45), upperReadinessBound(14, 0))
                            ),
                            complexCompleted(12, 0)
                    ),
                    Arguments.of( /* Only late data present */
                            calculationLimit(calculationStartTime(9, 30), calculationEndTime(12, 0)),
                            Map.of(
                                    dataLookBackLimit(180), readinessBound(lowerReadinessBound(9, 30), upperReadinessBound(9, 40))
                            ),
                            complexCompleted(12, 0)
                    ),
                    Arguments.of( /* Only late data present but it is ignored by data look back limit*/
                            calculationLimit(calculationStartTime(10, 30), calculationEndTime(12, 0)),
                            Map.of(
                                    dataLookBackLimit(90), readinessBound(lowerReadinessBound(5, 0), upperReadinessBound(5, 30))
                            ),
                            complexCompleted(12, 0)
                    )
            );
        }
    }

    static Map<KpiDefinitionEntity, ReadinessBound> convertKeysToDefinition(final Map<Integer, ReadinessBound> readinessBounds) {
        final Map<KpiDefinitionEntity, ReadinessBound> result = new HashMap<>(readinessBounds.size());

        readinessBounds.forEach((dataLookBackLimit, readinessBound) -> {
            result.put(kpiDefinition(dataLookBackLimit), readinessBound);
        });

        return result;
    }

    static Integer dataLookBackLimit(final int dataLookBackLimit) {
        return dataLookBackLimit;
    }

    static LocalDateTime calculationEndTime(final int hour, final int minute) {
        return testTime(hour, minute);
    }

    static LocalDateTime calculationStartTime(final int hour, final int minute) {
        return testTime(hour, minute);
    }

    static LocalDateTime upperReadinessBound(final int hour, final int minute) {
        return testTime(hour, minute);
    }

    static LocalDateTime lowerReadinessBound(final int hour, final int minute) {
        return testTime(hour, minute);
    }

    static LocalDateTime testTime(final int hour, final int minute) {
        return LocalDateTime.of(2_022, Month.OCTOBER, 8, hour, minute);
    }

    static LocalDateTime complexCompleted(final int hour, final int minute) {
        return testTime(hour, minute);
    }

    static KpiDefinitionEntity kpiDefinition(final int dataLookbackLimit) {
        return KpiDefinitionEntity.builder().withDataLookbackLimit(dataLookbackLimit).withAggregationPeriod(60).build();
    }

    static CalculationLimit calculationLimit(final LocalDateTime calculationStartTime, final LocalDateTime calculationEndTime) {
        return CalculationLimit.builder().calculationStartTime(calculationStartTime).calculationEndTime(calculationEndTime).build();
    }

    static ReadinessBound readinessBound(final LocalDateTime lowerReadinessBound, final LocalDateTime upperReadinessBound) {
        return ReadinessBound.builder().upperReadinessBound(upperReadinessBound).lowerReadinessBound(lowerReadinessBound).build();
    }
}