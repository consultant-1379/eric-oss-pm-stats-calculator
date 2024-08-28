/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.table;

import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.table;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class VirtualTableTest {
    @Nested
    class Instantiation {
        @Test
        void shouldInstantiateFromTableObject() {
            final VirtualTable actual = VirtualTable.virtualTable(table("kpi_cell_guid_60"));
            Assertions.assertThat(actual.name()).isEqualTo("kpi_cell_guid_60");
            Assertions.assertThat(actual.alias()).isEqualTo("cell_guid");
            Assertions.assertThat(actual.aggregationPeriod()).isEqualTo(60);
        }

        @Test
        void shouldInstantiateFromTableString() {
            final VirtualTable actual = VirtualTable.virtualTable("kpi_cell_guid_60");
            Assertions.assertThat(actual.name()).isEqualTo("kpi_cell_guid_60");
            Assertions.assertThat(actual.alias()).isEqualTo("cell_guid");
            Assertions.assertThat(actual.aggregationPeriod()).isEqualTo(60);
        }

        @Test
        void shouldFailInstantiation_whenTableHasNoAlias() {
            Assertions.assertThatThrownBy(() -> VirtualTable.virtualTable(table("cell_guid")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Alias could not be parsed from table name: cell_guid");
        }
    }
}