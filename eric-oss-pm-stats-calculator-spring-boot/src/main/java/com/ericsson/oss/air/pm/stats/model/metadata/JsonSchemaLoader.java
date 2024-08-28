/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.model.metadata;

import java.io.IOException;
import java.io.UncheckedIOException;

import com.ericsson.oss.air.pm.stats.calculator.api.model.SchemaModel;
import com.ericsson.oss.air.pm.stats.common.resources.utils.ResourceLoaderUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to load models from counter and KPI schemas.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonSchemaLoader {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Creates {@link SchemaModel} object from the given JSON schema.
     *
     * @param resourceFile Name of the resource file
     * @param valueType    {@link SchemaModel} type to bind content to
     * @return Deserialized implementation of {@link SchemaModel}
     */
    public static <T extends SchemaModel> T getModelConfig(final String resourceFile, final Class<T> valueType) {
        final T readValue = readSchema(resourceFile, valueType);
        log.info("Schema loaded with name: '{}' and with version: '{}' for the namespace: '{}'",
                readValue.getName(),
                readValue.getVersion(),
                readValue.getNamespace());
        return readValue;
    }

    private static <T extends SchemaModel> T readSchema(final String resourceFile, final Class<T> valueType) {
        try {
            return OBJECT_MAPPER.readValue(ResourceLoaderUtils.getClasspathResourceAsString(resourceFile), valueType);
        } catch (final IOException e) {
            throw new UncheckedIOException(String.format("Unable to create SchemaModel for resource file of '%s'", resourceFile), e);
        }
    }
}
