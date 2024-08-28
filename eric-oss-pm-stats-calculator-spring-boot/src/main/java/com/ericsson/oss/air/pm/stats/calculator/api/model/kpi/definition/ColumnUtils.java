/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiAggregationType;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;

/**
 * Utility class to parse column names.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ColumnUtils {

    private static final String FUNCTION_SYNTAX_LEADING_BRACKET = "(";
    private static final String FUNCTION_SYNTAX_TRAILING_BRACKET = ")";
    private static final String FUNCTION_SYNTAX_PARAMETER_DELIMITER = ",";
    private static final Pattern ARRAY_INDEX_PATTERN = Pattern.compile("\\[(.*?)\\]");
    private static final List<String> SPARK_FUNCTIONS = Arrays.asList("aggregate", "slice", "count", "TO_DATE", "NULLIF");
    private static final String UDF_FILTER_FUNCTION = "FDN_PARSE";

    /**
     * Parses a column by stripping of any aggregation types or spark functions.
     *
     * @param inputColumn the column to parse
     * @return the parsed column
     */
    public static String removeSparkFunctionFromColumn(final String inputColumn) {
        final List<String> functions = KpiAggregationType.valuesAsList().stream().map(KpiAggregationType::getJsonType).collect(toList());
        functions.addAll(SPARK_FUNCTIONS);
        functions.add(UDF_FILTER_FUNCTION);

        for (final String function : functions) {
            if (StringUtils.containsIgnoreCase(inputColumn, function) && inputColumn.contains(FUNCTION_SYNTAX_LEADING_BRACKET)) {
                final int length = inputColumn.contains(FUNCTION_SYNTAX_TRAILING_BRACKET) ? inputColumn.indexOf(FUNCTION_SYNTAX_TRAILING_BRACKET)
                        : inputColumn.length();
                final String cleanedColumn = inputColumn.substring(inputColumn.lastIndexOf(FUNCTION_SYNTAX_LEADING_BRACKET) + 1,
                        inputColumn.contains(FUNCTION_SYNTAX_PARAMETER_DELIMITER) ? inputColumn.indexOf(FUNCTION_SYNTAX_PARAMETER_DELIMITER)
                                : length);
                return removeArrayIndexSyntax(cleanedColumn);
            }
        }
        return cleanColumn(removeArrayIndexSyntax(inputColumn));
    }

    private static String removeArrayIndexSyntax(final String parsedColumn) {
        return ARRAY_INDEX_PATTERN.split(parsedColumn, 2)[0];
    }

    private static String cleanColumn(final String parsedColumn) {
        return parsedColumn.replaceAll(FUNCTION_SYNTAX_PARAMETER_DELIMITER, "").replaceAll("\\)", "");
    }
}
