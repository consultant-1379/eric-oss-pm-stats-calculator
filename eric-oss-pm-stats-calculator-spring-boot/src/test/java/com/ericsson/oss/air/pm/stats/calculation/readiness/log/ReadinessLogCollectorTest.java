/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.readiness.log;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculation.readiness.model.domain.DefinitionName;
import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity.KpiDefinitionEntityBuilder;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;
import com.ericsson.oss.air.pm.stats.service.api.ReadinessLogService;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReadinessLogCollectorTest {
    @Mock
    ReadinessLogService readinessLogServiceMock;

    @InjectMocks
    ReadinessLogCollector objectUnderTest;

    @Test
    void shouldCollectReadinessLogs(
            @Mock final ReadinessLog readinessLogMock1,
            @Mock final ReadinessLog readinessLogMock2,
            @Mock final ReadinessLog readinessLogMock3) {
        final KpiDefinitionEntity entity6 = kpiDefinitionEntity("6", "group_1");
        final KpiDefinitionEntity entity4 = kpiDefinitionEntity("4", "group_2");
        final KpiDefinitionEntity entity5 = kpiDefinitionEntity("5", "group_2");

        final KpiDefinitionEntity entity8 = kpiDefinitionEntity("8", "group_3");
        final KpiDefinitionEntity entity10 = kpiDefinitionEntity("10", "group_3");

        when(readinessLogServiceMock.collectLatestReadinessLogs("complexGroup", Set.of(ExecutionGroup.builder().withName("group_1").build()))).thenReturn(Set.of(readinessLogMock1));
        when(readinessLogServiceMock.collectLatestReadinessLogs("complexGroup", Set.of(ExecutionGroup.builder().withName("group_2").build(), ExecutionGroup.builder().withName("group_3").build()))).thenReturn(
                Set.of(readinessLogMock2, readinessLogMock3)
        );

        final Map<DefinitionName, Set<ReadinessLog>> actual = objectUnderTest.collectReadinessLogs("complexGroup", Map.of(
                DefinitionName.of("W"), Set.of(entity6),
                DefinitionName.of("Z"), Set.of(entity4, entity5, entity8, entity10)
        ));

        verify(readinessLogServiceMock).collectLatestReadinessLogs("complexGroup", Set.of(ExecutionGroup.builder().withName("group_1").build()));
        verify(readinessLogServiceMock).collectLatestReadinessLogs("complexGroup", Set.of(ExecutionGroup.builder().withName("group_2").build(), ExecutionGroup.builder().withName("group_3").build()));

        Assertions.assertThat(actual.entrySet()).satisfiesExactlyInAnyOrder(
                entry -> {
                    Assertions.assertThat(entry.getKey()).isEqualTo(DefinitionName.of("W"));
                    Assertions.assertThat(entry.getValue()).containsExactlyInAnyOrder(readinessLogMock1);
                },
                entry -> {
                    Assertions.assertThat(entry.getKey()).isEqualTo(DefinitionName.of("Z"));
                    Assertions.assertThat(entry.getValue()).containsExactlyInAnyOrder(readinessLogMock2, readinessLogMock3);
                }
        );
    }

    static KpiDefinitionEntity kpiDefinitionEntity(final String name, final String executionGroup) {
        final KpiDefinitionEntityBuilder builder = KpiDefinitionEntity.builder();
        builder.withName(name);
        builder.withExecutionGroup(ExecutionGroup.builder().withName(executionGroup).build());
        return builder.build();
    }

}