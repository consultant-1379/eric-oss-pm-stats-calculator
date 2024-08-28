/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.repository.internal;


import static com.ericsson.oss.air.pm.stats.calculator.repository.internal.AggregateFunction.MAX;
import static com.ericsson.oss.air.pm.stats.calculator.repository.internal.AggregateFunction.MIN;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import com.ericsson.oss.air.pm.stats.calculator.repository.internal.api.DataSourceRepository;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Database;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.data.datasource.DatasourceRegistry;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DataSourceRepositoryImpl implements DataSourceRepository {
    private final DatasourceRegistry datasourceRegistry;
    private final SparkService sparkService;

    @Override
    public Optional<Timestamp> findMaxAggregationBeginTime(final Database database, final Table table) {
        return findAggregationBeginTime(MAX, database, table);
    }

    @Override
    public Optional<Timestamp> findAggregationBeginTime(final Database database, final Table table) {
        return findAggregationBeginTime(MIN, database, table);
    }

    @Override
    public boolean isAvailable(@NonNull final Datasource datasource) {
        final JdbcDatasource jdbcDatasource = datasourceRegistry.getJdbcDatasource(datasource);

        try (final Connection ignored = jdbcDatasource.getConnection()) {
            return true;
        } catch (final SQLException e) {
            log.warn("Error connecting to data source '{}' at time of execution, " +
                     "KPIs depending on this data source will not be calculated, " +
                     "check that data sources have been configured correctly",
                     datasource.getName(),
                     e);
            return false;
        }
    }

    @Override
    public boolean doesTableContainData(final JdbcDatasource jdbcDatasource,
                                        final String tableName,
                                        final LocalDateTime start,
                                        final LocalDateTime end) {
        final String sourceFilter = String.format(
                "SELECT * FROM %s WHERE aggregation_begin_time BETWEEN ? AND ? FETCH FIRST 1 ROWS ONLY",
                tableName);

        try (final Connection connection = jdbcDatasource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sourceFilter)) {
            preparedStatement.setTimestamp(1, Timestamp.valueOf(start));
            preparedStatement.setTimestamp(2,Timestamp.valueOf(end));
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (final SQLException e) {
            log.warn("Error while querying '{}' table", tableName, e);
        }
        return false;
    }

    private Optional<Timestamp> findAggregationBeginTime(final AggregateFunction aggregateFunction, final Database database, final Table table) {
        final String sqlQuery = String.format("SELECT %s FROM %s", aggregateFunction.surround(Column.AGGREGATION_BEGIN_TIME), table.getName());

        try (final Connection connection = sparkService.connectionTo(database);
             final Statement preparedStatement = connection.createStatement();
             final ResultSet resultSet = preparedStatement.executeQuery(sqlQuery)) {
            if (resultSet.next()) {
                return Optional.ofNullable(resultSet.getTimestamp(1));
            }
        } catch (final SQLException e) {
            log.warn("Failed to get {} for source {}", aggregateFunction.surround(Column.AGGREGATION_BEGIN_TIME), table.getName(), e);
        }

        return Optional.empty();
    }
}
