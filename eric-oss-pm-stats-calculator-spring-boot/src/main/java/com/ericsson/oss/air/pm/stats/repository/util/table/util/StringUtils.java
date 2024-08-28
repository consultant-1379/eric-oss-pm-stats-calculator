/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util.table.util;

import java.util.Collection;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtils {

    public static String commaJoinEnquote(final Collection<? extends Column> elements) {
        return elements.stream().map(Column::getName).map(StringUtils::ensureQuoted).collect(Collectors.joining(", "));
    }

    public static String generatePrimaryKeyName(@NonNull final Table table) {
        return String.format("%s_pkey", table.getName());
    }

    public static String enquoteLiteral(final String literal) {
        return String.format("\"%s\"", literal);
    }

    public static String ensureQuoted(final String literal) {
        if (literal.startsWith("\"") && literal.endsWith("\"")) {
            return literal;
        }
        return enquoteLiteral(literal);
    }

    public static Collection<String> enquoteCollectionElements(final Collection<String> elements) {
        return elements.stream().map(StringUtils::enquoteLiteral).collect(Collectors.toList());
    }
}
