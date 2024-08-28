/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.verification;

import static com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils.A_DAY_AGO;
import static com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils.TODAY;
import static com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils.TWO_DAYS_AGO;
import static com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils.daysAgo;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.ericsson.oss.air.pm.stats.common.resources.utils.ResourceLoaderUtils;
import com.ericsson.oss.air.pm.stats.service.exporter.model.ExecutionReport;
import com.ericsson.oss.air.pm.stats.service.exporter.model.Table;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KpiExecutionReportVerifier {

    private static final String ON_DEMAND = "ON_DEMAND";
    private static final String FIRST_ON_DEMAND_TABLE_NAME = "kpi_sector_60";
    private static final String SECOND_ON_DEMAND_TABLE_NAME = "kpi_cell_sector_1440";
    private static final String THIRD_ON_DEMAND_TABLE_NAME = "kpi_sector_1440";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final AtomicBoolean FIRST_SIMPLE_ROUND = new AtomicBoolean(true);
    private static final AtomicInteger COMPLEX_60_REPORT = new AtomicInteger(0);

    private static final Map<String, Range> RELIABILITIES_MAP = Map.ofEntries(
            Map.entry("kpi_complex_", Range.of(daysAgo(1, 0), daysAgo(1, 1))),
            Map.entry("kpi_complex_60", range(2, 22, 1, 1)),
            Map.entry("kpi_simple_60", range(2, 22, 1, 1)),
            Map.entry("kpi_same_day_simple_60", Range.of(daysAgo(1, 0), daysAgo(1, 1))),
            Map.entry("kpi_complex_1440", Range.of(A_DAY_AGO, A_DAY_AGO)),
            Map.entry("kpi_complex2_60", range(2, 15, 1, 2)),
            Map.entry("lookback", range(1, 0, 1, 0)),
            Map.entry("kpi_cell_guid_simple_1440", Range.of(TWO_DAYS_AGO, A_DAY_AGO)),
            Map.entry("kpi_rel_guid_s_guid_t_guid_simple_", range(2, 22, 1, 1)),
            Map.entry("kpi_rel_guid_s_guid_t_guid_simple_filter_1440", Range.of(TWO_DAYS_AGO, A_DAY_AGO)),
            Map.entry("kpi_sector_60", range(2, 22, 1, 0)),
            Map.entry("kpi_sector_1440", Range.of(TWO_DAYS_AGO, A_DAY_AGO)),
            Map.entry("kpi_rolling_aggregation_1440", Range.of(TWO_DAYS_AGO, TODAY)),
            Map.entry("kpi_relation_guid_source_guid_target_guid_60", range(2, 22, 1, 2)),
            Map.entry("kpi_execution_id_1440", Range.of(TWO_DAYS_AGO, TODAY)),
            Map.entry("kpi_cell_sector_1440", Range.of(TWO_DAYS_AGO, A_DAY_AGO)),
            Map.entry("kpi_cell_guid_60", range(2, 22, 1, 2)),
            Map.entry("kpi_cell_guid_1440", Range.of(TWO_DAYS_AGO, TODAY)),
            Map.entry("kpi_limited_15", Range.of(daysAgo(2, 22), daysAgo(2, 22, 15))),
            Map.entry("kpi_limited_complex_15", Range.of(daysAgo(2, 22), daysAgo(2, 22, 15))),
            Map.entry("kpi_limited_complex_dependent_15", Range.of(daysAgo(2, 22), daysAgo(2, 22, 15))),
            Map.entry("kpi_limited_ondemand_15", Range.of(daysAgo(2, 22), daysAgo(2, 22, 30))),
            Map.entry("kpi_complex_example_1440", Range.of(TWO_DAYS_AGO, A_DAY_AGO)),
            Map.entry("kpi_simple_example_60", Range.of(daysAgo(2, 22), A_DAY_AGO)),
            Map.entry("kpi_example_table_60", Range.of(daysAgo(2, 22), A_DAY_AGO)),
            Map.entry("kpi_relation_simple_60", Range.of(daysAgo(1, 22), TODAY)),
            Map.entry("kpi_simple_fdn_cell_60", Range.of(daysAgo(1, 22), TODAY)),
            Map.entry("kpi_predefined_simple_60", Range.of(daysAgo(1, 22), TODAY)),
            Map.entry("kpi_predefined_complex_60", Range.of(daysAgo(1, 22), TODAY)),
            Map.entry("kpi_complex_fdn_60", Range.of(daysAgo(1, 22), TODAY)),
            Map.entry("kpi_on_demand_fdn_60", Range.of(daysAgo(1, 22), daysAgo(0, 2))),
            Map.entry("kpi_ondemand_fdn_edge_1440", Range.of(A_DAY_AGO, daysAgo(-1))),
            Map.entry("kpi_ondemand_fdn_agg_1440", Range.of(A_DAY_AGO, daysAgo(-1))),
            Map.entry("kpi_parameter_types_1440", Range.of(TWO_DAYS_AGO, TODAY))
    );

    public static void verifyExecutionReportsMatchExpected(final List<ExecutionReport> executionReports) {
        executionReports.forEach(KpiExecutionReportVerifier::assertExecutionReport);
    }

    @SneakyThrows
    private static void assertExecutionReport(final ExecutionReport actual) {
        log.info("Asserting Execution report with execution group: {} and calculation id: {}", actual.getExecutionGroup(), actual.getExecutionId());
        final ExecutionReport expected = getExpectedReport(actual);

        expected.getTables().forEach(table -> {
            final String key = getKey(table);
            table.getKpis().forEach(kpi -> {
                final Range reliabilities = RELIABILITIES_MAP.get(key);
                kpi.setCalculationStartTime(reliabilities.getCalculationStartTime());
                kpi.setReliabilityThreshold(reliabilities.getReliabilityThreshold());
            });
        });

        assertThat(actual.isScheduled()).isEqualTo(expected.isScheduled());
        assertThat(actual.getTables()).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(expected.getTables());
    }

    private static ExecutionReport getExpectedReport(ExecutionReport actual) throws IOException {
        if (actual.getExecutionGroup().equals(ON_DEMAND)) {
            if (actual.getTables().stream().anyMatch(table -> table.getName().equals(FIRST_ON_DEMAND_TABLE_NAME))) {
                return getExecutionReport("on_demand_1");
            } else {
                if (actual.getTables().stream().anyMatch(table -> table.getName().equals(SECOND_ON_DEMAND_TABLE_NAME))) {
                    return getExecutionReport("on_demand_2");
                } else {
                    if (actual.getTables().stream().anyMatch(table -> table.getName().equals(THIRD_ON_DEMAND_TABLE_NAME))) {
                        return getExecutionReport("on_demand_3");
                    } else {
                        return getExecutionReport("on_demand_fdn");
                    }
                }
            }
        }
        return getExecutionReport(actual.getExecutionGroup());
    }

    private static String getKey(final Table table) {
        String key = table.getName();
        if ("kpi_simple_60".equals(key)) {
            if (FIRST_SIMPLE_ROUND.get()) {
                FIRST_SIMPLE_ROUND.set(false);
            } else {
                key = "lookback";
            }
        }

        if ("kpi_complex_60".equals(key)) {
            if (COMPLEX_60_REPORT.getAndIncrement() >= 2) {  // 4 execution report will have the table, 2/heartbeat
                key = "lookback";
            }
        }
        return key;
    }

    private static ExecutionReport getExecutionReport(final String executionGroup) throws IOException {
        return OBJECT_MAPPER.readValue(ResourceLoaderUtils.getClasspathResourceAsStream(
                String.format("execution_reports/%s.json", executionGroup.toLowerCase())
                        .replaceAll("\\|", "-")), ExecutionReport.class);
    }

    private static Range range(final int fromDays, final int fromHour, final int toDays, final int toHours) {
        return Range.of(daysAgo(fromDays, fromHour), daysAgo(toDays, toHours));
    }

    @Data
    private static final class Range {
        private final long calculationStartTime;
        private final long reliabilityThreshold;

        private Range(final LocalDateTime calculationStartTime, final LocalDateTime reliabilityThreshold) {
            this.calculationStartTime = calculationStartTime.toEpochSecond(ZoneOffset.UTC);
            this.reliabilityThreshold = reliabilityThreshold.toEpochSecond(ZoneOffset.UTC);
        }

        private static Range of(final LocalDateTime calculationStartTime, final LocalDateTime reliabilityThreshold) {
            return new Range(calculationStartTime, reliabilityThreshold);
        }
    }
}
