/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.verification.util;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.FieldUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringifyUtils {

    private static final String JOINING_DELIMITER = ",";
    private static final String DELIMITER_CONTENT_ENCLOSER = "\"";
    @SneakyThrows
    public static String stringifyFields(final Object target) {
        final List<String> fieldNamesToInspect = collectFieldNames(target);
        final List<Object> fieldValues = new ArrayList<>(fieldNamesToInspect.size());

        for (final String fieldName : fieldNamesToInspect) {
            final Object fieldValue = FieldUtils.readDeclaredField(target, fieldName, true);
            fieldValues.add(fieldValue);
        }

        return fieldValues.stream()
                          .map(StringifyUtils::stringify)
                          .collect(Collectors.joining(JOINING_DELIMITER));
    }

    private static List<String> collectFieldNames(final Object target) {
        return Arrays.stream(target.getClass().getDeclaredFields())
                     .map(Field::getDeclaredAnnotations)
                     .flatMap(Arrays::stream)
                     .filter(FieldToInspect.class::isInstance)
                     .map(FieldToInspect.class::cast)
                     .map(FieldToInspect::fieldName)
                     .collect(Collectors.toList());
    }

    private static String stringify(final Object value) {
        if (value instanceof LocalDateTime) {
            return Timestamp.valueOf((LocalDateTime) value).toString();
        }

        if (value instanceof JsonArray) {
            final JsonArray jsonArray = (JsonArray) value;
            return jsonArray.toString()
                            .replace('[', '{')
                            .replace(']', '}');
        }

        if (value instanceof String && ((String) value).contains(JOINING_DELIMITER)) {
            return DELIMITER_CONTENT_ENCLOSER + value + DELIMITER_CONTENT_ENCLOSER;
        }

        return Objects.toString(value);
    }

    @Documented
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface FieldToInspect {
        String fieldName();
    }
}
