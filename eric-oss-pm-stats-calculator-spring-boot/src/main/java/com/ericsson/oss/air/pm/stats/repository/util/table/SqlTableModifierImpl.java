/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.repository.util.partition.PartitionUniqueIndex;
import com.ericsson.oss.air.pm.stats.repository.util.statement.SqlStatement;
import com.ericsson.oss.air.pm.stats.repository.util.statement.SqlStatementBuilder;
import com.ericsson.oss.air.pm.stats.repository.util.table.api.SqlTableModifier;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.ColumnDefinition;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.TableCreationInformation;
import com.ericsson.oss.air.pm.stats.repository.util.table.util.StringUtils;

import io.vavr.collection.Stream;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class SqlTableModifierImpl implements SqlTableModifier {

    @Override
    public List<SqlStatement> createAlterTableToAddNewColumns(final TableCreationInformation tableCreationInformation, final Map<String, KpiDataType> validAggregationElements) {
        return Stream.concat(newDefinitionColumns(tableCreationInformation),
                        newAggregationElementColumns(tableCreationInformation, validAggregationElements))
                .collect(Collectors.toList());
    }

    @Override
    public SqlStatement addPrimaryKeyConstraint(@NonNull final Table table, @NonNull final Collection<? extends Column> primaryKeys) {
        return SqlStatementBuilder
                .template("ALTER TABLE kpi_service_db.kpi.${tableName} ADD CONSTRAINT ${primaryKeyName} PRIMARY KEY (${primaryKeys})")
                .replace("tableName", table.getName())
                .replace("primaryKeyName", StringUtils.generatePrimaryKeyName(table))
                .replace("primaryKeys", StringUtils.commaJoinEnquote(primaryKeys))
                .create();
    }

    @Override
    public SqlStatement dropPrimaryKeyConstraint(@NonNull final Table table) {
        return SqlStatementBuilder.template("ALTER TABLE kpi_service_db.kpi.${tableName} DROP CONSTRAINT IF EXISTS ${primaryKeyName}")
                .replace("tableName", table.getName())
                .replace("primaryKeyName", StringUtils.generatePrimaryKeyName(table))
                .create();
    }

    @Override
    public SqlStatement dropUniqueIndex(@NonNull final PartitionUniqueIndex uniqueIndex) {
        return SqlStatementBuilder.template("DROP INDEX ${indexName}")
                .replace("indexName", uniqueIndex.getUniqueIndexName())
                .create();
    }

    @Override
    public SqlStatement createUniqueIndex(@NonNull final PartitionUniqueIndex uniqueIndex) {
        return SqlStatementBuilder.template("CREATE UNIQUE INDEX IF NOT EXISTS ${uniqueIndexName} ON ${partitionName} (${uniqueIndexColumns})")
                .replace("uniqueIndexName", uniqueIndex.getUniqueIndexName())
                .replace("partitionName", uniqueIndex.getPartitionName())
                .replace("uniqueIndexColumns", StringUtils.commaJoinEnquote(uniqueIndex.indexColumns()))
                .create();
    }

    @Override
    public SqlStatement changeColumnType(@NonNull final Table table, final @NonNull ColumnDefinition columnDefinition) {
        return SqlStatementBuilder
                .template("ALTER TABLE kpi_service_db.kpi.${tableName} ALTER COLUMN ${column} TYPE ${type} USING (${column}::${type})")
                .replace("tableName", table.getName())
                .replace("column", StringUtils.enquoteLiteral(columnDefinition.getColumn().getName()))
                .replace("type", columnDefinition.getDataType().getPostgresType())
                .create();
    }

    @Override
    public SqlStatement dropColumnsForTable(final String table, final Collection<String> columns) {
        return SqlStatementBuilder
                .template("ALTER TABLE kpi_service_db.kpi.${table} ${statement}")
                .replace("table", table)
                .replace("statement", columns.stream().map(column -> String.format("DROP COLUMN %s CASCADE", column)).collect(Collectors.joining(", ")))
                .create();
    }

    @Override
    public SqlStatement dropTables(final Collection<String> tables) {
        return SqlStatementBuilder
                .template("DROP TABLE IF EXISTS ${statement} CASCADE")
                .replace("statement", tables.stream().map(table -> String.format("kpi_service_db.kpi.%s", table)).collect(Collectors.joining(", ")))
                .create();
    }

    private List<SqlStatement> newAggregationElementColumns(@NonNull final TableCreationInformation tableCreationInformation, @NonNull final Map<String, KpiDataType> validAggregationElements) {
        return tableCreationInformation
                .collectAggregationElements()
                .stream()
                .map(aggregationElement -> createNewAggregationElementColumnStatement(aggregationElement, tableCreationInformation, validAggregationElements))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private SqlStatement createNewAggregationElementColumnStatement(final String aggregationElement,
                                                                    @NonNull final TableCreationInformation tableCreationInformation,
                                                                    @NonNull final Map<String, KpiDataType> validAggregationElements) {
        final KpiDataType kpiDataType = validAggregationElements.get(aggregationElement);

        return SqlStatementBuilder.template("ALTER TABLE kpi_service_db.kpi.${tableName} " +
                        "ADD COLUMN IF NOT EXISTS ${columnName} ${columnType} NOT NULL default ${defaultValue}")
                .replace("tableName", tableCreationInformation.getTableName())
                .replace("columnName", StringUtils.enquoteLiteral(aggregationElement))
                .replace("columnType", kpiDataType.getPostgresType())
                .replace("defaultValue", kpiDataType.getDefaultValue())
                .create();
    }

    private static List<SqlStatement> newDefinitionColumns(@NonNull final TableCreationInformation tableCreationInformation) {
        return tableCreationInformation.getDefinitions()
                .stream()
                .map(definition -> createNewDefinitionColumnStatement(definition, tableCreationInformation))
                .collect(Collectors.toList());
    }

    private static SqlStatement createNewDefinitionColumnStatement(@NonNull final KpiDefinitionEntity definition,
                                                                   @NonNull final TableCreationInformation tableCreationInformation) {
        final String columnName = definition.name();
        final String objectType = definition.objectType();

        return SqlStatementBuilder.template("ALTER TABLE kpi_service_db.kpi.${tableName} ADD COLUMN IF NOT EXISTS ${columnName} ${columnType}")
                .replace("tableName", tableCreationInformation.getTableName())
                .replace("columnName", StringUtils.enquoteLiteral(columnName))
                .replace("columnType", KpiDataType.forValue(objectType).getPostgresType())
                .create();
    }
}
