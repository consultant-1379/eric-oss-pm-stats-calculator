/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util;

import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.column;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.reference;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.table;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.service.util.sql.SqlProcessorDelegator;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.common.sqlparser.SqlParserImpl;
import com.ericsson.oss.air.pm.stats.common.sqlparser.SqlProcessorService;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;
import com.ericsson.oss.air.pm.stats.common.sqlparser.extractor.LogicalPlanExtractor;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.ExpressionCollector;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.LeafCollector;

import org.apache.commons.lang3.StringUtils;
import org.apache.spark.sql.execution.SparkSqlParser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SqlExpressionHelperImplTest {

    private static final String CELL_SECTOR = "cell_sector";
    private static final String CELL_GUID = "cell_guid";
    private final SqlParserImpl sqlParser = new SqlParserImpl(new SparkSqlParser(), new LogicalPlanExtractor());
    private final SqlProcessorService sqlProcessorService = new SqlProcessorService(sqlParser, new ExpressionCollector(new LeafCollector()));

    SqlExpressionHelperImpl objectUnderTest = new SqlExpressionHelperImpl(new SqlProcessorDelegator(sqlProcessorService));

    @MethodSource("provideFilterData")
    @ParameterizedTest(name = "[{index}] From filters: ''{0}'' SQL filter created: ''{1}''")
    void shouldFilter(final List<Filter> filters, final String expected) {
        final String actual = objectUnderTest.filter(filters);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> provideFilterData() {
        final Filter filterA = new Filter("a == 10");
        final Filter filterB = new Filter("b == 12");
        final Filter filterC = new Filter("c == 14");

        return Stream.of(
                Arguments.of(Collections.emptyList(), StringUtils.EMPTY),
                Arguments.of(Collections.singletonList(null), StringUtils.EMPTY),
                Arguments.of(Arrays.asList(filterA, filterB, filterC), "(a == 10) AND (b == 12) AND (c == 14)"),
                Arguments.of(Arrays.asList(Filter.EMPTY, filterB, filterC), "(b == 12) AND (c == 14)"),
                Arguments.of(Arrays.asList(filterA, Filter.EMPTY, filterC), "(a == 10) AND (c == 14)"),
                Arguments.of(Arrays.asList(filterA, filterB, Filter.EMPTY), "(a == 10) AND (b == 12)"),
                Arguments.of(Arrays.asList(null, filterB, filterC), "(b == 12) AND (c == 14)"),
                Arguments.of(Arrays.asList(filterA, null, filterC), "(a == 10) AND (c == 14)"),
                Arguments.of(Arrays.asList(filterA, filterB, null), "(a == 10) AND (b == 12)")
        );
    }

    @Nested
    @DisplayName("Testing basic expressions parsing")
    class ParseExpressions {

        @Test
        void givenExpressionWithFrom_thenFromExpressionProperlyReturned() {
            final KpiDefinition kpi = KpiDefinition.builder()
                    .withName("brackets_kpi")
                    .withAlias(CELL_GUID)
                    .withExpression("synthetic_counters_cell.counter1 FROM pm_events://synthetic_counters_cell")
                    .build();

            final String result = objectUnderTest.getFromExpression(kpi);

            assertThat(result).isEqualTo("pm_events://synthetic_counters_cell");
        }

        @Test
        void givenExpressionWithLowercaseFrom_thenFromExpressionProperlyReturned() {
            final KpiDefinition kpi = KpiDefinition.builder()
                    .withName("brackets_kpi")
                    .withAlias(CELL_GUID)
                    .withExpression("synthetic_counters_cell.counter1 from pm_events://synthetic_counters_cell")
                    .build();

            final String result = objectUnderTest.getFromExpression(kpi);

            assertThat(result).isEqualTo("pm_events://synthetic_counters_cell");
        }

        @Test
        void givenSimpleKpi_thenSchemaReturnedAsFromExpression() {
            final KpiDefinition kpi = KpiDefinition.builder()
                    .withName("brackets_kpi")
                    .withAlias(CELL_GUID)
                    .withExpression("synthetic_counters_cell.counter1")
                    .withInpDataIdentifier(DataIdentifier.of("4G|PM_COUNTERS|SampleCellFDD_1"))
                    .build();

            final String result = objectUnderTest.getFromExpression(kpi);

            assertThat(result).isEqualTo("SampleCellFDD_1");
        }

        @Test
        void givenExpressionWithFrom_thenSelectExpressionProperlyReturned() {
            String expression = "synthetic_counters_cell.counter1 FROM pm_events://synthetic_counters_cell";

            final String result = objectUnderTest.getSelectExpression(expression);

            assertThat(result).isEqualTo("synthetic_counters_cell.counter1");
        }

        @Test
        void givenExpressionWithLowercaseFrom_thenSelectExpressionProperlyReturned() {
            String expression = "synthetic_counters_cell.counter1 from pm_events://synthetic_counters_cell";

            final String result = objectUnderTest.getSelectExpression(expression);

            assertThat(result).isEqualTo("synthetic_counters_cell.counter1");
        }

        @Test
        void givenExpressionWithoutFrom_thenSelectExpressionProperlyReturned() {
            String expression = "synthetic_counters_cell.counter1";

            final String result = objectUnderTest.getSelectExpression(expression);

            assertThat(result).isEqualTo("synthetic_counters_cell.counter1");
        }

        @Test
        void givenExpressionWithWhere_thenWhereExpressionProperlyReturned() {
            String expression = "estimated_cells.coverage_balance_distance"
                    + " FROM kpi_post_agg://kpi_cell_sector_1440"
                    + " WHERE kpi_cell_sector_1440.ref_cell = estimated_cells.guid";

            final Optional<String> result = objectUnderTest.getWhereExpression(expression);

            assertThat(result).isNotEmpty();
            assertThat(result.get()).isEqualTo("kpi_cell_sector_1440.ref_cell = estimated_cells.guid");
        }

        @Test
        void givenExpressionWithoutWhere_thenWhereExpressionProperlyReturned() {
            String expression = "synthetic_counters_cell.counter1 FROM pm_events://synthetic_counters_cell";

            final Optional<String> result = objectUnderTest.getWhereExpression(expression);

            assertThat(result).isEmpty();
        }

        @Test
        void givenExpressionWithWhere_thenSourceExpressionProperlyReturned() {
            String expression = "kpi_post_agg://kpi_cell_sector_1440 "
                    + "WHERE kpi_cell_sector_1440.ref_cell = estimated_cells.guid";

            final String result = objectUnderTest.getSourceExpression(expression);

            assertThat(result).isEqualTo("kpi_post_agg://kpi_cell_sector_1440");
        }

        @Test
        void givenExpressionWithoutWhere_thenSourceExpressionProperlyReturned() {
            String expression = "kpi_post_agg://kpi_cell_sector_1440";

            final String result = objectUnderTest.getSourceExpression(expression);

            assertThat(result).isEqualTo("kpi_post_agg://kpi_cell_sector_1440");
        }

        @Test
        void givenSourceExpressionToken_thenDataSourceProperlyReturned() {
            String sourceExpressionToken = "kpi_post_agg://kpi_cell_sector_1440";

            final String result = objectUnderTest.getSourceFromExpressionToken(sourceExpressionToken);

            assertThat(result).isEqualTo("kpi_post_agg");
        }

        @Test
        void givenSourceExpressionToken_thenTableProperlyReturned() {
            String sourceExpressionToken = "kpi_post_agg://kpi_cell_sector_1440";

            final String result = objectUnderTest.getTableFromExpressionToken(sourceExpressionToken);

            assertThat(result).isEqualTo("kpi_cell_sector_1440");
        }
    }


    @Nested
    @DisplayName("Testing Kpi select expressions parsing")
    class ParseSelectExpressions {

        @Test
        void givenExpressionWithBraces_thenOutputFormulaRetainsBraces() {
            final KpiDefinition kpi = KpiDefinition.builder()
                    .withName("brackets_kpi")
                    .withAlias(CELL_GUID)
                    .withExpression("100 * (counters_cell.counter1 + counters_cell.counter2 + counters_cell.counter3 + counters_cell.counter4 + counters_cell.counter5) FROM pm_stats://counters_cell")
                    .withAggregationType("SUM")
                    .withAggregationPeriod("1440")
                    .withAggregationElements(Collections.singletonList("guid"))
                    .build();

            final String result = objectUnderTest.collectSelectExpressionsAsSql(List.of(kpi));

            assertThat(result).isEqualTo(
                    "100 * (SUM(counters_cell.counter1) + SUM(counters_cell.counter2) + SUM(counters_cell.counter3) + SUM(counters_cell.counter4) + SUM(counters_cell.counter5)) AS brackets_kpi");
        }

        @Test
        void givenArrayAggregationType_thenOutputFormulaCreatesExpressionWithCorrectAggregation() {
            final KpiDefinition kpi = KpiDefinition.builder()
                    .withName("array_kpi")
                    .withAlias(CELL_GUID)
                    .withExpression("synthetic_counters_cell.counter1 FROM pm_events://synthetic_counters_cell")
                    .withAggregationType("ARRAY_INDEX_SUM")
                    .withAggregationPeriod("1440")
                    .withAggregationElements(Collections.singletonList("guid"))
                    .build();

            final String result = objectUnderTest.collectSelectExpressionsAsSql(List.of(kpi));

            assertThat(result).isEqualTo("ARRAY_INDEX_SUM(synthetic_counters_cell.counter1) AS array_kpi");
        }

        @Test
        void givenExpressionWithADivisor_thenOutputFormulaIncludesCaseToHandleDivisionByZero() {
            final KpiDefinition kpi = KpiDefinition.builder()
                    .withName("divisor_kpi")
                    .withAlias(CELL_GUID)
                    .withExpression("counters_cell.counter1 / NULLIF(counters_cell.counter2, 0) FROM pm_stats://counters_cell")
                    .withAggregationType("SUM")
                    .withAggregationPeriod("1440")
                    .withAggregationElements(Collections.singletonList("guid"))
                    .build();

            final String result = objectUnderTest.collectSelectExpressionsAsSql(List.of(kpi));

            assertThat(result).isEqualTo("SUM(counters_cell.counter1 / NULLIF(counters_cell.counter2, 0)) AS divisor_kpi");
        }

        @Test
        void givenExpressionWithNoDivisor_thenOutputFormulaDoesNotIncludeCase() {
            final KpiDefinition kpi = KpiDefinition.builder()
                    .withName("no_divisor_kpi")
                    .withAlias(CELL_GUID)
                    .withExpression("counters_cell.counter1 FROM pm_stats://counters_cell")
                    .withAggregationType("SUM")
                    .withAggregationPeriod("1440")
                    .withAggregationElements(Collections.singletonList("guid"))
                    .build();

            final String result = objectUnderTest.collectSelectExpressionsAsSql(List.of(kpi));

            assertThat(result).isEqualTo("SUM(counters_cell.counter1) AS no_divisor_kpi");
        }

        @Test
        void givenExpressionMultipleParts_thenOutputFormulaCorrectlyAppliesAggregationToEachPart() {
            final KpiDefinition kpi = KpiDefinition.builder()
                    .withName("multipart_kpi")
                    .withAlias(CELL_GUID)
                    .withExpression(
                            "(counters_cell.counter1 * counters_relation.counter2) / NULLIF(counters_cell.counter3, 0) FROM pm_stats://counters_cell LEFT JOIN counters_cell ON pm_stats://counters_relation")
                    .withAggregationType("SUM")
                    .withAggregationPeriod("60")
                    .withAggregationElements(Collections.singletonList("guid"))
                    .build();

            final String result = objectUnderTest.collectSelectExpressionsAsSql(List.of(kpi));

            assertThat(result).isEqualTo(
                    "SUM((counters_cell.counter1 * counters_relation.counter2) / NULLIF(counters_cell.counter3, 0)) AS multipart_kpi");
        }


        @Test
        void givenExpressionWithSparkFunctions_thenOutputFormulaRetainsBraces() {
            final KpiDefinition kpi = KpiDefinition.builder()
                    .withName("spark_kpi")
                    .withAlias(CELL_GUID)
                    .withExpression(
                            "aggregate(slice(num_samples_rsrp_ta, cell_guid.num_samples_rsrp_ta_80_index," +
                                    " cell_guid.num_samples_rsrp_ta_90_index - cell_guid.num_samples_rsrp_ta_80_index), 0, (acc, x) -> acc + x) FROM kpi_inmemory://cell_guid")
                    .withAggregationType("SUM")
                    .withAggregationPeriod("1440")
                    .withAggregationElements(Collections.singletonList("guid"))
                    .build();

            final String result = objectUnderTest.collectSelectExpressionsAsSql(List.of(kpi));

            assertThat(result).isEqualTo(
                    "SUM(aggregate(slice(num_samples_rsrp_ta, cell_guid.num_samples_rsrp_ta_80_index," +
                            " cell_guid.num_samples_rsrp_ta_90_index - cell_guid.num_samples_rsrp_ta_80_index), 0, (acc, x) -> acc + x)) AS spark_kpi");
        }

        @Test
        void givenExpressionWithDecimalNumber_thenOutputFormulaHasCorrectFormat() {
            final KpiDefinition kpi = KpiDefinition.builder()
                    .withName("decimal_kpi")
                    .withAlias(CELL_GUID)
                    .withExpression("149.7 + kpi_cell_guid_1440.filter_kpi FROM kpi_db://kpi_cell_guid_1440")
                    .withAggregationType("FIRST")
                    .withAggregationPeriod("1440")
                    .withAggregationElements(Collections.singletonList("guid"))
                    .build();

            final String result = objectUnderTest.collectSelectExpressionsAsSql(List.of(kpi));

            assertThat(result).isEqualTo("149.7 + FIRST(kpi_cell_guid_1440.filter_kpi, true) AS decimal_kpi");
        }

        @Test
        void givenExpressionWithDecimalNumberInBrackets_thenOutputFormulaHasCorrectFormat() {
            final KpiDefinition kpi = KpiDefinition.builder()
                    .withName("decimal_kpi")
                    .withAlias(CELL_GUID)
                    .withExpression("(149.7 + kpi_cell_guid_1440.filter_kpi + 153.3) FROM kpi_db://kpi_cell_guid_1440")
                    .withAggregationType("FIRST")
                    .withAggregationPeriod("1440")
                    .withAggregationElements(Collections.singletonList("guid"))
                    .build();

            final String result = objectUnderTest.collectSelectExpressionsAsSql(List.of(kpi));

            assertThat(result).isEqualTo("(149.7 + FIRST(kpi_cell_guid_1440.filter_kpi, true) + 153.3) AS decimal_kpi");
        }

        @Test
        void givenExpressionWithAggregationType_percentileIndex90_thenOutputFormulaHasCorrectFormat() {
            final KpiDefinition kpi = KpiDefinition.builder()
                    .withName("coverage_balance_distance_bin")
                    .withAlias(CELL_SECTOR)
                    .withExpression("PERCENTILE_INDEX_90(kpi_cell_guid_1440.num_samples_ta) FROM kpi_db://kpi_cell_guid_1440 inner "
                            + "join cm://cell_sector on kpi_cell_guid_1440.guid = cell_sector.guid")
                    .withAggregationType("PERCENTILE_INDEX_90")
                    .withAggregationPeriod("1440")
                    .withAggregationElements(Collections.singletonList("guid"))
                    .build();

            final String result = objectUnderTest.collectSelectExpressionsAsSql(List.of(kpi));

            assertThat(result).isEqualTo("PERCENTILE_INDEX_90(kpi_cell_guid_1440.num_samples_ta) AS coverage_balance_distance_bin");
        }

        @Test
        void givenExpressionWithAggregationType_percentileIndex80_thenOutputFormulaHasCorrectFormat() {
            final KpiDefinition kpi = KpiDefinition.builder()
                    .withName("coverage_balance_signal_bin")
                    .withAlias(CELL_SECTOR)
                    .withExpression("PERCENTILE_INDEX_80(kpi_cell_guid_1440.num_samples_ta) FROM kpi_db://kpi_cell_guid_1440 "
                            + "inner join cm://cell_sector on kpi_cell_guid_1440.guid = cell_sector.guid")
                    .withAggregationType("PERCENTILE_INDEX_80")
                    .withAggregationPeriod("1440")
                    .withAggregationElements(Collections.singletonList("guid"))
                    .build();

            final String result = objectUnderTest.collectSelectExpressionsAsSql(List.of(kpi));

            assertThat(result).isEqualTo("PERCENTILE_INDEX_80(kpi_cell_guid_1440.num_samples_ta) AS coverage_balance_signal_bin");
        }

        //Wrong Sql syntax
        @Test
        void givenExpressionWithAggregationType_first_thenOutputFormulaHasCorrectFormat() {
            final KpiDefinition kpi = KpiDefinition.builder()
                    .withName("coverage_balance_signal_range_upper_bound")
                    .withAlias(CELL_SECTOR)
                    .withExpression("FIRST(greatest(kpi_cell_sector_1440.coverage_balance_signal_bin, "
                            + "kpi_cell_sector_1440.coverage_balance_signal_reference_bin), true) FROM kpi_db://kpi_cell_sector_1440 LEFT JOIN kpi_db://kpi_cell_sector "
                            + "ON kpi_cell_sector_1440.guid = kpi_cell_sector.guid AND kpi_cell_sector_1440.sector_id = kpi_cell_sector.sector_id WHERE "
                            + "kpi_cell_sector.coverage_balance_signal_range_calculation_time "
                            + "< TO_TIMESTAMP('DATE_SUB(CURRENT_DATE(),7)') OR kpi_cell_sector.coverage_balance_signal_range_calculation_time "
                            + "IS NULL ${sectors_for_signal_range_recalculation}")
                    .withAggregationType("FIRST")
                    .withAggregationPeriod("1440")
                    .withAggregationElements(Collections.singletonList("guid"))
                    .build();

            final String result = objectUnderTest.collectSelectExpressionsAsSql(List.of(kpi));

            assertThat(result).isEqualTo("FIRST(greatest(kpi_cell_sector_1440.coverage_balance_signal_bin,"
                    + " kpi_cell_sector_1440.coverage_balance_signal_reference_bin), true) "
                    + "AS coverage_balance_signal_range_upper_bound");
        }

        @Test
        void givenExpressionWithAggregationType_max_thenOutputFormulaHasCorrectFormat() {
            final KpiDefinition kpi = KpiDefinition.builder()
                    .withName("max_coverage_balance_distance_bin")
                    .withAlias("sector")
                    .withExpression("MAX(kpi_cell_sector_1440.coverage_balance_distance_bin) FROM kpi_db://kpi_cell_sector_1440")
                    .withAggregationType("MAX")
                    .withAggregationPeriod("1440")
                    .withAggregationElements(Collections.singletonList("guid"))
                    .build();

            final String result = objectUnderTest.collectSelectExpressionsAsSql(List.of(kpi));

            assertThat(result).isEqualTo("MAX(kpi_cell_sector_1440.coverage_balance_distance_bin) AS max_coverage_balance_distance_bin");
        }

        @Test
        void givenExpressionWithAggregationType_arrayIndexSum_thenOutputFormulaHasCorrectFormat() {
            final KpiDefinition kpi = KpiDefinition.builder()
                    .withName("num_samples_bad_rsrp_ta")
                    .withAlias("cell_guid")
                    .withExpression("ARRAY_INDEX_SUM(synthetic_counters_cell.ctrNumSamplesBadRsrpTa) FROM pm_events://synthetic_counters_cell")
                    .withAggregationType("ARRAY_INDEX_SUM")
                    .withAggregationPeriod("1440")
                    .withAggregationElements(Collections.singletonList("guid"))
                    .build();

            final String result = objectUnderTest.collectSelectExpressionsAsSql(List.of(kpi));

            assertThat(result).isEqualTo("ARRAY_INDEX_SUM(synthetic_counters_cell.ctrNumSamplesBadRsrpTa) AS num_samples_bad_rsrp_ta");
        }

        @Test
        void givenExpressionWithAggregationType_firstAndDivisor_thenOutputFormulaHasCorrectFormat() {
            final KpiDefinition kpi = KpiDefinition.builder()
                    .withName("percentage_bad_rsrp_samples_candidate")
                    .withAlias(CELL_SECTOR)
                    .withExpression("100 * FIRST( cell_sector.num_samples_bad_rsrp_candidate, true ) / NULLIF(FIRST( cell_sector.num_samples_rsrp_candidate, true ), 0) FROM kpi_inmemory://cell_sector")
                    .withAggregationType("FIRST")
                    .withAggregationPeriod("1440")
                    .withAggregationElements(Collections.singletonList("guid"))
                    .build();

            final String result = objectUnderTest.collectSelectExpressionsAsSql(List.of(kpi));

            assertThat(result).isEqualTo("100 * FIRST( cell_sector.num_samples_bad_rsrp_candidate, true ) / NULLIF(FIRST( cell_sector.num_samples_rsrp_candidate, true ), 0) AS percentage_bad_rsrp_samples_candidate");
        }

        @Test
        void givenExpressionWithAggregationType_firstAndMathematicalOperator_thenOutputFormulaHasCorrectFormat() {
            final KpiDefinition kpi = KpiDefinition.builder()
                    .withName("cell_availability")
                    .withAlias("cell_guid")
                    .withExpression("100 * (96 * 900 - FIRST(kpi_cell_guid_1440.pm_cell_downtime_auto_daily, true) - "
                            + "FIRST(kpi_cell_guid_1440.pm_cell_downtime_man_daily, true)) / NULLIF((96 * 900), 0) FROM kpi_db://kpi_cell_guid_1440")
                    .withAggregationType("FIRST")
                    .withAggregationPeriod("1440")
                    .withAggregationElements(Collections.singletonList("guid"))
                    .build();

            final String result = objectUnderTest.collectSelectExpressionsAsSql(List.of(kpi));

            assertThat(result).isEqualTo("100 * (96 * 900 - FIRST(kpi_cell_guid_1440.pm_cell_downtime_auto_daily, true) - " +
                    "FIRST(kpi_cell_guid_1440.pm_cell_downtime_man_daily, true)) / NULLIF((96 * 900), 0) AS cell_availability");
        }

        @Test
        void givenExpressionWithAggregationType_sumAndDivisor_thenOutputFormulaHasCorrectFormat() {
            final KpiDefinition kpi = KpiDefinition.builder()
                    .withName("contiguity")
                    .withAlias("cell_guid")
                    .withExpression("100 * (SUM(kpi_relation_guid_source_guid_target_guid_1440.out_succ_ho_intraf)) " +
                            "/ NULLIF((SUM(kpi_relation_guid_source_guid_target_guid_1440.out_succ_ho_intraf) + SUM(kpi_relation_guid_source_guid_target_guid_1440.out_succ_ho_interf)), 0) FROM kpi_db://kpi_relation_guid_source_guid_target_guid_1440")
                    .withAggregationType("SUM")
                    .withAggregationPeriod("1440")
                    .withAggregationElements(Collections.singletonList("guid"))
                    .build();

            final String result = objectUnderTest.collectSelectExpressionsAsSql(List.of(kpi));

            assertThat(result).isEqualTo("100 * (SUM(kpi_relation_guid_source_guid_target_guid_1440.out_succ_ho_intraf)) / " +
                    "NULLIF((SUM(kpi_relation_guid_source_guid_target_guid_1440.out_succ_ho_intraf) + SUM(kpi_relation_guid_source_guid_target_guid_1440.out_succ_ho_interf)), 0) AS contiguity");
        }

        @Test
        void givenExpressionWithAggregationType_firstAndIfExpression_thenOutputFormulaHasCorrectFormat() {
            final KpiDefinition kpi = KpiDefinition.builder()
                    .withName("num_samples_rsrp_candidate")
                    .withAlias(CELL_SECTOR)
                    .withExpression("FIRST(if( kpi_cell_sector.coverage_balance_signal_range_lower_bound = " +
                            "kpi_cell_sector.coverage_balance_signal_range_upper_bound , 1 , aggregate(slice( cell_sector.num_samples_rsrp_ta_candidate , " +
                            "kpi_cell_sector.coverage_balance_signal_range_lower_bound + 1 , kpi_cell_sector.coverage_balance_signal_range_upper_bound - " +
                            "kpi_cell_sector.coverage_balance_signal_range_lower_bound + 1), 0, (acc, x) -> acc + x)), true) FROM kpi_db://cell_sector inner " +
                            "join kpi_db://kpi_cell_sector on cell_sector.guid = kpi_cell_sector.guid and cell_sector.sector_id" +
                            " = kpi_cell_sector.sector_id")
                    .withAggregationType("FIRST")
                    .withAggregationPeriod("1440")
                    .withAggregationElements(Collections.singletonList("guid"))
                    .build();

            final String result = objectUnderTest.collectSelectExpressionsAsSql(List.of(kpi));

            assertThat(result).isEqualTo("FIRST(if( kpi_cell_sector.coverage_balance_signal_range_lower_bound = " +
                    "kpi_cell_sector.coverage_balance_signal_range_upper_bound , 1 , aggregate(slice( cell_sector.num_samples_rsrp_ta_candidate , " +
                    "kpi_cell_sector.coverage_balance_signal_range_lower_bound + 1 , kpi_cell_sector.coverage_balance_signal_range_upper_bound - " +
                    "kpi_cell_sector.coverage_balance_signal_range_lower_bound + 1), 0, (acc, x) -> acc + x)), true) AS num_samples_rsrp_candidate");
        }

        @Test
        void givenSimpleExpression_thenOutputExpressionHasCorrectFormat() {
            final KpiDefinition kpi = KpiDefinition.builder()
                    .withName("test_simple_kpi")
                    .withAlias(CELL_GUID)
                    .withExpression("fact_table.pmCounters1.counter1 * fact_table.pmCounters2.counter1.counterValue / NULLIF(fact_table.pmCounters2, 0) FROM pm_stats://counters_cell")
                    .withAggregationType("SUM")
                    .withAggregationPeriod("1440")
                    .withAggregationElements(Collections.singletonList("guid"))
                    .withInpDataIdentifier(DataIdentifier.of("dataSpace|schema|fact_table"))
                    .build();

            final String result = objectUnderTest.collectSelectExpressionsAsSql(List.of(kpi));

            assertThat(result).isEqualTo("SUM(fact_table.pmCounters1_counter1 * fact_table.pmCounters2_counter1_counterValue / NULLIF(fact_table.pmCounters2, 0)) "
                    + "AS test_simple_kpi");
        }

        @Test
        void givenSimpleExpressionWithCast_thenOutputExpressionHasCorrectFormat() {
            final KpiDefinition kpi = KpiDefinition.builder()
                                                   .withName("test_cast")
                                                   .withAlias(CELL_GUID)
                                                   .withExpression("kpi_table_60.kpi + CAST('2' AS INT) FROM kpi_db://kpi_table_60")
                                                   .withAggregationType("SUM")
                                                   .withAggregationPeriod("1440")
                                                   .withAggregationElements(Collections.singletonList("guid"))
                                                   .build();

            final String result = objectUnderTest.collectSelectExpressionsAsSql(List.of(kpi));

            assertThat(result).isEqualTo("SUM(kpi_table_60.kpi + CAST('2' AS INT)) AS test_cast");
        }
    }

    @Nested
    @DisplayName("Testing Kpi select-expressions parsing with no/extra whitespaces")
    class ParseSelectExpressionsWithNoOrExtraWhiteSpaces {

        @Test
        void givenExpressionWithExtraWhiteSpaces_thenOutputFormulaRetainsBraces() {
            final KpiDefinition kpi = KpiDefinition.builder()
                    .withName("brackets_kpi")
                    .withAlias(CELL_GUID)
                    .withExpression("100  * (counters_cell.counter1 + counters_cell.counter2 + counters_cell.counter3 + counters_cell.counter4 + counters_cell.counter5) FROM pm_stats://counters_cell")
                    .withAggregationType("SUM")
                    .withAggregationPeriod("1440")
                    .withAggregationElements(Collections.singletonList("guid"))
                    .build();

            final String result = objectUnderTest.collectSelectExpressionsAsSql(List.of(kpi));

            assertThat(result).isEqualTo(
                    "100 * (SUM(counters_cell.counter1) + SUM(counters_cell.counter2) + SUM(counters_cell.counter3) + SUM(counters_cell.counter4) + SUM(counters_cell.counter5)) AS brackets_kpi");
        }

        @Test
        void givenExpressionWithExtraWhiteSpacesAfterAggregationType_thenExpressionProperlyReturned() {
            final KpiDefinition kpi = KpiDefinition.builder()
                    .withName("whitespace_kpi")
                    .withAlias(CELL_GUID)
                    .withExpression("SUM   (fact_table.pmCounters1_counter1 * fact_table.pmCounters2_counter1_counterValue / NULLIF(fact_table.pmCounters2, 0)) FROM pm_stats://counters_cell")
                    .withAggregationType("SUM")
                    .withAggregationPeriod("1440")
                    .withAggregationElements(Collections.singletonList("guid"))
                    .withInpDataIdentifier(DataIdentifier.of("dataSpace|schema|fact_table"))
                    .build();

            final String result = objectUnderTest.collectSelectExpressionsAsSql(List.of(kpi));

            assertThat(result).isEqualTo("SUM   (fact_table.pmCounters1_counter1 * fact_table.pmCounters2_counter1_counterValue / NULLIF(fact_table.pmCounters2, 0)) "
                    + "AS whitespace_kpi");
        }
    }

    @Nested
    @DisplayName("Testing from-expressions parsing")
    class ParseFromExpressions {

        @Test
        void givenSimpleDefinition_thenSchemaReturned() {
            KpiDefinition simpleDefinition = KpiDefinition.builder()
                    .withInpDataIdentifier(DataIdentifier.of("dataSpace|dataCategory|fact_table"))
                    .build();

            String actual = objectUnderTest.getCleanedFromExpressionOrSchema(simpleDefinition);

            assertThat(actual).isEqualTo("fact_table");
        }

        @Test
        public void givenFromPartUsesDataSourcesAndAliases_dataSourcesAreCorrectlyClearedAndWhereUnaffected() {
            KpiDefinition testDefinition = KpiDefinition.builder()
                    .withExpression("estimated_cells.coverage_balance_distance " +
                            "FROM kpi_db://kpi_cell_sector_1440 " +
                            "left join kpi_db://kpi_cell_sector_1440 AS estimated_cells on kpi_cell_sector_1440.ref_cell = estimated_cells.guid " +
                            "WHERE kpi_cell_sector_1440.ref_cell = estimated_cells.guid")
                    .build();

            String actual = objectUnderTest.getCleanedFromExpressionOrSchema(testDefinition);

            assertThat(actual).isEqualTo("kpi_cell_sector_1440 " +
                    "left join kpi_cell_sector_1440 AS estimated_cells on kpi_cell_sector_1440.ref_cell = estimated_cells.guid " +
                    "WHERE kpi_cell_sector_1440.ref_cell = estimated_cells.guid");
        }

        @Test
        public void whenFromPartUsesAliasesWithNoDatasource_dataSourcesAreCorrectlyClearedAndWhereUnaffected() {
            KpiDefinition testDefinition = KpiDefinition.builder()
                    .withExpression("estimated_cells.coverage_balance_distance " +
                            "FROM kpi_cell_sector_1440 " +
                            "left join kpi_cell_sector_1440 AS estimated_cells on estimated_cells " +
                            "WHERE kpi_cell_sector_1440.ref_cell = estimated_cells.guid")
                    .build();

            String actual = objectUnderTest.getCleanedFromExpressionOrSchema(testDefinition);

            assertThat(actual).isEqualTo("kpi_cell_sector_1440 " +
                    "left join kpi_cell_sector_1440 AS estimated_cells on estimated_cells " +
                    "WHERE kpi_cell_sector_1440.ref_cell = estimated_cells.guid");
        }

        @Test
        public void givenFromPartUsesInMemoryDataSources_dataSourcesAreCorrectlyClearedAndWhereUnaffected() {
            KpiDefinition testDefinition = KpiDefinition.builder()
                    .withExpression("estimated_cells.coverage_balance_distance " +
                            "FROM kpi_inmemory://kpi_cell_sector_1440 " +
                            "WHERE kpi_cell_sector_1440.ref_cell = estimated_cells.guid")
                    .build();

            String actual = objectUnderTest.getCleanedFromExpressionOrSchema(testDefinition);

            assertThat(actual).isEqualTo("kpi_cell_sector_1440_kpis kpi_cell_sector_1440 " +
                    "WHERE kpi_cell_sector_1440.ref_cell = estimated_cells.guid");
        }

        @Test
        public void givenFromPartUsesPostAggDataSources_dataSourcesAreCorrectlyClearedAndWhereUnaffected() {
            KpiDefinition testDefinition = KpiDefinition.builder()
                    .withExpression("estimated_cells.coverage_balance_distance " +
                            "FROM kpi_post_agg://kpi_cell_sector_1440 " +
                            "WHERE kpi_cell_sector_1440.ref_cell = estimated_cells.guid")
                    .build();

            String actual = objectUnderTest.getCleanedFromExpressionOrSchema(testDefinition);

            assertThat(actual).isEqualTo("kpi_cell_sector_1440_kpis kpi_cell_sector_1440 " +
                    "WHERE kpi_cell_sector_1440.ref_cell = estimated_cells.guid");
        }
    }

    @Nested
    @DisplayName("Testing collecting relations")
    class CollectRelations {

        @Test
        void shouldReturnComplexRelations() {
            final KpiDefinition complexKpi = KpiDefinition.builder()
                    .withExecutionGroup("test_execution_group")
                    .withExpression("FIRST(CEIL(kpi_pme_cell_complex_60.ul_pusch_sinr_hourly_interim)) FROM kpi_db://kpi_pme_cell_complex_60")
                    .build();

            final Set<Relation> actual = objectUnderTest.collectComplexRelations(complexKpi.getExpression());

            final Set<Relation> expected = Set.of(Relation.of(Datasource.of("kpi_db"), Table.of("kpi_pme_cell_complex_60"), null));
            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnOndemandRelations() {
            final KpiDefinition ondemandKpi = KpiDefinition.builder()
                    .withExpression("FIRST(CEIL(kpi_pme_cell_complex_60.ul_pusch_sinr_hourly_interim)) FROM kpi_db://kpi_pme_cell_complex_60")
                    .build();

            final Set<Relation> actual = objectUnderTest.collectComplexRelations(ondemandKpi.getExpression());

            final Set<Relation> expected = Set.of(Relation.of(Datasource.of("kpi_db"), Table.of("kpi_pme_cell_complex_60"), null));
            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Testing collecting references")
    class CollectReferences {

        @Test
        void whenComplexDefinition_shouldReturnReferencesProperly() {
            final KpiDefinition complexKpi = KpiDefinition.builder()
                    .withExecutionGroup("test_execution_group")
                    .withExpression("FIRST(kpi_cell_guid_1440.sum_integer_1440) " +
                            "FROM kpi_db://kpi_cell_guid_1440 " +
                            "INNER JOIN dim_ds_0://dim_table_2 AS alias ON kpi_cell_guid_1440.agg_column_0 = alias.agg_column_0 " +
                            "WHERE kpi_cell_guid_1440.agg_column_0 > 0")
                    .build();

            final Set<Reference> actual = objectUnderTest.collectComplexReferences(complexKpi.getExpression());

            assertThat(actual).containsExactlyInAnyOrder(
                    reference(null, table("alias"), column("agg_column_0"), null),
                    reference(null, table("kpi_cell_guid_1440"), column("sum_integer_1440"), null),
                    reference(null, table("kpi_cell_guid_1440"), column("agg_column_0"), null)
            );
        }

        @Test
        void whenOndemandDefinition_shouldReturnReferencesProperly() {
            final KpiDefinition complexKpi = KpiDefinition.builder()
                    .withExpression("SUM(fact_table_0.integerColumn0) " +
                            "FROM fact_ds_1://fact_table_0 " +
                            "INNER JOIN dim_ds_0://dim_table_2 " +
                            "ON fact_table_0.agg_column_0 = dim_table_2.agg_column_0")
                    .build();

            final Set<Reference> actual = objectUnderTest.collectComplexReferences(complexKpi.getExpression());

            assertThat(actual).containsExactlyInAnyOrder(
                    reference(null, table("dim_table_2"), column("agg_column_0"), null),
                    reference(null, table("fact_table_0"), column("integerColumn0"), null),
                    reference(null, table("fact_table_0"), column("agg_column_0"), null)
            );
        }
    }
}