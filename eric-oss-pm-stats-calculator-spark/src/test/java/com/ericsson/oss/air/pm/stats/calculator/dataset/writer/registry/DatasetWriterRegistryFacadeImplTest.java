/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.writer.registry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import com.ericsson.oss.air.pm.stats.calculator.dataset.writer.api.DatasetWriter;
import com.ericsson.oss.air.pm.stats.calculator.model.exception.DatasetWriterNotFoundException;

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
class DatasetWriterRegistryFacadeImplTest {
    @Mock PluginRegistry<DatasetWriter, Integer> registryMock;

    @InjectMocks DatasetWriterRegistryFacadeImpl objectUnderTest;

    @Nested
    class TestingRegistry {
        @Mock DatasetWriter datasetWriterMock;

        @Captor ArgumentCaptor<Supplier<RuntimeException>> supplierArgumentCaptor;

        @Test
        void shouldLocateWriter() {
            when(registryMock.getPluginFor(eq(1), any())).thenReturn(datasetWriterMock);

            objectUnderTest.datasetWriter(1);

            verify(registryMock).getPluginFor(eq(1), any());
        }

        @Test
        void shouldRaiseException_whenWriterIsNotRegisteredForAggregationPeriod() {
            objectUnderTest.datasetWriter(null);

            verify(registryMock).getPluginFor(any(), supplierArgumentCaptor.capture());

            Assertions.assertThat(supplierArgumentCaptor.getValue().get())
                      .isInstanceOf(DatasetWriterNotFoundException.class)
                      .hasMessage("DatasetWriter for aggregation period null not found");
        }
    }
}