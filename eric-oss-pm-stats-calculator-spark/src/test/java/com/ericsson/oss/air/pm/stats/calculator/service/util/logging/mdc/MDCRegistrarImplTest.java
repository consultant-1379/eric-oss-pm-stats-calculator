/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.logging.mdc;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

@ExtendWith(MockitoExtension.class)
class MDCRegistrarImplTest {
    @Mock SparkService sparkServiceMock;

    @InjectMocks
    MDCRegistrarImpl objectUnderTest;

    @Test
    void shouldRegisterMDC() {
        try (final MockedStatic<MDC> mdcMockedStatic = mockStatic(MDC.class)) {
            when(sparkServiceMock.getApplicationId()).thenReturn("dummyApplicationID");
            when(sparkServiceMock.getCalculationId()).thenReturn(UUID.fromString("8864c91b-8257-4d2a-8ed4-60fcd7d242d5"));

            objectUnderTest.registerLoggingKeys();

            verify(sparkServiceMock).getApplicationId();
            verify(sparkServiceMock).getCalculationId();
            mdcMockedStatic.verify(() -> MDC.put("applicationIdLogLabelKey", "Application_ID: "));
            mdcMockedStatic.verify(() -> MDC.put("applicationId", String.format("%s ", "dummyApplicationID")));
            mdcMockedStatic.verify(() -> MDC.put("calculationIdLogLabelKey", "Calculation_ID: "));
            mdcMockedStatic.verify(() -> MDC.put("calculationId", String.format("%s ", "8864c91b-8257-4d2a-8ed4-60fcd7d242d5")));
        }
    }
}