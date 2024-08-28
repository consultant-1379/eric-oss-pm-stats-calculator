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

import static com.ericsson.oss.air.pm.stats.model.entity.KpiDefinition.COLUMN_SCHEMA_CATEGORY;
import static com.ericsson.oss.air.pm.stats.model.entity.KpiDefinition.COLUMN_SCHEMA_DATA_SPACE;
import static com.ericsson.oss.air.pm.stats.model.entity.KpiDefinition.COLUMN_SCHEMA_NAME;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ColumnUtils {
    public static Integer nullableInteger(final Object object) {
        return Optional.ofNullable(object)
                .map(String::valueOf)
                .map(Integer::parseInt)
                .orElse(null);
    }

    public static Boolean nullableBoolean(final Object object) {
        return Optional.ofNullable(object)
                .map(String::valueOf)
                .map(Boolean::parseBoolean)
                .orElse(null);
    }

    public static String nullableString(final Object object) {
        return Optional.ofNullable(object)
                .map(String::valueOf)
                .orElse(null);
    }

    public static Object[] nullableList(final List<String> object) {
        return Optional.ofNullable(object)
                .map(List::toArray)
                .orElse(null);
    }

    public static Timestamp nullableTimestamp(final LocalDateTime localDateTime) {
        return Optional.ofNullable(localDateTime)
                .map(Timestamp::valueOf)
                .orElse(null);
    }

    /**
     * In this current implementation the indexdef look like this:
     *
     * <pre>
     * CREATE UNIQUE INDEX kpi_cell_sector_60_p_2022_06_06_ui
     * ON kpi.kpi_cell_sector_60_p_2022_06_06
     * USING btree ("agg_column_0", "agg_column_1", "agg_column_2")
     * </pre>
     * <p>
     * From the SQL we are interested in only the columns.
     *
     * @param indexdef the index definition to parse
     * @return {@link List} of index columns
     */
    public static List<Column> parseUniqueIndexColumns(@NonNull final String indexdef) {
        return Arrays.stream(indexdef.substring(indexdef.lastIndexOf('(') + 1, indexdef.lastIndexOf(')')).split(","))
                .map(String::trim)
                .map(Column::of)
                .sorted(Comparator.comparing(Column::getName))
                .collect(Collectors.toList());
    }

    public static DataIdentifier decideIfPresent(ResultSet rs) throws SQLException {
        final String dataSpace = rs.getString(COLUMN_SCHEMA_DATA_SPACE);
        final String category = rs.getString(COLUMN_SCHEMA_CATEGORY);
        final String schemaName = rs.getString(COLUMN_SCHEMA_NAME);

        if (dataSpace == null || category == null || schemaName == null) {
            return null;
        }
        return DataIdentifier.of(dataSpace, category, schemaName);
    }
}
