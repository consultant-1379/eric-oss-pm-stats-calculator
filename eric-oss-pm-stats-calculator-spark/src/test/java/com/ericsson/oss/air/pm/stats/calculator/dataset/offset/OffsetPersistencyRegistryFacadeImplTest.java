/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.offset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.model.exception.OffsetPersistencyNotFoundException;
import com.ericsson.oss.air.pm.stats.calculator.dataset.offset.registry.OffsetPersistencyRegistryFacadeImpl;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.plugin.core.PluginRegistry;

@ExtendWith(MockitoExtension.class)
public class OffsetPersistencyRegistryFacadeImplTest {

    @Mock private PluginRegistry<OffsetPersistency, Collection<KpiDefinition>> offsetPersistencyRegistryMock;
    @InjectMocks private OffsetPersistencyRegistryFacadeImpl objectUnderTest;

    @Nested
    class OffsetPersistencyScenario {
        @Mock
        Set<KpiDefinition> kpiDefinitionsMock;

        @Captor ArgumentCaptor<Supplier<RuntimeException>> supplierArgumentCaptor;

        @Test
        void shouldCreateOffsetPersistencyKafka(@Mock final OffsetPersistency offsetPersistencyKafka) {
            when(offsetPersistencyRegistryMock.getPluginFor(eq(kpiDefinitionsMock), any())).thenReturn(offsetPersistencyKafka);

            final OffsetPersistency actual = objectUnderTest.offsetPersistency(kpiDefinitionsMock);

            verify(offsetPersistencyRegistryMock).getPluginFor(eq(kpiDefinitionsMock), any());

            Assertions.assertThat(actual).isEqualTo(offsetPersistencyKafka);
        }

        @Test
        void shouldRaiseException_whenDataLoaderIsNotRegisteredUnderKpiDefinitions() {
            objectUnderTest.offsetPersistency(null);

            verify(offsetPersistencyRegistryMock).getPluginFor(any(), supplierArgumentCaptor.capture());

            Assertions.assertThat(supplierArgumentCaptor.getValue().get())
                    .isInstanceOf(OffsetPersistencyNotFoundException.class)
                    .hasMessage("OffsetPersistency for KPI Definitions null not found");
        }
    }
}
