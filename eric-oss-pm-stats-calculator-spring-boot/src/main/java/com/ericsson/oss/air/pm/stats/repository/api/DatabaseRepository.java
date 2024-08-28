/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.api;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ejb.Local;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.Parameter;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.ColumnDefinition;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.TableCreationInformation;

@Local
public interface DatabaseRepository {

    /**
     * Checks if the <strong>KPI Database</strong> is available.
     *
     * @return true if the <strong>KPI Database</strong> is available, else false.
     */
    boolean isAvailable();

    /**
     * Executes a query to check if the given table by name exists.
     * <br>
     * Checked table types:
     * <ul>
     *     <li>TABLE</li>
     *     <li>PARTITIONED TABLE"</li>
     * </ul>
     *
     * @param name The name of the table to check
     * @return {@code true} if the table by name exits, otherwise false.
     */
    boolean doesTableExistByName(String name);

    /**
     * Finds all the calculation output tables.
     *
     * @return {@link List} of calculation output tables.
     * @implNote Calculation output table has the following pattern:
     * <strong>{@code  kpi_<alias>_<aggregation_period>}</strong>
     * <br>
     * The aggregation period might be null, in that case the pattern is:
     * <strong>{@code  kpi_<alias>}</strong>
     */
    List<String> findAllCalculationOutputTables();

    /**
     * Finds all the calculation output tables without the partitioned ones
     *
     * @return {@link List} of calculation output tables.
     * @implNote Calculation output table has the following pattern:
     * <strong>{@code  kpi_<alias>_<aggregation_period>}</strong>
     * <br>
     * The aggregation period might be null, in that case the pattern is:
     * <strong>{@code  kpi_<alias>}</strong>
     */
    List<String> findAllOutputTablesWithoutPartition();

    /**
     * Creates an output table based on the {@link KpiDefinitionEntity} supplied.
     *
     * @param connection               {@link Connection} to the database.
     * @param tableCreationInformation {@link TableCreationInformation} containing necessary information to create table.
     * @param validAggregationElements the {@link Map} of valid aggregation element names and types.
     * @throws SQLException If there are any errors creating the table.
     */
    void createOutputTable(Connection connection, TableCreationInformation tableCreationInformation, Map<String, KpiDataType> validAggregationElements) throws SQLException;

    /**
     * Add new columns to the output table based on the {@link TableCreationInformation} supplied.
     *
     * @param connection               {@link Connection} to the database.
     * @param tableCreationInformation {@link TableCreationInformation} containing necessary information to add new columns.
     * @param validAggregationElements the {@link Map} of valid aggregation element names and types.
     * @throws SQLException If there are any errors creating the table.
     */
    void addColumnsToOutputTable(Connection connection, TableCreationInformation tableCreationInformation, Map<String, KpiDataType> validAggregationElements) throws SQLException;

    /**
     * Drops primary key constraint.
     *
     * @param connection {@link Connection} to the database.
     * @param table      {@link Table} to drop primary key constraint for.
     * @throws SQLException If there are any errors dropping the constraint.
     */
    void dropPrimaryKeyConstraint(Connection connection, Table table) throws SQLException;

    /**
     * Adds primary key constraint.
     *
     * @param connection  {@link Connection} to the database.
     * @param table       {@link Table} to create primary key constraint
     * @param primaryKeys {@link Collection} of {@link Column}s to create as primary keys
     * @throws SQLException If there are any errors adding the constraint.
     */
    void addPrimaryKeyConstraint(Connection connection, Table table, Collection<? extends Column> primaryKeys) throws SQLException;

    /**
     * Changes the data type of column.
     *
     * @param table            {@link Table} to change colum in
     * @param columnDefinition {@link ColumnDefinition} containing information on what column should be changed
     * @throws SQLException If there are any errors changing the datatype of column.
     */
    void changeColumnType(Connection connection, Table table, ColumnDefinition columnDefinition) throws SQLException;

    /**
     * Finds all primary keys of the provided {@link Table}.
     *
     * @param table {@link Table} to read primary keys
     * @return {@link List} of primary key {@link Column}s
     */
    List<Column> findAllPrimaryKeys(Table table);

    /**
     * Finds column definitions of a given table.
     *
     * @param table {@link Table} to read column definitions for
     * @return {@link List} of {@link ColumnDefinition}s contains information on columns
     */
    List<ColumnDefinition> findAllColumnDefinitions(Table table);

    /**
     * Finds column definition for a given column name of a given table.
     *
     * @param table      {@link Table} to read column definitions for
     * @param columnName {@link String} to read column definitions for
     * @return {@link ColumnDefinition} contains information on the column
     */
    ColumnDefinition findColumnDefinition(Table table, String columnName);

    /**
     * Creates a table based on a request.
     *
     * @param connection           {@link Connection} to the database.
     * @param parameters           {@link List} of {@link Parameter} related to the tabular parameter.
     * @param tabularParameterName {@link String} the name of the tabular parameter identifier
     * @throws SQLException If there are any errors creating the table.
     */
    void createTabularParameterTable(Connection connection, List<Parameter> parameters, String tabularParameterName) throws SQLException;

    /**
     * Saves tabular parameter to the database.
     *
     * @param connection             {@link Connection} to the database.
     * @param table                  {@link Table} name of the table to save to.
     * @param header                 {@link String} header of the data.
     * @param tabularParameterValues {@link Reader} of the csv table to save.
     * @throws SQLException If any error occurs during persistence.
     * @throws IOException  If any error occurs during failed or interrupted I/O operations.
     */
    long saveTabularParameter(Connection connection, Table table, String header, Reader tabularParameterValues) throws SQLException, IOException;

    /**
     * Deletes the columns from the output table
     *
     * @param connection {@link Connection} to the database.
     * @param table      The table's name
     * @param columns    The  names of the columns to be deleted
     * @throws SQLException If the database operation is not successful
     */
    void deleteColumnsForTable(Connection connection, String table, Collection<String> columns) throws SQLException;

    /**
     * Deletes a tables from the database
     *
     * @param connection {@link Connection} to the database.
     * @param tables     The tables' names
     * @throws SQLException If the database operation is not successful
     */
    void deleteTables(Connection connection, Collection<String> tables) throws SQLException;

    /**
     * Creates schema in database for collection by collectionId
     *
     * @param collectionId
     */
    void createSchemaByCollectionId(UUID collectionId);
}
