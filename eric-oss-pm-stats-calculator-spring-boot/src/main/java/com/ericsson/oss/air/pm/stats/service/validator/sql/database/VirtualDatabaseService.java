/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql.database;

import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_DB;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.TABULAR_PARAMETERS;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.inMemoryDatasources;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.column;
import static com.ericsson.oss.air.pm.stats.model.exception.KpiDefinitionValidationException.badRequest;
import static com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.table.VirtualAlias.virtualAlias;
import static com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.table.VirtualTable.virtualTable;
import static lombok.AccessLevel.PUBLIC;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.element.AggregationElement;
import com.ericsson.oss.air.pm.stats.common.sqlparser.SqlProcessorService;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Alias;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;
import com.ericsson.oss.air.pm.stats.model.entity.Parameter;
import com.ericsson.oss.air.pm.stats.model.entity.TabularParameter;
import com.ericsson.oss.air.pm.stats.repository.api.ParameterRepository;
import com.ericsson.oss.air.pm.stats.repository.exception.DatasourceNotFoundException;
import com.ericsson.oss.air.pm.stats.service.api.DatabaseService;
import com.ericsson.oss.air.pm.stats.service.validator.sql.SqlRelationExtractor;
import com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.VirtualDatabase;
import com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.VirtualDatabases;
import com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.table.VirtualTable;

import kpi.model.KpiDefinitionRequest;
import kpi.model.KpiDefinitionTable;
import kpi.model.api.table.Table;
import kpi.model.api.table.definition.KpiDefinition;
import kpi.model.api.table.definition.api.NameAttribute;
import kpi.model.ondemand.OnDemandParameter;
import kpi.model.ondemand.OnDemandTabularParameter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@NoArgsConstructor /* EJB definition requires no-arg constructor */
@AllArgsConstructor(access = PUBLIC) /* Internal testing only */
public class VirtualDatabaseService {
    @Inject private SqlRelationExtractor relationExtractor;

    @Inject private ParameterRepository parameterRepository;

    @Inject private SqlProcessorService sqlProcessorService;
    @Inject private DatabaseService databaseService;

    /**
     * Creates the expected final state of the database if the {@link KpiDefinitionRequest} would be persisted.
     * <p>
     * From the {@link KpiDefinitionRequest} the following attributes add new columns to the database:
     * <ol>
     *     <li>
     *         {@link NameAttribute} - the actual name of the KPI Definition
     *     </li>
     *     <li>
     *         {@link AggregationElement} - the processed {@link Column} or {@link Alias}
     *     </li>
     * </ol>
     * <p>
     * The {@link KpiDefinitionRequest} contains information on the output table if it is not present in the
     * pre-fetched snapshot of the actual database.
     * <p>
     * <strong>NOTE:</strong> External {@link Datasource}s, in-memory {@link Datasource}s and tabular parameter {@link Datasource}s are registered.
     *
     * @param kpiDefinitionRequest {@link KpiDefinitionRequest} to process
     * @return {@link VirtualDatabases} containing the final state of the databases
     */
    @SuppressWarnings("squid:S3776")
    public VirtualDatabases virtualize(@NonNull final KpiDefinitionRequest kpiDefinitionRequest) {
        final VirtualDatabases virtualDatabases = VirtualDatabases.empty();

        final VirtualDatabase kpiDatabase = enrichWithRequest(kpiDefinitionRequest, kpiDatabase());
        virtualDatabases.registerDatabase(kpiDatabase);

        //  Register in-memory datasources
        for (final Datasource datasource : inMemoryDatasources()) {
            virtualDatabases.registerDatabase(VirtualDatabase.toInMemory(datasource, kpiDatabase));
        }

        //  Register tabular parameter datasource
        virtualDatabases.registerDatabase(tabularParameterDatabase(kpiDefinitionRequest));

        //  Check for unknown external data sources
        nonSimpleTables(kpiDefinitionRequest).forEach(table -> {
            for (final KpiDefinition kpiDefinition : table.kpiDefinitions()) {
                for (final Relation relation : relationExtractor.extractColumns(kpiDefinition)) {
                    relation.datasource().filter(Datasource::isUnknown).ifPresent(datasource -> {
                        throw new DatasourceNotFoundException(String.format(
                                "Datasource '%s' is unknown. Use sources '%s'", datasource.getName(),
                                Arrays.stream(Datasource.DatasourceType.values()).map(Datasource.DatasourceType::asDataSource).map(Datasource::getName).sorted().toList()
                        ));
                    });
                }
            }
        });

        return virtualDatabases;
    }

    private VirtualDatabase tabularParameterDatabase(final @NonNull KpiDefinitionRequest kpiDefinitionRequest) {
        final VirtualDatabase tabularParameterDatabase = VirtualDatabase.empty(TABULAR_PARAMETERS);

        for (final Parameter parameter : parameterRepository.findAllParameters()) {
            final TabularParameter tabularParameter = parameter.tabularParameter();
            if (Objects.nonNull(tabularParameter)) {
                tabularParameterDatabase.addColumn(virtualAlias(tabularParameter.name()), column(parameter.name()));
            }
        }

        for (final OnDemandTabularParameter tabularParameter : kpiDefinitionRequest.onDemand().tabularParameters()) {
            for (final OnDemandParameter parameter : tabularParameter.columns()) {
                tabularParameterDatabase.addColumn(virtualAlias(tabularParameter.name()), column(parameter.name()));
            }
        }
        return tabularParameterDatabase;
    }

    private VirtualDatabase enrichWithRequest(@NonNull final KpiDefinitionRequest kpiDefinitionRequest, final VirtualDatabase kpiDatabase) {
        kpiDefinitionRequest.tables().forEach(table -> {
            final VirtualTable virtualTable = virtualTable(table.tableName());
            for (final KpiDefinition kpiDefinition : table.kpiDefinitions()) {
                kpiDatabase.addColumn(virtualTable, Column.of(kpiDefinition.name().value()));

                final Set<Reference> aggregationElements = sqlProcessorService.extractAggregationElementTableColumns(kpiDefinition.aggregationElements());
                for (final Reference aggregationElement : aggregationElements) {
                    final Optional<Column> aliasColumn = aggregationElement.alias().map(alias -> column(alias.name()));
                    aliasColumn.or(aggregationElement::column).ifPresentOrElse(
                            column -> kpiDatabase.addColumn(virtualTable, column),
                            () -> {
                                throw badRequest(String.format(
                                        "'%s' '%s' contained no <column> and <alias>",
                                        AggregationElement.class.getSimpleName(), aggregationElement
                                ));
                            });
                }
            }

            for (final Column internalColumn : Column.internals()) {
                kpiDatabase.addColumn(virtualTable, internalColumn);
            }
        });

        return kpiDatabase;
    }

    private VirtualDatabase kpiDatabase() {
        final VirtualDatabase virtualDatabase = VirtualDatabase.empty(KPI_DB);

        final List<String> outputTables = databaseService.findAllOutputTablesWithoutPartition();
        for (final String outputTable : outputTables) {
            final List<String> columns = databaseService.findColumnNamesForTable(outputTable);
            for (final String column : columns) {
                final VirtualTable virtualTableReference = virtualTable(outputTable);
                virtualDatabase.addColumn(virtualTableReference, column(column));
            }
        }

        return virtualDatabase;
    }

    private static Collection<Table> nonSimpleTables(@NonNull final KpiDefinitionRequest kpiDefinitionRequest) {
        return Stream.of(kpiDefinitionRequest.onDemand(), kpiDefinitionRequest.scheduledComplex())
                .map(KpiDefinitionTable::kpiOutputTables)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
