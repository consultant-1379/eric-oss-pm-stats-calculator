/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql.calculation.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;

/**
 * Utils class used for sql query creation.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SqlUtils {
    private static final Pattern PATTERN_TO_SPLIT_ON_DOT = Pattern.compile("\\.");
    private static final Pattern PATTERN_TO_SPLIT_ON_WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern PATTERN_TO_SPLIT_ON_ALIAS = Pattern.compile("\\s+(?i)AS\\s+");
    private static final Pattern PATTERN_TO_CHECK_FUNCTION = Pattern.compile("^[A-Za-z][A-Za-z0-9_]+\\(.+\\)\\s+(?i)AS\\s+[a-zA-Z][a-zA-Z0-9_]{0,55}");

    private static final String SQL_COLUMN_ALIAS_CHECK = " as ";
    private static final String SQL_TABLE_NAME_AND_COLUMN_NAME_SEPARATOR = ".";

    /**
     * This method simplifies column expression by keeping either alias(if present) or column name, based on @param isAliasPreferred, and by keeping
     * table name based on @param isTableNameToRemove.
     *
     * @param columnExpressions   A {@link List} of column expressions to be simplified
     * @param isAliasPreferred    Flag, if set to true cleaned column expression will be set to alias otherwise to column name
     * @param isTableNameToRemove Flag, if set to true cleaned column expression will have table name (if present)
     * @return A {@link List} of simplified column expressions
     */
    public static List<String> simplifyColumnExpression(final List<String> columnExpressions, final boolean isAliasPreferred,
                                                        final boolean isTableNameToRemove) {

        final String simplifiedColumnExpressionTemplate = "${tableName}${separator}${columnName}";

        final Set<String> columnsCleaned = new LinkedHashSet<>(columnExpressions.size());
        for (final String columnExpression : columnExpressions) {
            if (PATTERN_TO_CHECK_FUNCTION.matcher(columnExpression).matches()) {
                columnsCleaned.add(getFunctionOrAlias(columnExpression, isAliasPreferred));
            } else {
                final Map<String, String> values = new HashMap<>(3);
                values.put("tableName", isTableNameToRemove ? StringUtils.EMPTY : getTableName(columnExpression));
                values.put("separator", StringUtils.isEmpty(values.get("tableName")) ? StringUtils.EMPTY : SQL_TABLE_NAME_AND_COLUMN_NAME_SEPARATOR);
                values.put("columnName", getColumnName(columnExpression, isAliasPreferred));

                columnsCleaned.add(StringSubstitutor.replace(simplifiedColumnExpressionTemplate, values));
            }
        }
        return new ArrayList<>(columnsCleaned);
    }

    private static String getColumnName(final String columnExpression, final boolean isAliasPreferred) {
        final int requiredValueIndex = isAliasPreferred ? 2 : 0;

        String columnName = columnExpression;
        if (columnExpression.toLowerCase(Locale.ENGLISH).contains(SQL_TABLE_NAME_AND_COLUMN_NAME_SEPARATOR)) {
            columnName = PATTERN_TO_SPLIT_ON_DOT.split(columnExpression)[1];
        }

        if (columnExpression.toLowerCase(Locale.ENGLISH).contains(SQL_COLUMN_ALIAS_CHECK)) {
            columnName = PATTERN_TO_SPLIT_ON_WHITESPACE.split(columnName)[requiredValueIndex];
        }
        return columnName;
    }

    private static String getTableName(final String column) {
        String tableName = StringUtils.EMPTY;
        if (column.toLowerCase(Locale.ENGLISH).contains(SQL_TABLE_NAME_AND_COLUMN_NAME_SEPARATOR)) {
            tableName = PATTERN_TO_SPLIT_ON_DOT.split(column)[0];
        }
        return tableName;
    }

    private static String getFunctionOrAlias(final String expression, final boolean isAliasPreferred) {
        return PATTERN_TO_SPLIT_ON_ALIAS.split(expression)[isAliasPreferred ? 1 : 0];
    }
}
