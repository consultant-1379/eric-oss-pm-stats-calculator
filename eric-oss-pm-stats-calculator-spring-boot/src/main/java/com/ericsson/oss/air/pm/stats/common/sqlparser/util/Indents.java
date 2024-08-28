/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.sqlparser.util;

import static java.lang.System.lineSeparator;
import static lombok.AccessLevel.PRIVATE;

import java.util.StringJoiner;
import java.util.StringTokenizer;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public final class Indents {

    public static String indent(final String multiline, final int count, final int times) {
        final StringTokenizer stringTokenizer = new StringTokenizer(multiline, lineSeparator());

        final String indent = indent(count, times);
        if (stringTokenizer.countTokens() == 1) {
            return indent + multiline;
        }

        final StringJoiner stringJoiner = new StringJoiner(lineSeparator());
        while (stringTokenizer.hasMoreTokens()) {
            final String token = stringTokenizer.nextToken();
            stringJoiner.add(indent + token);
        }

        return String.join(lineSeparator(), stringJoiner.toString());
    }

    public static String indent(final int times) {
        return indent(2, times);
    }

    public static String indent(final int count, final int times) {
        return " ".repeat(count).repeat(times);
    }
}
