/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter;

import static com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.FilterType.DEFAULT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.api.FilterHandler;
import com.ericsson.oss.air.pm.stats.calculator.model.exception.FilterHandlerNotFoundException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
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
class FilterHandlerRegistryFacadeImplTest {
    @Mock PluginRegistry<FilterHandler, FilterType> filterHandlerRegistryMock;

    @InjectMocks FilterHandlerRegistryFacadeImpl objectUnderTest;

    @Nested
    @DisplayName("Testing filter handler")
    class FilterHandlerTest {
        @Mock FilterHandler filterHandlerMock;

        @Captor ArgumentCaptor<Supplier<RuntimeException>> supplierArgumentCaptor;

        @Test
        void shouldFilterHandler() {
            when(filterHandlerRegistryMock.getPluginFor(eq(DEFAULT), any())).thenReturn(filterHandlerMock);

            objectUnderTest.filterHandler(DEFAULT);

            verify(filterHandlerRegistryMock).getPluginFor(eq(DEFAULT), any());
        }

        @Test
        void shouldRaiseException_whenFilterHandlerIsNorRegisteredUnderType() {
            objectUnderTest.filterHandler(null);

            verify(filterHandlerRegistryMock).getPluginFor(any(), supplierArgumentCaptor.capture());

            Assertions.assertThat(supplierArgumentCaptor.getValue().get())
                      .isInstanceOf(FilterHandlerNotFoundException.class)
                      .hasMessage("FilterHandler with type null not found");
        }
    }
}