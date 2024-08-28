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

import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.air.pm.stats.calculation.limits.model.ReadinessBound;
import com.ericsson.oss.air.pm.stats.calculation.readiness.ReadinessWindowCollector;
import com.ericsson.oss.air.pm.stats.calculation.readiness.model.ReadinessWindow;
import com.ericsson.oss.air.pm.stats.calculation.readiness.model.domain.DataSource;
import com.ericsson.oss.air.pm.stats.calculation.readiness.model.domain.DefinitionName;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReadinessBoundCollectorTest {
    final LocalDateTime testTime = LocalDateTime.of(2_022, Month.OCTOBER, 8, 12, 0);

    @Mock
    ReadinessWindowCollector readinessWindowCollectorMock;

    @InjectMocks
    ReadinessBoundCollector objectUnderTest;

    @Test
    void shouldCollectReadinessBounds() {
        final List<KpiDefinitionEntity> kpiDefinitions = List.of(
                kpiDefinition("A", 20),
                kpiDefinition("B", 15)
        );

        when(readinessWindowCollectorMock.collect("complexGroup", kpiDefinitions)).thenReturn(Map.of(
                DefinitionName.of("A"), List.of(
                        readinessWindow(testTime, testTime.plusMinutes(10), "datasource1"),
                        readinessWindow(testTime.plusMinutes(5), testTime.plusMinutes(5), "datasource2")
                ),
                DefinitionName.of("B"), List.of(
                        readinessWindow(testTime, testTime.plusMinutes(10), "datasource1"),
                        readinessWindow(testTime.plusMinutes(5), testTime.plusMinutes(10), "datasource3")
                )
        ));

        final Map<KpiDefinitionEntity, ReadinessBound> actual = objectUnderTest.calculateReadinessBounds("complexGroup", kpiDefinitions);

        verify(readinessWindowCollectorMock).collect("complexGroup", kpiDefinitions);

        Assertions.assertThat(actual).containsOnly(
                entry(kpiDefinition("A", 20), readinessBound(testTime, testTime.minusMinutes(15))),
                entry(kpiDefinition("B", 15), readinessBound(testTime, testTime.minusMinutes(5)))
        );
    }

    @Test
    void shouldBeEmptyCollectedReadinessBoundsFromEmptyWindows() {
        final List<KpiDefinitionEntity> kpiDefinitions = List.of(
                kpiDefinition("A", 20),
                kpiDefinition("B", 15)
        );
        when(readinessWindowCollectorMock.collect("complexGroup", kpiDefinitions)).thenReturn(Map.of(
                DefinitionName.of("A"), List.of(),
                DefinitionName.of("B"), List.of()
        ));

        final Map<KpiDefinitionEntity, ReadinessBound> actual = objectUnderTest.calculateReadinessBounds("complexGroup", kpiDefinitions);

        verify(readinessWindowCollectorMock).collect("complexGroup", kpiDefinitions);

        Assertions.assertThat(actual).isEmpty();
    }

    static ReadinessBound readinessBound(final LocalDateTime lowerReadinessBound, final LocalDateTime upperReadinessBound) {
        return ReadinessBound.builder().lowerReadinessBound(lowerReadinessBound).upperReadinessBound(upperReadinessBound).build();
    }

    static ReadinessWindow readinessWindow(final LocalDateTime earliestCollectedData, final LocalDateTime latestCollectedData, final String datasource) {
        return ReadinessWindow.of(DataSource.of(datasource), earliestCollectedData, latestCollectedData);
    }

    static KpiDefinitionEntity kpiDefinition(final String name, final int dataReliabilityOffset) {
        return KpiDefinitionEntity.builder().withName(name).withDataReliabilityOffset(dataReliabilityOffset).build();
    }
}