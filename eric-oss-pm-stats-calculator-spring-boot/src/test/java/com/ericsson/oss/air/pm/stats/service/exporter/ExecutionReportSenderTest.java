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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculation.event.SendingReportToExporterEvent;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.calculator.configuration.CalculatorProperties;
import com.ericsson.oss.air.pm.stats.service.api.CalculationService;
import com.ericsson.oss.air.pm.stats.service.exporter.builder.ExecutionReportBuilderFacade;
import com.ericsson.oss.air.pm.stats.service.exporter.model.ExecutionReport;
import com.ericsson.oss.air.pm.stats.service.exporter.util.KpiExporterException;

import com.google.common.util.concurrent.Uninterruptibles;
import io.github.resilience4j.retry.Retry;
import lombok.SneakyThrows;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.Verification;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;


@ExtendWith(MockitoExtension.class)
@DisplayName("Execution report sender test")
class ExecutionReportSenderTest {
    private static final String TOPIC_NAME = "topicName";
    private static final UUID TEST_UUID = UUID.fromString("5c39de77-800f-42b5-ab2d-771bc4301c8d");

    @Mock CalculatorProperties calculatorPropertiesMock;
    @Mock(name = "updateCalculationStateRetry") Retry updateCalculationStateRetryMock;
    @Mock ExecutionReportBuilderFacade executionReportBuilderFacadeMock;
    @Mock CalculationService calculationServiceMock;
    @Mock KafkaProducer<String, ExecutionReport> producerMock;

    @InjectMocks ExecutionReportSender objectUnderTest;

    @Nested
    class SendExecutionReport {
        @Mock ExecutionReport executionReportMock;
        @Mock Future<RecordMetadata> futureMock;

        @Test
        void shouldSendExecutionReport() {
            try (final MockedStatic<Uninterruptibles> uninterruptiblesMockedStatic = mockStatic(Uninterruptibles.class)) {
                final Verification verification = () -> Uninterruptibles.getUninterruptibly(futureMock, 5, TimeUnit.SECONDS);
                ReflectionTestUtils.setField(objectUnderTest, "traceIdFormat", "b3");

                final String b3HeaderValue = TEST_UUID.toString().replace("-", "") + "-1000000000000000";
                ProducerRecord<String, ExecutionReport> pr = new ProducerRecord<>(TOPIC_NAME, executionReportMock);
                pr.headers().add("b3", b3HeaderValue.getBytes(StandardCharsets.UTF_8));
                doReturn(TOPIC_NAME).when(calculatorPropertiesMock).getKafkaExecutionReportTopicName();
                doReturn(executionReportMock).when(executionReportBuilderFacadeMock).buildExecutionReport(TEST_UUID);
                doReturn(futureMock).when(producerMock).send(pr);
                uninterruptiblesMockedStatic.when(verification).thenAnswer(invocation -> null);

                Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.sendExecutionReport(SendingReportToExporterEvent.of(TEST_UUID)));

                verify(executionReportBuilderFacadeMock).buildExecutionReport(TEST_UUID);
                verify(calculatorPropertiesMock).getKafkaExecutionReportTopicName();
                verify(producerMock).send(pr);
                uninterruptiblesMockedStatic.verify(verification);
                verify(calculationServiceMock).updateCompletionStateWithRetry(TEST_UUID, KpiCalculationState.FINISHED, updateCalculationStateRetryMock);
            }
        }

        @Test
        void shouldThrowException_whenSendingMessageFails() {
            try (final MockedStatic<Uninterruptibles> uninterruptiblesMockedStatic = mockStatic(Uninterruptibles.class)) {
                final Verification verification = () -> Uninterruptibles.getUninterruptibly(futureMock, 5, TimeUnit.SECONDS);
                ReflectionTestUtils.setField(objectUnderTest, "traceIdFormat", "b3");
                final String b3HeaderValue = TEST_UUID.toString().replace("-", "") + "-1000000000000000";
                ProducerRecord<String, ExecutionReport> pr = new ProducerRecord<>(TOPIC_NAME, executionReportMock);
                pr.headers().add("b3", b3HeaderValue.getBytes(StandardCharsets.UTF_8));

                doReturn(TOPIC_NAME).when(calculatorPropertiesMock).getKafkaExecutionReportTopicName();
                doReturn(executionReportMock).when(executionReportBuilderFacadeMock).buildExecutionReport(TEST_UUID);
                doReturn(futureMock).when(producerMock).send(pr);
                uninterruptiblesMockedStatic.when(verification).thenThrow(ExecutionException.class);

                Assertions.assertThatThrownBy(() -> objectUnderTest.sendExecutionReport(SendingReportToExporterEvent.of(TEST_UUID)))
                        .isInstanceOf(KpiExporterException.class);

                verify(executionReportBuilderFacadeMock).buildExecutionReport(TEST_UUID);
                verify(calculatorPropertiesMock).getKafkaExecutionReportTopicName();
                verify(producerMock).send(pr);
                uninterruptiblesMockedStatic.verify(verification);
                verify(calculationServiceMock, never()).updateCompletionStateWithRetry(TEST_UUID, KpiCalculationState.FINISHED, updateCalculationStateRetryMock);
            }
        }

        @SneakyThrows
        @MethodSource("provideTraceIdHeaderKeyValuePair")
        @ParameterizedTest(name = "[{index}] For traceId format: ''{0}'' traceId: ''{1}'' created")
        void shouldSetCorrectTraceIdKafkaHeader(final String traceIdFormat, final Map<String, String> traceIdKeyValuePair) {

            ReflectionTestUtils.setField(objectUnderTest, "traceIdFormat", traceIdFormat);
            ProducerRecord<String, ExecutionReport> pr = new ProducerRecord<>(TOPIC_NAME, executionReportMock);

            for(Map.Entry<String, String> entry : traceIdKeyValuePair.entrySet()){
                pr.headers().add(entry.getKey(), entry.getValue().getBytes(StandardCharsets.UTF_8));
            }

            doReturn(TOPIC_NAME).when(calculatorPropertiesMock).getKafkaExecutionReportTopicName();
            doReturn(executionReportMock).when(executionReportBuilderFacadeMock).buildExecutionReport(TEST_UUID);
            doReturn(futureMock).when(producerMock).send(pr);

            objectUnderTest.sendExecutionReport(SendingReportToExporterEvent.of(TEST_UUID));

            verify(producerMock).send(pr);
        }

        private static Stream<Arguments> provideTraceIdHeaderKeyValuePair() {
            final Map<String, String> compositeMap = new LinkedHashMap<>();
            compositeMap.put("b3", "5c39de77800f42b5ab2d771bc4301c8d-1000000000000000");
            compositeMap.put("traceparent", "00-5c39de77800f42b5ab2d771bc4301c8d-1000000000000000-00");
            return Stream.of(Arguments.of("b3", Map.of("b3", "5c39de77800f42b5ab2d771bc4301c8d-1000000000000000")),
                    Arguments.of("w3c", Map.of("traceparent", "00-5c39de77800f42b5ab2d771bc4301c8d-1000000000000000-00")),
                    Arguments.of("composite", compositeMap));
        }
    }
}
