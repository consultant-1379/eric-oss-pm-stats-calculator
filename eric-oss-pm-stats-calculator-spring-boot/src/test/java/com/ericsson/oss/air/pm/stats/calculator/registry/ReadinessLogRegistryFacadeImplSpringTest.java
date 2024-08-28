/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.registry;

import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType.SCHEDULED_COMPLEX;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.service.api.CalculationService;
import com.ericsson.oss.air.pm.stats.service.api.registry.ReadinessLogRegistry;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.plugin.core.PluginRegistry;

@ExtendWith(MockitoExtension.class)
class ReadinessLogRegistryFacadeImplSpringTest {
    @Mock CalculationService calculationServiceMock;
    @Mock PluginRegistry<ReadinessLogRegistry, KpiType> pluginRegistryMock;
    @InjectMocks ReadinessLogRegistryFacadeImplSpring objectUnderTest;

    @Test
    void shouldVerifyReadinessLogRegistry(@Mock final ReadinessLogRegistry readinessLogRegistryMock) {
        final UUID uuid = UUID.fromString("54e2b020-97e4-4db6-af05-4d261f809722");

        when(calculationServiceMock.forceFetchKpiTypeByCalculationId(uuid)).thenReturn(SCHEDULED_COMPLEX);
        when(pluginRegistryMock.getPluginFor(eq(SCHEDULED_COMPLEX), any())).thenReturn(readinessLogRegistryMock);

        final ReadinessLogRegistry actual = objectUnderTest.readinessLogRegistry(uuid);

        verify(calculationServiceMock).forceFetchKpiTypeByCalculationId(uuid);
        verify(pluginRegistryMock).getPluginFor(eq(SCHEDULED_COMPLEX), any());

        Assertions.assertThat(actual).isEqualTo(readinessLogRegistryMock);
    }
}