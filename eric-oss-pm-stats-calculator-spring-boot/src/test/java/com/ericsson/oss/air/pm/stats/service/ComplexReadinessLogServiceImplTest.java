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
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;
import com.ericsson.oss.air.pm.stats.repository.api.ComplexReadinessLogRepository;
import com.ericsson.oss.air.pm.stats.test_utils.DriverManagerMock;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ComplexReadinessLogServiceImplTest {
    @Mock
    ComplexReadinessLogRepository complexReadinessLogRepositoryMock;

    @InjectMocks
    ComplexReadinessLogServiceImpl objectUnderTest;

    @ParameterizedTest
    @EnumSource(value = KpiType.class, names = "SCHEDULED_COMPLEX")
    void shouldSupport(final KpiType kpiType) {
        final boolean actual = objectUnderTest.doesSupport(kpiType);
        Assertions.assertThat(actual).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = KpiType.class, names = {"SCHEDULED_SIMPLE", "ON_DEMAND"})
    void shouldNotSupports(final KpiType kpiType) {
        final boolean actual = objectUnderTest.doesSupport(kpiType);
        Assertions.assertThat(actual).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = KpiType.class, names = "SCHEDULED_COMPLEX")
    void shouldSupports(final KpiType kpiType) {
        final boolean actual = objectUnderTest.supports(kpiType);
        Assertions.assertThat(actual).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = KpiType.class, names = {"SCHEDULED_SIMPLE", "ON_DEMAND"})
    void shouldNotSupport(final KpiType kpiType) {
        final boolean actual = objectUnderTest.supports(kpiType);
        Assertions.assertThat(actual).isFalse();
    }

    @Test
    void shouldFindByCalculationId(@Mock final List<ReadinessLog> readinessLogsMock) {
        final UUID uuid = uuid("21fa53e3-c486-4aee-ba55-9b501bd5f475");
        when(complexReadinessLogRepositoryMock.findByCalculationId(uuid)).thenReturn(readinessLogsMock);

        final List<ReadinessLog> actual = objectUnderTest.findByCalculationId(uuid);

        verify(complexReadinessLogRepositoryMock).findByCalculationId(uuid);

        Assertions.assertThat(actual).isEqualTo(readinessLogsMock);
    }

    @Test
    void shouldSave() {
        DriverManagerMock.prepare(connectionMock -> {
            final UUID complexCalculationId = uuid("37f6b2ea-1863-457b-87d4-20d64be02fc4");
            final List<String> simpleExecutionGroups = List.of("dependency");

            objectUnderTest.save(complexCalculationId, simpleExecutionGroups);
            verify(complexReadinessLogRepositoryMock).save(connectionMock, complexCalculationId, simpleExecutionGroups);
        });
    }
}