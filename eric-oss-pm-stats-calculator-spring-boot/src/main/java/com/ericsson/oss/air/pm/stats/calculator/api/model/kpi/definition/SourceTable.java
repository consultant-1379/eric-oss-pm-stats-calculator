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

import static com.ericsson.oss.air.pm.stats.common.model.constants.DatasourceConstants.DATASOURCE_DELIMITER;
import static com.ericsson.oss.air.pm.stats.common.model.constants.DatasourceConstants.TABLE_DELIMITER;
import static lombok.AccessLevel.PRIVATE;

import java.util.Objects;
import java.util.regex.Pattern;

import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

/**
 * Class for keeping the datasource and table on kpi expression.
 */
@Data
@AllArgsConstructor(access = PRIVATE)
public final class SourceTable {

    private final Datasource datasource;
    private final Table table;

    public static SourceTable from(@NonNull final Relation relation) {
        return new SourceTable(relation.datasource().orElse(null), relation.table());
    }

    public SourceTable(final String sqlExpression) {
        final String sqlExpressionWithoutBraces = removeLeadingAndTrailingCharacter(sqlExpression, "(", ")");

        final String[] datasourceTokens = sqlExpressionWithoutBraces.split(Pattern.quote(DATASOURCE_DELIMITER), 2);
        datasource = Datasource.of(datasourceTokens[0]);
        final String tableStr = datasourceTokens[1];
        table = tableStr.contains(TABLE_DELIMITER) ? Table.of(tableStr.split(Pattern.quote(TABLE_DELIMITER), 2)[0]) : Table.of(tableStr);
    }

    public boolean isNonInMemory() {
        return datasource.isNonInMemory();
    }

    public boolean hasSameTable(final Table table) {
        return Objects.equals(this.table, table);
    }

    public boolean isInternal() {
        return datasource.isInternal();
    }

    public static boolean isValidSource(final String input) {
        return input.contains(DATASOURCE_DELIMITER);
    }

    private static String removeLeadingAndTrailingCharacter(final String input, final String leading, final String trailing) {
        return input.substring(input.startsWith(leading) ? 1 : 0, input.endsWith(trailing) ? (input.length() - 1) : input.length());
    }
}
