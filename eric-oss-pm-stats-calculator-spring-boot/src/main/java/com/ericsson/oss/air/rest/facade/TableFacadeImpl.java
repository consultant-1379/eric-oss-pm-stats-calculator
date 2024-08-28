/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.facade;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.TableCreationInformation;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.TableDefinitions;
import com.ericsson.oss.air.pm.stats.service.api.DatabaseService;
import com.ericsson.oss.air.pm.stats.service.table.output.api.OutputTableCreator;
import com.ericsson.oss.air.pm.stats.service.table.output.api.OutputTableUpdater;
import com.ericsson.oss.air.rest.facade.api.TableFacade;
import com.ericsson.oss.air.rest.facade.grouper.api.DefinitionGrouper;
import com.ericsson.oss.air.rest.util.AggregationElementUtils;

import kpi.model.KpiDefinitionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class TableFacadeImpl implements TableFacade {

    private final DatabaseService databaseService;
    private final OutputTableCreator outputTableCreator;
    private final OutputTableUpdater outputTableUpdater;
    private final DefinitionGrouper definitionGrouper;
    private final AggregationElementUtils aggregationElementUtils;

    @Override
    public void createOrUpdateOutputTable(final KpiDefinitionRequest kpiDefinition, final Optional<UUID> collectionId) {
        final LinkedList<TableDefinitions> tableDefinitions = definitionGrouper.groupByOutputTable(kpiDefinition);

        ListIterator<TableDefinitions> tableDefinitionsIterator = tableDefinitions.listIterator();

        collectionId.ifPresent(databaseService::createSchemaByCollectionId);

        //TODO: a graph could help decrease the number of calls to the database, and to simplify the logic
        while (tableDefinitionsIterator.hasNext()) {
            final TableDefinitions tableDefinition = tableDefinitionsIterator.next();
            final TableCreationInformation tableCreationInformation = TableCreationInformation.of(tableDefinition);
            final Set<String> aggregationDbTables = tableCreationInformation.collectAggregationDbTables();
            if (aggregationDbTables.isEmpty() || databaseService.doesTablesExistByName(aggregationDbTables)) {
                final Map<String, KpiDataType> validAggregationElements = aggregationElementUtils.collectValidAggregationElements(tableCreationInformation);
                if (databaseService.doesTableExistByName(tableDefinition.getTable().getName())) {
                    outputTableUpdater.updateOutputTable(tableCreationInformation, validAggregationElements);
                } else {
                    outputTableCreator.createOutputTable(tableCreationInformation, validAggregationElements);
                }
                tableDefinitions.remove(tableDefinition);
                tableDefinitionsIterator = tableDefinitions.listIterator(0);
            }
        }
    }
}
