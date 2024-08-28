/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation;

import static org.mockito.Mockito.verify;

import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculation.event.SendingReportToExporterEvent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ExecutionReportEventPublisherTest {

    @Mock
    ApplicationEventPublisher applicationEventPublisherMock;

    @InjectMocks
    ExecutionReportEventPublisher objectUnderTest;

    @Test
    void shouldPushEvent() {
        final UUID uuid = UUID.fromString("5c97ec86-338e-4f05-8ecf-e6c7074196bb");
        objectUnderTest.pushEvent(uuid);

        verify(applicationEventPublisherMock).publishEvent(SendingReportToExporterEvent.of(uuid));
    }

}