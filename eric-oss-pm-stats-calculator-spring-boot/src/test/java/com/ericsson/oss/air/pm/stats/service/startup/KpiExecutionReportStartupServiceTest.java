/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.startup;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculation.event.SendingReportToExporterEvent;
import com.ericsson.oss.air.pm.stats.model.entity.Calculation;
import com.ericsson.oss.air.pm.stats.service.api.CalculationService;
import com.ericsson.oss.air.pm.stats.service.exporter.ExecutionReportSender;
import com.ericsson.oss.air.pm.stats.service.exporter.ExecutionReportTopicCreator;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KpiExecutionReportStartupServiceTest {

    @Mock
    ExecutionReportTopicCreator executionReportTopicCreatorMock;
    @Mock
    ExecutionReportSender executionReportSenderMock;
    @Mock
    CalculationService calculationServiceMock;

    @InjectMocks
    KpiExecutionReportStartupService objectUnderTest;

    @Test
    void whenServiceStartup_shouldExecutionReportSenderBeCalled(
            @Mock final Calculation calculationMock1,
            @Mock final Calculation calculationMock2) {
        final UUID uuid1 = UUID.fromString("8864c91b-8257-4d2a-8ed4-60fcd7d242d5");
        final UUID uuid2 = UUID.fromString("0347972f-e1a7-473d-b9cd-83651a3ae3ad");

        when(calculationMock1.getCalculationId()).thenReturn(uuid1);
        when(calculationMock2.getCalculationId()).thenReturn(uuid2);

        when(calculationServiceMock.findCalculationReadyToBeExported()).thenReturn(List.of(calculationMock1, calculationMock2));

        objectUnderTest.onServiceStart();

        final InOrder inOrder = inOrder(executionReportSenderMock, executionReportTopicCreatorMock, calculationServiceMock);
        inOrder.verify(executionReportTopicCreatorMock).createTopic();
        inOrder.verify(calculationServiceMock).findCalculationReadyToBeExported();
        Assertions.assertThatNoException().isThrownBy(() -> inOrder.verify(executionReportSenderMock).sendExecutionReport(SendingReportToExporterEvent.of(uuid1)));
        Assertions.assertThatNoException().isThrownBy(() -> inOrder.verify(executionReportSenderMock).sendExecutionReport(SendingReportToExporterEvent.of(uuid2)));

    }
}
