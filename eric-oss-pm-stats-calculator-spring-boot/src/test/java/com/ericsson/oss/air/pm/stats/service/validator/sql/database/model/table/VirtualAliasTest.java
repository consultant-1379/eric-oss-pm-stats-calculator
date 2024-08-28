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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class VirtualAliasTest {

    @Nested
    class Instantiation {
        @Test
        void shouldCreateFromVirtualTableReference() {
            final VirtualAlias actual = VirtualAlias.from(VirtualTable.virtualTable("kpi_cell_guid_60"));
            Assertions.assertThat(actual.name()).isEqualTo("cell_guid");
            Assertions.assertThat(actual.alias()).isEqualTo("cell_guid");
            Assertions.assertThat(actual.aggregationPeriod()).isEqualTo(60);
        }

        @Test
        void shouldCreateFromAlias() {
            final VirtualAlias actual = VirtualAlias.virtualAlias("cell_guid");
            Assertions.assertThat(actual.name()).isEqualTo("cell_guid");
            Assertions.assertThat(actual.alias()).isEqualTo("cell_guid");
            Assertions.assertThat(actual.aggregationPeriod()).isEqualTo(-1);
        }

        @Test
        void shouldCreateFromAliasAndAggregationPeriod() {
            final VirtualAlias actual = VirtualAlias.virtualAlias("cell_guid", 1_440);
            Assertions.assertThat(actual.name()).isEqualTo("cell_guid");
            Assertions.assertThat(actual.alias()).isEqualTo("cell_guid");
            Assertions.assertThat(actual.aggregationPeriod()).isEqualTo(1_440);
        }
    }

    @Nested
    class ToString {
        @Test
        void shouldValidateToString_withNonDefaultAggregationPeriod() {
            final VirtualAlias actual = VirtualAlias.virtualAlias("cell_guid", 60);
            Assertions.assertThat(actual).hasToString("cell_guid(60)");
        }

        @Test
        void shouldValidateToString_withDefaultAggregationPeriod() {
            final VirtualAlias actual = VirtualAlias.virtualAlias("cell_guid");
            Assertions.assertThat(actual).hasToString("cell_guid(DEFAULT)");
        }
    }

}