/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.sql;

import static com.ericsson.oss.air.pm.stats.calculator._test.TestObjectFactory.sqlProcessorDelegator;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.CELL_GUID;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.CELL_GUID_SIMPLE;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.CELL_SECTOR;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DAILY_AGGREGATION_PERIOD_IN_MINUTES;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_AGGREGATION_PERIOD_INT;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.HOURLY_AGGREGATION_PERIOD_IN_MINUTES;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.RELATION_GUID_SOURCE_GUID_TARGET_GUID;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.SECTOR;
import static java.util.stream.Collectors.groupingBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator._test.TestObjectFactory;
import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiRetrievalException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.SqlExpressionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.util.KpiDefinitionFileRetriever;
import com.ericsson.oss.air.pm.stats.common.model.DatasourceType;
import com.ericsson.oss.air.pm.stats.model.KpiDefinitionHandler;
import com.ericsson.oss.air.pm.stats.model.KpiDefinitionHierarchy;

import org.apache.spark.sql.execution.SparkSqlParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link SqlCreator}.
 */
@ExtendWith(MockitoExtension.class)
public class SqlCreatorTest {

    @Mock KpiDefinitionHierarchy kpiDefinitionHierarchyMock;

    private static final Set<KpiDefinition> ALL_KPI_DEFINITIONS = retrieveKpiDefinitions(new KpiDefinitionFileRetriever());
    private static final String CELL_GUID_TABLES = "cell_guid_kpis cell_guid";
    private static final String FACT_TABLE_0 = "fact_table_0";
    private static final String FACT_TABLE_2 = "fact_table_2";
    private static final String FACT_TABLE_3 = "fact_table_3";
    private static final String DIM_TABLE_1 = "dim_table_1";

    private final SqlExpressionHelperImpl sqlExpressionHelper = new SqlExpressionHelperImpl(sqlProcessorDelegator(new SparkSqlParser()));
    private final KpiDefinitionHelperImpl kpiDefinitionHelper = TestObjectFactory.kpiDefinitionHelper(kpiDefinitionHierarchyMock);

    private final Map<Integer, List<KpiDefinition>> dailyKpis = kpiDefinitionHelper.getStagedKpisForAggregationPeriod(
            ALL_KPI_DEFINITIONS, DAILY_AGGREGATION_PERIOD_IN_MINUTES);
    private final Map<Integer, List<KpiDefinition>> hourlyKpis = kpiDefinitionHelper.getStagedKpisForAggregationPeriod(
            ALL_KPI_DEFINITIONS, HOURLY_AGGREGATION_PERIOD_IN_MINUTES);

    private SqlCreator objectUnderTest;

    @BeforeAll
    static void init() {
        final Collection<KpiDefinition> postAggregationKpiDefinitions = KpiDefinitionHandler.getPostAggregationKpiDefinitions(ALL_KPI_DEFINITIONS);
        ALL_KPI_DEFINITIONS.removeAll(postAggregationKpiDefinitions);
    }

    @Test
    public void createMaxTimestampSql_for_factTable0() {
        objectUnderTest = sqlCreator(DAILY_AGGREGATION_PERIOD_IN_MINUTES);
        final String result = objectUnderTest.createMaxTimestampSql(FACT_TABLE_0);
        assertThat(result).isEqualTo("SELECT MAX(aggregation_begin_time) as latest_time_collected FROM fact_table_0");
    }

    @Nested
    class StageSqlCreator {

        @Test
        public void whenSimpleKpis_andFactTable2_andStage1_thenTimedSqlTemplateIsUsed() {
            List<KpiDefinition> kpis = selectKpis(dailyKpis, 1, CELL_GUID_SIMPLE, FACT_TABLE_2);
            final List<String> aggregationElements = collectAggregationElementsForAlias(kpis, CELL_GUID_SIMPLE);

            objectUnderTest = sqlCreator(DAILY_AGGREGATION_PERIOD_IN_MINUTES);

            final String actual = objectUnderTest.createKpiSql(kpis, aggregationElements, 1);
            assertThat(actual).isEqualTo(
                "SELECT fact_table_2.agg_column_0, " +
                    "MAX(CAST(date_format(fact_table_2.aggregation_begin_time, 'yyyy-MM-dd') AS TIMESTAMP)) AS aggregation_begin_time, " +
                    "FIRST(ADD_INTEGER_TO_ARRAY_WITH_LIMIT(fact_table_2.integerArrayColumn0, 1, 5)) AS first_add_integer_to_array_with_limit_simple, " +
                    "FIRST(CALCULATE_PERCENTILE_BIN(fact_table_2.integerArrayColumn0, 0)) AS first_calculate_percentile_bin_simple, " +
                    "FIRST(CALCULATE_PERCENTILE_VALUE(fact_table_2.floatArrayColumn0, 0)) AS first_calculate_percentile_value_simple, " +
                    "FIRST(UPDATE_NULL_TIME_ADVANCED_KPIS(fact_table_2.integerArrayColumn0, 5)) AS first_update_null_time_advanced_kpis_simple, " +
                    "SUM(fact_table_2.integerArrayColumn0[1] + fact_table_2.integerArrayColumn0[3]) AS sum_integer_arrayindex_1440_simple " +
                "FROM fact_table_2 " +
                "GROUP BY fact_table_2.agg_column_0, " +
                    "CAST(date_format(fact_table_2.aggregation_begin_time, 'yyyy-MM-dd') AS TIMESTAMP)");
        }

        @Test
        public void whenNonSimpleKpis_andDimTable_thenNoTimedSqlTemplateIsUsed(@Mock KpiDefinitionHelperImpl kpiDefinitionHelperMock) {
            List<KpiDefinition> kpis = selectKpis(dailyKpis, 1, RELATION_GUID_SOURCE_GUID_TARGET_GUID, DIM_TABLE_1);
            final List<String> aggregationElements = collectAggregationElementsForAlias(kpis, RELATION_GUID_SOURCE_GUID_TARGET_GUID);

            objectUnderTest = new SqlCreator(DAILY_AGGREGATION_PERIOD_IN_MINUTES, sqlProcessorDelegator(new SparkSqlParser()), kpiDefinitionHelperMock);
            when(kpiDefinitionHelperMock.isKpiDataSourcesContainType(kpis.get(0), DatasourceType.FACT)).thenReturn(false);

            final String actual = objectUnderTest.createKpiSql(kpis, aggregationElements, 1);

            verify(kpiDefinitionHelperMock).isKpiDataSourcesContainType(kpis.get(0), DatasourceType.FACT);
            assertThat(actual).isEqualTo(
                "SELECT dim_table_1.agg_column_0, dim_table_1.agg_column_1, dim_table_1.agg_column_2, " +
                    "FIRST(dim_table_1.booleanColumn0) AS first_boolean_1440 " +
                "FROM dim_table_1 " +
                "GROUP BY dim_table_1.agg_column_0, dim_table_1.agg_column_1, dim_table_1.agg_column_2");
        }

        @Test
        public void whenNonSimpleKpi_andDefaultAggPeriod_thenNoTimedSqlTemplateIsUsed() {
            final List<KpiDefinition> kpisToCalculate = List.of(getKpiByName("sum_operator_integer_kpidb_filter"));
            final List<String> aggregationElements = collectAggregationElementsForAlias(kpisToCalculate, SECTOR);

            objectUnderTest = sqlCreator(DEFAULT_AGGREGATION_PERIOD_INT);

            final String actual = objectUnderTest.createKpiSql(kpisToCalculate, aggregationElements, 1);
            assertThat(actual).isEqualTo(
                "SELECT kpi_sector_60.agg_column_0, " +
                    "SUM(kpi_sector_60.sum_integer_60_join_kpidb) * SUM(kpi_sector_60.sum_integer_60_join_kpidb) AS sum_operator_integer_kpidb_filter " +
                "FROM kpi_sector_60 " +
                "GROUP BY kpi_sector_60.agg_column_0");
}

        @Test
        public void whenStage1_andFactTable0_andHourly_andAliasCellGuid_thenTimedSqlTemplateIsProperlyUsed() {
            List<KpiDefinition> kpis = selectKpis(hourlyKpis, 1, CELL_GUID, FACT_TABLE_0);
            final List<String> aggregationElements = collectAggregationElementsForAlias(kpis, CELL_GUID);

            objectUnderTest = sqlCreator(HOURLY_AGGREGATION_PERIOD_IN_MINUTES);

            final String actual = objectUnderTest.createKpiSql(kpis, aggregationElements, 1);
            assertThat(actual).isEqualTo(
                "SELECT fact_table_0.agg_column_0, " +
                    "MAX(CAST(date_format(fact_table_0.aggregation_begin_time, 'yyyy-MM-dd HH') AS TIMESTAMP)) AS aggregation_begin_time, " +
                    "SUM(fact_table_0.integerColumn0) AS sum_integer_60, " +
                    "COUNT(fact_table_0.integerColumn0) AS sum_integer_count_60 " +
                "FROM fact_table_0 GROUP BY fact_table_0.agg_column_0, " +
                "CAST(date_format(fact_table_0.aggregation_begin_time, 'yyyy-MM-dd HH') AS TIMESTAMP)");
        }


        @Test
        public void whenStage1_andFactTable3_andHourly_andAliasRelationGuid_thenTimedSqlTemplateIsProperlyUsed() {
            List<KpiDefinition> kpis = selectKpis(hourlyKpis, 1, RELATION_GUID_SOURCE_GUID_TARGET_GUID, FACT_TABLE_3);
            final List<String> aggregationElements = collectAggregationElementsForAlias(kpis,
                    RELATION_GUID_SOURCE_GUID_TARGET_GUID);

            objectUnderTest = sqlCreator(HOURLY_AGGREGATION_PERIOD_IN_MINUTES);

            final String actual = objectUnderTest.createKpiSql(kpis, aggregationElements, 1);
            assertThat(actual).isEqualTo(
                "SELECT fact_table_3.agg_column_0, fact_table_3.agg_column_1, fact_table_3.agg_column_2, " +
                    "MAX(CAST(date_format(fact_table_3.aggregation_begin_time, 'yyyy-MM-dd HH') AS TIMESTAMP)) AS aggregation_begin_time, " +
                    "FIRST(fact_table_3.integerColumn0) / NULLIF(0, 0) AS first_float_divideby0_60 " +
                "FROM fact_table_3 GROUP BY fact_table_3.agg_column_0, fact_table_3.agg_column_1, fact_table_3.agg_column_2, " +
                    "CAST(date_format(fact_table_3.aggregation_begin_time, 'yyyy-MM-dd HH') AS TIMESTAMP)");
        }

        @Test
        public void whenStage2_andFactTable_thenTimedSqlTemplateIsProperlyUsed() {
            List<KpiDefinition> kpis = selectKpis(hourlyKpis, 2, CELL_GUID, CELL_GUID_TABLES);
            final List<String> aggregationElements = collectAggregationElementsForAlias(kpis, CELL_GUID);

            objectUnderTest = sqlCreator(HOURLY_AGGREGATION_PERIOD_IN_MINUTES);

            final String actual = objectUnderTest.createKpiSql(kpis, aggregationElements, 2);
            assertThat(actual).isEqualTo(
                "SELECT cell_guid.agg_column_0, FIRST(cell_guid.aggregation_begin_time) AS aggregation_begin_time, " +
                    "FIRST(cell_guid.sum_integer_count_60) / FIRST(NULLIF(cell_guid.sum_integer_60, 0)) " +
                        "AS first_integer_operator_60_stage2 " +
                "FROM cell_guid_kpis cell_guid " +
                "GROUP BY cell_guid.agg_column_0, cell_guid.aggregation_begin_time");
        }

        @Test
        public void whenStage1_andFactTable0_thenTimedSqlTemplateIsProperlyUsed() {
            List<KpiDefinition> kpis = selectKpis(dailyKpis, 1, CELL_GUID, FACT_TABLE_0);
            final List<String> aggregationElements = collectAggregationElementsForAlias(kpis, CELL_GUID);

            objectUnderTest = sqlCreator(DAILY_AGGREGATION_PERIOD_IN_MINUTES);

            final String actual = objectUnderTest.createKpiSql(kpis, aggregationElements, 1);
            assertThat(actual).isEqualTo(
                "SELECT fact_table_0.agg_column_0, " +
                    "MAX(CAST(date_format(fact_table_0.aggregation_begin_time, 'yyyy-MM-dd') AS TIMESTAMP)) AS aggregation_begin_time, " +
                    "FIRST(fact_table_0.timestampColumn0) AS first_timestamp_1440, MAX(fact_table_0.integerColumn0) AS max_long_1440, " +
                    "SUM(fact_table_0.integerColumn0) AS sum_integer_1440 " +
                "FROM fact_table_0 GROUP BY fact_table_0.agg_column_0, " +
                    "CAST(date_format(fact_table_0.aggregation_begin_time, 'yyyy-MM-dd') AS TIMESTAMP)");
        }

        @Test
        public void whenFdnUdfAggregationElement_andFactTable0_thenTimedSqlTemplateIsProperlyUsed() {
            objectUnderTest = sqlCreator(DAILY_AGGREGATION_PERIOD_IN_MINUTES);

            final List<KpiDefinition> fdnKpis = selectKpis(dailyKpis, 1, "fdn_agg", FACT_TABLE_0);
            final List<String> aggregationElements = collectAggregationElementsForAlias(fdnKpis, "fdn_agg");
            final String actual = objectUnderTest.createKpiSql(fdnKpis, aggregationElements, 1);

            assertThat(actual).isEqualTo(
                    "SELECT FDN_PARSE(fact_table_0.agg_column_0, \"region\") AS region, " +
                    "MAX(CAST(date_format(fact_table_0.aggregation_begin_time, 'yyyy-MM-dd') AS TIMESTAMP)) AS aggregation_begin_time, " +
                    "SUM(fact_table_0.integerColumn0) AS fdn_agg_sum_integer_1440 " +
                "FROM fact_table_0 " +
                "GROUP BY FDN_PARSE(fact_table_0.agg_column_0, 'region'), " +
                    "CAST(date_format(fact_table_0.aggregation_begin_time, 'yyyy-MM-dd') AS TIMESTAMP)");
        }

        @Test
        public void whenStage1_andFactTable2_andDaily_andAliasCellGuid_thenTimedSqlTemplateIsProperlyUsed() {
            List<KpiDefinition> kpis = selectKpis(dailyKpis, 1, CELL_GUID, FACT_TABLE_2);
            final List<String> aggregationElements = collectAggregationElementsForAlias(kpis, CELL_GUID);

            objectUnderTest = sqlCreator(DAILY_AGGREGATION_PERIOD_IN_MINUTES);

            final String actual = objectUnderTest.createKpiSql(kpis, aggregationElements, 1);
            assertThat(actual).isEqualTo(
                "SELECT fact_table_2.agg_column_0, " +
                    "MAX(CAST(date_format(fact_table_2.aggregation_begin_time, 'yyyy-MM-dd') AS TIMESTAMP)) AS aggregation_begin_time, " +
                    "ARRAY_INDEX_SUM(fact_table_2.integerArrayColumn0) AS arrayindexsum_integerarray_1440, " +
                    "FIRST(aggregate(slice( fact_table_2.integerArrayColumn0, 1, 3), 0, (acc, x) -> acc + x)) AS first_integer_aggregate_slice_1440, " +
                    "PERCENTILE_INDEX_80(fact_table_2.integerArrayColumn0) AS percentileindex80_integer_1440, " +
                    "PERCENTILE_INDEX_90(fact_table_2.integerArrayColumn0) AS percentileindex90_integer_1440, " +
                    "SUM(fact_table_2.integerArrayColumn0[1] + fact_table_2.integerArrayColumn0[3]) AS sum_integer_arrayindex_1440, " +
                    "FIRST(TRANSFORM(fact_table_2.integerArrayColumn0 , x -> x * fact_table_2.floatColumn0)) AS transform_int_array_to_float_array " +
                "FROM fact_table_2 " +
                "GROUP BY fact_table_2.agg_column_0, " +
                    "CAST(date_format(fact_table_2.aggregation_begin_time, 'yyyy-MM-dd') AS TIMESTAMP)");
        }

        @Test
        public void whenStage1_andFactTable3_andDaily_andAliasRelationGuid_thenTimedSqlTemplateIsProperlyUsed() {
            List<KpiDefinition> kpis = selectKpis(dailyKpis, 1, RELATION_GUID_SOURCE_GUID_TARGET_GUID, FACT_TABLE_3);
            final List<String> aggregationElements = collectAggregationElementsForAlias(kpis,
                    RELATION_GUID_SOURCE_GUID_TARGET_GUID);

            objectUnderTest = sqlCreator(DAILY_AGGREGATION_PERIOD_IN_MINUTES);

            final String actual = objectUnderTest.createKpiSql(kpis, aggregationElements, 1);
            assertThat(actual).isEqualTo(
                "SELECT fact_table_3.agg_column_0, fact_table_3.agg_column_1, fact_table_3.agg_column_2, " +
                    "MAX(CAST(date_format(fact_table_3.aggregation_begin_time, 'yyyy-MM-dd') AS TIMESTAMP)) AS aggregation_begin_time, " +
                    "SUM(fact_table_3.integerColumn0) + SUM(fact_table_3.integerColumn0) AS sum_integer_operator_1440, " +
                    "SUM(fact_table_3.integerColumn0) AS sum_real_1440 " +
                "FROM fact_table_3 " +
                "GROUP BY fact_table_3.agg_column_0, fact_table_3.agg_column_1, fact_table_3.agg_column_2, " +
                    "CAST(date_format(fact_table_3.aggregation_begin_time, 'yyyy-MM-dd') AS TIMESTAMP)");
        }

        @Test
        public void whenJoinExpression_andStage2_thenTimedSqlTemplateIsProperlyUsed() {
            Set<KpiDefinition> allKpis = KpiDefinitionHandler.getKpisForAGivenStageAndAliasFromStagedKpis(dailyKpis, 2, CELL_SECTOR);
            final List<String> aggregationElements = collectAggregationElementsForAlias(allKpis, CELL_SECTOR);

            objectUnderTest = sqlCreator(HOURLY_AGGREGATION_PERIOD_IN_MINUTES);

            final String actual = objectUnderTest.createKpiSql(new ArrayList<>(allKpis), aggregationElements, 2);
            assertThat(actual).isEqualTo(
                "SELECT alias.agg_column_0 AS agg_column_0, alias.agg_column_1 AS agg_column_1, " +
                    "FIRST(kpi_cell_guid_1440.aggregation_begin_time) AS aggregation_begin_time, " +
                    "FIRST(kpi_cell_guid_1440.sum_integer_1440) AS first_integer_1440_join_kpidb_filter " +
                "FROM kpi_cell_guid_1440 " +
                    "INNER JOIN dim_table_2 AS alias ON kpi_cell_guid_1440.agg_column_0 = alias.agg_column_0 " +
                "WHERE kpi_cell_guid_1440.agg_column_0 > 0 " +
                "GROUP BY alias.agg_column_0, alias.agg_column_1, kpi_cell_guid_1440.aggregation_begin_time");
        }

        @Test
        public void whenJoinExpression_andStage1_thenTimedSqlTemplateIsProperlyUsed() {
            objectUnderTest = sqlCreator(DAILY_AGGREGATION_PERIOD_IN_MINUTES);
            final List<KpiDefinition> kpisToCalculate = List.of(getKpiByName("first_integer_1440_join_kpidb_filter"));
            final List<String> aggregationElements = collectAggregationElementsForAlias(kpisToCalculate, CELL_SECTOR);

            final String actual = objectUnderTest.createKpiSql(kpisToCalculate, aggregationElements, 1);
            assertThat(actual).isEqualTo(
                "SELECT alias.agg_column_0 AS agg_column_0, alias.agg_column_1 AS agg_column_1, " +
                    "MAX(CAST(date_format(kpi_cell_guid_1440.aggregation_begin_time, 'yyyy-MM-dd') AS TIMESTAMP)) AS aggregation_begin_time, " +
                    "FIRST(kpi_cell_guid_1440.sum_integer_1440) AS first_integer_1440_join_kpidb_filter " +
                "FROM kpi_cell_guid_1440 INNER JOIN dim_table_2 AS alias ON kpi_cell_guid_1440.agg_column_0 = alias.agg_column_0 " +
                "WHERE kpi_cell_guid_1440.agg_column_0 > 0 " +
                "GROUP BY alias.agg_column_0, alias.agg_column_1, " +
                    "CAST(date_format(kpi_cell_guid_1440.aggregation_begin_time, 'yyyy-MM-dd') AS TIMESTAMP)");
        }

        private List<KpiDefinition> selectKpis(final Map<Integer, List<KpiDefinition>> stagedKpis, final int stage, final String alias, final String table) {
            final Set<KpiDefinition> kpisForAGivenStageAndAlias = KpiDefinitionHandler.getKpisForAGivenStageAndAliasFromStagedKpis(stagedKpis, stage, alias);
            final List<KpiDefinition> kpis = kpisForAGivenStageAndAlias.stream().collect(groupingBy(sqlExpressionHelper::getCleanedFromExpressionOrSchema)).get(table);
            Collections.sort(kpis);
            return kpis;
        }
    }

    @Nested
    class UnionSqlCreator {

        @Test
        public void unionWithDb_aggObject2_daily() {
            objectUnderTest = sqlCreator(DAILY_AGGREGATION_PERIOD_IN_MINUTES);
            final Set<KpiDefinition> kpis = KpiDefinitionHandler.getKpisForAGivenAliasFromStagedKpis(dailyKpis, RELATION_GUID_SOURCE_GUID_TARGET_GUID);
            final List<String> aggregationElements = collectAggregationElementsForAlias(kpis,
                    RELATION_GUID_SOURCE_GUID_TARGET_GUID);
            final Set<Timestamp> timestampSet = new HashSet<>(1);
            final Timestamp timestamp = Timestamp.from(Instant.now());
            timestampSet.add(timestamp);
            final String result = objectUnderTest.createUnionSql(kpis, aggregationElements, RELATION_GUID_SOURCE_GUID_TARGET_GUID,
                    DAILY_AGGREGATION_PERIOD_IN_MINUTES, timestampSet);
            assertThat(result).isEqualTo(String.format(
                   "SELECT agg_column_0, agg_column_1, agg_column_2, aggregation_begin_time, sum_real_1440, sum_integer_operator_1440, first_boolean_1440 " +
                           "FROM relation_guid_source_guid_target_guid_kpis " +
                           "UNION ALL SELECT agg_column_0, agg_column_1, agg_column_2, aggregation_begin_time, sum_real_1440, sum_integer_operator_1440, first_boolean_1440 FROM kpi_relation_guid_source_guid_target_guid_1440 " +
                           "WHERE aggregation_begin_time in (TO_TIMESTAMP('%s'))",
                    timestamp));
        }

        @Test
        public void unionWithDb_aggObject1_daily() {
            objectUnderTest = sqlCreator(DAILY_AGGREGATION_PERIOD_IN_MINUTES);
            final Set<KpiDefinition> kpis = KpiDefinitionHandler.getKpisForAGivenAliasFromStagedKpis(dailyKpis, CELL_SECTOR);
            final List<String> aggregationElements = collectAggregationElementsForAlias(kpis, CELL_SECTOR);
            final Set<Timestamp> timestampSet = new HashSet<>(1);
            final Timestamp timestamp = Timestamp.from(Instant.now());
            timestampSet.add(timestamp);
            final String result = objectUnderTest.createUnionSql(kpis, aggregationElements, CELL_SECTOR, DAILY_AGGREGATION_PERIOD_IN_MINUTES, timestampSet);
            assertThat(result)
                    .isEqualTo(String.format(
                            "SELECT agg_column_0, agg_column_1, aggregation_begin_time, first_integer_1440_join_kpidb_filter " +
                                    "FROM cell_sector_kpis UNION ALL SELECT agg_column_0, agg_column_1, aggregation_begin_time, " +
                                    "first_integer_1440_join_kpidb_filter FROM kpi_cell_sector_1440 " +
                                    "WHERE aggregation_begin_time in (TO_TIMESTAMP('%s'))", timestamp));
        }

        @Test
        public void testUnionQueryForDefaultAggregationPeriodDoesNotIncludeTimestamps() {
            objectUnderTest = sqlCreator(DEFAULT_AGGREGATION_PERIOD_INT);
            final Set<KpiDefinition> kpis = KpiDefinitionHandler.getKpisForAGivenAliasFromStagedKpis(dailyKpis, CELL_SECTOR);
            final List<String> aggregationElements = collectAggregationElementsForAlias(kpis, CELL_SECTOR);

            final String result = objectUnderTest.createUnionSql(kpis, aggregationElements, CELL_SECTOR, DAILY_AGGREGATION_PERIOD_IN_MINUTES,
                    Collections.emptySet());
            assertThat(result)
                    .isEqualTo("SELECT agg_column_0, agg_column_1, first_integer_1440_join_kpidb_filter FROM cell_sector_kpis " +
                            "UNION ALL SELECT agg_column_0, agg_column_1, first_integer_1440_join_kpidb_filter FROM kpi_cell_sector_1440");
        }
    }

    @Nested
    class AggregationSqlCreator {

        @Test
        public void aggregateKpis_aggObject2_daily() {
            objectUnderTest = sqlCreator(DAILY_AGGREGATION_PERIOD_IN_MINUTES);
            final String alias = RELATION_GUID_SOURCE_GUID_TARGET_GUID;
            final Set<KpiDefinition> kpis = KpiDefinitionHandler.getKpisForAGivenAliasFromStagedKpis(dailyKpis, alias);
            final List<String> aggregationElements = collectAggregationElementsForAlias(kpis, alias);

            final String result = objectUnderTest.createAggregationSql(kpis, alias, aggregationElements);
            assertThat(result).isEqualTo(
                    "SELECT agg_column_0, agg_column_1, agg_column_2, MAX(aggregation_begin_time) AS aggregation_begin_time, " +
                            "SUM(sum_real_1440) AS sum_real_1440, " +
                            "SUM(sum_integer_operator_1440) AS sum_integer_operator_1440, " +
                            "FIRST(first_boolean_1440, true) AS first_boolean_1440 FROM relation_guid_source_guid_target_guid_kpis " +
                            "GROUP BY agg_column_0, agg_column_1, agg_column_2, aggregation_begin_time");

        }

        @Test
        public void aggregateKpis_aggObject1_daily() {
            objectUnderTest = sqlCreator(DAILY_AGGREGATION_PERIOD_IN_MINUTES);
            final String alias = CELL_SECTOR;
            final Set<KpiDefinition> kpis = KpiDefinitionHandler.getKpisForAGivenAliasFromStagedKpis(dailyKpis, alias);
            final List<String> aggregationElements = collectAggregationElementsForAlias(kpis, alias);

            final String result = objectUnderTest.createAggregationSql(kpis, alias, aggregationElements);
            assertThat(result)
                    .isEqualTo(
                            "SELECT agg_column_0, agg_column_1, MAX(aggregation_begin_time) " +
                                    "AS aggregation_begin_time, FIRST(first_integer_1440_join_kpidb_filter, true) " +
                                    "AS first_integer_1440_join_kpidb_filter FROM cell_sector_kpis " +
                                    "GROUP BY agg_column_0, agg_column_1, aggregation_begin_time");
        }

        @Test
        public void testAggregationForKpisForDefaultAggregationPeriodDoesNotIncludeTimestamps() {
            objectUnderTest = sqlCreator(DEFAULT_AGGREGATION_PERIOD_INT);
            final String alias = CELL_SECTOR;
            final Set<KpiDefinition> kpis = KpiDefinitionHandler.getKpisForAGivenAliasFromStagedKpis(dailyKpis, alias);
            final List<String> aggregationElements = collectAggregationElementsForAlias(kpis, alias);

            final String result = objectUnderTest.createAggregationSql(kpis, alias, aggregationElements);
            assertThat(result).isEqualTo(
                    "SELECT agg_column_0, agg_column_1, FIRST(first_integer_1440_join_kpidb_filter, true) " +
                            "AS first_integer_1440_join_kpidb_filter FROM cell_sector_kpis GROUP BY agg_column_0, agg_column_1");
        }
    }

    @Nested
    class JoinSqlCreator {

        @Test
        public void joinKpiToViewForAlias_aggObject1_daily_whenNoPreviouslyCalculatedKpisExist() {
            objectUnderTest = sqlCreator(DAILY_AGGREGATION_PERIOD_IN_MINUTES);
            final String alias = CELL_SECTOR;
            final KpiDefinition kpiDefinitionToCalculate = getKpiByName("first_integer_1440_join_kpidb_filter");
            final KpiDefinition alreadyCalculatedKpi = getKpiByName("arrayindexsum_integerarray_1440");
            final Set<KpiDefinition> kpisToCalculate = new HashSet<>();
            final Set<KpiDefinition> alreadyCalculatedKpis = new HashSet<>();
            kpisToCalculate.add(kpiDefinitionToCalculate);
            alreadyCalculatedKpis.add(alreadyCalculatedKpi);
            final List<String> aggregationElements = collectAggregationElementsForAlias(kpisToCalculate, alias);

            final String secondDatasource = alias + "_test";
            final String result = objectUnderTest.createJoinSql(alias, kpisToCalculate, alreadyCalculatedKpis, secondDatasource, aggregationElements);
            assertThat(result).isEqualTo("SELECT coalesce(cell_sector.agg_column_0, cell_sector_test.agg_column_0) AS agg_column_0, " +
                    "coalesce(cell_sector.agg_column_1, cell_sector_test.agg_column_1) AS agg_column_1, " +
                    "coalesce(cell_sector.aggregation_begin_time, cell_sector_test.aggregation_begin_time) " +
                    "AS aggregation_begin_time, cell_sector.arrayindexsum_integerarray_1440, cell_sector_test.first_integer_1440_join_kpidb_filter " +
                    "FROM cell_sector_kpis cell_sector FULL OUTER JOIN cell_sector_test ON ((cell_sector.agg_column_0 = cell_sector_test.agg_column_0) " +
                    "AND (cell_sector.agg_column_1 = cell_sector_test.agg_column_1)) AND (cell_sector.aggregation_begin_time = cell_sector_test.aggregation_begin_time)");
        }

        @Test
        public void joinKpiToViewForAlias_aggObject1_daily_whenPreviouslyCalculatedKpisExist() {
            objectUnderTest = sqlCreator(DAILY_AGGREGATION_PERIOD_IN_MINUTES);
            final String alias = CELL_SECTOR;
            final KpiDefinition kpiDefinitionToCalculate = getKpiByName("first_integer_1440_join_kpidb_filter");
            final Set<KpiDefinition> kpisToCalculate = new HashSet<>();
            final Set<KpiDefinition> alreadyCalculatedKpis = new HashSet<>();
            kpisToCalculate.add(kpiDefinitionToCalculate);
            final List<String> aggregationElements = collectAggregationElementsForAlias(kpisToCalculate, alias);

            final String secondDatasource = alias + "_test";
            final String result = objectUnderTest.createJoinSql(alias, kpisToCalculate, alreadyCalculatedKpis, secondDatasource, aggregationElements);
            assertThat(result).isEqualTo("SELECT coalesce(cell_sector.agg_column_0, cell_sector_test.agg_column_0) " +
                    "AS agg_column_0, coalesce(cell_sector.agg_column_1, cell_sector_test.agg_column_1) AS agg_column_1, " +
                    "coalesce(cell_sector.aggregation_begin_time, cell_sector_test.aggregation_begin_time) AS aggregation_begin_time, " +
                    "cell_sector_test.first_integer_1440_join_kpidb_filter FROM cell_sector_kpis cell_sector FULL OUTER JOIN cell_sector_test " +
                    "ON ((cell_sector.agg_column_0 = cell_sector_test.agg_column_0) AND (cell_sector.agg_column_1 = cell_sector_test.agg_column_1)) " +
                    "AND (cell_sector.aggregation_begin_time = cell_sector_test.aggregation_begin_time)");
        }

        @Test
        public void joinKpiToViewForAlias_aggObject2_daily_whenNoPreviouslyCalculatedKpisExist() {
            objectUnderTest = sqlCreator(DAILY_AGGREGATION_PERIOD_IN_MINUTES);
            final String alias = RELATION_GUID_SOURCE_GUID_TARGET_GUID;
            final KpiDefinition kpiDefinitionToCalculate = getKpiByName("sum_real_1440");
            final KpiDefinition alreadyCalculatedKpi = getKpiByName("sum_integer_operator_1440");
            final Set<KpiDefinition> kpisToCalculate = new HashSet<>();
            final Set<KpiDefinition> alreadyCalculatedKpis = new HashSet<>();
            kpisToCalculate.add(kpiDefinitionToCalculate);
            alreadyCalculatedKpis.add(alreadyCalculatedKpi);
            final List<String> aggregationElements = collectAggregationElementsForAlias(kpisToCalculate, alias);

            final String secondDatasource = alias + "_test";
            final String result = objectUnderTest.createJoinSql(alias, kpisToCalculate, alreadyCalculatedKpis, secondDatasource, aggregationElements);

            final String expected = ("SELECT coalesce(relation_guid_source_guid_target_guid.agg_column_0, " +
                    "relation_guid_source_guid_target_guid_test.agg_column_0) AS agg_column_0, " +
                    "coalesce(relation_guid_source_guid_target_guid.agg_column_1, relation_guid_source_guid_target_guid_test.agg_column_1) " +
                    "AS agg_column_1, coalesce(relation_guid_source_guid_target_guid.agg_column_2, " +
                    "relation_guid_source_guid_target_guid_test.agg_column_2) AS agg_column_2, " +
                    "coalesce(relation_guid_source_guid_target_guid.aggregation_begin_time, " +
                    "relation_guid_source_guid_target_guid_test.aggregation_begin_time) AS aggregation_begin_time, " +
                    "relation_guid_source_guid_target_guid.sum_integer_operator_1440, " +
                    "relation_guid_source_guid_target_guid_test.sum_real_1440 " +
                    "FROM relation_guid_source_guid_target_guid_kpis relation_guid_source_guid_target_guid " +
                    "FULL OUTER JOIN relation_guid_source_guid_target_guid_test ON (((relation_guid_source_guid_target_guid.agg_column_0 = " +
                    "relation_guid_source_guid_target_guid_test.agg_column_0) AND (relation_guid_source_guid_target_guid.agg_column_1 = " +
                    "relation_guid_source_guid_target_guid_test.agg_column_1)) AND (relation_guid_source_guid_target_guid.agg_column_2 = " +
                    "relation_guid_source_guid_target_guid_test.agg_column_2)) AND (relation_guid_source_guid_target_guid.aggregation_begin_time = " +
                    "relation_guid_source_guid_target_guid_test.aggregation_begin_time)");

            assertThat(result).isEqualTo(expected);
        }

        @Test
        public void joinKpiToViewForAlias_aggObject2_daily_whenPreviouslyCalculatedKpisExist() {
            objectUnderTest = sqlCreator(DAILY_AGGREGATION_PERIOD_IN_MINUTES);
            final String alias = RELATION_GUID_SOURCE_GUID_TARGET_GUID;
            final KpiDefinition kpiDefinitionToCalculate = getKpiByName("sum_integer_operator_1440");
            final Set<KpiDefinition> kpisToCalculate = new HashSet<>();
            final Set<KpiDefinition> alreadyCalculatedKpis = new HashSet<>();
            kpisToCalculate.add(kpiDefinitionToCalculate);
            final List<String> aggregationElements = collectAggregationElementsForAlias(kpisToCalculate, alias);

            final String secondDatasource = alias + "_test";
            final String result = objectUnderTest.createJoinSql(alias, kpisToCalculate, alreadyCalculatedKpis, secondDatasource, aggregationElements);

            assertThat(result).isEqualTo("SELECT coalesce(relation_guid_source_guid_target_guid.agg_column_0, relation_guid_source_guid_target_guid_test.agg_column_0) AS agg_column_0, " +
                    "coalesce(relation_guid_source_guid_target_guid.agg_column_1, relation_guid_source_guid_target_guid_test.agg_column_1) AS agg_column_1, " +
                    "coalesce(relation_guid_source_guid_target_guid.agg_column_2, relation_guid_source_guid_target_guid_test.agg_column_2) AS agg_column_2, " +
                    "coalesce(relation_guid_source_guid_target_guid.aggregation_begin_time, relation_guid_source_guid_target_guid_test.aggregation_begin_time) " +
                    "AS aggregation_begin_time, relation_guid_source_guid_target_guid_test.sum_integer_operator_1440 FROM " +
                    "relation_guid_source_guid_target_guid_kpis relation_guid_source_guid_target_guid " +
                    "FULL OUTER JOIN relation_guid_source_guid_target_guid_test ON (((relation_guid_source_guid_target_guid.agg_column_0 = " +
                    "relation_guid_source_guid_target_guid_test.agg_column_0) AND (relation_guid_source_guid_target_guid.agg_column_1 = " +
                    "relation_guid_source_guid_target_guid_test.agg_column_1)) AND (relation_guid_source_guid_target_guid.agg_column_2 = " +
                    "relation_guid_source_guid_target_guid_test.agg_column_2)) AND (relation_guid_source_guid_target_guid.aggregation_begin_time = " +
                    "relation_guid_source_guid_target_guid_test.aggregation_begin_time)");
        }

        @Test
        public void testKpiJoinSqlQueryForDefaultAggregationPeriodDoesNotIncludeTimestamps() {
            objectUnderTest = sqlCreator(DEFAULT_AGGREGATION_PERIOD_INT);
            final String alias = CELL_SECTOR;
            final KpiDefinition kpiDefinitionToCalculate = getKpiByName("first_integer_1440_join_kpidb_filter");
            final KpiDefinition alreadyCalculatedKpi = getKpiByName("arrayindexsum_integerarray_1440");
            final Set<KpiDefinition> kpisToCalculate = new HashSet<>();
            final Set<KpiDefinition> alreadyCalculatedKpis = new HashSet<>();
            kpisToCalculate.add(kpiDefinitionToCalculate);
            alreadyCalculatedKpis.add(alreadyCalculatedKpi);
            final List<String> aggregationElements = collectAggregationElementsForAlias(kpisToCalculate, alias);

            final String secondDatasource = alias + "_test";
            final String result = objectUnderTest.createJoinSql(alias, kpisToCalculate, alreadyCalculatedKpis, secondDatasource, aggregationElements);
            assertThat(result).isEqualTo(
                    "SELECT coalesce(cell_sector.agg_column_0, cell_sector_test.agg_column_0) AS agg_column_0, " +
                            "coalesce(cell_sector.agg_column_1, cell_sector_test.agg_column_1) AS agg_column_1, " +
                            "cell_sector.arrayindexsum_integerarray_1440, cell_sector_test.first_integer_1440_join_kpidb_filter " +
                            "FROM cell_sector_kpis cell_sector FULL OUTER JOIN cell_sector_test ON " +
                            "((cell_sector.agg_column_0 = cell_sector_test.agg_column_0) AND (cell_sector.agg_column_1 = cell_sector_test.agg_column_1))");
        }
    }

    private List<String> collectAggregationElementsForAlias(Collection<KpiDefinition> kpis, String alias) {
        return kpiDefinitionHelper.getKpiAggregationElementsForAlias(kpis, alias);
    }

    private SqlCreator sqlCreator(final int aggregationPeriodInMinutes) {
        return new SqlCreator(aggregationPeriodInMinutes, sqlProcessorDelegator(new SparkSqlParser()), kpiDefinitionHelper);
    }

    public static Set<KpiDefinition> retrieveKpiDefinitions(final KpiDefinitionFileRetriever kpiDefinitionRetriever) {
        try {
            return kpiDefinitionRetriever.retrieveAllKpiDefinitions();
        } catch (final KpiRetrievalException e) {
            return Collections.emptySet();
        }
    }

    static KpiDefinition getKpiByName(final String kpiName) {
        return ALL_KPI_DEFINITIONS.stream()
                .filter(kpi -> kpi.getName().equals(kpiName))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Unable to find KPI '%s'", kpiName)));
    }
}