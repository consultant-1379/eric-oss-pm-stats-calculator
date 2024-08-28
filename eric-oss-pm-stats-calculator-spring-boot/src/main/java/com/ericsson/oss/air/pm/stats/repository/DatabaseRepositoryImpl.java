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
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.column;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.model.entity.Parameter;
import com.ericsson.oss.air.pm.stats.model.exception.EntityNotFoundException;
import com.ericsson.oss.air.pm.stats.model.exception.UncheckedSqlException;
import com.ericsson.oss.air.pm.stats.repository.api.DatabaseRepository;
import com.ericsson.oss.air.pm.stats.repository.util.statement.SqlStatementExecutor;
import com.ericsson.oss.air.pm.stats.repository.util.table.api.SqlTableGenerator;
import com.ericsson.oss.air.pm.stats.repository.util.table.api.SqlTableModifier;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.ColumnDefinition;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.TableCreationInformation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DatabaseRepositoryImpl implements DatabaseRepository {
    private static final String KPI_SERVICE_DB = "kpi_service_db";
    private static final String SCHEMA = "kpi";

    /**
     * <strong>kpi_definition</strong> and <strong>kpi_calculation</strong> tables matching
     * for the output table name criteria, but they are used internally.
     */
    private static final List<String> INTERNAL_TABLE_NAMES = Arrays.asList("kpi_definition", "kpi_calculation", "kpi_execution_groups");
    private static final String[] TABLE_TYPES = {"TABLE", "PARTITIONED TABLE"};

    private final SqlTableGenerator sqlTableGenerator;
    private final SqlTableModifier sqlTableModifier;

    @Override
    public boolean isAvailable() {
        try (final Connection ignored = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties())) {
            return true;
        } catch (final SQLException e) {
            log.error("Could not connect to the KPI Database", e);
            return false;
        }
    }

    @Override
    public boolean doesTableExistByName(final String name) {
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final ResultSet resultSet = connection.getMetaData().getTables(KPI_SERVICE_DB, SCHEMA, name, TABLE_TYPES)) {
            return resultSet.next();
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public List<String> findAllCalculationOutputTables() {
        final String sql = "SELECT table_name AS calculation_output_table_names " +
                "FROM information_schema.tables " +
                "WHERE table_schema = 'kpi' " +
                "  AND table_name LIKE 'kpi_%'";

        return gatherTableData(sql);
    }

    @Override
    public List<String> findAllOutputTablesWithoutPartition() {
        return gatherTableData("SELECT info.table_name AS calculation_output_table_names " +
                               "FROM information_schema.tables info " +
                               "INNER JOIN pg_class pg on pg.relname = info.table_name " +
                               "WHERE info.table_schema = 'kpi' " +
                               "  AND info.table_name LIKE 'kpi_%' " +
                               "  AND NOT pg.relispartition " +
                               "  AND info.table_type = 'BASE TABLE' ");
    }

    private List<String> gatherTableData(final String sql) {
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery(sql)) {
            final List<String> result = new ArrayList<>();

            while (resultSet.next()) {
                final String calculationOutputTableNames = resultSet.getString("calculation_output_table_names");
                if (isNotInternalTable(calculationOutputTableNames)) {
                    result.add(calculationOutputTableNames);
                }
            }
            return result;
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public void createOutputTable(final Connection connection, final TableCreationInformation tableCreationInformation, final Map<String, KpiDataType> validAggregationElements) throws SQLException {
        SqlStatementExecutor.executeUpdate(connection, sqlTableGenerator.generateOutputTable(tableCreationInformation, validAggregationElements));
    }

    @Override
    public void createTabularParameterTable(final Connection connection, final List<Parameter> parameters, final String tabularParameterName) throws SQLException {
        SqlStatementExecutor.executeUpdate(connection, sqlTableGenerator.generateTabularParameterTableSql(parameters, tabularParameterName));
    }

    @Override
    public void addColumnsToOutputTable(final Connection connection, final TableCreationInformation tableCreationInformation, Map<String, KpiDataType> validAggregationElements) throws SQLException {
        SqlStatementExecutor.executeBatch(connection, sqlTableModifier.createAlterTableToAddNewColumns(tableCreationInformation, validAggregationElements));
    }

    @Override
    public void dropPrimaryKeyConstraint(final Connection connection, final Table table) throws SQLException {
        SqlStatementExecutor.executeUpdate(connection, sqlTableModifier.dropPrimaryKeyConstraint(table));
    }

    @Override
    public void addPrimaryKeyConstraint(final Connection connection,
                                        final Table table,
                                        final Collection<? extends Column> primaryKeys) throws SQLException {
        SqlStatementExecutor.executeUpdate(connection, sqlTableModifier.addPrimaryKeyConstraint(table, primaryKeys));
    }

    @Override
    public void changeColumnType(final Connection connection, final Table table, final ColumnDefinition columnDefinition) throws SQLException {
        SqlStatementExecutor.executeUpdate(connection, sqlTableModifier.changeColumnType(table, columnDefinition));
    }

    @Override
    public List<Column> findAllPrimaryKeys(final Table table) {
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final ResultSet resultSet = connection.getMetaData().getPrimaryKeys(null, null, table.getName())) {
            final List<Column> result = new ArrayList<>();

            while (resultSet.next()) {
                result.add(Column.of(resultSet.getString("COLUMN_NAME")));
            }
            return result;
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public List<ColumnDefinition> findAllColumnDefinitions(final Table table) {
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final ResultSet resultSet = connection.getMetaData().getColumns(null, null, table.getName(), null)) {
            final List<ColumnDefinition> result = new ArrayList<>();

            while (resultSet.next()) {
                result.add(ColumnDefinition.of(
                        column(resultSet.getString("COLUMN_NAME")),
                        convertToKpiDataType(resultSet.getString("TYPE_NAME"))
                ));
            }

            return result;
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public ColumnDefinition findColumnDefinition(final Table table, final String columnName) {
        final List<ColumnDefinition> columnDefinitions = findAllColumnDefinitions(table);
        return columnDefinitions.stream().filter(columnDefinition -> columnDefinition.hasSameName(columnName)).findFirst().orElseThrow(() -> {
            final String message = String.format("Column '%s' is not found for table '%s'", columnName, table.getName());
            return new EntityNotFoundException(message);
        });
    }

    @Override
    public long saveTabularParameter(final Connection connection, final Table table, final String header, final Reader tabularParameterValues) throws SQLException, IOException {
        final String tableName = table.getName();
        final String sql;

        if (header == null) {
            sql = String.format("COPY kpi_service_db.kpi.%s FROM STDIN WITH (FORMAT csv)", tableName);
        } else {
            final String correctedHeader = Arrays.stream(header.split(","))
                    .map(column -> String.format("\"%s\"", column.strip()))
                    .collect(Collectors.joining(","));
            sql = String.format("COPY kpi_service_db.kpi.%s (%s) FROM STDIN WITH (FORMAT csv)", tableName, correctedHeader);
        }

        final CopyManager copyManager = new CopyManager((BaseConnection) connection);
        return copyManager.copyIn(sql, tabularParameterValues);
    }

    @Override
    public void deleteColumnsForTable(final Connection connection, final String table, final Collection<String> columns) throws SQLException {
        SqlStatementExecutor.executeUpdate(connection, sqlTableModifier.dropColumnsForTable(table, columns));
    }

    @Override
    public void deleteTables(final Connection connection, final Collection<String> tables) throws SQLException {
        SqlStatementExecutor.executeUpdate(connection, sqlTableModifier.dropTables(tables));
    }

    @Override
    public void createSchemaByCollectionId(final UUID collectionId) {
        String sql = String.format("CREATE SCHEMA IF NOT EXISTS \"kpi_%s\"", collectionId);

        try(final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
            final Statement statement = connection.createStatement()){
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public boolean isNotInternalTable(final String tableName) {
        return INTERNAL_TABLE_NAMES.stream()
                .noneMatch(internalTableName -> isInternalTableName(internalTableName, tableName));
    }

    private boolean isInternalTableName(final String internalTableName, final String tableName) {
        return internalTableName.equalsIgnoreCase(tableName);
    }

    private KpiDataType convertToKpiDataType(final String type) {
        return KpiDataType.valuesAsList()
                .stream()
                .filter(kpiDataType -> kpiDataType.getColumnType().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("'%s' type is not known.", type)));
    }

}
