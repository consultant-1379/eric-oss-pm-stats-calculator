/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql.database.resolver;

import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_DB;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_IN_MEMORY;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.TABULAR_PARAMETERS;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.alias;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.column;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.datasource;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.relation;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.table;
import static com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.VirtualDatabase.toInMemory;
import static com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.table.VirtualAlias.virtualAlias;
import static com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.table.VirtualTable.virtualTable;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;
import com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.VirtualDatabase;
import com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.VirtualDatabases;
import com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.table.api.VirtualTableReference;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DatabaseResolverTest {

    @ParameterizedTest
    @MethodSource("provideResolveAnyData")
    void shouldResolveAny(
            final int aggregationPeriod, final Collection<Relation> relations, final Table table, final Column column, final Optional<Relation> expected
    ) {
        final VirtualDatabases virtualDatabases = virtualDatabases();

        final DatabaseResolver databaseResolver = new DatabaseResolver(aggregationPeriod, virtualDatabases);
        final Optional<Relation> actual = databaseResolver.anyResolve(relations, table, column);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> provideResolveAnyData() {
        return Stream.of(
                arguments(
                        60,
                        List.of(relation(KPI_DB, table("kpi_cell_guid_60"), null)),
                        table("kpi_cell_guid_60"), column("col_1"),
                        Optional.of(relation(KPI_DB, table("kpi_cell_guid_60"), null))
                ),
                arguments(
                        -1,
                        List.of(relation(KPI_DB, table("kpi_cell_guid_60"), null)),
                        table("kpi_cell_guid_60"), column("col_1"),
                        Optional.of(relation(KPI_DB, table("kpi_cell_guid_60"), null))
                ),
                arguments(
                        60,
                        List.of(relation(KPI_IN_MEMORY, table("cell_guid"), null)),
                        table("cell_guid"), column("col_1"),
                        Optional.of(relation(KPI_IN_MEMORY, table("cell_guid"), null))
                ),
                arguments(
                        60,
                        List.of(relation(KPI_IN_MEMORY, table("cell_guid"), alias("alias"))),
                        table("alias"), column("col_1"),
                        Optional.of(relation(KPI_IN_MEMORY, table("cell_guid"), alias("alias")))
                ),
                arguments(
                        60,
                        List.of(
                                relation(KPI_IN_MEMORY, table("cell_guid"), alias("alias")),
                                relation(KPI_IN_MEMORY, table("cell_guid"), null)
                        ),
                        table("cell_guid"), column("col_1"),
                        Optional.of(relation(KPI_IN_MEMORY, table("cell_guid"), null))
                ),
                arguments(
                        60,
                        List.of(relation(TABULAR_PARAMETERS, table("tabular_parameter"), null)),
                        table("tabular_parameter"), column("b_col_1"),
                        Optional.of(relation(TABULAR_PARAMETERS, table("tabular_parameter"), null))
                ),
                arguments(
                        60,
                        List.of(relation(KPI_IN_MEMORY, table("cell_guid"), alias("alias"))),
                        table("cell_guid"), column("col_1"),
                        Optional.empty()
                ),
                arguments(
                        60,
                        List.of(relation(KPI_IN_MEMORY, table("cell_guid"), null)),
                        table("cell_sector"), column("col_1"),
                        Optional.empty()
                ),
                arguments(
                        1_440,
                        List.of(
                                relation(KPI_IN_MEMORY, table("cell_guid"), null),
                                relation(KPI_DB, table("kpi_cell_guid_1440"), null)
                        ),
                        table("cell_guid"), column("col_1"),
                        Optional.empty()
                )
        );
    }

    static VirtualDatabases virtualDatabases() {
        final VirtualDatabase kpiDb = virtualDatabase(KPI_DB, Map.of(
                virtualTable("kpi_cell_guid_60"), List.of(column("col_1"), column("col_2")),
                virtualTable("kpi_cell_guid_1440"), List.of(column("col_3"), column("col_4"))
        ));

        final VirtualDatabase factDatabase = virtualDatabase(datasource("fact_ds_0"), Map.of(
                virtualAlias("cell_guid"), List.of(column("f_col_1"), column("f_col_2")),
                virtualAlias("cell_guid", 60), List.of(column("f_col_1"), column("f_col_2"))
        ));

        final VirtualDatabase tabularParameters = virtualDatabase(TABULAR_PARAMETERS, Map.of(
                virtualAlias("tabular_parameter"), List.of(column("b_col_1"), column("b_col_2"))
        ));


        final VirtualDatabases virtualDatabases = VirtualDatabases.empty();
        virtualDatabases.registerDatabase(kpiDb);
        virtualDatabases.registerDatabase(factDatabase);
        virtualDatabases.registerDatabase(tabularParameters);
        virtualDatabases.registerDatabase(toInMemory(KPI_IN_MEMORY, kpiDb));
        return virtualDatabases;
    }

    static VirtualDatabase virtualDatabase(final Datasource datasource, final Map<VirtualTableReference, List<Column>> tables) {
        final VirtualDatabase virtualDatabase = VirtualDatabase.empty(datasource);

        tables.forEach((virtualTableReference, columns) -> {
            for (final Column column : columns) {
                virtualDatabase.addColumn(virtualTableReference, column);
            }
        });

        return virtualDatabase;
    }
}