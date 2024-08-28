/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util;

import static com.ericsson.oss.air.pm.stats.repository.util.PartitionUtils.createPartitionUniqueIndexName;

import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.repository.util.partition.Partition;
import com.ericsson.oss.air.pm.stats.repository.util.table.util.StringUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Utils class for creating a table.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableUtils {
    private static final String COLUMN_DELIMINATOR = ", ";
    private static final String WHITESPACE = " ";

    /**
     * To get unique index creation SQL.
     * <br>
     * Creates the following pattern:
     * <pre>
     * CREATE UNIQUE INDEX IF NOT EXISTS &lt;index_name&gt; ON &lt;table_name&gt;
     * ON (&lt;index_column_1&gt;, &lt;index_column_2&gt;, ...);
     * </pre>
     *
     * @param partition {@link Partition} containing all information to create unique index SQL.
     * @return unique index creation SQL.
     */
    public static String createUniqueIndexSql(@NonNull final Partition partition) {
        final StringBuilder createUniqueIndexSql = new StringBuilder("CREATE UNIQUE INDEX IF NOT EXISTS %s ON %s (");

        for (final String uniqueIndexColumn : partition.getUniqueIndexColumns().stream().sorted(String::compareTo).collect(Collectors.toList())) {
            createUniqueIndexSql.append(WHITESPACE);
            createUniqueIndexSql.append(StringUtils.enquoteLiteral((uniqueIndexColumn)));
            createUniqueIndexSql.append(COLUMN_DELIMINATOR);
        }
        createUniqueIndexSql.deleteCharAt(createUniqueIndexSql.lastIndexOf(","));
        createUniqueIndexSql.append(");");
        return String.format(createUniqueIndexSql.toString(), createPartitionUniqueIndexName(partition), partition.getPartitionName());
    }
}
