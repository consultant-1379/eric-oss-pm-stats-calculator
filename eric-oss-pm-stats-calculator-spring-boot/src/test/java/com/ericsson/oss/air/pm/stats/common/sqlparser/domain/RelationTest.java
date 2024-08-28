/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.sqlparser.domain;

import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_DB;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.alias;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.relation;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.table;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RelationTest {

    @ParameterizedTest
    @MethodSource("provideToStringData")
    void shouldVerifyToString(final Relation relation, final String expected) {
        Assertions.assertThat(relation).hasToString(expected);
    }

    @Nested
    @TestInstance(PER_CLASS)
    class ValidateHasMethods {
        @ParameterizedTest
        @MethodSource("provideHasDatasourceData")
        void shouldValidateHasDatasource(final Relation relation, final Datasource datasource, final boolean expected) {
            Assertions.assertThat(relation.hasDatasource(datasource)).isEqualTo(expected);
        }

        @ParameterizedTest
        @MethodSource("provideHasTableData")
        void shouldValidateHasTable(final Relation relation, final Table table, final boolean expected) {
            Assertions.assertThat(relation.hasTable(table)).isEqualTo(expected);
        }

        @ParameterizedTest
        @MethodSource("provideHasAliasData")
        void shouldValidateHasAlias(final Relation relation, final Alias alias, final boolean expected) {
            Assertions.assertThat(relation.hasAlias(alias)).isEqualTo(expected);
        }

        Stream<Arguments> provideHasDatasourceData() {
            return Stream.of(
                    arguments(relation(KPI_DB, table("table"), null), KPI_DB, true),
                    arguments(relation(KPI_DB, table("table"), null), null, false),
                    arguments(relation(null, table("table"), null), KPI_DB, false)
            );
        }

        Stream<Arguments> provideHasTableData() {
            return Stream.of(
                    arguments(relation(KPI_DB, table("table"), null), table("table"), true),
                    arguments(relation(KPI_DB, table("table"), null), null, false)
            );
        }

        Stream<Arguments> provideHasAliasData() {
            return Stream.of(
                    arguments(relation(KPI_DB, table("table"), alias("alias")), alias("alias"), true),
                    arguments(relation(KPI_DB, table("table"), null), alias("alias"), false),
                    arguments(relation(KPI_DB, table("table"), alias("alias")), null, false)
            );
        }
    }

    static Stream<Arguments> provideToStringData() {
        return Stream.of(
                arguments(relation(KPI_DB, table("table"), alias("alias")), "kpi_db.table AS alias"),
                arguments(relation(KPI_DB, table("table"), null), "kpi_db.table"),
                arguments(relation(null, table("table"), alias("alias")), "table AS alias"),
                arguments(relation(null, table("table"), null), "table")
        );
    }
}