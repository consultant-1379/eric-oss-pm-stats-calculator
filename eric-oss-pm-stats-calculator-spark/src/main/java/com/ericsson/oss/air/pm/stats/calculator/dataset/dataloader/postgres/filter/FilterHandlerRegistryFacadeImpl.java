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

import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.api.FilterHandler;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.api.FilterHandlerRegistryFacade;
import com.ericsson.oss.air.pm.stats.calculator.model.exception.FilterHandlerNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FilterHandlerRegistryFacadeImpl implements FilterHandlerRegistryFacade {
    private final PluginRegistry<FilterHandler, FilterType> filterHandlerRegistry;

    @Override
    public FilterHandler filterHandler(final FilterType filterType) {
        return filterHandlerRegistry.getPluginFor(filterType, () -> {
            final String message = String.format("%s with type %s not found", FilterHandler.class.getSimpleName(), filterType);
            return new FilterHandlerNotFoundException(message);
        });
    }

}
