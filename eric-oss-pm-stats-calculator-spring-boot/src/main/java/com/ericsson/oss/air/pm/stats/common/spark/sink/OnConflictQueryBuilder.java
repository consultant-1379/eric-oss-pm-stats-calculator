/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.spark.sink;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import scala.collection.mutable.WrappedArray;

/**
 * A utility class that provides a generic implementation to construct SQL statements and queries.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class OnConflictQueryBuilder {


    /**
     * Used to build a query to insert or update the database.
     *
     * @param tableName
     *            {@link String} The name of the table to update
     * @param fieldsToUpdate
     *            {@link Map} The columns that need to be updated and the new values to update them to
     * @param primaryKeys
     *            {@link List} Specifies the column names which constitute the primary key
     * @param updateColumns
     *            {@link List} Specifies the column names which needs to be updated in case of conflict
     * @return {@link String} The sql update query
     */
    static String buildUpsertQuery(final String tableName, final Map<String, Object> fieldsToUpdate,
            final List<String> primaryKeys, final List<String> updateColumns) {
        Validate.notEmpty(fieldsToUpdate, "'fieldsToUpdate' is empty");
        Validate.notEmpty(updateColumns, "'updateColumns' is empty");

        final boolean isNotPartitioned = tableName.endsWith("_") || !fieldsToUpdate.containsKey("aggregation_begin_time");
        final StringBuilder sqlQueryBuilder = new StringBuilder("INSERT INTO ").append(isNotPartitioned ? tableName : deduceTableName(tableName, fieldsToUpdate));
        buildSetClauseForUpdate(fieldsToUpdate, sqlQueryBuilder);
        buildOnConflict(primaryKeys, updateColumns, fieldsToUpdate, sqlQueryBuilder);
        sqlQueryBuilder.append(';');
        log.debug("SQL upsert query '{}'", sqlQueryBuilder);
        return sqlQueryBuilder.toString();
    }

    private static String deduceTableName(final String tableName, final Map<String, Object> fieldsToUpdate) {
        final Timestamp aggregationBeginTime = ((Timestamp) fieldsToUpdate.get("aggregation_begin_time"));
        return tableName + "_p_" + aggregationBeginTime.toLocalDateTime().toLocalDate().toString().replace("-","_");
    }

    private static void buildSetClauseForUpdate(final Map<String, Object> fieldsToUpdate, final StringBuilder sqlQueryBuilder) {
        final StringBuilder keysBuilder = new StringBuilder("(");
        final StringBuilder valuesBuilder = new StringBuilder("(");
        for (final Map.Entry<String, Object> entry : fieldsToUpdate.entrySet()) {
            keysBuilder.append(String.format("\"%s\",", entry.getKey()));
            valuesBuilder.append(String.format("%s,", extractValue(entry.getValue())));
        }
        keysBuilder.setLength(keysBuilder.length() - 1);
        valuesBuilder.setLength(valuesBuilder.length() - 1);
        keysBuilder.append(')');
        valuesBuilder.append(')');

        sqlQueryBuilder.append(keysBuilder).append(" VALUES ").append(valuesBuilder);
    }

    private static void buildOnConflict(final List<String> primaryKeys, final List<String> updateColumns, final Map<String, Object> fieldsToUpdate,
            final StringBuilder queryBuilder) {
        if (primaryKeys != null && !primaryKeys.isEmpty()) {
            queryBuilder.append(" ON CONFLICT (");
            for (final String key : primaryKeys) {
                queryBuilder.append(String.format("\"%s\"", key)).append(',');
            }
            queryBuilder.setLength(queryBuilder.length() - 1);
            queryBuilder.append(") DO UPDATE SET ");

            for (final String column : updateColumns) {
                final Object value = fieldsToUpdate.get(column);
                queryBuilder.append(String.format("\"%s\"=%s,", column, extractValue(value)));
            }
            queryBuilder.setLength(queryBuilder.length() - 1);
        }
    }

    private static String extractValue(final Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof WrappedArray) {
            final WrappedArray wrappedArray = (WrappedArray) value;
            return String.format("'%s'", transformWrappedArray(wrappedArray));
        }
        return String.format("'%s'", value.toString());
    }

    private static String transformWrappedArray(final WrappedArray wrappedArray) {
        String arrayAsString = wrappedArray.toString();
        arrayAsString = arrayAsString.replace(wrappedArray.stringPrefix(), "");
        final StringBuilder stringArray = new StringBuilder(arrayAsString);
        stringArray.setCharAt(0, '{');
        stringArray.setCharAt(stringArray.length() - 1, '}');
        return stringArray.toString();
    }
}
