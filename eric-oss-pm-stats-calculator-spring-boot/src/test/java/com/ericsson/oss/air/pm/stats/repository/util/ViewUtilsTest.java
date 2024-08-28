/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util;

import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ViewUtilsTest {

    @ParameterizedTest
    @CsvSource(textBlock =
            """
                    kpi_table_, kpi_table_view
                    kpi_table_15, kpi_table_15_view
                    kpi_table_60, kpi_table_60_view
                    kpi_table_1440, kpi_table_1440_view
                    """
    )
    void shouldConstructViewName(final String tableName, final String expected) {
        final String actual = ViewUtils.constructReliabilityViewName(Table.of(tableName));
        Assertions.assertThat(actual).isEqualTo(expected);
    }
}