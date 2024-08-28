/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.exception.util;

import static org.assertj.core.api.InstanceOfAssertFactories.throwable;

import java.util.Arrays;
import java.util.List;

import com.ericsson.oss.air.rest.exception._util.ExceptionUtils;
import com.ericsson.oss.air.rest.exception.util.JacksonPathsTest.Wrapper.Stuff;
import com.ericsson.oss.air.rest.exception.util.JacksonPathsTest.Wrapper.Stuff.OtherStuff;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class JacksonPathsTest {
    static final ObjectMapper OBJECT_MAPPER = ExceptionUtils.objectMapper();

    @Test
    void shouldNormalizePath() throws Exception {
        final Stuff stuff = new Stuff();
        stuff.setOtherStuffs(Arrays.asList(new OtherStuff("other_stuff_1"), null));

        final Wrapper wrapper = new Wrapper();
        wrapper.setStuff(stuff);

        final String json = OBJECT_MAPPER.writeValueAsString(wrapper);

        Assertions.assertThatThrownBy(() -> OBJECT_MAPPER.readValue(json, Wrapper.class))
                .asInstanceOf(throwable(JsonMappingException.class))
                .satisfies(jsonMappingException -> {
                    final String actual = JacksonPaths.normalizePath(jsonMappingException);
                    Assertions.assertThat(actual).isEqualTo("stuff -> otherStuffs[1]");
                });
    }

    @Data
    static class Wrapper {
        private Stuff stuff;

        @Data
        @Setter(onMethod_ = @JsonSetter(contentNulls = Nulls.FAIL))
        static class Stuff {
            private List<OtherStuff> otherStuffs;

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            static class OtherStuff {
                private String element;
            }
        }
    }
}