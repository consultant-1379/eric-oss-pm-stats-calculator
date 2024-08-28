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
import static kpi.model.util.PatternConstants.PATTERN_FOR_FLOAT;
import static lombok.AccessLevel.PRIVATE;

import java.util.regex.Pattern;

import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

/**
 * Class for keeping the datasource, table and column on kpi expression.
 */
@Data
@AllArgsConstructor(access = PRIVATE)
public final class SourceColumn {

    @EqualsAndHashCode.Exclude
    private Datasource datasource;

    private Table table;
    private final String column;

    public static SourceColumn from(@NonNull final Reference reference) {
        final Datasource datasource = reference.datasource().orElse(null);
        final Table table = reference.table().orElse(null);
        return new SourceColumn(datasource, table, reference.columnOrAliasName());
    }

    public SourceColumn(final String sqlExpression) {
        String tempExpression = removeLeadingAndTrailingCharacter(sqlExpression, "(", ")");

        if (tempExpression.contains(DATASOURCE_DELIMITER)) {
            final String[] datasourceTokens = tempExpression.split(Pattern.quote(DATASOURCE_DELIMITER), 2);
            tempExpression = datasourceTokens[1];
            datasource = Datasource.of(datasourceTokens[0]);
        }

        final String[] tokens = tempExpression.split(Pattern.quote(TABLE_DELIMITER), 2);
        table = Table.of(ColumnUtils.removeSparkFunctionFromColumn(tokens[0]));
        column = ColumnUtils.removeSparkFunctionFromColumn(tokens[1]);
    }

    public boolean hasTable() {
        return table != null;
    }

    public boolean hasDatasource() {
        return datasource != null;
    }

    public boolean isNonInMemoryDataSource() {
        return !Datasource.isInMemory(datasource);
    }

    public static boolean isValidSource(final String input) {
        return input.contains(TABLE_DELIMITER) && !PATTERN_FOR_FLOAT.matcher(removeLeadingAndTrailingCharacter(input, "(", ")")).find();
    }

    private static String removeLeadingAndTrailingCharacter(final String input, final String leading, final String trailing) {
        return input.substring(input.startsWith(leading) ? 1 : 0, input.endsWith(trailing) ? (input.length() - 1) : input.length());
    }
}
