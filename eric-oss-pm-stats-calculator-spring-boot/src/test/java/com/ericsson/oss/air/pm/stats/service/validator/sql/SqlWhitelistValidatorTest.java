/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats._util.JsonLoaders;
import com.ericsson.oss.air.pm.stats._util.Serialization;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.service.validator.sql.exception.SqlWhitelistValidationException;
import com.ericsson.oss.air.pm.stats.service.validator.sql.whitelist.SqlValidatorImpl;

import kpi.model.KpiDefinitionRequest;
import org.apache.spark.sql.execution.SparkSqlParser;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class SqlWhitelistValidatorTest {
    private final static Map<String, String> allowedSqlElements = fillAllowedSqlElements();

    @TestFactory
    Stream<DynamicTest> validateSqlAllowedElementsKpiDefinitionRequest() {

        return allowedSqlElements.entrySet().stream().map(entry -> {
            final SqlWhitelistValidator objectUnderTest = objectUnderTest();
            final String displayName = String.format("%s --> %s", entry.getKey(), entry.getValue());

            return dynamicTest(displayName, () -> {
                final KpiDefinitionRequest validKpiDefinitionRequest = populateKpiDefinitionTemplate(entry.getKey(), entry.getValue());
                Assertions.assertDoesNotThrow(() -> objectUnderTest.validateSqlElements(validKpiDefinitionRequest));
            });
        });
    }

    @TestFactory
    Stream<DynamicTest> validateSqlAllowedElementsKpiDefinitionEntity() {

        return allowedSqlElements.entrySet().stream().map(entry -> {
            final SqlWhitelistValidator objectUnderTest = objectUnderTest();
            final String displayName = String.format("%s --> %s", entry.getKey(), entry.getValue());

            return dynamicTest(displayName, () -> {
                //final
                KpiDefinitionEntity validKpiDefinitionEntity = new KpiDefinitionEntity(null, entry.getKey(), null, entry.getValue(), null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
                Assertions.assertDoesNotThrow(() -> objectUnderTest.validateSqlElements(validKpiDefinitionEntity));
            });
        });
    }

    @TestFactory
    Stream<DynamicTest> validateProhibitedSqlElementsKpiDefinitionRequest() {
        // First index of the List is the prohibited element, the second is the sql query
        final Map<String, List<String>> notAllowedSqlElementsByKpiDefinitionName = Map.of(
                "date_format", List.of("date_format", "SELECT date_format('2016-04-08', 'y') FROM VALUES (1), (2), (1) AS tab(col)"),
                "collect_set", List.of("collect_set", "SELECT collect_set(col) FROM VALUES (1), (2), (1) AS tab(col)"),
                "group_by", List.of("group by", "SELECT col FROM VALUES (1), (2), (1) AS tab(col) GROUP BY col"),
                "sort_by", List.of("sortorder", "SELECT name, age FROM tabular_parameters://cell_configuration_test ORDER BY age NULLS LAST"),
                "order_by", List.of("sortorder", "SELECT /*+ REPARTITION(zip_code) */ name, age, zip_code FROM person SORT BY 1")
        );

        return notAllowedSqlElementsByKpiDefinitionName.entrySet().stream().map(entry -> {
            final SqlWhitelistValidator objectUnderTest = objectUnderTest();
            final String displayName = String.format("%s --> %s", entry.getKey(), entry.getValue().get(1));

            return dynamicTest(displayName, () -> {
                final KpiDefinitionRequest invalidKpiDefinitionRequest = populateKpiDefinitionTemplate(entry.getKey(), entry.getValue().get(1));

                assertThatThrownBy(() -> objectUnderTest.validateSqlElements(invalidKpiDefinitionRequest))
                        .isInstanceOf(SqlWhitelistValidationException.class)
                        .extracting("notAllowedSqlElementsByKpiDefinition")
                        .asInstanceOf(InstanceOfAssertFactories.map(String.class, Set.class))
                        .contains(Map.entry(entry.getKey(), Set.of(entry.getValue().get(0))));
            });
        });
    }

    @TestFactory
    Stream<DynamicTest> validateProhibitedSqlElementsKpiDefinitionEntity() {
        // First index of the List is the prohibited element, the second is the sql query
        final Map<String, List<String>> notAllowedSqlElementsByKpiDefinitionName = Map.of(
                "date_format", List.of("date_format", "SELECT date_format('2016-04-08', 'y') FROM VALUES (1), (2), (1) AS tab(col)"),
                "collect_set", List.of("collect_set", "SELECT collect_set(col) FROM VALUES (1), (2), (1) AS tab(col)"),
                "group_by", List.of("group by", "SELECT col FROM VALUES (1), (2), (1) AS tab(col) GROUP BY col"),
                "sort_by", List.of("sortorder", "SELECT name, age FROM tabular_parameters://cell_configuration_test ORDER BY age NULLS LAST"),
                "order_by", List.of("sortorder", "SELECT /*+ REPARTITION(zip_code) */ name, age, zip_code FROM person SORT BY 1")
        );

        return notAllowedSqlElementsByKpiDefinitionName.entrySet().stream().map(entry -> {
            final SqlWhitelistValidator objectUnderTest = objectUnderTest();
            final String displayName = String.format("%s --> %s", entry.getKey(), entry.getValue().get(1));

            return dynamicTest(displayName, () -> {
                KpiDefinitionEntity invalidKpiDefinitionEntity = new KpiDefinitionEntity(null, entry.getKey(), null, entry.getValue().get(1), null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

                assertThatThrownBy(() -> objectUnderTest.validateSqlElements(invalidKpiDefinitionEntity))
                        .isInstanceOf(SqlWhitelistValidationException.class)
                        .extracting("notAllowedSqlElementsByKpiDefinition")
                        .asInstanceOf(InstanceOfAssertFactories.map(String.class, Set.class))
                        .contains(Map.entry(entry.getKey(), Set.of(entry.getValue().get(0))));
            });
        });
    }

    private static KpiDefinitionRequest populateKpiDefinitionTemplate(final String name, final String expression) {
        final String template = JsonLoaders.load("json/WhiteListTestKpiTemplate.json");
        return Serialization.deserialize(String.format(template, name.toLowerCase(Locale.ENGLISH), expression), KpiDefinitionRequest.class);
    }

    private static SqlWhitelistValidator objectUnderTest() {
        return new SqlWhitelistValidator(new SparkSqlParser(), new SqlValidatorImpl());
    }

    private static Map<String, String> fillAllowedSqlElements() {
        final Map<String, String> allowedSqlElements = new HashMap<String, String>();

        allowedSqlElements.put("count", "SELECT count(*) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("first", "SELECT first(cell_configuration_test.tabular_parameter_dimension) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("last", "SELECT last(cell_configuration_test.tabular_parameter_dimension) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("min", "SELECT min(cell_configuration_test.tabular_parameter_dimension) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("sort_array", "SELECT sort_array(array_column) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("coalesce", "SELECT coalesce(column1, column2) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("IF", "SELECT IF(condition, value_if_true, value_if_false) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("hour", "SELECT hour(timestamp_column) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("addition", "SELECT column1 + column2 AS result FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("multiplication", "SELECT column1 * column2 AS result FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("subtraction", "SELECT column1 - column2 AS result FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("not", "SELECT NOT column1 FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("greatest", "SELECT greatest(10, 9, 2, 4, 3) FROM VALUES (1), (2), (1) AS tab(col)");
        allowedSqlElements.put("CAST", "SELECT CAST(cell_configuration_test.tabular_parameter_dimension AS INT) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("concat", "SELECT concat(' ', 'Spark', 'SQL') FROM VALUES (1), (2), (1) AS tab(col)");
        allowedSqlElements.put("any", "SELECT any(col) FROM vakyes (true), (false) as tab(col)");
        allowedSqlElements.put("bool_and", "SELECT bool_and(cell_configuration_test.tabular_parameter_dimension) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("bool_or", "SELECT bool_or(cell_configuration_test.tabular_parameter_dimension) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("every", "SELECT every(cell_configuration_test.tabular_parameter_dimension) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("some", "SELECT some(cell_configuration_test.tabular_parameter_dimension) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("mean", "SELECT mean(cell_configuration_test.tabular_parameter_dimension) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("array", "SELECT array(column1, column2) AS newArray FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("array_max", "SELECT array_max(arrayColumn) AS maxElement FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("array_min", "SELECT array_min(arrayColumn) AS minElement FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("slice", "SELECT slice(arrayColumn, 1, 2) AS slicedArray FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("ifnull", "SELECT ifnull(cell_configuration_test.tabular_parameter_dimension, 'defaultValue') AS result FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("nanvl", "SELECT nanvl(cell_configuration_test.tabular_parameter_dimension, replacement_value) AS result FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("nullif", "SELECT nullif(cell_configuration_test.tabular_parameter_dimension, 'value') FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("nvl", "SELECT nvl(cell_configuration_test.tabular_parameter_dimension, 'defaultValue') FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("nvl2", "SELECT nvl2(cell_configuration_test.tabular_parameter_dimension, 'valueIfNotNull', 'valueIfNull') FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("date_add", "SELECT date_add(date_column, 5) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("datediff", "SELECT datediff(end_date_column, start_date_column) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("months_between", "SELECT months_between(date1_column, date2_column) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("next_day", "SELECT next_day(date_column, 'Monday') FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("last_day", "SELECT last_day(date_column) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("date_part", "SELECT date_part('MONTH', date_column) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("day", "SELECT day(date_column) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("dayofmonth", "SELECT dayofmonth(date_column) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("dayofweek", "SELECT dayofweek(date_column) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("dayofyear", "SELECT dayofyear(date_column) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("extract", "SELECT extract(YEAR FROM TIMESTAMP '2019-08-12 01:00:00.123456')");
        allowedSqlElements.put("month", "SELECT month(date_column) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("quarter", "SELECT quarter(date_column) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("weekday", "SELECT weekday(date_column) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("weekofyear", "SELECT weekofyear(date_column) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("year", "SELECT year(date_column) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("minute", "SELECT minute(time_column) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("second", "SELECT second(time_column) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("ceil", "SELECT ceil(cell_configuration_test.tabular_parameter_dimension) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("ceiling", "SELECT ceiling(cell_configuration_test.tabular_parameter_dimension) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("div", "SELECT column1_name DIV column2_name FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("mod", "SELECT mod(column1_name, column2_name) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("abs", "SELECT abs(cell_configuration_test.tabular_parameter_dimension) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("isnan", "SELECT isnan(cell_configuration_test.tabular_parameter_dimension) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("isnull", "SELECT isnull(cell_configuration_test.tabular_parameter_dimension) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("isnotnull", "SELECT isnotnull(cell_configuration_test.tabular_parameter_dimension) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("substring", "SELECT substring(cell_configuration_test.tabular_parameter_dimension, start, length) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("substr", "SELECT substr(cell_configuration_test.tabular_parameter_dimension, start, length) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("trim", "SELECT trim(cell_configuration_test.tabular_parameter_dimension) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("aggregate", "SELECT aggregate(cell_configuration_test.tabular_parameter_dimension, initialValue, mergeFunction) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("array_sort", "SELECT array_sort(array_column) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("cardinality", "SELECT cardinality(array_column) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("ADD_DOUBLE_TO_ARRAY_WITH_LIMIT", "FIRST(ADD_DOUBLE_TO_ARRAY_WITH_LIMIT(cell_guid.cell_handover_success_rate_hourly_rss, kpi_cell_guid_60.cell_handover_success_rate_hourly, 8)) FROM kpi_db://kpi_cell_guid_60 LEFT JOIN kpi_db://kpi_cell_guid_on_demand_60 as cell_guid ON kpi_cell_guid_60.nodeFDN = cell_guid.nodeFDN AND kpi_cell_guid_60.moFdn = cell_guid.moFdn AND cell_guid.aggregation_begin_time = (kpi_cell_guid_60.aggregation_begin_time - interval 1 day) WHERE cell_guid.cell_handover_success_rate_hourly_rss IS NULL");
        allowedSqlElements.put("ADD_INTEGER_TO_ARRAY_WITH_LIMIT", "FIRST(ADD_INTEGER_TO_ARRAY_WITH_LIMIT(cell_guid.uplink_pusch_sinr_hourly_rss, kpi_cell_guid_60.ul_pusch_sinr_hourly, 8)) FROM kpi_db://kpi_cell_guid_60 LEFT JOIN kpi_db://kpi_cell_guid_on_demand_60 as cell_guid ON kpi_cell_guid_60.nodeFDN = cell_guid.nodeFDN AND kpi_cell_guid_60.moFdn = cell_guid.moFdn AND cell_guid.aggregation_begin_time = (kpi_cell_guid_60.aggregation_begin_time - interval 1 day) WHERE cell_guid.uplink_pusch_sinr_hourly_rss IS NULL");
        allowedSqlElements.put("ADD_LONG_TO_ARRAY_WITH_LIMIT", "FIRST(ADD_LONG_TO_ARRAY_WITH_LIMIT(cell_guid.uplink_pusch_sinr_hourly_rss, kpi_cell_guid_60.ul_pusch_sinr_hourly, 8)) FROM kpi_db://kpi_cell_guid_60 LEFT JOIN kpi_db://kpi_cell_guid_on_demand_60 as cell_guid ON kpi_cell_guid_60.nodeFDN = cell_guid.nodeFDN AND kpi_cell_guid_60.moFdn = cell_guid.moFdn AND cell_guid.aggregation_begin_time = (kpi_cell_guid_60.aggregation_begin_time - interval 1 day) WHERE cell_guid.uplink_pusch_sinr_hourly_rss IS NULL");
        allowedSqlElements.put("APPEND_DOUBLE_ARRAY_GIVEN_CARDINALITY_AND_LIMIT", "FIRST(APPEND_DOUBLE_ARRAY_GIVEN_CARDINALITY_AND_LIMIT(cell_guid_flm.detrended_rolling_array, cell_guid_flm.hourly_detrended_array, kpi_cell_guid_flm_on_demand_60.num_detrended_rolling_array, 1), true) FROM kpi_db://kpi_cell_guid_flm_on_demand_60 join kpi_db://kpi_cell_guid_flm_on_demand_60 as cell_guid_flm ON kpi_cell_guid_flm_on_demand_60.nodeFDN = cell_guid_flm.nodeFDN AND hour(kpi_cell_guid_flm_on_demand_60.aggregation_begin_time) = hour(cell_guid_flm.aggregation_begin_time)");
        allowedSqlElements.put("APPEND_INTEGER_ARRAY", "FIRST(APPEND_INTEGER_ARRAY(CASE WHEN cell_guid_flm.num_detrended_rolling_array IS NULL THEN cell_guid_flm.num_hourly_detrended_array ELSE cell_guid_flm.num_detrended_rolling_array END, kpi_cell_guid_flm_on_demand_60.num_hourly_detrended_array, 1+1), true) FROM kpi_db://kpi_cell_guid_flm_on_demand_60 join kpi_db://kpi_cell_guid_flm_on_demand_60 as cell_guid_flm ON kpi_cell_guid_flm_on_demand_60.nodeFDN = cell_guid_flm.nodeFDN AND hour(kpi_cell_guid_flm_on_demand_60.aggregation_begin_time) = hour(cell_guid_flm.aggregation_begin_time)");
        allowedSqlElements.put("APPEND_LONG_ARRAY", "FIRST(APPEND_LONG_ARRAY(CASE WHEN cell_guid_flm.num_detrended_rolling_array IS NULL THEN cell_guid_flm.num_hourly_detrended_array ELSE cell_guid_flm.num_detrended_rolling_array END, kpi_cell_guid_flm_on_demand_60.num_hourly_detrended_array, 1+1), true) FROM kpi_db://kpi_cell_guid_flm_on_demand_60 join kpi_db://kpi_cell_guid_flm_on_demand_60 as cell_guid_flm ON kpi_cell_guid_flm_on_demand_60.nodeFDN = cell_guid_flm.nodeFDN AND hour(kpi_cell_guid_flm_on_demand_60.aggregation_begin_time) = hour(cell_guid_flm.aggregation_begin_time)");
        allowedSqlElements.put("ARRAY_INDEX_SUM", "ARRAY_INDEX_SUM(NbIotCell.pmCounters.pmZTemporary219) FROM VALUES (1), (2), (1) AS tab(col)");
        allowedSqlElements.put("ARRAY_INDEX_SUM_INTEGER", "ARRAY_INDEX_SUM_INTEGER(NbIotCell.pmCounters.pmZTemporary219) FROM VALUES (1), (2), (1) AS tab(col)");
        allowedSqlElements.put("ARRAY_INDEX_SUM_DOUBLE", "ARRAY_INDEX_SUM_DOUBLE(NbIotCell.pmCounters.pmZTemporary219) FROM VALUES (1.0), (2.0), (1.0) AS tab(col)");
        allowedSqlElements.put("ARRAY_INDEX_SUM_LONG", "ARRAY_INDEX_SUM_LONG(NbIotCell.pmCounters.pmZTemporary219) FROM VALUES (1), (2), (1) AS tab(col)");
        allowedSqlElements.put("ARRAY_SIZE", "ARRAY_SIZE(FIRST(kpi_cell_guid_60.num_samples_ta_hourly, true)) FROM kpi_db://kpi_cell_guid_60");
        allowedSqlElements.put("CALCULATE_PERCENTILE_BIN", "FIRST(CALCULATE_PERCENTILE_BIN(kpi_cell_guid_1440.num_samples_ta, 90), true) FROM kpi_db://kpi_cell_guid_1440");
        allowedSqlElements.put("CALCULATE_PERCENTILE_VALUE", "FIRST(CALCULATE_PERCENTILE_VALUE(freqband_bandwidth_r_flm.mcu_cdf, 1)) FROM kpi_inmemory://freqband_bandwidth_r_flm");
        allowedSqlElements.put("CASE_WHEN", "FIRST(CASE WHEN kpi_cell_guid_60.double_count_factor_radio_tx_rank_distr IS NULL THEN NULL ELSE CONCAT(ZIP_WITH( kpi_cell_guid_60.cqi1, TRANSFORM( kpi_cell_guid_60.cqi2, x -> x * kpi_cell_guid_60.double_count_factor_radio_tx_rank_distr), (cqi1Value,cCqi2Value) -> if (cqi1Value-cCqi2Value < 0, 0, cqi1Value-cCqi2Value)), TRANSFORM( kpi_cell_guid_60.cqi2, x -> x * kpi_cell_guid_60.double_count_factor_radio_tx_rank_distr) ) END) FROM kpi_db://kpi_cell_guid_60");
        allowedSqlElements.put("FDN_PARSE", "FDN_PARSE(NbIotCell.nodeFDN, 'Equipment') FROM VALUES (1), (2), (1) AS tab(col)");
        allowedSqlElements.put("MEDIAN_OF_VALUES", "FIRST(MEDIAN_OF_VALUES(cell_sector_flm.detrended_probability_lower)) FROM kpi_inmemory://cell_sector_flm");
        allowedSqlElements.put("PERCENTILE_INDEX_90", "PERCENTILE_INDEX_90(kpi_cell_guid_1440.num_samples_ta) FROM kpi_db://kpi_cell_guid_1440");
        allowedSqlElements.put("TRANSFORM", "FIRST(CASE WHEN kpi_cell_guid_60.double_count_factor_radio_tx_rank_distr IS NULL THEN NULL ELSE CONCAT(ZIP_WITH( kpi_cell_guid_60.cqi1, TRANSFORM( kpi_cell_guid_60.cqi2, x -> x * kpi_cell_guid_60.double_count_factor_radio_tx_rank_distr), (cqi1Value,cCqi2Value) -> if (cqi1Value-cCqi2Value < 0, 0, cqi1Value-cCqi2Value)), TRANSFORM( kpi_cell_guid_60.cqi2, x -> x * kpi_cell_guid_60.double_count_factor_radio_tx_rank_distr) ) END) FROM kpi_db://kpi_cell_guid_60");
        allowedSqlElements.put("avg", "SELECT avg(col) FROM VALUES (1), (2), (3) AS tab(col)");
        allowedSqlElements.put("round", "SELECT round(2.5, 0) FROM VALUES (1), (2), (1) AS tab(col)");
        allowedSqlElements.put("floor", "SELECT floor(3.1411, 3) FROM VALUES (1), (2), (1) AS tab(col)");
        allowedSqlElements.put("in", "SELECT 1 in(1, 2, 3) FROM VALUES (1), (2), (1) AS tab(col)");
        allowedSqlElements.put("least", "SELECT least(10, 9, 2, 4, 3) FROM VALUES (1), (2), (1) AS tab(col)");
        allowedSqlElements.put("ln", "SELECT ln(column1_name) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("log", "SELECT log(column1_name) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("log10", "SELECT log10(column1_name) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("max", "SELECT max(col) FROM VALUES (10), (50), (20) AS tab(col)");
        allowedSqlElements.put("negative", "SELECT negative(1) FROM VALUES (1), (2), (1) AS tab(col)");
        allowedSqlElements.put("positive", "SELECT positive(1) FROM VALUES (1), (2), (1) AS tab(col)");
        allowedSqlElements.put("sum", "SELECT sum(col) FROM VALUES (5), (10), (15) AS tab(col)");
        allowedSqlElements.put("double", "SELECT double(cell_configuration_test.tabular_parameter_dimension) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("collect_list", "SELECT collect_list(cell_configuration_test.tabular_parameter_dimension) FROM tabular_parameters://cell_configuration_test");
        allowedSqlElements.put("to_timestamp", "SELECT to_timestamp(col) FROM VALUES ('07-01-2019'), ('06-24-2019'), ('11-16-2019') AS tab(col)");
        allowedSqlElements.put("date_trunc", "SELECT date_trunc(col) FROM VALUES ('yyyy','07-01-2019'), ('mm','06-24-2019'), ('dd','11-16-2019') AS tab(col)");
        allowedSqlElements.put("percentile_index_80", "PERCENTILE_INDEX_80(80) FROM kpi_db://kpi_cell_guid_1440");
        allowedSqlElements.put("update_null_time_advanced_kpis", "UPDATE_NULL_TIME_ADVANCED_KPIS(1, 2, 3) FROM kpi_db://kpi_cell_guid_1440");

        return allowedSqlElements;
    }
}