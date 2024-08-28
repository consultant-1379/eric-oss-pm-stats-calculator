/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service;

import static lombok.AccessLevel.PUBLIC;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiPersistenceException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.Format;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload.TabularParameters;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.AggregationElement;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.Parameter;
import com.ericsson.oss.air.pm.stats.repository.api.DatabaseRepository;
import com.ericsson.oss.air.pm.stats.repository.api.ParameterRepository;
import com.ericsson.oss.air.pm.stats.repository.api.PartitionRepository;
import com.ericsson.oss.air.pm.stats.repository.util.partition.PartitionUniqueIndex;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.ColumnDefinition;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.TableCreationInformation;
import com.ericsson.oss.air.pm.stats.service.api.DatabaseService;
import com.ericsson.oss.air.pm.stats.service.util.CollectionHelpers;
import com.ericsson.oss.air.pm.stats.service.util.Mappers;
import com.ericsson.oss.air.pm.stats.service.util.Mergers;
import com.ericsson.oss.air.pm.stats.service.util.TabularParameterUtils;
import com.ericsson.oss.air.pm.stats.service.validator.PostgresDataTypeChangeValidator;
import com.ericsson.oss.air.pm.stats.utils.JsonToCsvConverter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor(access = PUBLIC)
public class DatabaseServiceImpl implements DatabaseService {

    private DatabaseRepository databaseRepository;
    private PartitionRepository partitionRepository;
    private PostgresDataTypeChangeValidator dataTypeChangeValidator;
    private ParameterRepository parameterRepository;

    @Override
    public boolean isAvailable() {
        return databaseRepository.isAvailable();
    }

    @Override
    public boolean doesTableExistByName(final String name) {
        return databaseRepository.doesTableExistByName(name);
    }

    @Override
    public boolean doesTablesExistByName(final Set<String> names) {
        return names.stream().allMatch(this::doesTableExistByName);
    }

    @Override
    public List<String> findAllCalculationOutputTables() {
        return databaseRepository.findAllCalculationOutputTables();
    }

    @Override
    public List<String> findAllOutputTablesWithoutPartition() {
        return databaseRepository.findAllOutputTablesWithoutPartition();
    }

    @Override
    public List<String> findColumnNamesForTable(final String table) {
        return databaseRepository.findAllColumnDefinitions(Table.of(table)).stream().map(columnDefinition ->
                columnDefinition.getColumn().getName()).toList();
    }

    @Override
    public Map<String, KpiDataType> findAggregationElementColumnType(final List<AggregationElement> aggregationElements) {
        final Map<String, KpiDataType> aggregationElementsAndTypes = new HashMap<>();
        final Map<Table, List<AggregationElement>> aggregationElementsByTable = aggregationElements.stream()
                .collect(Collectors.groupingBy(aggregationElement -> Table.of(aggregationElement.getSourceTable())));

        for (final Map.Entry<Table, List<AggregationElement>> entry : aggregationElementsByTable.entrySet()) {
            final List<ColumnDefinition> columnDefinitions = databaseRepository.findAllColumnDefinitions(entry.getKey());

            for (final AggregationElement aggregationElement : entry.getValue()) {
                final KpiDataType columnType = columnDefinitions.stream()
                        .filter(definition -> definition.getColumn().getName().equals(aggregationElement.getSourceColumn()))
                        .findFirst()
                        .orElseThrow(() -> new KpiPersistenceException(
                                String.format("Invalid aggregation element in the given definitions: '%s' is invalid.", aggregationElement.getExpression())))
                        .getDataType();

                aggregationElementsAndTypes.put(aggregationElement.getTargetColumn(), columnType);
            }
        }
        return aggregationElementsAndTypes;
    }

    @Override
    public void createOutputTable(final Connection connection, final TableCreationInformation tableCreationInformation, final Map<String, KpiDataType> validAggregationElements) throws SQLException {
        databaseRepository.createOutputTable(connection, tableCreationInformation, validAggregationElements);
    }

    @Override
    public void addColumnsToOutputTable(final Connection connection, final TableCreationInformation tableCreationInformation, final Map<String, KpiDataType> validAggregationElements) throws SQLException {
        databaseRepository.addColumnsToOutputTable(connection, tableCreationInformation, validAggregationElements);
    }

    @Override
    public void recreatePrimaryKeyConstraint(final Connection connection,
                                             final Table table,
                                             @NonNull final Collection<? extends Column> wantedPrimaryKeys) throws SQLException {
        if (wantedPrimaryKeys.isEmpty()) {
            return;
        }

        final List<Column> oldPrimaryKeys = databaseRepository.findAllPrimaryKeys(table);
        final List<Column> newPrimaryKeys = Mergers.symmetricDifference(oldPrimaryKeys, wantedPrimaryKeys);

        if (newPrimaryKeys.isEmpty()) {
            return;
        }

        recreatePrimaryKeys(connection, table, Mergers.mergeSorted(oldPrimaryKeys, newPrimaryKeys, Comparator.comparing(Column::getName)));
    }

    @Override
    public void recreateUniqueIndex(final Connection connection,
                                    final Table table,
                                    @NonNull final Collection<? extends Column> wantedIndexes) throws SQLException {
        if (wantedIndexes.isEmpty()) {
            return;
        }

        final List<PartitionUniqueIndex> partitionUniqueIndexes = partitionRepository.findAllPartitionUniqueIndexes(table);
        for (final PartitionUniqueIndex partitionUniqueIndex : partitionUniqueIndexes) {
            if (isIndexRecreationNeeded(wantedIndexes, partitionUniqueIndex.indexColumnsWithTrimmedQuotes())) {
                recreateUniqueIndex(connection, wantedIndexes, partitionUniqueIndex);
            }
        }
    }

    @Override
    public void updateSchema(final Connection connection, final TableCreationInformation tableCreationInformation, final Map<String, KpiDataType> validAggregationElements) throws SQLException {
        final List<ColumnDefinition> changingColumDefinitions = collectChangingColumDefinitions(tableCreationInformation, validAggregationElements);

        final Table table = Table.of(tableCreationInformation.getTableName());
        final List<ColumnDefinition> changedColumnDefinitions = collectOldColumnDefinitions(tableCreationInformation, validAggregationElements);

        for (final ColumnDefinition changingDefinition : changingColumDefinitions) {
            isValidColumnDefinitionChange(tableCreationInformation, changingDefinition, changedColumnDefinitions);

            log.info("Column data type for table: '{}' has changed, column: '{}', type: '{}'", tableCreationInformation.getTableName(),
                    changingDefinition.getColumn().getName(), changingDefinition.getDataType().getPostgresType());

            databaseRepository.changeColumnType(connection, table, changingDefinition);
        }
    }

    @Override
    public void changeColumnType(final Connection connection, final KpiDefinitionEntity kpiDefinitionEntity) {
        isValidColumnDefinitionChange(kpiDefinitionEntity);
        final Table table = Table.of(kpiDefinitionEntity.tableName());
        final ColumnDefinition changingDefinition = ColumnDefinition.from(kpiDefinitionEntity.name(), kpiDefinitionEntity.objectType());
        try {
            databaseRepository.changeColumnType(connection, table, changingDefinition);
            log.info("Column data type for table: '{}' has changed, column: '{}', type: '{}'", table.getName(),
                    changingDefinition.getColumn().getName(), changingDefinition.getDataType().getPostgresType());
        } catch (SQLException e) {
            throw new KpiPersistenceException(String.format("Error changing column type, KPI output table: '%s', column: '%s', type: '%s'",
                    table.getName(), changingDefinition.getColumn().getName(), changingDefinition.getDataType().getPostgresType()), e);
        }
    }

    @Override
    public long saveTabularParameter(final Connection connection, final TabularParameters tabularParameters, final UUID calculationId) throws SQLException, IOException {
        String valueForReader;
        String header;
        if (tabularParameters.getFormat() == Format.CSV) {
            valueForReader = tabularParameters.getValue();
            header = tabularParameters.getHeader();
        } else {
            final TabularParameters convertedData = JsonToCsvConverter.convertJsonToCsv(tabularParameters.getValue());
            valueForReader = convertedData.getValue();
            header = convertedData.getHeader();
        }
        try (Reader reader = new StringReader(valueForReader)) {
            final Table tabularParameterTable = Table.of(TabularParameterUtils.makeUniqueTableNameForTabularParameters(tabularParameters.getName(), calculationId));
            return databaseRepository.saveTabularParameter(connection, tabularParameterTable, header, reader);
        }
    }

    @Override
    public void createTabularParameterTables(final Connection connection, final Collection<String> tabularParameterNames, final UUID calculationId) throws SQLException {

        for (final String tabularParameterName : tabularParameterNames) {
            List<Parameter> parameters = parameterRepository.findParametersForTabularParameter(tabularParameterName);
            final String uniqueTabularParameterName = TabularParameterUtils.makeUniqueTableNameForTabularParameters(tabularParameterName, calculationId);
            databaseRepository.createTabularParameterTable(connection, parameters, uniqueTabularParameterName);
            log.info("TabularParameterTable with name: {} was created.", uniqueTabularParameterName);
        }
    }

    @Override
    public void deleteColumnsForTable(final Connection connection, final String table, final Collection<String> columns) throws SQLException {
        databaseRepository.deleteColumnsForTable(connection, table, columns);
    }

    @Override
    public void deleteTables(final Connection connection, final Collection<String> tables) throws SQLException {
        databaseRepository.deleteTables(connection, tables);
    }

    @Override
    public void createSchemaByCollectionId(final UUID collectionId) {
        databaseRepository.createSchemaByCollectionId(collectionId);
    }

    private void isValidColumnDefinitionChange(TableCreationInformation tableCreationInformation, ColumnDefinition targetColumnDefinition,
                                               List<ColumnDefinition> sourceColumnDefinitions) throws SQLException {
        String targetName = targetColumnDefinition.getColumn().getName();
        final Optional<ColumnDefinition> matchingSource = sourceColumnDefinitions.stream()
                .filter(sourceColumnDefinition -> sourceColumnDefinition.getColumn().getName().equals(targetName)).findFirst();

        if (matchingSource.isPresent()
                && dataTypeChangeValidator.isInvalidChange(matchingSource.get().getDataType(), targetColumnDefinition.getDataType())) {
            throw new SQLException(String.format(
                    "Column data type change from '%s' to '%s' for table: '%s' is invalid",
                    matchingSource.get().getDataType().getColumnType(),
                    targetColumnDefinition.getDataType().getColumnType(),
                    tableCreationInformation.getTableName()
            ));
        }
    }

    private void isValidColumnDefinitionChange(final KpiDefinitionEntity kpiDefinitionEntity) {
        final Table table = Table.of(kpiDefinitionEntity.tableName());
        final ColumnDefinition persistedColumn = databaseRepository.findColumnDefinition(table, kpiDefinitionEntity.name());
        final ColumnDefinition targetColumn = ColumnDefinition.from(kpiDefinitionEntity.name(), kpiDefinitionEntity.objectType());
        if (dataTypeChangeValidator.isInvalidChange(persistedColumn.getDataType(), targetColumn.getDataType())) {
            throw new KpiPersistenceException(String.format(
                    "Column data type change from '%s' to '%s' for table: '%s' is invalid",
                    persistedColumn.getDataType(), targetColumn.getDataType(), table.getName()
            ));
        }
    }

    private void recreateUniqueIndex(final Connection connection,
                                     final Collection<? extends Column> wantedIndexes,
                                     final PartitionUniqueIndex partitionUniqueIndex) throws SQLException {
        partitionRepository.dropUniqueIndex(connection, partitionUniqueIndex);

        final PartitionUniqueIndex newPartitionUniqueIndex =
                partitionUniqueIndex.withIndexes(
                        Mergers.union(partitionUniqueIndex.indexColumns(), wantedIndexes)
                );
        partitionRepository.createUniqueIndex(connection, newPartitionUniqueIndex);
    }

    private void recreatePrimaryKeys(final Connection connection, final Table table, final List<? extends Column> primaryKeys) throws SQLException {
        databaseRepository.dropPrimaryKeyConstraint(connection, table);
        databaseRepository.addPrimaryKeyConstraint(connection, table, primaryKeys);
    }

    private List<ColumnDefinition> collectChangingColumDefinitions(@NonNull final TableCreationInformation tableCreationInformation, final Map<String, KpiDataType> validAggregationElements) {
        final Table table = Table.of(tableCreationInformation.getTableName());
        final List<ColumnDefinition> wantedColumnDefinitions = collectWantedColumnDefinitions(tableCreationInformation, validAggregationElements);
        final List<ColumnDefinition> persistedColumnDefinitions = databaseRepository.findAllColumnDefinitions(table);

        return getCollect(persistedColumnDefinitions, wantedColumnDefinitions);
    }

    private List<ColumnDefinition> collectOldColumnDefinitions(final TableCreationInformation tableCreationInformation, final Map<String, KpiDataType> validAggregationElements) {
        final Table table = Table.of(tableCreationInformation.getTableName());
        final List<ColumnDefinition> wantedColumnDefinitions = collectWantedColumnDefinitions(tableCreationInformation, validAggregationElements);
        final List<ColumnDefinition> persistedColumnDefinitions = databaseRepository.findAllColumnDefinitions(table);

        return getCollect(wantedColumnDefinitions, persistedColumnDefinitions);
    }

    private List<ColumnDefinition> getCollect(final List<ColumnDefinition> sourceColumnDefinitions, final List<ColumnDefinition> targetColumnDefinitions) {
        return targetColumnDefinitions.stream()
                .filter(targetColumnDefinition -> doesColumnNeedsToBeChanged(targetColumnDefinition, sourceColumnDefinitions))
                .toList();
    }

    private List<ColumnDefinition> collectWantedColumnDefinitions(@NonNull final TableCreationInformation tableCreationInformation, Map<String, KpiDataType> validAggregationElements) {
        final List<ColumnDefinition> wantedColumnDefinitions = Mergers.merge(
                Mappers.toColumnDefinition(tableCreationInformation.collectAggregationElements(), validAggregationElements),
                Mappers.toColumnDefinition(tableCreationInformation.getDefinitions())
        );

        if (tableCreationInformation.isNonDefaultAggregationPeriod()) {
            wantedColumnDefinitions.add(ColumnDefinition.AGGREGATION_BEGIN_TIME);
        }

        return wantedColumnDefinitions;
    }

    private boolean doesColumnNeedsToBeChanged(final ColumnDefinition wantedColumnDefinition,
                                               @NonNull final List<ColumnDefinition> persistedColumnDefinitions) {
        return persistedColumnDefinitions
                .stream()
                .anyMatch(persistedColumnDefinition -> doesColumnNeedsToBeChanged(wantedColumnDefinition, persistedColumnDefinition));
    }

    private boolean doesColumnNeedsToBeChanged(@NonNull final ColumnDefinition left, final ColumnDefinition right) {
        return left.isSameColumn(right) && left.isDifferentDataType(right);
    }

    private boolean isIndexRecreationNeeded(final Collection<? extends Column> wantedIndexes, final Collection<? super Column> currentIndexes) {
        return CollectionHelpers.anyMissing(wantedIndexes, currentIndexes);
    }

}
