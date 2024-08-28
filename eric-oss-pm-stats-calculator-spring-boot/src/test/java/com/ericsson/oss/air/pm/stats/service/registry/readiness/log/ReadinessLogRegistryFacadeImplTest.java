/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.registry.readiness.log;

import static com.ericsson.oss.air.pm.stats._util.TestHelpers.uuid;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType.ON_DEMAND;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType.SCHEDULED_COMPLEX;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.stream.Stream;
import javax.enterprise.inject.Instance;

import com.ericsson.oss.air.pm.stats.service.api.CalculationService;
import com.ericsson.oss.air.pm.stats.service.api.registry.ReadinessLogRegistry;
import com.ericsson.oss.air.pm.stats.service.registry.readiness.log.exception.ReadinessLogRegistryNotFoundException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReadinessLogRegistryFacadeImplTest {
    @Mock
    CalculationService calculationServiceMock;
    @Mock
    Instance<ReadinessLogRegistry> readinessLogRegistriesMock;

    @InjectMocks
    ReadinessLogRegistryFacadeImpl objectUnderTest;

    @Test
    void shouldRaiseException_whenNoReadinessLogRegistrySupportsKpiType(@Mock final ReadinessLogRegistry readinessLogRegistryMock) {
        final UUID uuid = uuid("0248d181-f6a2-4a44-ae45-1cc9ddd71594");

        when(calculationServiceMock.forceFetchKpiTypeByCalculationId(uuid)).thenReturn(ON_DEMAND);
        when(readinessLogRegistriesMock.stream()).thenReturn(Stream.of(readinessLogRegistryMock));
        when(readinessLogRegistryMock.doesSupport(ON_DEMAND)).thenReturn(false);

        Assertions.assertThatThrownBy(() -> objectUnderTest.readinessLogRegistry(uuid))
                .isInstanceOf(ReadinessLogRegistryNotFoundException.class)
                .hasMessage("No '%s' implementation supports '%s' KPI type", ReadinessLogRegistry.class.getSimpleName(), ON_DEMAND);

        verify(calculationServiceMock).forceFetchKpiTypeByCalculationId(uuid);
        verify(readinessLogRegistriesMock).stream();
        verify(readinessLogRegistryMock).doesSupport(ON_DEMAND);
    }

    @Test
    void shouldVerifyReadinessLogRegistry(@Mock final ReadinessLogRegistry readinessLogRegistryMock) {
        final UUID uuid = uuid("54e2b020-97e4-4db6-af05-4d261f809722");

        when(calculationServiceMock.forceFetchKpiTypeByCalculationId(uuid)).thenReturn(SCHEDULED_COMPLEX);
        when(readinessLogRegistriesMock.stream()).thenReturn(Stream.of(readinessLogRegistryMock));
        when(readinessLogRegistryMock.doesSupport(SCHEDULED_COMPLEX)).thenReturn(true);

        final ReadinessLogRegistry actual = objectUnderTest.readinessLogRegistry(uuid);

        verify(calculationServiceMock).forceFetchKpiTypeByCalculationId(uuid);
        verify(readinessLogRegistriesMock).stream();
        verify(readinessLogRegistryMock).doesSupport(SCHEDULED_COMPLEX);

        Assertions.assertThat(actual).isEqualTo(readinessLogRegistryMock);
    }
}