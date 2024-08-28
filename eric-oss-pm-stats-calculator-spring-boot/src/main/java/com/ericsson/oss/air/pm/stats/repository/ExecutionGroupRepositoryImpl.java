/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository;

import static lombok.AccessLevel.PUBLIC;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.repository.api.ExecutionGroupRepository;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Stateless
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class ExecutionGroupRepositoryImpl implements ExecutionGroupRepository {
    @Inject
    private ExecutionGroupGenerator executionGroupGenerator;

    @Nullable
    @Override
    public Long getOrSave(final Connection connection, final KpiDefinitionEntity kpiDefinitionEntity) throws SQLException {
        final String executionGroup = executionGroupGenerator.generateOrGetExecutionGroup(kpiDefinitionEntity);
        if (Objects.isNull(executionGroup)) {
            return null;
        }
        final String sql = "SELECT id FROM kpi_service_db.kpi.kpi_execution_groups " +
                "WHERE execution_group = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, executionGroup);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return save(connection, executionGroup);
    }

    private Long save(final Connection connection, final String executionGroup) throws SQLException {
        final String sql = "INSERT INTO kpi_service_db.kpi.kpi_execution_groups(execution_group) " +
                "VALUES (?)";

        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, executionGroup);
            preparedStatement.executeUpdate();

            try (ResultSet rs = preparedStatement.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return null;
    }

    @Override
    @Nullable
    public ExecutionGroup findExecutionGroupById(final Connection connection, final Integer id) throws SQLException {
        final String sql =
                "SELECT id, execution_group " +
                        "FROM kpi_service_db.kpi.kpi_execution_groups " +
                        "WHERE id = ?";
        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);

            try (final ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return ExecutionGroup.builder().withId(rs.getInt("id")).withName(rs.getString("execution_group")).build();
                }
            }

            return null;
        }
    }
}
