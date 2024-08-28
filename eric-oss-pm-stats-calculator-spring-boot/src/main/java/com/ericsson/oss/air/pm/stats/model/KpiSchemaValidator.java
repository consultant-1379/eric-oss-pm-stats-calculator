/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.model;

import java.util.List;
import java.util.regex.Pattern;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.model.util.Instances;

import com.google.common.collect.Iterables;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Checks if a value contains a parameter token.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KpiSchemaValidator {

    /**
     * Checks if a value contains a parameter token.
     *
     * @param value the value to be checked for parameter token.
     * @return true if parameter token present in value.
     */
    public static boolean valueContainsParameterToken(final Object value) {
        if (value instanceof List) {
            final List<?> list = (List<?>) value;
            if (isFilter(Iterables.getFirst(list, null))) {
                return listContainsParameterToken(Instances.cast(list, Filter.class));
            }
            final List<String> listValue = (List) value;
            return listContainsParameterToken(listValue);
        }
        return stringContainsParameterToken(String.valueOf(value));
    }

    private static boolean isFilter(Object obj) {
        return obj instanceof Filter;
    }

    private static boolean listContainsParameterToken(final List<?> listValue) {
        if (Instances.isInstanceOf(listValue, Filter.class)) {
            @SuppressWarnings("unchecked") final List<Filter> filters = (List<Filter>) listValue;

            for (final Filter filter : filters) {
                if (stringContainsParameterToken(filter)) {
                    return true;
                }
            }

            return false;
        }

        for (final Object element : listValue) {
            if (stringContainsParameterToken(element.toString())) {
                return true;
            }
        }
        return false;
    }

    private static boolean stringContainsParameterToken(final Filter filter) {
        return stringContainsParameterToken(filter.getName());
    }

    private static boolean stringContainsParameterToken(final String attributeValue) {
        final Pattern parameterTokenPattern = Pattern.compile("\\$\\{.*}");
        return attributeValue == null || parameterTokenPattern.matcher(attributeValue).find();
    }
}
