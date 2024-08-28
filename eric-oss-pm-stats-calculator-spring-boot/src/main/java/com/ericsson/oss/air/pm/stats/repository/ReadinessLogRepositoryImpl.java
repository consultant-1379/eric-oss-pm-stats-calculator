/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository;

import static com.ericsson.oss.air.pm.stats.common.env.DatabaseProperties.getKpiServiceJdbcConnection;
import static com.ericsson.oss.air.pm.stats.common.env.DatabaseProperties.getKpiServiceJdbcProperties;

import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import javax.ejb.Stateless;

import com.ericsson.oss.air.pm.stats.common.model.collection.CollectionIdProxy;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;
import com.ericsson.oss.air.pm.stats.model.exception.UncheckedSqlException;
import com.ericsson.oss.air.pm.stats.repository.api.ReadinessLogRepository;
import com.ericsson.oss.air.pm.stats.repository.util.ArrayUtils;
import com.ericsson.oss.air.pm.stats.repository.util.EntityMappers;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Stateless
public class ReadinessLogRepositoryImpl implements ReadinessLogRepository {

    @Override
    public List<ReadinessLog> findLatestReadinessLogsByExecutionGroup(final String complexExecutionGroup, final List<String> executionGroups) {
        final String sql = "SELECT * " +
                "FROM kpi_service_db.kpi.readiness_log " +
                "INNER JOIN kpi_service_db.kpi.kpi_calculation ON kpi_service_db.kpi.readiness_log.kpi_calculation_id = kpi_service_db.kpi.kpi_calculation.calculation_id" +
                "    WHERE kpi_service_db.kpi.kpi_calculation.execution_group = ANY(?) " + /* SIMPLE */
                "      AND kpi_service_db.kpi.kpi_calculation.state = ANY(?) " +           /* SIMPLE +DONE */
                "      AND kpi_service_db.kpi.kpi_calculation.time_completed >= ANY( " +
                "            SELECT case count(*) when 0 then to_timestamp('1970-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') else max(time_completed) end AS time_completed " +
                "            FROM kpi_service_db.kpi.kpi_calculation " +
                "            WHERE state = ANY(?) " +      /* COMPLEX +DONE */
                "              AND execution_group = ? " + /* COMPLEX */
                "              AND collection_id = ? " +
                "        " + /* If there are no calculations yet, we fetch from the artificial beginning */
                "     ) " +
                "";

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            final Array calculatedState = ArrayUtils.successfullyFinishedStates(connection);

            preparedStatement.setArray(1, connection.createArrayOf("VARCHAR", executionGroups.toArray()));
            preparedStatement.setArray(2, calculatedState);

            preparedStatement.setArray(3, calculatedState);
            preparedStatement.setString(4, complexExecutionGroup);
            preparedStatement.setObject(5, CollectionIdProxy.COLLECTION_ID);

            return EntityMappers.parseReadinessLogResult(preparedStatement);
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public List<ReadinessLog> findByCalculationId(final UUID calculationId) {
        final String sql = "SELECT id, "
                + "       datasource, "
                + "       collected_rows_count, "
                + "       earliest_collected_data, "
                + "       latest_collected_data, "
                + "       kpi_calculation_id  "
                + "FROM kpi_service_db.kpi.readiness_log "
                + "WHERE kpi_calculation_id = ? "
                + "AND collection_id = ? ";

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, calculationId);
            preparedStatement.setObject(2, CollectionIdProxy.COLLECTION_ID);

            return EntityMappers.parseReadinessLogResult(preparedStatement);
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }
}
