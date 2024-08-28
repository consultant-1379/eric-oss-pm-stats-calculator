/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.mapper;

import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_DB;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.alias;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.column;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.reference;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.relation;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.table;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SourceColumn;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SourceTable;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;

import org.junit.jupiter.api.Test;

class SourceMapperTest {

    @Test
    void mapReferencesToSourceColumnTest() {
        final Set<Relation> relations = Set.of(
                relation(KPI_DB, table("table1"), null),
                relation(null, table("table3"), alias("alias"))
        );

        final Set<Reference> references = Set.of(
                reference(null, table("table1"), column("column1"), null),
                reference(KPI_DB, table("table2"), column("column2"), null),
                reference(null, table("alias"), column("column3"), null)
        );

        final Set<SourceColumn> actual = SourceMapper.toSourceColumns(references, relations);

        assertThat(actual).containsExactlyInAnyOrder(
                SourceColumn.from(reference(KPI_DB, table("table1"), column("column1"), null)),
                SourceColumn.from(reference(KPI_DB, table("table2"), column("column2"), null)),
                SourceColumn.from(reference(null, table("table3"), column("column3"), null))
        );
    }

    @Test
    void mapRelationsToSourceTablesTest() {
        final Set<Relation> relations = Set.of(
                relation(KPI_DB, table("table1"), null),
                relation(null, table("table3"), alias("alias"))
        );

        final Set<SourceTable> actual = SourceMapper.toSourceTables(relations);

        assertThat(actual).containsExactlyInAnyOrder(
                SourceTable.from(relation(KPI_DB, table("table1"), null)),
                SourceTable.from(relation(null, table("table3"), null))
        );
    }
}