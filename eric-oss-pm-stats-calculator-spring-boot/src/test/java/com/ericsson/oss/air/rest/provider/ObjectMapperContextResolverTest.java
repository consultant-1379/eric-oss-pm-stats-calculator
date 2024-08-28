/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ObjectMapperContextResolverTest {
    ObjectMapperContextResolver objectUnderTest = new ObjectMapperContextResolver();

    @Test
    void shouldVerifyRegisteredModules() {
        final ObjectMapper objectMapper = objectUnderTest.getContext(Object.class);

        Assertions.assertThat(objectMapper.getRegisteredModuleIds()).contains(
                "jackson-datatype-jsr310"  /* JavaTimeModule is utilized on the REST API */
        );
    }
}