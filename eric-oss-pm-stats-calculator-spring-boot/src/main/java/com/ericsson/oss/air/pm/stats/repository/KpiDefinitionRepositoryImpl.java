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
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_COLLECTION_ID;
import static com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup.COLUMN_EXECUTION_GROUP_NAME;
import static com.ericsson.oss.air.pm.stats.model.entity.KpiDefinition.COLLECTION_ID;
import static com.ericsson.oss.air.pm.stats.model.entity.KpiDefinition.COLUMN_AGGREGATION_ELEMENTS;
import static com.ericsson.oss.air.pm.stats.model.entity.KpiDefinition.COLUMN_AGGREGATION_PERIOD;
import static com.ericsson.oss.air.pm.stats.model.entity.KpiDefinition.COLUMN_AGGREGATION_TYPE;
import static com.ericsson.oss.air.pm.stats.model.entity.KpiDefinition.COLUMN_ALIAS;
import static com.ericsson.oss.air.pm.stats.model.entity.KpiDefinition.COLUMN_DATA_LOOKBACK_LIMIT;
import static com.ericsson.oss.air.pm.stats.model.entity.KpiDefinition.COLUMN_DATA_RELIABILITY_OFFSET;
import static com.ericsson.oss.air.pm.stats.model.entity.KpiDefinition.COLUMN_EXECUTION_GROUP_ID;
import static com.ericsson.oss.air.pm.stats.model.entity.KpiDefinition.COLUMN_EXPORTABLE;
import static com.ericsson.oss.air.pm.stats.model.entity.KpiDefinition.COLUMN_EXPRESSION;
import static com.ericsson.oss.air.pm.stats.model.entity.KpiDefinition.COLUMN_FILTERS;
import static com.ericsson.oss.air.pm.stats.model.entity.KpiDefinition.COLUMN_NAME;
import static com.ericsson.oss.air.pm.stats.model.entity.KpiDefinition.COLUMN_OBJECT_TYPE;
import static com.ericsson.oss.air.pm.stats.model.entity.KpiDefinition.COLUMN_REEXPORT_LATE_DATA;
import static com.ericsson.oss.air.pm.stats.model.entity.KpiDefinition.COLUMN_SCHEMA_CATEGORY;
import static com.ericsson.oss.air.pm.stats.model.entity.KpiDefinition.COLUMN_SCHEMA_DATA_SPACE;
import static com.ericsson.oss.air.pm.stats.model.entity.KpiDefinition.COLUMN_SCHEMA_DETAIL_ID;
import static com.ericsson.oss.air.pm.stats.model.entity.KpiDefinition.COLUMN_SCHEMA_NAME;
import static com.ericsson.oss.air.pm.stats.model.entity.KpiDefinition.COLUMN_TIME_DELETED;
import static lombok.AccessLevel.PUBLIC;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail;
import com.ericsson.oss.air.pm.stats.common.model.collection.CollectionIdProxy;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.exception.UncheckedSqlException;
import com.ericsson.oss.air.pm.stats.repository.api.ExecutionGroupRepository;
import com.ericsson.oss.air.pm.stats.repository.api.KpiDefinitionRepository;
import com.ericsson.oss.air.pm.stats.repository.api.SchemaDetailRepository;
import com.ericsson.oss.air.pm.stats.repository.util.ColumnUtils;
import com.ericsson.oss.air.pm.stats.repository.util.RepositoryUtils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
@Stateless
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class KpiDefinitionRepositoryImpl implements KpiDefinitionRepository {

    public static final String STRING_TYPE = "VARCHAR";
    @Inject private ExecutionGroupRepository executionGroupRepository;
    @Inject private SchemaDetailRepository schemaDetailRepository;

    @Override
    public long count() {
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS count FROM kpi_service_db.kpi.kpi_definition WHERE time_deleted IS NULL")) {
            resultSet.next();
            return resultSet.getLong("count");
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public long countComplexKpi() {
        final String sql = "SELECT COUNT(*) AS count " +
            "FROM kpi_service_db.kpi.kpi_definition def " +
            "WHERE def.schema_name IS NULL " +
            "AND def.execution_group_id IS NOT NULL " +
            "AND time_deleted IS NULL";
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
            final Statement statement = connection.createStatement();
            final ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getLong("count");
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public List<KpiDefinitionEntity> findAll() {
        final String sql = "SELECT name, " +
                           "       alias, " +
                           "       expression, " +
                           "       object_type, " +
                           "       aggregation_type, " +
                           "       aggregation_period, " +
                           "       aggregation_elements, " +
                           "       exportable, " +
                           "       schema_data_space, " +
                           "       schema_category, " +
                           "       schema_name, " +
                           "       execution_group_id, " +
                           "       data_reliability_offset, " +
                           "       data_lookback_limit, " +
                           "       filters,  " +
                           "       reexport_late_data, " +
                           "       schema_detail_id, " +
                           "       id, " +
                           "       collection_id " +
                           "FROM kpi_service_db.kpi.kpi_definition " +
                           "WHERE time_deleted IS NULL";
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final Statement preparedStatement = connection.createStatement();
             final ResultSet resultSet = preparedStatement.executeQuery(sql)) {

            final List<KpiDefinitionEntity> result = new ArrayList<>();
            while (resultSet.next()) {

                result.add(kpiDefinitionEntityBuilder(connection, resultSet));
            }
            return result;
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public List<KpiDefinitionEntity> findAll(final UUID collectionId) {
        final String sql = "SELECT name, " +
                "       alias, " +
                "       expression, " +
                "       object_type, " +
                "       aggregation_type, " +
                "       aggregation_period, " +
                "       aggregation_elements, " +
                "       exportable, " +
                "       schema_data_space, " +
                "       schema_category, " +
                "       schema_name, " +
                "       execution_group_id, " +
                "       data_reliability_offset, " +
                "       data_lookback_limit, " +
                "       filters,  " +
                "       reexport_late_data, " +
                "       schema_detail_id, " +
                "       id, " +
                "       collection_id " +
                "FROM kpi_service_db.kpi.kpi_definition " +
                "WHERE time_deleted IS NULL AND collection_id = ? ";
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, collectionId);
            try(final ResultSet resultSet = preparedStatement.executeQuery()) {
                final List<KpiDefinitionEntity> result = new ArrayList<>();
                while (resultSet.next()) {

                    result.add(kpiDefinitionEntityBuilder(connection, resultSet));
                }
                return result;
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public List<KpiDefinitionEntity> findAllIncludingSoftDeleted() {
        final String sql =
                        "SELECT * " +
                        "FROM kpi_service_db.kpi.kpi_definition";
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final Statement preparedStatement = connection.createStatement();
             final ResultSet resultSet = preparedStatement.executeQuery(sql)) {

            final List<KpiDefinitionEntity> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(kpiDefinitionEntityBuilderWithTimeDeleted(connection, resultSet));
            }
            return result;

        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public List<KpiDefinitionEntity> findAllIncludingSoftDeleted(final UUID collectionID) {
        final String sql =
                "SELECT * " +
                        "FROM kpi_service_db.kpi.kpi_definition " +
                        "WHERE collection_id = ?";
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, collectionID);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                final List<KpiDefinitionEntity> result = new ArrayList<>();
                while (resultSet.next()) {
                    result.add(kpiDefinitionEntityBuilderWithTimeDeleted(connection, resultSet));
                }
                return result;
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }


    @Override
    public List<KpiDefinitionEntity> findComplexKpis() {
        final String sql = "SELECT name, " +
                "       alias, " +
                "       expression, " +
                "       object_type, " +
                "       aggregation_type, " +
                "       aggregation_period, " +
                "       aggregation_elements, " +
                "       exportable, " +
                "       schema_data_space, " +
                "       schema_category, " +
                "       schema_name, " +
                "       execution_group_id, " +
                "       data_reliability_offset, " +
                "       data_lookback_limit, " +
                "       filters,  " +
                "       reexport_late_data, " +
                "       schema_detail_id, " +
                "       id, " +
                "       collection_id " +
                "FROM kpi_service_db.kpi.kpi_definition def " +
                "WHERE def.schema_name IS NULL " +
                "AND def.execution_group_id IS NOT NULL " +
                "AND time_deleted IS NULL AND collection_id = ? ";
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, CollectionIdProxy.COLLECTION_ID);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                final List<KpiDefinitionEntity> result = new ArrayList<>();
                while (resultSet.next()) {
                    result.add(kpiDefinitionEntityBuilder(connection, resultSet));
                }
                return result;
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public Set<String> findAllKpiNames() {
        final String sql = "SELECT name FROM kpi_service_db.kpi.kpi_definition";
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery(sql)) {
            final Set<String> result = new HashSet<>();
            while (resultSet.next()) {
                result.add(resultSet.getString("name"));
            }
            return result;
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }


    @Override
    public Set<String> findAllKpiNames(UUID collectionId) {
        final String sql = "SELECT name FROM kpi_service_db.kpi.kpi_definition WHERE collection_id = ?";
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setObject(1, collectionId);
            try(final ResultSet resultSet = preparedStatement.executeQuery()) {
                final Set<String> result = new HashSet<>();
                while (resultSet.next()) {
                    result.add(resultSet.getString("name"));
                }
                return result;
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public Optional<KpiDefinitionEntity> findByName(final String name, final UUID collectionId) {
        final String sql = "SELECT name, " +
                "       alias, " +
                "       expression, " +
                "       object_type, " +
                "       aggregation_type, " +
                "       aggregation_period, " +
                "       aggregation_elements, " +
                "       exportable, " +
                "       schema_data_space, " +
                "       schema_category, " +
                "       schema_name, " +
                "       execution_group_id, " +
                "       data_reliability_offset, " +
                "       data_lookback_limit, " +
                "       filters,  " +
                "       reexport_late_data, " +
                "       schema_detail_id, " +
                "       id, " +
                "       collection_id " +
                "FROM kpi_service_db.kpi.kpi_definition " +
                "WHERE name = ? " +
                "AND time_deleted IS NULL AND collection_id = ? ";
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            preparedStatement.setObject(2, collectionId);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(kpiDefinitionEntityBuilder(connection, resultSet))
                        : Optional.empty();
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    //Adjust collectionId later
    @Override
    public List<KpiDefinitionEntity> findByNames(final Set<String> names) {
        final String sql = "SELECT id, name, " +
                           "       alias, " +
                           "       expression, " +
                           "       object_type, " +
                           "       aggregation_type, " +
                           "       aggregation_period, " +
                           "       aggregation_elements, " +
                           "       exportable, " +
                           "       schema_data_space, " +
                           "       schema_category, " +
                           "       schema_name, " +
                           "       execution_group_id, " +
                           "       data_reliability_offset, " +
                           "       data_lookback_limit, " +
                           "       filters,  " +
                           "       reexport_late_data, " +
                           "       schema_detail_id," +
                           "       collection_id " +
                           "FROM kpi_service_db.kpi.kpi_definition " +
                           "WHERE name = ANY(?) " +
                           "AND time_deleted IS NULL";
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setArray(1, connection.createArrayOf(STRING_TYPE, names.toArray()));
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                final List<KpiDefinitionEntity> result = new ArrayList<>();

                while (resultSet.next()) {
                    result.add(kpiDefinitionEntityBuilder(connection, resultSet));
                }
                return result;
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public Set<String> findAllSimpleExecutionGroups() {
        final String sql = "SELECT executionGroup.execution_group " +
                "FROM kpi_service_db.kpi.kpi_definition definition " +
                "INNER JOIN kpi_service_db.kpi.kpi_execution_groups executionGroup " +
                "ON definition.execution_group_id = executionGroup.id " +
                "WHERE definition.schema_name IS NOT NULL " +
                "AND time_deleted IS NULL";
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery(sql)) {
            final Set<String> result = new HashSet<>();
            while (resultSet.next()) {
                result.add(resultSet.getString("execution_group"));
            }
            return result;
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public Set<String> findAllComplexExecutionGroups() {
        final String sql = "SELECT executionGroup.execution_group " +
                "FROM kpi_service_db.kpi.kpi_definition definition " +
                "INNER JOIN kpi_service_db.kpi.kpi_execution_groups executionGroup " +
                "ON definition.execution_group_id = executionGroup.id " +
                "WHERE definition.schema_name IS NULL " +
                "AND time_deleted IS NULL";
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery(sql)) {
            final Set<String> result = new HashSet<>();
            while (resultSet.next()) {
                result.add(resultSet.getString("execution_group"));
            }
            return result;
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public Map<String, List<String>> findAllSimpleKpiNamesGroupedByExecGroups() {
        final String sql = "SELECT def.name, exe.execution_group " +
                "FROM kpi_service_db.kpi.kpi_definition def " +
                "INNER JOIN kpi_service_db.kpi.kpi_execution_groups exe " +
                "ON def.execution_group_id = exe.id " +
                "WHERE def.schema_name IS NOT NULL " +
                "AND time_deleted IS NULL";
        return findProjection(sql, Column.of(COLUMN_NAME), Column.of(COLUMN_EXECUTION_GROUP_NAME));
    }

    @Override
    public Map<String, List<String>> findAllComplexKpiNamesGroupedByExecGroups() {
        final String sql = "SELECT def.name, exe.execution_group " +
                "FROM kpi_service_db.kpi.kpi_definition def " +
                "INNER JOIN kpi_service_db.kpi.kpi_execution_groups exe " +
                "ON def.execution_group_id = exe.id " +
                "WHERE def.schema_name IS NULL " +
                "AND time_deleted IS NULL";
        return findProjection(sql, Column.of(COLUMN_NAME), Column.of(COLUMN_EXECUTION_GROUP_NAME));
    }

    @Override
    public List<String> findAllKpiNamesWithAliasAndAggregationPeriod(final String alias, final int aggregationPeriod) {
        final String sql = "SELECT name " +
                "FROM kpi_service_db.kpi.kpi_definition " +
                "WHERE alias = ? " +
                "AND aggregation_period = ? " +
                "AND time_deleted IS NULL AND collection_id = ? ";
        try (Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, alias);
            preparedStatement.setInt(2, aggregationPeriod);
            preparedStatement.setObject(3, CollectionIdProxy.COLLECTION_ID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                final List<String> result = new ArrayList<>();

                while (resultSet.next()) {
                    result.add(resultSet.getString(COLUMN_NAME));
                }

                return result;
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }


    //Only used in retention no collectionId needed
    @Override
    public int countKpisWithAliasAndAggregationPeriod(final Connection connection, final Table table) {
        final String sql = "SELECT count(name) as counted_kpis " +
                           "FROM kpi_service_db.kpi.kpi_definition " +
                           "WHERE alias = ? " +
                           "AND aggregation_period = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, table.getAlias());
            preparedStatement.setInt(2, table.getAggregationPeriod());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt("counted_kpis");
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public Set<Pair<String, Integer>> findOnDemandAliasAndAggregationPeriods() {
        final String sql = "SELECT alias, aggregation_period " +
                "FROM kpi_service_db.kpi.kpi_definition " +
                "WHERE schema_name is NULL " +
                "AND execution_group_id IS NULL " +
                "AND time_deleted IS NULL";
        return findAllAliasAndAggregationPeriodPairs(sql);
    }

    @Override
    public Set<Pair<String, Integer>> findScheduledAliasAndAggregationPeriods() {
        final String sql = "SELECT alias, aggregation_period " +
                "FROM kpi_service_db.kpi.kpi_definition " +
                "WHERE execution_group_id IS NOT NULL " +
                "AND time_deleted IS NULL";
        return findAllAliasAndAggregationPeriodPairs(sql);
    }

    private Set<Pair<String, Integer>> findAllAliasAndAggregationPeriodPairs(final String sql) {
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery(sql)) {
            Set<Pair<String, Integer>> result = new HashSet<>();
            while (resultSet.next()) {
                result.add(Pair.of(resultSet.getString(COLUMN_ALIAS), resultSet.getInt(COLUMN_AGGREGATION_PERIOD)));
            }
            return result;
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public void update(final Connection connection, final KpiDefinitionEntity kpiDefinitionEntity, final UUID collectionId) throws SQLException {
        final String sql = "UPDATE kpi_service_db.kpi.kpi_definition " +
                           "SET alias = ?, " +                      // 1
                           "    expression = ?, " +                 // 2
                           "    object_type = ?, " +                // 3
                           "    aggregation_type = ?, " +           // 4
                           "    aggregation_period = ?, " +         // 5
                           "    aggregation_elements = ?, " +       // 6
                           "    exportable = ?, " +                 // 7
                           "    filters = ?, " +                    // 8
                           "    schema_data_space = ?, " +          // 9
                           "    schema_category = ?, " +            // 10
                           "    schema_name = ?, " +                // 11
                           "    execution_group_id = ?, " +         // 12
                           "    data_reliability_offset = ?, " +    // 13
                           "    data_lookback_limit = ?, " +        // 14
                           "    reexport_late_data = ?, " +         // 15
                           "    schema_detail_id = ? " +            // 16
                           "    WHERE collection_id = ? " +            // 17
                            "   AND name = ?";                        // 18

        final Long executionGroupId = executionGroupRepository.getOrSave(connection, kpiDefinitionEntity);
        final SchemaDetail schemaDetail = kpiDefinitionEntity.schemaDetail();
        final Integer schemaDetailId = Objects.isNull(schemaDetail) ? null : schemaDetailRepository.getOrSave(connection, schemaDetail);

        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            PreparedStatementBuilder.of(connection, preparedStatement, kpiDefinitionEntity)
                                    .withIndexAlias(1)
                                    .withIndexExpression(2)
                                    .withIndexObjectType(3)
                                    .withIndexAggregationType(4)
                                    .withIndexAggregationPeriod(5)
                                    .withIndexAggregationElements(6)
                                    .withIndexExportable(7)
                                    .withIndexFilters(8)
                                    .withIndexSchemaDataSpace(9)
                                    .withIndexSchemaCategory(10)
                                    .withIndexSchemaName(11)
                                    .withIndexExecutionGroupId(12, executionGroupId)
                                    .withIndexDataReliabilityOffset(13)
                                    .withIndexDataLookBackLimit(14)
                                    .withIndexReexportLateData(15)
                                    .withIndexSchemaDetailId(16, schemaDetailId)
                                    .withIndexExplicitCollectionId(17, collectionId)
                                    .withIndexName(18)
                                    .executeUpdate();
        }
    }

    @Override
    public void softDelete(final Connection connection, final List<String> kpiDefinitionNames, final UUID collectionId) throws SQLException {
        final String sql = "UPDATE kpi_service_db.kpi.kpi_definition " +
                           "SET time_deleted = ? " +
                           "WHERE name = ANY(?) AND collection_id = ? AND time_deleted IS NULL";
        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            preparedStatement.setArray(2, connection.createArrayOf(STRING_TYPE, kpiDefinitionNames.toArray()));
            preparedStatement.setObject(3, collectionId );
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public void save(final Connection connection, final KpiDefinitionEntity kpiDefinitionEntity) throws SQLException {
        final String sql = "INSERT INTO kpi_service_db.kpi.kpi_definition(name, " +                     // 1
                "                                              alias, " +                    // 2
                "                                              expression, " +               // 3
                "                                              object_type, " +              // 4
                "                                              aggregation_type, " +         // 5
                "                                              aggregation_period, " +       // 6
                "                                              aggregation_elements, " +     // 7
                "                                              exportable, " +               // 8
                "                                              filters, " +                  // 9
                "                                              schema_data_space, " +        // 10
                "                                              schema_category, " +          // 11
                "                                              schema_name, " +              // 12
                "                                              execution_group_id, " +       // 13
                "                                              data_reliability_offset, " +  // 14
                "                                              data_lookback_limit, " +      // 15
                "                                              reexport_late_data, " +       // 16
                "                                              schema_detail_id," +          // 17
                "                                              collection_id) " +            // 18
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        final Long executionGroupId = executionGroupRepository.getOrSave(connection, kpiDefinitionEntity);
        final SchemaDetail schemaDetail = kpiDefinitionEntity.schemaDetail();
        final Integer schemaDetailId = Objects.isNull(schemaDetail) ? null : schemaDetailRepository.getOrSave(connection, schemaDetail);

            try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                PreparedStatementBuilder.of(connection, preparedStatement, kpiDefinitionEntity)
                                        .withIndexName(1)
                                        .withIndexAlias(2)
                                        .withIndexExpression(3)
                                        .withIndexObjectType(4)
                                        .withIndexAggregationType(5)
                                        .withIndexAggregationPeriod(6)
                                        .withIndexAggregationElements(7)
                                        .withIndexExportable(8)
                                        .withIndexFilters(9)
                                        .withIndexSchemaDataSpace(10)
                                        .withIndexSchemaCategory(11)
                                        .withIndexSchemaName(12)
                                        .withIndexExecutionGroupId(13, executionGroupId)
                                        .withIndexDataReliabilityOffset(14)
                                        .withIndexDataLookBackLimit(15)
                                        .withIndexReexportLateData(16)
                                        .withIndexSchemaDetailId(17, schemaDetailId)
                                        .withIndexExplicitCollectionId(18, DEFAULT_COLLECTION_ID)
                                        .executeUpdate();
            }
    }

    @Override
    public void saveAll(final Connection connection, final List<KpiDefinitionEntity> kpiDefinitionEntities) throws SQLException {
        final String sql = "INSERT INTO kpi_service_db.kpi.kpi_definition(name, " +                     // 1
                "                                              alias, " +                    // 2
                "                                              expression, " +               // 3
                "                                              object_type, " +              // 4
                "                                              aggregation_type, " +         // 5
                "                                              aggregation_period, " +       // 6
                "                                              aggregation_elements, " +     // 7
                "                                              exportable, " +               // 8
                "                                              filters, " +                  // 9
                "                                              schema_data_space, " +        // 10
                "                                              schema_category, " +          // 11
                "                                              schema_name, " +              // 12
                "                                              execution_group_id, " +       // 13
                "                                              data_reliability_offset, " +  // 14
                "                                              data_lookback_limit, " +      // 15
                "                                              reexport_late_data, " +       // 16
                "                                              schema_detail_id," +          // 17
                "                                              collection_id) " +            // 18
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (final KpiDefinitionEntity kpiDefinitionEntity : kpiDefinitionEntities) {
                final Long executionGroupId = executionGroupRepository.getOrSave(connection, kpiDefinitionEntity);
                final SchemaDetail schemaDetail = kpiDefinitionEntity.schemaDetail();
                final Integer schemaDetailId = Objects.isNull(schemaDetail) ? null : schemaDetailRepository.getOrSave(connection, schemaDetail);

                PreparedStatementBuilder.of(connection, preparedStatement, kpiDefinitionEntity)
                                        .withIndexName(1)
                                        .withIndexAlias(2)
                                        .withIndexExpression(3)
                                        .withIndexObjectType(4)
                                        .withIndexAggregationType(5)
                                        .withIndexAggregationPeriod(6)
                                        .withIndexAggregationElements(7)
                                        .withIndexExportable(8)
                                        .withIndexFilters(9)
                                        .withIndexSchemaDataSpace(10)
                                        .withIndexSchemaCategory(11)
                                        .withIndexSchemaName(12)
                                        .withIndexExecutionGroupId(13, executionGroupId)
                                        .withIndexDataReliabilityOffset(14)
                                        .withIndexDataLookBackLimit(15)
                                        .withIndexReexportLateData(16)
                                        .withIndexSchemaDetailId(17, schemaDetailId)
                                        .withIndexCollectionId(18);

                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }

    private Map<String, List<String>> findProjection(final String sql, final Column kpiNameColumn, final Column executionColumn) {
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery(sql)) {
            final Map<String, List<String>> result = new HashMap<>();
            while (resultSet.next()) {
                final String kpiName = resultSet.getString(kpiNameColumn.getName());
                final String executionGroup = resultSet.getString(executionColumn.getName());

                if (!result.containsKey(executionGroup)) {
                    result.put(executionGroup, new ArrayList<>());
                }
                result.get(executionGroup).add(kpiName);
            }
            return result;
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public List<KpiDefinitionEntity> findKpiDefinitionsByExecutionGroup(final String executionGroupName) {
        final String sql = "SELECT name, " +
                "       alias, " +
                "       expression, " +
                "       object_type, " +
                "       aggregation_type, " +
                "       aggregation_period, " +
                "       aggregation_elements, " +
                "       exportable, " +
                "       schema_data_space, " +
                "       schema_category, " +
                "       schema_name, " +
                "       execution_group_id, " +
                "       data_reliability_offset, " +
                "       data_lookback_limit, " +
                "       filters,  " +
                "       reexport_late_data,  " +
                "       schema_detail_id, " +
                "       def.id, " +
                "       collection_id " +
                "FROM kpi_service_db.kpi.kpi_definition def " +
                "INNER JOIN kpi_service_db.kpi.kpi_execution_groups exe " +
                "ON def.execution_group_id = exe.id " +
                "WHERE exe.execution_group = ? " +
                "AND time_deleted IS NULL AND collection_id = ? ";

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, executionGroupName);
            preparedStatement.setObject(2, CollectionIdProxy.COLLECTION_ID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                final List<KpiDefinitionEntity> result = new ArrayList<>();

                while (resultSet.next()) {
                    result.add(kpiDefinitionEntityBuilder(connection, resultSet));
                }
                return result;
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public List<KpiDefinitionEntity> findOnDemandKpiDefinitionsByCalculationId(final UUID calculationId) {
        final String sql = "SELECT name, " +
                "       alias, " +
                "       expression, " +
                "       object_type, " +
                "       aggregation_type, " +
                "       aggregation_period, " +
                "       aggregation_elements, " +
                "       exportable, " +
                "       execution_group_id, " +
                "       schema_data_space, " +
                "       schema_category, " +
                "       schema_name, " +
                "       data_reliability_offset, " +
                "       data_lookback_limit, " +
                "       filters, " +
                "       reexport_late_data, " +
                "       schema_detail_id, " +
                "       id, " +
                "       def.collection_id " +
                "FROM kpi_service_db.kpi.kpi_definition def " +
                "INNER JOIN kpi_service_db.kpi.on_demand_definitions_per_calculation calc " +
                "ON def.id = calc.kpi_definition_id " +
                "WHERE calc.calculation_id = ? " +
                "AND time_deleted IS NULL " +
                "AND calc.collection_id = ?";

        try (Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, calculationId);
            preparedStatement.setObject(2, CollectionIdProxy.COLLECTION_ID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                final List<KpiDefinitionEntity> result = new ArrayList<>();

                while (resultSet.next()) {

                    result.add(kpiDefinitionEntityBuilder(connection, resultSet));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public void saveOnDemandCalculationRelation(final Set<String> kpiNames, final UUID calculationId) {
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties())) {
            saveRelations(connection, findAllRelatedDefinitionId(connection, kpiNames), calculationId);
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public void deleteKpiDefinitionsByName(final Connection connection, final Collection<String> kpiNames) {
        final String sql = "DELETE FROM kpi_service_db.kpi.kpi_definition WHERE name = ANY(?)";

        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setArray(1, connection.createArrayOf(STRING_TYPE, kpiNames.toArray()));
            preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    private KpiDefinitionEntity kpiDefinitionEntityBuilderWithTimeDeleted(final Connection connection, final ResultSet resultSet) throws SQLException {
        final KpiDefinitionEntity kpiDefinitionEntity = kpiDefinitionEntityBuilder(connection, resultSet);
        final Timestamp timestamp = resultSet.getTimestamp(COLUMN_TIME_DELETED);
        kpiDefinitionEntity.timeDeleted(timestamp == null ? null : timestamp.toLocalDateTime());
        return kpiDefinitionEntity;
    }

    private KpiDefinitionEntity kpiDefinitionEntityBuilder(final Connection connection, final ResultSet resultSet) throws SQLException {
        final List<String> aggregationElements = RepositoryUtils.parseArrayToString(resultSet.getArray(COLUMN_AGGREGATION_ELEMENTS));
        final List<String> filters = RepositoryUtils.parseArrayToString(resultSet.getArray(COLUMN_FILTERS));
        final ExecutionGroup executionGroup = executionGroupRepository.findExecutionGroupById(connection, resultSet.getInt(COLUMN_EXECUTION_GROUP_ID));
        final SchemaDetail schemaDetail = schemaDetailRepository.findById(resultSet.getInt(COLUMN_SCHEMA_DETAIL_ID)).orElse(null);

        return KpiDefinitionEntity.builder()
                .withId(resultSet.getInt("id"))
                .withName(resultSet.getString(COLUMN_NAME))
                .withAlias(resultSet.getString(COLUMN_ALIAS))
                .withExpression(resultSet.getString(COLUMN_EXPRESSION))
                .withObjectType(resultSet.getString(COLUMN_OBJECT_TYPE))
                .withAggregationType(resultSet.getString(COLUMN_AGGREGATION_TYPE))
                .withAggregationPeriod(resultSet.getInt(COLUMN_AGGREGATION_PERIOD))
                .withAggregationElements(aggregationElements)
                .withFilters(filters)
                .withExportable(resultSet.getBoolean(COLUMN_EXPORTABLE))
                .withExecutionGroup(executionGroup)
                .withDataReliabilityOffset(resultSet.getInt(COLUMN_DATA_RELIABILITY_OFFSET))
                .withDataLookbackLimit(resultSet.getInt(COLUMN_DATA_LOOKBACK_LIMIT))
                .withReexportLateData(resultSet.getBoolean(COLUMN_REEXPORT_LATE_DATA))
                .withSchemaDetail(schemaDetail)
                .withSchemaName(resultSet.getString(COLUMN_SCHEMA_NAME))
                .withSchemaCategory(resultSet.getString(COLUMN_SCHEMA_CATEGORY))
                .withSchemaDataSpace(resultSet.getString(COLUMN_SCHEMA_DATA_SPACE))
                .withCollectionId((UUID) resultSet.getObject(COLLECTION_ID))
                .build();
    }

    private List<Integer> findAllRelatedDefinitionId(final Connection connection, final Set<String> kpiNames) throws SQLException {
        final String sql = "SELECT id FROM kpi_service_db.kpi.kpi_definition WHERE name = ANY(?) AND time_deleted IS NULL AND collection_id = ?";
        final List<Integer> ids = new ArrayList<>();

        try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setArray(1, connection.createArrayOf(STRING_TYPE, kpiNames.toArray()));
            stmt.setObject(2, CollectionIdProxy.COLLECTION_ID);

            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    ids.add(resultSet.getInt(1));
                }
            }
        }
        return ids;
    }

    private void saveRelations(final Connection connection, final List<Integer> definitionIds, final UUID calculationID) throws SQLException {
        final String sql = "INSERT INTO kpi.on_demand_definitions_per_calculation " +
                "(kpi_definition_id, calculation_id, collection_id) VALUES (?,?,?)";
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (final int definitionId : definitionIds) {
                stmt.setInt(1, definitionId);
                stmt.setObject(2, calculationID);
                stmt.setObject(3, CollectionIdProxy.COLLECTION_ID);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class PreparedStatementBuilder {
        private final Connection connection;
        private final PreparedStatement preparedStatement;
        private final KpiDefinitionEntity kpiDefinitionEntity;

        static PreparedStatementBuilder of(final Connection connection,
                                           final PreparedStatement preparedStatement,
                                           final KpiDefinitionEntity kpiDefinitionEntity) {
            return new PreparedStatementBuilder(connection, preparedStatement, kpiDefinitionEntity);
        }

        public PreparedStatementBuilder withIndexName(final int parameterIndex) throws SQLException {
            preparedStatement.setString(parameterIndex, kpiDefinitionEntity.name());
            return this;
        }

        public PreparedStatementBuilder withIndexAlias(final int parameterIndex) throws SQLException {
            preparedStatement.setString(parameterIndex, kpiDefinitionEntity.alias());
            return this;
        }

        public PreparedStatementBuilder withIndexExpression(final int parameterIndex) throws SQLException {
            preparedStatement.setString(parameterIndex, kpiDefinitionEntity.expression());
            return this;
        }

        public PreparedStatementBuilder withIndexObjectType(final int parameterIndex) throws SQLException {
            preparedStatement.setString(parameterIndex, kpiDefinitionEntity.objectType());
            return this;
        }

        public PreparedStatementBuilder withIndexAggregationType(final int parameterIndex) throws SQLException {
            preparedStatement.setString(parameterIndex, kpiDefinitionEntity.aggregationType());
            return this;
        }

        public PreparedStatementBuilder withIndexAggregationPeriod(final int parameterIndex) throws SQLException {
            preparedStatement.setInt(parameterIndex, kpiDefinitionEntity.aggregationPeriod());
            return this;
        }

        public PreparedStatementBuilder withIndexAggregationElements(final int parameterIndex) throws SQLException {
            final Object[] aggregationElements = kpiDefinitionEntity.aggregationElements().toArray();
            preparedStatement.setArray(parameterIndex, connection.createArrayOf(STRING_TYPE, aggregationElements));
            return this;
        }

        public PreparedStatementBuilder withIndexExportable(final int parameterIndex) throws SQLException {
            preparedStatement.setBoolean(parameterIndex, kpiDefinitionEntity.exportable());
            return this;
        }

        public PreparedStatementBuilder withIndexFilters(final int parameterIndex) throws SQLException {
            final Object[] filters = kpiDefinitionEntity.filters().toArray();
            preparedStatement.setArray(parameterIndex, connection.createArrayOf(STRING_TYPE, filters));
            return this;
        }

        public PreparedStatementBuilder withIndexSchemaDataSpace(final int parameterIndex) throws SQLException {
            preparedStatement.setString(parameterIndex, ColumnUtils.nullableString(kpiDefinitionEntity.schemaDataSpace()));
            return this;
        }

        public PreparedStatementBuilder withIndexSchemaCategory(final int parameterIndex) throws SQLException {
            preparedStatement.setString(parameterIndex, ColumnUtils.nullableString(kpiDefinitionEntity.schemaCategory()));
            return this;
        }

        public PreparedStatementBuilder withIndexSchemaName(final int parameterIndex) throws SQLException {
            preparedStatement.setString(parameterIndex, ColumnUtils.nullableString(kpiDefinitionEntity.schemaName()));
            return this;
        }

        public PreparedStatementBuilder withIndexExecutionGroupId(final int parameterIndex, final Long executionGroupId) throws SQLException {
            preparedStatement.setObject(parameterIndex, executionGroupId, Types.BIGINT);
            return this;
        }

        public PreparedStatementBuilder withIndexDataReliabilityOffset(final int parameterIndex) throws SQLException {
            preparedStatement.setObject(parameterIndex, ColumnUtils.nullableInteger(kpiDefinitionEntity.dataReliabilityOffset()));
            return this;
        }

        public PreparedStatementBuilder withIndexDataLookBackLimit(final int parameterIndex) throws SQLException {
            preparedStatement.setObject(parameterIndex, ColumnUtils.nullableInteger(kpiDefinitionEntity.dataLookbackLimit()));
            return this;
        }

        public PreparedStatementBuilder withIndexReexportLateData(final int parameterIndex) throws SQLException {
            preparedStatement.setObject(parameterIndex, kpiDefinitionEntity.reexportLateData());
            return this;
        }

        public PreparedStatementBuilder withIndexSchemaDetailId(final int parameterIndex, final Integer schemaDetailId) throws SQLException {
            preparedStatement.setObject(parameterIndex, schemaDetailId, Types.INTEGER);
            return this;
        }

        public PreparedStatementBuilder withIndexExplicitCollectionId(final int parameterIndex, UUID collectionId) throws SQLException {
            preparedStatement.setObject(parameterIndex, collectionId);
            return this;
        }

        public PreparedStatementBuilder withIndexCollectionId(final int parameterIndex) throws SQLException {
            preparedStatement.setObject(parameterIndex, kpiDefinitionEntity.collectionId());
            return this;
        }


        public void executeUpdate() throws SQLException {
            preparedStatement.executeUpdate();
        }

    }
}
