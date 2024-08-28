/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.exporter;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.ericsson.oss.air.pm.stats.calculation.event.SendingReportToExporterEvent;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.calculator.configuration.CalculatorProperties;
import com.ericsson.oss.air.pm.stats.service.api.CalculationService;
import com.ericsson.oss.air.pm.stats.service.exporter.builder.ExecutionReportBuilderFacade;
import com.ericsson.oss.air.pm.stats.service.exporter.model.ExecutionReport;
import com.ericsson.oss.air.pm.stats.service.exporter.util.KpiExporterException;

import com.google.common.util.concurrent.Uninterruptibles;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Class used to send execution reports to Kafka.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExecutionReportSender {

    private static final String TRACE_ID_FORMAT_B3_KEY = "b3";
    private static final String TRACE_ID_FORMAT_W3C_KEY = "traceparent";
    private static final String B3_HEADER_FORMAT = "%s-1000000000000000";
    private static final String W3C_HEADER_FORMAT = "00-%s-1000000000000000-00";

    @Value("${ericsson.tracing.propagator.type}")
    private String traceIdFormat;

    private final CalculatorProperties calculatorProperties;
    private final Retry updateCalculationStateRetry;
    private final ExecutionReportBuilderFacade executionReportBuilderFacade;
    private final CalculationService calculationService;
    private final KafkaProducer<String, ExecutionReport> producer;

    //ToDo: Transactional solution should be implemented for sending execution report to Kafka and updating completion state, if possible.

    /**
     * Builds and sends execution report to Kafka.
     * After successful sending calculation state is updated to {@link KpiCalculationState} FINISHED.
     *
     * @param reportToExporterEvent {@link SendingReportToExporterEvent} containing event related information
     * @throws KpiExporterException if sending execution report fails
     */
    @Async
    @EventListener
    public void sendExecutionReport(final SendingReportToExporterEvent reportToExporterEvent) throws KpiExporterException {
        final UUID calculationId = reportToExporterEvent.calculationId();
        ExecutionReport executionReport;
        try {
            executionReport = executionReportBuilderFacade.buildExecutionReport(calculationId);
            log.info("ExecutionReport of calculation '{}' was built successfully.", calculationId);
        } catch (final Exception e) {
            log.error("Failed to build ExecutionReport of calculation '{}' with error: {}.", calculationId, e);
            throw new KpiExporterException("Failed to process exporting execution reports");
        }

        final String executionReportTopicName = calculatorProperties.getKafkaExecutionReportTopicName();

        try {
            Uninterruptibles.getUninterruptibly(
                    producer.send(createProducerRecord(executionReportTopicName, executionReport, calculationId)),
                    5,
                    TimeUnit.SECONDS);
            log.info("ExecutionReport of calculation '{}' was exported on '{}' topic.", calculationId, executionReportTopicName);
        } catch (final Exception e) {
            log.error("Failed to export ExecutionReport of calculation '{}' on '{}' topic with error: {}.", calculationId, executionReportTopicName, e);
            throw new KpiExporterException("Failed to process exporting execution reports");
        }

        calculationService.updateCompletionStateWithRetry(calculationId, KpiCalculationState.FINISHED, updateCalculationStateRetry);
    }

    private ProducerRecord<String, ExecutionReport> createProducerRecord(final String executionReportTopicName, final ExecutionReport executionReport, final UUID calculationId ){

        final String traceId = calculationId.toString().replace("-", "");
        final String b3HeaderValue = String.format(B3_HEADER_FORMAT, traceId);
        final String w3cHeaderValue = String.format(W3C_HEADER_FORMAT, traceId);
        final ProducerRecord<String, ExecutionReport> producerRecord = new ProducerRecord<>(executionReportTopicName, executionReport);

        if(traceIdFormat.equals("b3")){
            producerRecord.headers().add(TRACE_ID_FORMAT_B3_KEY, b3HeaderValue.getBytes(StandardCharsets.UTF_8));
        } else if(traceIdFormat.equals("w3c")){
            producerRecord.headers().add(TRACE_ID_FORMAT_W3C_KEY, w3cHeaderValue.getBytes(StandardCharsets.UTF_8));
        } else {
            producerRecord.headers().add(TRACE_ID_FORMAT_B3_KEY, b3HeaderValue.getBytes(StandardCharsets.UTF_8));
            producerRecord.headers().add(TRACE_ID_FORMAT_W3C_KEY, w3cHeaderValue.getBytes(StandardCharsets.UTF_8));
        }
        return producerRecord;
    }
}
