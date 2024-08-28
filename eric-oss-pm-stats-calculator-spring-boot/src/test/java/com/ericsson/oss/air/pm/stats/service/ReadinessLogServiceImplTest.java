/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service;

import static com.ericsson.oss.air.pm.stats._util.TestHelpers.uuid;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;
import com.ericsson.oss.air.pm.stats.repository.api.ReadinessLogRepository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReadinessLogServiceImplTest {
    @Mock
    ReadinessLogRepository readinessLogRepositoryMock;

    @InjectMocks
    ReadinessLogServiceImpl objectUnderTest;

    @ParameterizedTest
    @EnumSource(value = KpiType.class, names = "SCHEDULED_SIMPLE")
    void shouldSupport(final KpiType kpiType) {
        final boolean actual = objectUnderTest.doesSupport(kpiType);
        Assertions.assertThat(actual).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = KpiType.class, names = {"SCHEDULED_COMPLEX", "ON_DEMAND"})
    void shouldNotSupport(final KpiType kpiType) {
        final boolean actual = objectUnderTest.doesSupport(kpiType);
        Assertions.assertThat(actual).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = KpiType.class, names = "SCHEDULED_SIMPLE")
    void shouldSupports(final KpiType kpiType) {
        final boolean actual = objectUnderTest.supports(kpiType);
        Assertions.assertThat(actual).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = KpiType.class, names = {"SCHEDULED_COMPLEX", "ON_DEMAND"})
    void shouldNotSupports(final KpiType kpiType) {
        final boolean actual = objectUnderTest.supports(kpiType);
        Assertions.assertThat(actual).isFalse();
    }

    @Test
    void shouldFindByCalculationId(@Mock final List<ReadinessLog> readinessLogsMock) {
        final UUID uuid = uuid("21fa53e3-c486-4aee-ba55-9b501bd5f475");
        when(readinessLogRepositoryMock.findByCalculationId(uuid)).thenReturn(readinessLogsMock);

        final List<ReadinessLog> actual = objectUnderTest.findByCalculationId(uuid);

        verify(readinessLogRepositoryMock).findByCalculationId(uuid);

        Assertions.assertThat(actual).isEqualTo(readinessLogsMock);
    }

    @Test
    void shouldCollectReadinessLogs(@Mock final ReadinessLog readinessLogMock1, @Mock final ReadinessLog readinessLogMock2) {
        final ExecutionGroup executionGroup1 = ExecutionGroup.builder().withName("executionGroup1").build();
        final ExecutionGroup executionGroup2 = ExecutionGroup.builder().withName("executionGroup2").build();
        final List<String> executionGroupNames = List.of(executionGroup1.name(), executionGroup2.name());

        when(readinessLogRepositoryMock.findLatestReadinessLogsByExecutionGroup("complexGroup", executionGroupNames))
                .thenReturn(List.of(readinessLogMock1, readinessLogMock2));

        final Set<ReadinessLog> actual = objectUnderTest.collectLatestReadinessLogs("complexGroup", List.of(executionGroup1, executionGroup2));

        verify(readinessLogRepositoryMock).findLatestReadinessLogsByExecutionGroup("complexGroup", executionGroupNames);

        Assertions.assertThat(actual).containsExactlyInAnyOrder(readinessLogMock1, readinessLogMock2);
    }
}