/*******************************************************************************
 * COPYRIGHT Ericsson 2024
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
import static com.ericsson.oss.air.pm.stats.model.entity.TabularParameter.COLUMN_TABULAR_PARAMETERS_NAME;
import static lombok.AccessLevel.PUBLIC;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.common.model.collection.CollectionIdProxy;
import com.ericsson.oss.air.pm.stats.model.entity.TabularParameter;
import com.ericsson.oss.air.pm.stats.model.exception.UncheckedSqlException;
import com.ericsson.oss.air.pm.stats.repository.api.ParameterRepository;
import com.ericsson.oss.air.pm.stats.repository.api.TabularParameterRepository;

import kpi.model.ondemand.OnDemandTabularParameter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Stateless
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class TabularParameterRepositoryImpl implements TabularParameterRepository {

    @Inject
    private ParameterRepository parameterRepository;

    @Override
    public List<TabularParameter> findAllTabularParameters() {
        final String sql = "SELECT name FROM kpi_service_db.kpi.on_demand_tabular_parameters WHERE collection_id = ? ORDER BY id";

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, CollectionIdProxy.COLLECTION_ID);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                final List<TabularParameter> result = new ArrayList<>();
                while (resultSet.next()) {
                    result.add(TabularParameter.builder().withName(resultSet.getString(COLUMN_TABULAR_PARAMETERS_NAME)).build());
                }
                return result;
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public void saveTabularParameter(final Connection connection, final OnDemandTabularParameter tabularParameter, final UUID collectionId) throws SQLException {
        final Integer tabularParamId = save(connection, tabularParameter, collectionId);
        parameterRepository.saveParametersWithTabularParameterId(connection, tabularParameter.columns(), tabularParamId, collectionId);
    }

    private Integer save(final Connection connection, final OnDemandTabularParameter tabularParameter, final UUID collectionId) {
        final String sql = "INSERT INTO kpi_service_db.kpi.on_demand_tabular_parameters(name, collection_id) VALUES (?,?)";

        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, tabularParameter.name());
            preparedStatement.setObject(2, collectionId);
            preparedStatement.executeUpdate();

            try (ResultSet rs = preparedStatement.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }
}
