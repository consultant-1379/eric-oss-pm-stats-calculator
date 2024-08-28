/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util.table.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.repository.util.partition.PartitionUniqueIndex;
import com.ericsson.oss.air.pm.stats.repository.util.statement.SqlStatement;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.ColumnDefinition;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.TableCreationInformation;

public interface SqlTableModifier {

    /**
     * Create SQL <strong>ALTER</strong> statements to add new columns.
     *
     * @param tableCreationInformation {@link TableCreationInformation} containing necessary information to {@link SqlStatement}s.
     * @param validAggregationElements the {@link Map} of valid aggregation element names and types.
     * @return {@link List} of {@link SqlStatement}s on adding new columns
     */
    List<SqlStatement> createAlterTableToAddNewColumns(TableCreationInformation tableCreationInformation, Map<String, KpiDataType> validAggregationElements);

    /**
     * Create SQL <strong>ALTER</strong> statement to add primary key constraint.
     *
     * @param table       {@link Table} to create primary key constraint
     * @param primaryKeys {@link Collection} of {@link Column} to create as primary keys
     * @return {@link SqlStatement} on adding constraint
     */
    SqlStatement addPrimaryKeyConstraint(Table table, Collection<? extends Column> primaryKeys);

    /**
     * Create SQL <strong>DROP CONSTRAINT</strong> statement to remove the primary key constraint.
     *
     * @param table {@link Table} to drop primary key constraint
     * @return {@link SqlStatement} on removing constraint
     */
    SqlStatement dropPrimaryKeyConstraint(Table table);

    /**
     * Create SQL <strong>DROP INDEX</strong> statements to drop unique index.
     *
     * @param uniqueIndex {@link PartitionUniqueIndex} containing index information
     * @return {@link SqlStatement} on dropping unique index
     */
    SqlStatement dropUniqueIndex(PartitionUniqueIndex uniqueIndex);

    /**
     * Create SQL <strong>CREATE UNIQUE INDEX</strong> statement to create unique index for a partition.
     *
     * @param uniqueIndex {@link PartitionUniqueIndex} containing index information
     * @return {@link SqlStatement} on creating unique index
     */
    SqlStatement createUniqueIndex(PartitionUniqueIndex uniqueIndex);

    /**
     * Create SQL <strong>ALTER TABLE</strong> statement to change the data type of column.
     *
     * @param table            {@link Table} to change colum in
     * @param columnDefinition {@link ColumnDefinition} containing information on the change
     * @return {@link SqlStatement} on changing column type
     */
    SqlStatement changeColumnType(Table table, ColumnDefinition columnDefinition);

    /**
     * Create SQL <strong>ALTER TABLE</strong> statement to drop the given columns
     *
     * @param table   The table's name, where the columns are located
     * @param columns {@link List} The column names, that should be dropped
     * @return {@link SqlStatement} on dropping the columns
     */
    SqlStatement dropColumnsForTable(String table, Collection<String> columns);

    /**
     * Create SQL <strong>DROP TABLE</strong> statement to drop the given tables
     *
     * @param tables The tables' names
     * @return {@link SqlStatement} on dropping the table
     */
    SqlStatement dropTables(Collection<String> tables);
}
