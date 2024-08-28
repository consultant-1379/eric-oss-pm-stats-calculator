/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.api;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.ejb.Local;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiPersistenceException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload.TabularParameters;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.AggregationElement;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.TableCreationInformation;

@Local
public interface DatabaseService {
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
     * Executes a query to check if the given tables by names exist.
     *
     * @param names Names of the tables to check
     * @return {@code true} if tables by names exit, otherwise false.
     */
    boolean doesTablesExistByName(Set<String> names);

    /**
     * Finds all the calculation output tables.
     *
     * @return {@link List} of calculation output tables.
     * @implNote Calculation output table has the following pattern:
     * <strong>{@code  kpi_<alias>_<aggregation_period>}</strong>
     * <br>
     * The aggregation period might be empty, in that case the pattern is:
     * <strong>{@code  kpi_<alias>}</strong>
     */
    List<String> findAllCalculationOutputTables();

    /**
     * Finds all calculation output tables ignoring the partitions.
     *
     * @return {@link List} of calculation output tables
     */
    List<String> findAllOutputTablesWithoutPartition();

    /**
     * Finds the column names existing in the given table.
     *
     * @param table Name of the table
     * @return {@link List} of the names of the columns
     */
    List<String> findColumnNamesForTable(String table);

    /**
     * Finds column name and types of a {@link List} of {@link AggregationElement}
     *
     * @param aggregationElements {@link List} of {@link AggregationElement}
     * @return {@link Map} of aggregation element column name and type.
     */
    Map<String, KpiDataType> findAggregationElementColumnType(List<AggregationElement> aggregationElements) throws KpiPersistenceException;

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
     * Adds new columns to the output table based on the {@link KpiDefinitionEntity} supplied.
     *
     * @param connection               {@link Connection} to the database.
     * @param tableCreationInformation {@link TableCreationInformation} containing necessary information to add new columns.
     * @param validAggregationElements the {@link Map} of valid aggregation element names and types.
     * @throws SQLException If there are any errors adding new columns to the table.
     */
    void addColumnsToOutputTable(Connection connection, TableCreationInformation tableCreationInformation, Map<String, KpiDataType> validAggregationElements) throws SQLException;


    /**
     * Recreates primary key constraint.
     *
     * @param connection        {@link Connection} to the database.
     * @param table             {@link Table} to create primary key constraint
     * @param wantedPrimaryKeys {@link Collection} of {@link Column} to create as primary keys
     * @throws SQLException If there are any errors recreating primary key constraint.
     */
    void recreatePrimaryKeyConstraint(Connection connection, Table table, Collection<? extends Column> wantedPrimaryKeys) throws SQLException;

    /**
     * Recreates unique index.
     *
     * @param connection    {@link Connection} to the database.
     * @param table         {@link Table} to create primary key constraint
     * @param wantedIndexes {@link Collection} of {@link Column} to create as unique index
     * @throws SQLException If there are any errors recreating unique index.
     */
    void recreateUniqueIndex(Connection connection, Table table, Collection<? extends Column> wantedIndexes) throws SQLException;

    /**
     * Updates database schema column definitions
     *
     * @param connection               {@link Connection} to the database.
     * @param tableCreationInformation {@link TableCreationInformation} containing necessary information to update the schema.
     * @param validAggregationElements the {@link Map} of valid aggregation element names and types.
     * @throws SQLException If there are any errors updating the schema.
     */
    void updateSchema(Connection connection, TableCreationInformation tableCreationInformation, Map<String, KpiDataType> validAggregationElements) throws SQLException;

    /**
     * Changes column type in output table
     *
     * @param connection          {@link Connection} to the database.
     * @param kpiDefinitionEntity {@link KpiDefinitionEntity} containing necessary information to change the column type.
     */
    void changeColumnType(Connection connection, KpiDefinitionEntity kpiDefinitionEntity);

    /**
     * Saves tabular parameter to the database.
     *
     * @param connection        {@link Connection} to the database.
     * @param tabularParameters {@link TabularParameters} data to save.
     * @param calculationId     {@link UUID} the id of the calculation
     */
    long saveTabularParameter(Connection connection, TabularParameters tabularParameters, UUID calculationId) throws SQLException, IOException;

    /**
     * Creates a table based on a request.
     *
     * @param connection            {@link Connection} to the database.
     * @param tabularParameterNames the names of tabular parameter.
     * @param calculationId         {@link UUID} the id of the calculation
     * @throws SQLException If there are any errors creating the table.
     */
    void createTabularParameterTables(Connection connection, Collection<String> tabularParameterNames, UUID calculationId) throws SQLException;

    /**
     * Deletes the columns from the output table
     *
     * @param connection {@link Connection} to the database.
     * @param table      The table's name
     * @param columns    The names of the columns to be deleted
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
     * Creates schema in databse by collectionId
     * @param collectionId
     */
    void createSchemaByCollectionId(UUID collectionId);
}
