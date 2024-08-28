/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EntityMappers {

    public static List<ReadinessLog> parseReadinessLogResult(final PreparedStatement preparedStatement) throws SQLException {
        try (final ResultSet resultSet = preparedStatement.executeQuery()) {
            final List<ReadinessLog> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(allColumnMapperReadinessLog(resultSet));
            }
            return result;
        }
    }

    private static ReadinessLog allColumnMapperReadinessLog(@NonNull final ResultSet resultSet) throws SQLException {
        return ReadinessLog.builder()
                .withId(resultSet.getInt("id"))
                .withDatasource(resultSet.getString("datasource"))
                .withCollectedRowsCount(resultSet.getLong("collected_rows_count"))
                .withEarliestCollectedData(resultSet.getTimestamp("earliest_collected_data").toLocalDateTime())
                .withLatestCollectedData(resultSet.getTimestamp("latest_collected_data").toLocalDateTime())
                .withKpiCalculationId(resultSet.getObject("kpi_calculation_id", UUID.class))
                .build();
    }
}
