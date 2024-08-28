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
import static com.ericsson.oss.air.pm.stats.model.entity.Parameter.COLUMN_PARAMETERS_NAME;
import static com.ericsson.oss.air.pm.stats.model.entity.Parameter.COLUMN_PARAMETERS_TABULAR_PARAMETER_ID;
import static com.ericsson.oss.air.pm.stats.model.entity.Parameter.COLUMN_PARAMETERS_TYPE;

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

import com.ericsson.oss.air.pm.stats.common.model.collection.CollectionIdProxy;
import com.ericsson.oss.air.pm.stats.model.entity.Parameter;
import com.ericsson.oss.air.pm.stats.model.entity.TabularParameter;
import com.ericsson.oss.air.pm.stats.model.exception.UncheckedSqlException;
import com.ericsson.oss.air.pm.stats.repository.api.ParameterRepository;

import kpi.model.ondemand.OnDemandParameter;
import kpi.model.ondemand.ParameterType;

@Stateless
public class ParameterRepositoryImpl implements ParameterRepository {

    @Override
    public void saveAllParameters(final Connection connection, final List<OnDemandParameter> parameters, final UUID collectionId) throws SQLException {
        final String sql = "INSERT INTO kpi_service_db.kpi.on_demand_parameters(name, type, collection_id) VALUES (?, ?, ?)";

        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (OnDemandParameter param : parameters) {
                preparedStatement.setString(1, param.name());
                preparedStatement.setString(2, param.type().name());
                preparedStatement.setObject(3, collectionId);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }

    @Override
    public void saveParametersWithTabularParameterId(final Connection connection, final List<OnDemandParameter> parameters, final Integer id, final UUID collectionId) throws SQLException {
        final String sql = "INSERT INTO kpi_service_db.kpi.on_demand_parameters(name, type, tabular_parameter_id, collection_id) VALUES (?, ?, ?, ?)";

        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (OnDemandParameter param : parameters) {
                preparedStatement.setString(1, param.name());
                preparedStatement.setString(2, param.type().name());
                preparedStatement.setInt(3, id);
                preparedStatement.setObject(4, collectionId);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }

    @Override
    public List<Parameter> findAllParameters() {
        final String sql = "SELECT parameters.name, parameters.type, parameters.tabular_parameter_id, tabularParameter.name AS tabular_parameter_name " +
                           "FROM kpi_service_db.kpi.on_demand_parameters parameters " +
                           "LEFT JOIN kpi_service_db.kpi.on_demand_tabular_parameters tabularParameter " +
                           "ON parameters.tabular_parameter_id = tabularParameter.id " +
                           "ORDER BY parameters.id";
        return getParameters(sql);
    }

    @Override
    public List<Parameter> findAllParameters(final UUID collectionId) {
        final String sql = "SELECT parameters.name, parameters.type, parameters.tabular_parameter_id, tabularParameter.name AS tabular_parameter_name " +
                "FROM kpi_service_db.kpi.on_demand_parameters parameters " +
                "LEFT JOIN kpi_service_db.kpi.on_demand_tabular_parameters tabularParameter " +
                "ON parameters.tabular_parameter_id = tabularParameter.id " +
                "WHERE parameters.collection_id = ? " +
                "ORDER BY parameters.id";
        return getParametersWithCollectionId(sql, collectionId);
    }

    @Override
    public List<Parameter> findAllSingleParameters() {
        final String sql = "SELECT parameters.name, parameters.type, parameters.tabular_parameter_id " +
                           "FROM kpi_service_db.kpi.on_demand_parameters parameters " +
                           "WHERE parameters.tabular_parameter_id IS null " +
                           "ORDER BY parameters.id";
        return getParameters(sql);
    }

    private List<Parameter> getParameters(final String sql) {
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final Statement preparedStatement = connection.createStatement();
             final ResultSet resultSet = preparedStatement.executeQuery(sql)) {

            final List<Parameter> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(parameterBuilder(resultSet));
            }
            return result;

        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    private List<Parameter> getParametersWithCollectionId(final String sql, final UUID collectionId) {
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setObject(1, collectionId);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                final List<Parameter> result = new ArrayList<>();
                while (resultSet.next()) {
                    result.add(parameterBuilder(resultSet));
                }
                return result;
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public List<Parameter> findParametersForTabularParameter(final String tabularParameterName) {
        final String sql = "SELECT param.name as name, param.type as type " +
                "FROM kpi_service_db.kpi.on_demand_parameters as param " +
                "INNER JOIN kpi_service_db.kpi.on_demand_tabular_parameters as tabularParameter ON tabularParameter.id = param.tabular_parameter_id " +
                "WHERE tabularParameter.name = ? AND param.collection_id = ? " +
                "ORDER BY param.id";

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tabularParameterName);
            preparedStatement.setObject(2, CollectionIdProxy.COLLECTION_ID);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                final List<Parameter> tabularParameterColumns = new ArrayList<>();
                while (resultSet.next()) {
                    tabularParameterColumns.add(Parameter.builder()
                            .withName(resultSet.getString("name"))
                            .withType(ParameterType.valueOf(resultSet.getString("type")))
                            .build());
                }
                return tabularParameterColumns;
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public List<Parameter> findParametersForListOfTabularParameter(final List<String> tabularParameterDataSourceTableNames) {

        final String sql = "SELECT parameters.name, parameters.type, parameters.tabular_parameter_id, tabularParameters.name AS tabular_parameter_name " +
                "FROM kpi_service_db.kpi.on_demand_parameters parameters " +
                "INNER JOIN kpi_service_db.kpi.on_demand_tabular_parameters tabularParameters " +
                "ON parameters.tabular_parameter_id = tabularParameters.id " +
                "WHERE tabularParameters.name = ANY (?) AND parameters.collection_id = ? " +
                "ORDER BY parameters.id";

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setArray(1, connection.createArrayOf("varchar", tabularParameterDataSourceTableNames.toArray()));
            preparedStatement.setObject(2, CollectionIdProxy.COLLECTION_ID);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                final List<Parameter> result = new ArrayList<>();
                while (resultSet.next()) {
                    result.add(parameterBuilder(resultSet));
                }
                return result;
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    private Parameter parameterBuilder(ResultSet resultSet) throws SQLException {
        final int tabularParameterId = resultSet.getInt(COLUMN_PARAMETERS_TABULAR_PARAMETER_ID);
        final TabularParameter tabularParameter = tabularParameterId == 0 ? null : TabularParameter.builder()
                .withId(tabularParameterId)
                .withName(resultSet.getString("tabular_parameter_name"))
                .build();
        return Parameter.builder()
                .withName(resultSet.getString(COLUMN_PARAMETERS_NAME))
                .withType(ParameterType.valueOf(resultSet.getString(COLUMN_PARAMETERS_TYPE)))
                .withTabularParameter(tabularParameter)
                .build();
    }
}
