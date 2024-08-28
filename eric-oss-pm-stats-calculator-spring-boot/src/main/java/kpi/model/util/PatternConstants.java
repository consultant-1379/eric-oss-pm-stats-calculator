/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.util;

import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Pattern Constants.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PatternConstants {
    public static final Pattern PATTERN_TO_FIND_PARAMETER_TOKEN = Pattern.compile("\\$\\{[^{}]*}");
    //Pattern to find exactly one FROM, case-insensitive:
    //The 1st part of the regex creates a negative lookahead for having more than 1 froms in the expression, case-insensitive.
    //The 2nd part of the regex matches exactly one FROM, case-insensitive.
    public static final Pattern PATTERN_TO_FIND_EXACTLY_ONE_FROM = Pattern.compile("^(?!.*\\bFROM\\b.*\\bFROM\\b)(?=.*\\bFROM\\b).+$", Pattern.CASE_INSENSITIVE);
    public static final Pattern PATTERN_NAME_ATTRIBUTE = Pattern.compile("^[a-z][a-zA-Z0-9_]{0,55}$");
    public static final Pattern PATTERN_PARAMETER_NAME = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_.]{0,55}$");
    public static final Pattern PATTERN_ALIAS_ATTRIBUTE = Pattern.compile("^[a-z][a-z0-9_]{0,38}$");
    public static final Pattern PATTERN_INP_DATA_IDENTIFIER = Pattern.compile("^[^\\|]+\\|[^\\|]+\\|[^\\|]+$");
    public static final Pattern PATTERN_TO_FIND_MORE_THAN_ONE_DATASOURCE = Pattern.compile("^(?!.*://.*://).*$");
    public static final Pattern PATTERN_FOR_FLOAT = Pattern.compile("-?\\d+\\.\\d+$");
    private static final Pattern PATTERN_TABLE = Pattern.compile("[a-zA-Z][a-zA-Z0-9_]{0,55}");
    private static final Pattern PATTERN_COLUMN = Pattern.compile("[a-zA-Z][a-zA-Z0-9_]{0,55}");
    private static final Pattern PATTERN_PARAMETER = Pattern.compile("'\\$\\{[a-zA-Z][a-zA-Z0-9_.]*\\}'");
    private static final String ALLOWED_AGGREGATION_ELEMENT_UDF = "FDN_PARSE";

    // TODO: use a more readable substitutor than the String.format, e.g. StringSubstitutor.replace().
    /**
     * In case of complex and on-demand KPIs, a Spark User-defined function is allowed as aggregation element:
     * <br>
     * <pre>{@code
     *      <udf_function>(<table>.<column>, arg2) AS <column>
     * }</pre>
     */
    public static final Pattern PATTERN_AGGREGATION_ELEMENT_UDF = Pattern.compile(
            String.format("^%3$s\\(%1$s\\.%2$s\\,\\s?\".+\"\\)\\s+(?i)AS\\s+%2$s$", PATTERN_TABLE, PATTERN_COLUMN, ALLOWED_AGGREGATION_ELEMENT_UDF));

    /**
     * The 'AGGREGATION_ELEMENT' attribute valid patterns for simple KPIs are:
     * <br>
     * <pre>{@code
     *      <table>.<column>
     *      <table>.<column> AS <column>
     *      'parameter' AS <column>
     * }</pre>
     */
    public static final Pattern PATTERN_AGGREGATION_ELEMENT = Pattern.compile(
            String.format(
                    "^%1$s\\.%2$s$|^%1$s\\.%2$s (?i)AS %2$s$|^%3$s (?i)AS %2$s$", PATTERN_TABLE, PATTERN_COLUMN, PATTERN_PARAMETER));

    /**
     * The 'AGGREGATION_ELEMENT' attribute valid patterns for non-simple KPIs are:
     * <br>
     * <pre>{@code
     *      <table>.<column>
     *      <table>.<column> AS <column>
     *      'parameter' AS <column>
     *      <udf_function>(<table>.<column>, arg2) AS <column>
     * }</pre>
     */
    public static final Pattern PATTERN_NON_SIMPLE_AGGREGATION_ELEMENT = Pattern.compile(
            String.format(
                    "^%1$s\\.%2$s$|^%1$s\\.%2$s\\s+(?i)AS\\s+%2$s$|^%3$s\\s+(?i)AS\\s+%2$s$|^%4$s",
                    PATTERN_TABLE, PATTERN_COLUMN, PATTERN_PARAMETER, PATTERN_AGGREGATION_ELEMENT_UDF));
}
