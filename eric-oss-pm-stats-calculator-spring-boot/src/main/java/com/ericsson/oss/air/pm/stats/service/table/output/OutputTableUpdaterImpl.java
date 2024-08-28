/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.table.output;

import static lombok.AccessLevel.PUBLIC;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiPersistenceException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.TableCreationInformation;
import com.ericsson.oss.air.pm.stats.repository.util.transaction.TransactionExecutor;
import com.ericsson.oss.air.pm.stats.service.api.DatabaseService;
import com.ericsson.oss.air.pm.stats.service.table.output.api.OutputTableUpdater;
import com.ericsson.oss.air.pm.stats.service.util.Mappers;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class OutputTableUpdaterImpl implements OutputTableUpdater {
    @Inject
    private DatabaseService databaseService;

    @Override
    public void updateOutputTable(final TableCreationInformation tableCreationInformation, final Map<String, KpiDataType> validAggregationElements) {
        try {
            //  TODO: Execute in transaction has to be carried out since when adding new columns for example and not committing it it will
            //        LOCK the corresponding table then reading the column definitions is blocked - deadlock
            //        <table> ==> WRITE - add new columns (not committed)
            //        <table> ==> READ  - read column definitions (cannot read since <table> is under WRITE)
            TransactionExecutor.execute(connection -> {
                databaseService.addColumnsToOutputTable(connection, tableCreationInformation, validAggregationElements);

                final Table table = Table.of(tableCreationInformation.getTableName());
                final List<Column> aggregationElements = Mappers.toColumns(tableCreationInformation.collectAggregationElements());

                if (tableCreationInformation.isDefaultAggregationPeriod()) {
                    databaseService.recreatePrimaryKeyConstraint(connection, table, aggregationElements);
                } else {
                    databaseService.recreateUniqueIndex(connection, table, aggregationElements);
                }

                databaseService.updateSchema(connection, tableCreationInformation, validAggregationElements);
            });
        } catch (final SQLException e) {
            throw new KpiPersistenceException(String.format("Error updating KPI output table: '%s'", tableCreationInformation.getTableName()), e);
        }
    }
}
