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

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatasourceTableFilters;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.*;

class DatasourceTableFiltersTest {
    static final Table TABLE_1 = Table.of("table1");
    static final Table TABLE_2 = Table.of("table2");

    static final Datasource DATASOURCE_1 = Datasource.of("datasource1");
    static final Datasource DATASOURCE_2 = Datasource.of("datasource2");

    @Test
    void newInstanceTestWithInitialCapacity() {
        final DatasourceTableFilters datasourceTableFilters = DatasourceTableFilters.newInstance(1);
        Assertions.assertThat(datasourceTableFilters).isNotNull();
    }

    @MethodSource("provideIsFilterableData")
    @ParameterizedTest(name = "[{index}] DatasourceTableFilters ''{0}'' with datasource ''{1}'' and ''{2}'' is filterable ''{3}''")
    void shouldVerifyIsFilterable(final DatasourceTableFilters datasourceTableFilters,
                                  final Datasource datasource,
                                  final Table table,
                                  final boolean expected) {
        final boolean actual = datasourceTableFilters.isFilterable(datasource, table);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @MethodSource("provideGetFiltersData")
    @ParameterizedTest(name = "[{index}] DatasourceTableFilters ''{0}''  with datasource ''{1}'' and ''{2}'' is returns filters ''{3}''")
    void shouldGetFilters(final DatasourceTableFilters datasourceTableFilters,
                          final Datasource datasource,
                          final Table table,
                          final List<Filter> expected) {
        final List<Filter> actual = datasourceTableFilters.getFilters(datasource, table);
        Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    static Stream<Arguments> provideGetFiltersData() {
        final List<Filter> filters = singletonList(new Filter("filter"));

        final DatasourceTableFilters datasourceTableFilters = DatasourceTableFilters.newInstance();
        datasourceTableFilters.put(DATASOURCE_1, singletonMap(TABLE_1, filters));

        return Stream.of(
                Arguments.of(datasourceTableFilters, DATASOURCE_1, TABLE_1, filters),
                Arguments.of(datasourceTableFilters, DATASOURCE_2, TABLE_2, emptyList()),
                Arguments.of(datasourceTableFilters, DATASOURCE_2, TABLE_2, emptyList())
        );
    }

    static Stream<Arguments> provideIsFilterableData() {
        final DatasourceTableFilters datasourceTableFilters = DatasourceTableFilters.newInstance();
        datasourceTableFilters.put(DATASOURCE_1, singletonMap(TABLE_1, emptyList()));

        return Stream.of(
                Arguments.of(datasourceTableFilters, DATASOURCE_1, TABLE_1, true),
                Arguments.of(datasourceTableFilters, DATASOURCE_1, TABLE_2, false),
                Arguments.of(datasourceTableFilters, DATASOURCE_2, TABLE_2, false)
        );
    }
}
