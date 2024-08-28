/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util.table;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity.KpiDefinitionEntityBuilder;
import com.ericsson.oss.air.pm.stats.repository.util.partition.PartitionUniqueIndex;
import com.ericsson.oss.air.pm.stats.repository.util.statement.SqlStatement;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.ColumnDefinition;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.TableCreationInformation;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SqlTableModifierImplTest {

    @InjectMocks
    SqlTableModifierImpl objectUnderTest;

    @Test
    void shouldCreateAlterTableToAddNewColumns() {
        final KpiDefinitionEntity definition1 = entity("definition1", "REAL", List.of("fdn", "aggregation2"));
        final KpiDefinitionEntity definition2 = entity("definition2", "BOOLEAN", List.of("aggregation2", "execution_id"));

        final TableCreationInformation tableCreationInformation = TableCreationInformation.of("tableName", "60", List.of(definition1, definition2));

        final List<SqlStatement> actual = objectUnderTest.createAlterTableToAddNewColumns(tableCreationInformation,
                Map.of(
                        "fdn", KpiDataType.POSTGRES_UNLIMITED_STRING,
                        "aggregation2", KpiDataType.POSTGRES_LONG,
                        "execution_id", KpiDataType.POSTGRES_STRING));

        Assertions.assertThat(actual)
                .containsExactly(
                        SqlStatement.of("ALTER TABLE kpi_service_db.kpi.tableName ADD COLUMN IF NOT EXISTS \"definition1\" real"),
                        SqlStatement.of("ALTER TABLE kpi_service_db.kpi.tableName ADD COLUMN IF NOT EXISTS \"definition2\" boolean"),
                        SqlStatement.of("ALTER TABLE kpi_service_db.kpi.tableName ADD COLUMN IF NOT EXISTS \"aggregation2\" int8 NOT NULL default 0"),
                        SqlStatement.of("ALTER TABLE kpi_service_db.kpi.tableName ADD COLUMN IF NOT EXISTS \"execution_id\" varchar(255) NOT NULL default ''"),
                        SqlStatement.of("ALTER TABLE kpi_service_db.kpi.tableName ADD COLUMN IF NOT EXISTS \"fdn\" varchar NOT NULL default ''")
                );
    }

    @Test
    void shouldDropPrimaryKeyConstraint() {
        final SqlStatement actual = objectUnderTest.dropPrimaryKeyConstraint(Table.of("tableName"));

        Assertions.assertThat(actual).isEqualTo(SqlStatement.of("ALTER TABLE kpi_service_db.kpi.tableName DROP CONSTRAINT IF EXISTS tableName_pkey"));
    }

    @Test
    void shouldAddPrimaryKeyConstraint() {
        final SqlStatement actual = objectUnderTest.addPrimaryKeyConstraint(Table.of("tableName"),
                Arrays.asList(Column.of("aggregation2"), Column.of("fdn")));

        Assertions.assertThat(actual)
                .isEqualTo(SqlStatement.of("ALTER TABLE kpi_service_db.kpi.tableName ADD CONSTRAINT tableName_pkey PRIMARY KEY (\"aggregation2\", \"fdn\")"));
    }

    @Test
    void shouldDropUniqueIndex() {
        final SqlStatement actual =
                objectUnderTest.dropUniqueIndex(new PartitionUniqueIndex("partition1",
                        "ui1",
                        Collections.singletonList(Column.of("index"))));

        Assertions.assertThat(actual).isEqualTo(SqlStatement.of("DROP INDEX ui1"));
    }

    @Test
    void shouldCreateUniqueIndex() {
        final SqlStatement actual = objectUnderTest.createUniqueIndex(new PartitionUniqueIndex(
                "partition1",
                "ui1",
                Arrays.asList(Column.of("index1"), Column.of("index2")))
        );

        Assertions.assertThat(actual).isEqualTo(SqlStatement.of("CREATE UNIQUE INDEX IF NOT EXISTS ui1 ON partition1 (\"index1\", \"index2\")"));
    }

    @Test
    void shouldChangeColumnType() {
        final SqlStatement actual = objectUnderTest.changeColumnType(Table.of("table"),
                ColumnDefinition.of(Column.of("column"), KpiDataType.POSTGRES_INTEGER));

        Assertions.assertThat(actual)
                .isEqualTo(SqlStatement.of("ALTER TABLE kpi_service_db.kpi.table ALTER COLUMN \"column\" TYPE int4 USING (\"column\"::int4)"));
    }

    @Test
    void shouldDropColumns() {
        final SqlStatement actual = objectUnderTest.dropColumnsForTable("table", List.of("column1", "column2"));

        Assertions.assertThat(actual)
                .isEqualTo(SqlStatement.of("ALTER TABLE kpi_service_db.kpi.table DROP COLUMN column1 CASCADE, DROP COLUMN column2 CASCADE"));
    }

    @Test
    void shouldDropTables() {
        final SqlStatement actual = objectUnderTest.dropTables(List.of("table1", "table2"));

        Assertions.assertThat(actual)
                .isEqualTo(SqlStatement.of("DROP TABLE IF EXISTS kpi_service_db.kpi.table1, kpi_service_db.kpi.table2 CASCADE"));
    }

    static KpiDefinitionEntity entity(final String name, final String objectType, final List<String> aggregationElements) {
        final KpiDefinitionEntityBuilder builder = KpiDefinitionEntity.builder();
        builder.withName(name);
        builder.withObjectType(objectType);
        builder.withAggregationElements(aggregationElements);
        return builder.build();
    }
}