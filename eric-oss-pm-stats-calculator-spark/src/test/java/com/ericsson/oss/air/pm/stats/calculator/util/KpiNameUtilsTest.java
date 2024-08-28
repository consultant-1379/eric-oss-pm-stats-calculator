/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.util;

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_AGGREGATION_PERIOD_INT;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit Tests for {@link KpiNameUtils} class.
 */
class KpiNameUtilsTest {

    @Test
    void givenAlias_whenTemporaryViewNameIsGenerated_thenAliasIsPresent() {
        final String temporaryDatasetViewName = KpiNameUtils.createTemporaryDatasetViewName("alias");
        assertThat(temporaryDatasetViewName).isEqualTo("alias_kpis");
    }

    @Test
    void givenAliasAndDataSource_whenTemporaryViewNameIsGenerated_thenAliasAndDatasourceIsPresent() {
        final String temporaryDatasetViewName = KpiNameUtils.createTemporaryDatasetViewName("alias", "datasource");
        assertThat(temporaryDatasetViewName).isEqualTo("alias_datasource_kpis");
    }

    @Test
    void givenAliasAndAggregationPeriod_whenOutputNameIsGenerated_thenAliasAndAggregationPeriodArePresent() {
        final String outputTableName = KpiNameUtils.createOutputTableName("alias", 1440);
        assertThat(outputTableName).isEqualTo("kpi_alias_1440");
    }

    @Test
    void givenAliasAndDefaultAggregationPeriod_whenOutputNameIsGenerated_thenAliasAndAggregationPeriodArePresent() {
        final String outputTableName = KpiNameUtils.createOutputTableName("alias", DEFAULT_AGGREGATION_PERIOD_INT);
        assertThat(outputTableName).isEqualTo("kpi_alias_");
    }

    @ParameterizedTest
    @MethodSource("provideCreateOutputTableData")
    void shouldCreateOutputTable(final String alias, final int aggregationPeriod, final Table expected) {
        final Table actual = KpiNameUtils.createOutputTable(alias, aggregationPeriod);
        assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> provideCreateOutputTableData() {
        return Stream.of(
                Arguments.of("alias", -1, Table.of("kpi_alias_")),
                Arguments.of("alias", 60, Table.of("kpi_alias_60"))
        );
    }
}
