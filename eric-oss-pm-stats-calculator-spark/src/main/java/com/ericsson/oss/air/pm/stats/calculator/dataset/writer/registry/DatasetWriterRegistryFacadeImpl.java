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

import com.ericsson.oss.air.pm.stats.calculator.dataset.writer.api.DatasetWriter;
import com.ericsson.oss.air.pm.stats.calculator.dataset.writer.registry.api.DatasetWriterRegistryFacade;
import com.ericsson.oss.air.pm.stats.calculator.model.exception.DatasetWriterNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatasetWriterRegistryFacadeImpl implements DatasetWriterRegistryFacade {
    private final PluginRegistry<DatasetWriter, Integer> datasetWriterPluginRegistry;

    @Override
    public DatasetWriter datasetWriter(final Integer aggregationPeriod) {
        return datasetWriterPluginRegistry.getPluginFor(aggregationPeriod, () -> {
            final String message = String.format("%s for aggregation period %s not found", DatasetWriter.class.getSimpleName(), aggregationPeriod);
            return new DatasetWriterNotFoundException(message);
        });
    }
}
