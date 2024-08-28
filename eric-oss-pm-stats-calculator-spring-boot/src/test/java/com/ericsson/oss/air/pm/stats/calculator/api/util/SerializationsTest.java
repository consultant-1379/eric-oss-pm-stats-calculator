/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.util;

import java.util.Map;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.SerializationException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class SerializationsTest {

    @Test
    void shouldSerialize() {
        final String actual = Serializations.writeValueAsString(Map.of("key", "value"));
        Assertions.assertThat(actual).isEqualTo("{\"key\":\"value\"}");
    }

    @Test
    void shouldThrowExceptionOnSerializationFailure() {
        Assertions.assertThatThrownBy(() -> Serializations.writeValueAsString(new Object()))
                .isInstanceOf(SerializationException.class)
                .hasMessage(
                        "No serializer found for class java.lang.Object and no properties discovered to create " +
                                "BeanSerializer (to avoid exception, disable SerializationFeature.FAIL_ON_EMPTY_BEANS)"
                );
    }
}