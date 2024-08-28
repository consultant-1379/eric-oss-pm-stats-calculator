/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql.database.model;

import static com.ericsson.oss.air.pm.stats.common.model.collection.Column.AGGREGATION_BEGIN_TIME;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_DB;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_IN_MEMORY;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.column;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.table;
import static com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.table.VirtualAlias.virtualAlias;
import static com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.table.VirtualTable.virtualTable;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.table.api.VirtualTableReference;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class VirtualDatabaseTest {

    @Nested
    @TestInstance(PER_CLASS)
    class LocateTable {
        @Test
        void shouldFailLocateTable_whenColumnIsContainedByMultipleTables() {
            final ThrowingCallable throwingCallable = () -> {
                final VirtualDatabase virtualDatabase = virtualDatabase(KPI_DB, Map.of(
                        virtualTable("kpi_cell_guid_60"), List.of(AGGREGATION_BEGIN_TIME),
                        virtualTable("kpi_cell_guid_1440"), List.of(AGGREGATION_BEGIN_TIME)
                ));

                virtualDatabase.locateTable(AGGREGATION_BEGIN_TIME);
            };

            Assertions.assertThatThrownBy(throwingCallable)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("The required 'aggregation_begin_time' appears in multiple tables '[kpi_cell_guid_1440, kpi_cell_guid_60]'");
        }

        @ParameterizedTest
        @MethodSource("provideLocateTableData")
        void shouldLocateTable(final VirtualDatabase database, final Column column, final Optional<Table> expected) {
            final Optional<Table> actual = database.locateTable(column);
            Assertions.assertThat(actual).isEqualTo(expected);
        }

        Stream<Arguments> provideLocateTableData() {
            return Stream.of(
                    arguments(kpiDatabase(), column("col_1"), Optional.of(table("kpi_cell_guid_60"))),
                    arguments(kpiDatabase(), column("col_5"), Optional.empty()),
                    arguments(inMemoryDatabase(), column("col_1"), Optional.of(table("cell_guid")))
            );
        }
    }

    @Nested
    class ConvertToInMemory {
        @Test
        void shouldFailConversionIfTheTargetDatasourceIsNonInMemory() {
            Assertions.assertThatThrownBy(() -> VirtualDatabase.toInMemory(KPI_DB, kpiDatabase()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("The provided 'VirtualDatabase' 'Datasource' 'kpi_db' is not in-memory");
        }

        @Test
        void shouldFailConversionIfTheSourceDatasourceIsAlreadyInMemory() {
            Assertions.assertThatThrownBy(() -> VirtualDatabase.toInMemory(KPI_IN_MEMORY, inMemoryDatabase()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("The provided 'VirtualDatabase' 'Datasource' 'kpi_inmemory' is in-memory");
        }

        @Test
        void shouldConvertToInMemory() {
            final VirtualDatabase actual = VirtualDatabase.toInMemory(KPI_IN_MEMORY, kpiDatabase());
            Assertions.assertThat(actual).isEqualTo(virtualDatabase(KPI_IN_MEMORY, Map.of(
                    virtualAlias("cell_guid", 60), List.of(column("col_1"), column("col_2")),
                    virtualAlias("cell_guid", 1_440), List.of(column("col_3"), column("col_4"))
            )));
        }
    }

    @Nested
    @TestInstance(PER_CLASS)
    class VerifyDoesContain {

        @ParameterizedTest
        @MethodSource("provideReferenceIsContainedData")
        void shouldVerifyIfReferenceIsContained(final VirtualDatabase database, final VirtualTableReference reference, final boolean expected) {
            final boolean actual = database.doesContain(reference);
            Assertions.assertThat(actual).isEqualTo(expected);
        }

        @ParameterizedTest
        @MethodSource("provideReferenceAndColumnIsContainedData")
        void shouldVerifyIfReferenceAndColumnIsContained(
                final VirtualDatabase database, final VirtualTableReference reference, final Column column, final boolean expected
        ) {
            final boolean actual = database.doesContain(reference, column);
            Assertions.assertThat(actual).isEqualTo(expected);
        }

        Stream<Arguments> provideReferenceAndColumnIsContainedData() {
            return Stream.of(
                    arguments(kpiDatabase(), null, column("col_1"), true),
                    arguments(kpiDatabase(), virtualTable("kpi_cell_guid_60"), column("col_1"), true),
                    arguments(kpiDatabase(), virtualTable("kpi_cell_sector_60"), column("col_1"), false),
                    arguments(inMemoryDatabase(), virtualAlias("cell_guid", 60), column("col_1"), true),
                    arguments(inMemoryDatabase(), virtualAlias("cell_guid", -1), column("col_1"), false),
                    arguments(inMemoryDatabase(), virtualAlias("cell_sector", 60), column("col_1"), false)
            );
        }

        Stream<Arguments> provideReferenceIsContainedData() {
            return Stream.of(
                    arguments(kpiDatabase(), virtualTable("kpi_cell_guid_60"), true),
                    arguments(kpiDatabase(), virtualTable("kpi_cell_sector_60"), false),
                    arguments(inMemoryDatabase(), virtualAlias("cell_guid", 60), true),
                    arguments(inMemoryDatabase(), virtualAlias("cell_guid", -1), false),
                    arguments(inMemoryDatabase(), virtualAlias("cell_sector", 60), false)
            );
        }
    }

    static VirtualDatabase inMemoryDatabase() {
        return virtualDatabase(KPI_IN_MEMORY, Map.of(
                virtualAlias("cell_guid", 60), List.of(column("col_1"), column("col_2")),
                virtualAlias("cell_guid", 1_440), List.of(column("col_3"), column("col_4"))
        ));
    }

    static VirtualDatabase kpiDatabase() {
        return virtualDatabase(KPI_DB, Map.of(
                virtualTable("kpi_cell_guid_60"), List.of(column("col_1"), column("col_2")),
                virtualTable("kpi_cell_guid_1440"), List.of(column("col_3"), column("col_4"))
        ));
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