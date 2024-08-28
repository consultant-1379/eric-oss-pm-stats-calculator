/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.constant;

import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Pattern Constants.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PatternConstants {

    public static final Pattern PATTERN_TO_SPLIT_ON_WHITESPACE = Pattern.compile("\\s+");
    public static final Pattern PATTERN_TO_SPLIT_ON_DOT = Pattern.compile("\\.");
    public static final Pattern PATTERN_TO_SPLIT_WHITESPACE_AND_DOT = Pattern.compile("[\\.\\s+]");
    public static final Pattern PATTERN_TO_SPLIT_ON_DB_DELIMITER = Pattern.compile("://");
    public static final Pattern MATHEMATICAL_OPERATORS = Pattern.compile("[%/*+\\-=()]");
}
