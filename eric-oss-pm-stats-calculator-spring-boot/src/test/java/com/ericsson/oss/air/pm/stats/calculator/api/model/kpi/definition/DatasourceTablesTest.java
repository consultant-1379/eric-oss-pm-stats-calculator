/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatasourceTables;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.entry;

class DatasourceTablesTest {
    @Test
    void newInstanceTestWithInitialCapacity() {
        DatasourceTables dataSourceTablesWithInitialCapacity = DatasourceTables.newInstance(1);
        Assertions.assertThat(dataSourceTablesWithInitialCapacity).isNotNull();
    }

    @Nested
    @DisplayName("test computeIfAbsent methods")
    class TestComputeIfAbsent {

        @Test
        void shouldComputeIfAbsent_fromSourceTable() {
            final DatasourceTables datasourceTables = DatasourceTables.newInstance();

            final SourceTable sourceTable = new SourceTable("pm_stats://table1");

            final boolean actual = datasourceTables.computeIfAbsent(sourceTable);

            Assertions.assertThat(actual).isTrue();
            Assertions.assertThat(datasourceTables.get(Datasource.of("pm_stats"))).containsExactlyInAnyOrder(
                    Table.of("table1")
            );
        }

        @Test
        void shouldAddNew_andModifiableSet_fromDataSource() {
            final DatasourceTables datasourceTables = DatasourceTables.newInstance();

            final Set<Table> actual = datasourceTables.computeIfAbsent(Datasource.of("unknownDatasource"));

            Assertions.assertThat(actual).isEmpty();
            Assertions.assertThatNoException().isThrownBy(() -> actual.add(Table.of("newTable")));
        }
    }

    @Test
    void shouldReturnNonAvailableDatasourceTables() {
        final DatasourceTables datasourceTables = DatasourceTables.newInstance();

        final Datasource datasource1 = Datasource.of("datasource1");
        final Datasource datasource2 = Datasource.of("datasource2");
        final Datasource datasource3 = Datasource.of("datasource3");
        final Datasource datasource4 = Datasource.of("datasource4");

        final Table table1 = Table.of("table1");
        final Table table2 = Table.of("table2");
        final Table table3 = Table.of("table3");

        datasourceTables.computeIfAbsent(datasource1).add(table1);
        datasourceTables.computeIfAbsent(datasource2).add(table2);
        datasourceTables.computeIfAbsent(datasource3).add(table3);

        final DatasourceTables actual = datasourceTables.filterAvailableDataSources(Sets.newLinkedHashSet(datasource2, datasource4));

        Assertions.assertThat(actual.entrySet()).containsExactlyInAnyOrder(
                entry(datasource1, Collections.singleton(table1)),
                entry(datasource3, Collections.singleton(table3))
        );
    }

    @ParameterizedTest(name = "[{index}] DatasourceTables: {1} contains: {4} Datasource: {2} with Table: {3}")
    @ArgumentsSource(ProvideDatasourceTablesContainsTableArgument.class)
    void whenDatasourceTablesContainsTable(Datasource putDatasource, Datasource datasource, Set<Table> tables,
                                           Table table, DatasourceTables datasourceTables, boolean expected) {
        datasourceTables.put(putDatasource, tables);
        boolean isTableContains = datasourceTables.containsTable(datasource, table);
        Assertions.assertThat(isTableContains).isEqualTo(expected);
    }

    private static final class ProvideDatasourceTablesContainsTableArgument implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) {
            return Stream.of(
                    Arguments.of(
                            Datasource.KPI_DB, Datasource.of("datasource"),
                            Sets.newLinkedHashSet(Table.of("table1"), Table.of("table2")),
                            Table.of("table"),
                            DatasourceTables.newInstance(),
                            false
                    ),
                    Arguments.of(
                            Datasource.KPI_DB, Datasource.of("kpi_db"),
                            Sets.newLinkedHashSet(Table.of("table1"), Table.of("table2")),
                            Table.of("table"),
                            DatasourceTables.newInstance(),
                            false
                    ),
                    Arguments.of(
                            Datasource.KPI_DB, Datasource.of("kpi_db"),
                            Sets.newLinkedHashSet(Table.of("table1"), Table.of("table2")),
                            Table.of("table1"),
                            DatasourceTables.newInstance(),
                            true
                    ),
                    Arguments.of(
                            Datasource.KPI_IN_MEMORY, Datasource.of("kpi_inmemory"),
                            Sets.newLinkedHashSet(Table.of("table1"), Table.of("table2")),
                            Table.of("table2"),
                            DatasourceTables.newInstance(),
                            true
                    )
            );
        }
    }

}
