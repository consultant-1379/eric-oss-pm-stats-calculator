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
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;

import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;
import com.ericsson.oss.air.pm.stats.model.exception.UncheckedSqlException;
import com.ericsson.oss.air.pm.stats.repository.api.ComplexReadinessLogRepository;
import com.ericsson.oss.air.pm.stats.repository.util.ArrayUtils;
import com.ericsson.oss.air.pm.stats.repository.util.EntityMappers;

import com.google.common.base.Preconditions;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

@Slf4j
@ApplicationScoped
public class ComplexReadinessLogRepositoryImpl implements ComplexReadinessLogRepository {

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    @Override
    public void save(@NonNull final Connection connection, final UUID complexCalculationId, @NonNull final Collection<String> simpleExecutionGroups) {
        Preconditions.checkArgument(
                CollectionUtils.isNotEmpty(simpleExecutionGroups),
                "simpleExecutionGroups is empty. Every complex execution group must depend on a simple execution group."
        );

        final String currentComplex =
                "SELECT time_created AS actual_time_created " +
                        "FROM kpi_service_db.kpi.kpi_calculation " +
                        "WHERE calculation_id = ? ";                               /* 4. COMPLEX calculation ID from the passed parameters */

        final String previousComplex =
                "SELECT CASE COUNT(*) WHEN 0 THEN TO_TIMESTAMP('1970-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') " +
                        "       ELSE MAX(time_completed) END AS previous_time_completed " +
                        "FROM kpi_service_db.kpi.kpi_calculation " +
                        "WHERE execution_group = (SELECT execution_group " +
                        "                         FROM kpi_service_db.kpi.kpi_calculation " +
                        "                         WHERE calculation_id = ?) " +     /* 5. COMPLEX calculation ID from the passed parameters */
                        "  AND state = ANY(?) " +                                   /* 6. COMPLEX execution groups in DONE state */
                        "  AND calculation_id != ?";                                /* 7. COMPLEX calculation ID from the passed parameters */

        final String finalSql =
                "INSERT INTO kpi_service_db.kpi.complex_readiness_log (simple_readiness_log_id, complex_calculation_id) " +
                        "SELECT simple_readiness_log.id, " +
                        "       ? as complex_calculation_id " +                     /* 1. COMPLEX calculation ID (this is constantly the passed parameter) */
                        "FROM kpi_service_db.kpi.readiness_log AS simple_readiness_log " +
                        "INNER JOIN kpi_service_db.kpi.kpi_calculation AS calculation " +
                        "ON simple_readiness_log.kpi_calculation_id = calculation.calculation_id " +
                        " WHERE calculation.execution_group = ANY (?) " +           /* 2. COMPLEX execution groups' SIMPLE dependencies */
                        "   AND calculation.state = ANY(?) " +                      /* 3. SIMPLE execution groups in DONE state */
                        "   AND calculation.time_created <= ANY (" + currentComplex + ") " +
                        "   AND calculation.time_completed >= ANY (" + previousComplex + ')';

        try (final PreparedStatement preparedStatement = connection.prepareStatement(finalSql)) {
            final Array executionGroupsArray = connection.createArrayOf("VARCHAR", simpleExecutionGroups.toArray(EMPTY_STRING_ARRAY));
            final Array successfullyFinishedStates = ArrayUtils.successfullyFinishedStates(connection);

            preparedStatement.setObject(1, complexCalculationId);
            preparedStatement.setArray(2, executionGroupsArray);
            preparedStatement.setArray(3, successfullyFinishedStates);
            preparedStatement.setObject(4, complexCalculationId);
            preparedStatement.setObject(5, complexCalculationId);
            preparedStatement.setArray(6, successfullyFinishedStates);
            preparedStatement.setObject(7, complexCalculationId);

            preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public List<ReadinessLog> findByCalculationId(final UUID calculationId) {
        final String sql =
                "SELECT simple_readiness_log.* " +
                        "FROM       kpi_service_db.kpi.readiness_log         AS simple_readiness_log " +
                        "INNER JOIN kpi_service_db.kpi.complex_readiness_log AS complex_readiness_log " +
                        "        ON simple_readiness_log.id = complex_readiness_log.simple_readiness_log_id " +
                        "WHERE complex_readiness_log.complex_calculation_id = ?";

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, calculationId);

            return EntityMappers.parseReadinessLogResult(preparedStatement);
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }
}
