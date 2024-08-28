/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.readiness;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculation.readiness.log.ReadinessLogCollector;
import com.ericsson.oss.air.pm.stats.calculation.readiness.model.ReadinessWindow;
import com.ericsson.oss.air.pm.stats.calculation.readiness.model.domain.DataSource;
import com.ericsson.oss.air.pm.stats.calculation.readiness.model.domain.DefinitionName;
import com.ericsson.oss.air.pm.stats.calculation.readiness.window.ReadinessWindowCalculator;
import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity.KpiDefinitionEntityBuilder;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;
import com.ericsson.oss.air.pm.stats.repository.SimpleKpiDependencyCache;
import com.ericsson.oss.air.pm.stats.service.api.ReadinessLogService;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReadinessWindowCollectorTest {
    static final LocalDateTime TEST_TIME = LocalDateTime.of(2_022, Month.OCTOBER, 8, 12, 0);

    @Mock
    ReadinessLogService readinessLogServiceMock;
    @Mock
    SimpleKpiDependencyCache simpleKpiDependencyCacheMock;

    @Spy
    ReadinessWindowCalculator readinessWindowCalculatorSpy; /* Intentionally spy */
    @InjectMocks
    ReadinessLogCollector readinessLogCollectorSpy = spy(new ReadinessLogCollector()); /* Intentionally spy */

    @InjectMocks
    ReadinessWindowCollector objectUnderTest;

    @Test
    void shouldCollect() { /* Testing the whole collection process */
        final KpiDefinitionEntity entity6 = kpiDefinitionEntity("6", "group_1");

        final KpiDefinitionEntity entity4 = kpiDefinitionEntity("4", "group_2");
        final KpiDefinitionEntity entity5 = kpiDefinitionEntity("5", "group_2");

        final KpiDefinitionEntity entity8 = kpiDefinitionEntity("8", "group_3");
        final KpiDefinitionEntity entity10 = kpiDefinitionEntity("10", "group_3");

        final List<String> definitionNames = List.of("W", "Z");

        when(simpleKpiDependencyCacheMock.loadDependenciesForMultipleDefinitions(definitionNames)).thenReturn(Map.of(
                "W", Set.of(entity6),
                "Z", Set.of(entity4, entity5, entity8, entity10)
        ));

        when(readinessLogServiceMock.collectLatestReadinessLogs("complexGroup", Set.of(ExecutionGroup.builder().withName("group_1").build()))).thenReturn(
                Set.of(readinessLog(1, TEST_TIME, TEST_TIME.plusMinutes(10), "cell"))
        );

        when(readinessLogServiceMock.collectLatestReadinessLogs("complexGroup", Set.of(ExecutionGroup.builder().withName("group_2").build(), ExecutionGroup.builder().withName("group_3").build()))).thenReturn(
                Set.of(
                        readinessLog(2, TEST_TIME.plusMinutes(5), TEST_TIME.plusMinutes(5), "cell"),
                        readinessLog(3, TEST_TIME, TEST_TIME.plusMinutes(5), "support_unit")
                )
        );

        final Map<DefinitionName, List<ReadinessWindow>> actual = objectUnderTest.collect("complexGroup", definitionNames);

        verify(simpleKpiDependencyCacheMock).loadDependenciesForMultipleDefinitions(definitionNames);
        verify(readinessLogServiceMock).collectLatestReadinessLogs("complexGroup", Set.of(ExecutionGroup.builder().withName("group_1").build()));
        verify(readinessLogServiceMock).collectLatestReadinessLogs("complexGroup", Set.of(ExecutionGroup.builder().withName("group_2").build(), ExecutionGroup.builder().withName("group_3").build()));

        Assertions.assertThat(actual.entrySet()).satisfiesExactlyInAnyOrder(
                readinessWindowEntry -> {
                    Assertions.assertThat(readinessWindowEntry.getKey()).isEqualTo(DefinitionName.of("W"));
                    Assertions.assertThat(readinessWindowEntry.getValue()).containsExactlyInAnyOrder(
                            readinessWindow("cell", TEST_TIME, TEST_TIME.plusMinutes(10))
                    );
                },
                readinessWindowEntry -> {
                    Assertions.assertThat(readinessWindowEntry.getKey()).isEqualTo(DefinitionName.of("Z"));
                    Assertions.assertThat(readinessWindowEntry.getValue()).containsExactlyInAnyOrder(
                            readinessWindow("cell", TEST_TIME, TEST_TIME.plusMinutes(10)),
                            readinessWindow("support_unit", TEST_TIME, TEST_TIME.plusMinutes(5))
                    );
                }
        );
    }

    static ReadinessWindow readinessWindow(final String dataSource, final LocalDateTime earliestCollectedData, final LocalDateTime latestCollectedData) {
        return ReadinessWindow.of(DataSource.of(dataSource), earliestCollectedData, latestCollectedData);
    }

    static ReadinessLog readinessLog(final int id, final LocalDateTime earliestCollectedData, final LocalDateTime latestCollectedData, final String datasource) {
        return ReadinessLog.builder()
                .withId(id)
                .withDatasource(datasource)
                .withEarliestCollectedData(earliestCollectedData)
                .withLatestCollectedData(latestCollectedData)
                .build();
    }

    static KpiDefinitionEntity kpiDefinitionEntity(final String name, final String executionGroup) {
        final KpiDefinitionEntityBuilder builder = KpiDefinitionEntity.builder();
        builder.withName(name);
        builder.withExecutionGroup(ExecutionGroup.builder().withName(executionGroup).build());
        builder.withFilters(List.of());
        return builder.build();
    }

}