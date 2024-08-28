/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util;

import java.sql.Array;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RepositoryUtils {
    public static List<String> parseArrayToString(final Array array) throws SQLException {
        return isArrayNull(array) ? null : getObjects(array)
                .collect(Collectors.toList());
    }

    public static List<Filter> parseArrayToFilter(final Array array) throws SQLException {
        return isArrayNull(array) ? null : getObjects(array)
                .map(Filter::new)
                .collect(Collectors.toList());
    }

    private static boolean isArrayNull(final Array array) {
        return array == null;
    }

    private static Stream<String> getObjects(final Array array) throws SQLException {
        return Arrays.stream(((Object[]) array.getArray()))
                .map(Objects::toString);
    }
}
