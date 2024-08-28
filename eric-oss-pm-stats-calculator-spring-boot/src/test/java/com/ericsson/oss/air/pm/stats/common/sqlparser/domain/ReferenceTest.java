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
import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.datasource;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference.Location;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.alias;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.column;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.table;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.References;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class ReferenceTest {

    @Nested
    @TestInstance(PER_CLASS)
    class Instantiation {

        @ParameterizedTest
        @MethodSource("provideFailInstantiationData")
        void shouldFailInstantiation(final Supplier<Reference> referenceSupplier, final String errorMessage) {
            Assertions.assertThatThrownBy(referenceSupplier::get)
                      .isInstanceOf(IllegalArgumentException.class)
                      .hasMessage(errorMessage);
        }

        Stream<Arguments> provideFailInstantiationData() {
            return Stream.of(
                    arguments(
                            (Supplier<Reference>) () -> reference(KPI_DB, null, column("column"), alias("alias")),
                            "When 'Datasource' present then 'Table' must be present as well 'kpi_db.column AS alias[]'"
                    ),
                    arguments(
                            (Supplier<Reference>) () -> reference(KPI_DB, table("table"), null, alias("alias")),
                            "When 'Table' present then 'Column' must be present as well 'kpi_db.table<parameter> AS alias[]'"
                    )
            );
        }
    }

    @ParameterizedTest
    @CsvSource({
            "          ,      ,       , alias, true",
            "datasource, table, column, alias, false",
            "          , table, column, alias, false",
            "          ,      , column, alias, true",
            "          ,      , column,      , false",
    })
    void shouldVerifyIsParameterizedAggregationElement(
            final String datasource, final String table, final String column, final String alias, final boolean expected
    ) {
        final Reference reference = reference(
                datasource == null ? null : datasource(datasource),
                table == null ? null : table(table),
                column == null ? null : column(column),
                alias == null ? null : alias(alias)
        );
        Assertions.assertThat(reference.isParameterizedAggregationElement()).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value =
            {
            "datasource | table | column | alias | FDN_PARSE(table.column, arg) AS alias | true",
            "           | table | column | alias | FDN_PARSE(table.column, arg) AS alias | true",
            "           |       | column | alias | FDN_PARSE(table.column, arg) AS alias | false",
            "datasource | table | column |       | FDN_PARSE(table.column, arg) AS alias | false",
            "           |       |        | alias | FDN_PARSE(table.column, arg) AS alias | false"
    })
    void shouldVerifyIsFunctionAggregationElement(
            final String datasource, final String table, final String column, final String alias, final String function, final boolean expected
    ) {
        final Reference reference = reference(
                datasource == null ? null : datasource(datasource),
                table == null ? null : table(table),
                column == null ? null : column(column),
                alias == null ? null : alias(alias),
                function
        );
        Assertions.assertThat(reference.isFunctionAggregationElement()).isEqualTo(expected);
    }

    @Nested
    class Required {
        @Test
        void shouldThrowExceptionWhenRequiredColumnIsNotPresent() {
            Assertions.assertThatThrownBy(() -> reference(null, null, null, alias("alias")).requiredColumn())
                      .isInstanceOf(IllegalArgumentException.class)
                      .hasMessage("'column' is not present");
        }

        @Test
        void shouldThrowExceptionWhenRequiredTableIsNotPresent() {
            Assertions.assertThatThrownBy(() -> reference(null, null, null, alias("alias")).requiredTable())
                      .isInstanceOf(IllegalArgumentException.class)
                      .hasMessage("'table' is not present");
        }

        @Test
        void shouldVerifyRequiredColumn() {
            final Reference reference = reference(KPI_DB, table("table"), column("column"), alias("alias"));
            final Column actual = reference.requiredColumn();
            Assertions.assertThat(actual).isEqualTo(column("column"));
        }

        @Test
        void shouldVerifyRequiredTable() {
            final Reference reference = reference(KPI_DB, table("table"), column("column"), alias("alias"));
            final Table actual = reference.requiredTable();
            Assertions.assertThat(actual).isEqualTo(table("table"));
        }
    }

    @Nested
    @TestInstance(PER_CLASS)
    class ToString {

        @ParameterizedTest
        @MethodSource("provideToStringData")
        void shouldVerifyToString(final Reference reference, final String expected) {
            Assertions.assertThat(reference).hasToString(expected);
        }

        Stream<Arguments> provideToStringData() {
            return Stream.of(
                    arguments(
                            reference(KPI_DB, table("table"), column("column"), alias("alias")),
                            "kpi_db.table.column AS alias[]"
                    ),
                    arguments(
                            reference(null, table("table"), column("column"), alias("alias")),
                            "table.column AS alias[]"
                    ),
                    arguments(
                            reference(null, null, column("column"), alias("alias")),
                            "column AS alias[]"
                    ),
                    arguments(
                            reference(null, null, null, alias("alias")),
                            "<parameter> AS alias[]"
                    )
            );
        }
    }

    static Reference reference(final Datasource datasource, final Table table, final Column column, final Alias alias, final Location... locations) {
        final Reference reference = References.reference(datasource, table, column, alias);
        reference.addLocation(List.of(locations));
        return reference;
    }

    static Reference reference(final Datasource datasource, final Table table, final Column column, final Alias alias, final String function,
                               final Location... locations) {
        final Reference reference = References.reference(datasource, table, column, alias, function);
        reference.addLocation(List.of(locations));
        return reference;
    }
}