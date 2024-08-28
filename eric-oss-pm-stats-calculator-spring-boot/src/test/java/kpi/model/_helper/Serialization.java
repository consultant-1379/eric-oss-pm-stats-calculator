/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model._helper;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Serialization {
    public static final ObjectMapper OBJECT_MAPPER = JsonMapper
            .builder()
            .configure(MapperFeature.ALLOW_COERCION_OF_SCALARS, false)
            .build();

    @SneakyThrows
    public static <T> T deserialize(final String content, final Class<T> type) {
        return OBJECT_MAPPER.readValue(content, type);
    }
}
