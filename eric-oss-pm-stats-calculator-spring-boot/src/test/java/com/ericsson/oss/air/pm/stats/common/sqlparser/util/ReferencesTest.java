/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.sqlparser.util;

import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_DB;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference.Location.AGGREGATION_ELEMENTS;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference.Location.EXPRESSION;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference.Location.FILTERS;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.alias;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.column;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.markLocation;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.mergeReferences;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.table;

import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Alias;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference.Location;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ReferencesTest {

    @Test
    void shouldMarkLocation() {
        final Reference reference1 = reference(KPI_DB, table("table_1"), column("column_1"), null, EXPRESSION);
        final Reference reference2 = reference(KPI_DB, table("table_1"), column("column_2"), null, FILTERS);
        final Reference reference3 = reference(KPI_DB, table("table_2"), column("column_3"), alias("alias_1"));

        markLocation(List.of(reference1, reference2, reference3), FILTERS);

        Assertions.assertThat(reference1.locations()).containsExactlyInAnyOrder(FILTERS, EXPRESSION);
        Assertions.assertThat(reference2.locations()).containsExactlyInAnyOrder(FILTERS);
        Assertions.assertThat(reference3.locations()).containsExactlyInAnyOrder(FILTERS);
    }

    @Test
    void shouldMergeReferences() {
        final Reference reference1 = reference(KPI_DB, table("table_1"), column("column_1"), null, EXPRESSION);
        final Reference reference2 = reference(KPI_DB, table("table_1"), column("column_2"), null, FILTERS);
        final Reference reference3 = reference(KPI_DB, table("table_2"), column("column_3"), alias("alias_1"), AGGREGATION_ELEMENTS);
        final Reference reference4 = reference(null, table("table_2"), column("column_3"), alias("alias_1"));
        final Reference reference5 = reference(KPI_DB, table("table_2"), column("column_3"), alias("alias_1"), EXPRESSION);

        final Set<Reference> actual = mergeReferences(Set.of(reference1, reference2), Set.of(reference3), Set.of(reference4, reference5));

        Assertions.assertThat(actual).containsExactlyInAnyOrder(
                reference(KPI_DB, table("table_1"), column("column_1"), null, EXPRESSION),
                reference(KPI_DB, table("table_1"), column("column_2"), null, FILTERS),
                reference(KPI_DB, table("table_2"), column("column_3"), alias("alias_1"), EXPRESSION, AGGREGATION_ELEMENTS),
                reference(null, table("table_2"), column("column_3"), alias("alias_1"))
        );
    }

    static Reference reference(final Datasource datasource, final Table table, final Column column, final Alias alias, final Location... locations) {
        final Reference reference = References.reference(datasource, table, column, alias);
        reference.addLocation(List.of(locations));
        return reference;
    }
}