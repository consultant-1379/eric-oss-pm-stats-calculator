/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.configuration.property.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class ConfigurationLoggers {

    public static String createLog(@NonNull final Object target) {
        return createLog(target, 1);
    }

    private static String createLog(@NonNull final Object target, final int depth) {
        final Field[] declaredFields = target.getClass().getDeclaredFields();

        final List<String> fields = new ArrayList<>();
        for (final Field declaredField : declaredFields) {
            if (isStatic(declaredField) || declaredField.isSynthetic()) {
                continue;
            }

            final String fieldName = declaredField.getName();
            final Object fieldValue = readFieldValue(target, fieldName);

            if (isNestedConfiguration(declaredField)) {
                fields.add(fieldName(fieldName, depth));
                fields.add(createLog(fieldValue, depth + 1));
            } else {
                fields.add(field(fieldName, fieldValue, depth));
            }
        }

        return String.join(System.lineSeparator(),fields);
    }

    private static boolean isNestedConfiguration(@NonNull final AnnotatedElement annotatedElement) {
        return annotatedElement.isAnnotationPresent(NestedConfiguration.class);
    }

    private static boolean isStatic(@NonNull final Member member) {
        return Modifier.isStatic(member.getModifiers());
    }

    private static String field(final String fieldName, final Object value, final int depth) {
        return String.format("%s = %s", fieldName(fieldName, depth), value);
    }

    private static String fieldName(final String fieldName, final int depth) {
        return String.format("%s%s", StringUtils.repeat("\t", depth), fieldName);
    }

    @SneakyThrows
    private static Object readFieldValue(final @NonNull Object target, final String fieldName) {
        return FieldUtils.readDeclaredField(target, fieldName, true);
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface NestedConfiguration { }
}
