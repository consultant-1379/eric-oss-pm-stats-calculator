/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.repository.internal.api;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import com.ericsson.oss.air.pm.stats.common.model.collection.Database;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;

public interface DataSourceRepository {
    /**
     * Find the <strong>MAX</strong> value for the <strong>aggregation_begin_time</strong> column in the provided {@link Database}|{@link Table}.
     *
     * @param database
     *         {@link Database} to search in.
     * @param table
     *         {@link Table} to search in.
     * @return {@link Optional} containing {@link Timestamp} if the value is present otherwise {@link Optional#empty()}.
     */
    Optional<Timestamp> findMaxAggregationBeginTime(Database database, Table table);

    /**
     * Find the <strong>MIN</strong> value for the <strong>aggregation_begin_time</strong> column in the provided {@link Database}|{@link Table}.
     *
     * @param database
     *         {@link Database} to search in.
     * @param table
     *         {@link Table} to search in.
     * @return {@link Optional} containing {@link Timestamp} if the value is present otherwise {@link Optional#empty()}.
     */
    Optional<Timestamp> findAggregationBeginTime(Database database, Table table);

    /**
     * Checks if the provided {@link Datasource} is available or not.
     *
     * @param datasource
     *         {@link Datasource} to verify.
     * @return true if the {@link Datasource} is available otherwise false.
     */
    boolean isAvailable(Datasource datasource);

    /**
     * Checks if a given {@link JdbcDatasource}'s table contains data for the provided time range.
     *
     * @param jdbcDatasource
     *         {@link JdbcDatasource} to check.
     * @param tableName
     *         the table to check data for.
     * @param start
     *         start of the range, inclusive.
     * @param end
     *         end of the range, inclusive.
     * @return true if the {@link JdbcDatasource}'s table contains data for the provided time range otherwise false.
     */
    boolean doesTableContainData(JdbcDatasource jdbcDatasource, String tableName, LocalDateTime start, LocalDateTime end);
}
