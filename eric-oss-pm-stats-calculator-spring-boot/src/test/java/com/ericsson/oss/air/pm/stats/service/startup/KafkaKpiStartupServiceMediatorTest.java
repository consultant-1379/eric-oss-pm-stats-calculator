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

import static org.mockito.Mockito.verify;

import com.ericsson.oss.air.pm.stats.service.api.KpiExposureService;
import com.ericsson.oss.air.pm.stats.service.facade.KafkaOffsetCheckerFacade;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KafkaKpiStartupServiceMediatorTest {
    @Mock
    KafkaOffsetCheckerFacade kafkaOffsetCheckerFacadeMock;
    @Mock
    KpiExposureService kpiExposureServiceMock;
    @InjectMocks
    KafkaKpiStartupServiceMediator objectUnderTest;

    @Test
    void shouldCompareKafkaOffsetWithDatabase() {
        objectUnderTest.callKafkaStartupRelatedMethods();
        verify(kafkaOffsetCheckerFacadeMock).compareDatabaseToKafka();
        verify(kpiExposureServiceMock).updateExposure();
    }
}