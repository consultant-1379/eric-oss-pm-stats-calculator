/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.registry;

import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.DataLoader;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDatasets;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.plugin.core.PluginRegistry;

@ExtendWith(MockitoExtension.class)
class DataLoaderRegistryFacadeImplTest {
    @Mock PluginRegistry<DataLoader, Collection<KpiDefinition>> dataLoaderRegistryMock;

    @InjectMocks DataLoaderRegistryFacadeImpl objectUnderTest;

    @Test
    void shouldCreateDefaultKafkaLoader(
            @Mock final DataLoader kafkaDataLoaderMock,
            @Mock final Set<KpiDefinition> kpiDefinitionsMock,
            @Mock final Iterator<TableDatasets> iteratorMock
    ) {
        when(dataLoaderRegistryMock.getRequiredPluginFor(kpiDefinitionsMock)).thenReturn(kafkaDataLoaderMock);
        when(kafkaDataLoaderMock.iteratorDefaultFilter(kpiDefinitionsMock)).thenReturn(iteratorMock);

        final Iterator<TableDatasets> actual = objectUnderTest.defaultFilterIterator(kpiDefinitionsMock);

        Assertions.assertThat(actual).isEqualTo(iteratorMock);
    }

    @Test
    void shouldCreateCustomKafkaLoader(
            @Mock final DataLoader kafkaDataLoaderMock,
            @Mock final Set<KpiDefinition> kpiDefinitionsMock
    ) {
        when(dataLoaderRegistryMock.getRequiredPluginFor(kpiDefinitionsMock)).thenReturn(kafkaDataLoaderMock);
        when(kafkaDataLoaderMock.loadDatasetsWithCustomFilter(Set.of(), kpiDefinitionsMock)).thenReturn(TableDatasets.of());

        final TableDatasets actual = objectUnderTest.customFilterDatasets(kpiDefinitionsMock, Set.of());

        Assertions.assertThat(actual).isEqualTo(TableDatasets.of());
    }
}