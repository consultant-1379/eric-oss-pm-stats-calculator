/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.exporter.util;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiCalculatorErrorCode;
import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiCalculatorException;
import com.ericsson.oss.air.pm.stats.service.exporter.model.ExecutionReport;
import com.ericsson.oss.air.pm.stats.service.exporter.model.Kpi;
import com.ericsson.oss.air.pm.stats.service.exporter.model.Table;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("KafkaSerializer tests")
class KafkaJsonSerializerTest {

    KafkaJsonSerializer objectUnderTest = new KafkaJsonSerializer();

    @Test
    void shouldSerialize() {
        final ExecutionReport executionReport = new ExecutionReport();
        executionReport.setExecutionId(UUID.fromString("12345678-1234-1234-1234-000000000001"));
        executionReport.setExecutionStart(
                LocalDateTime.parse("2022-08-01T10:01:00").toEpochSecond(ZoneOffset.UTC));
        executionReport.setExecutionEnd(LocalDateTime.parse("2022-08-01T10:01:01").toEpochSecond(ZoneOffset.UTC));
        executionReport.setScheduled(true);
        executionReport.setExecutionGroup("fact_table_1");
        executionReport.setTables(Collections.singletonList(Table
                .builder()
                .withName("cell_guid")
                .withAggregationPeriod(60)
                .withListOfKpis(List.of("kpi", "kpi2"))
                .withListOfDimensions(List.of("dimension1", "dimension2"))
                .withKpis(Collections.singletonList(Kpi.builder().withName("kpi").withExportable(true).withReexportLateData(true).withReliabilityThreshold(1663761600).withCalculationStartTime(1653141200).build()))
                .build()
        ));
        final byte[] actual = objectUnderTest.serialize("testTopic", executionReport);
        final String expected = "{" +
                "\"execution_id\":\"12345678-1234-1234-1234-000000000001\"," +
                "\"scheduled\":true," +
                "\"execution_start\":1659348060," +
                "\"execution_end\":1659348061," +
                "\"execution_group\":\"fact_table_1\"," +
                "\"tables\":[{" +
                "\"name\":\"cell_guid\"," +
                "\"aggregation_period\":60," +
                "\"kpis\":[{" +
                "\"name\":\"kpi\"," +
                "\"reexport_late_data\":true," +
                "\"exportable\":true," +
                "\"reliability_threshold\":1663761600," +
                "\"calculation_start_time\":1653141200" +
                "}]," +
                "\"list_of_kpis\":[\"kpi\",\"kpi2\"]," +
                "\"list_of_dimensions\":[\"dimension1\",\"dimension2\"]" +
                "}]}";

        Assertions.assertThat(actual).asString().isEqualTo(expected);
    }

    @Test
    void shouldNotSerialize() {
        final ExecutionReport report = mock(ExecutionReport.class);
        doThrow(RuntimeException.class).when(report).getExecutionGroup();
        final KpiCalculatorException exception =
                Assertions.catchThrowableOfType(() -> objectUnderTest.serialize("testTopic", report), KpiCalculatorException.class);
        Assertions.assertThat(exception).returns(KpiCalculatorErrorCode.KPI_SENDING_EXECUTION_REPORT_ERROR, Assertions.from(KpiCalculatorException::getErrorCode));
    }
}
