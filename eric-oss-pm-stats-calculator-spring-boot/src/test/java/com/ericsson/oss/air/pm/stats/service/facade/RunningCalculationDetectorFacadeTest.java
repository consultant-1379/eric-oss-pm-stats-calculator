/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.facade;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import com.ericsson.oss.air.pm.stats.service.api.CalculationService;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RunningCalculationDetectorFacadeTest {
    @Mock
    KpiDefinitionService kpiDefinitionServiceMock;
    @Mock
    CalculationService calculationServiceMock;

    @InjectMocks
    RunningCalculationDetectorFacade objectUnderTest;

    @Test
    void shouldVerifyIsAnySimpleCalculationRunning(@Mock final Set<String> executionGroupsMock) {
        when(kpiDefinitionServiceMock.findAllSimpleExecutionGroups()).thenReturn(executionGroupsMock);
        when(calculationServiceMock.isAnyCalculationRunning(executionGroupsMock)).thenReturn(true);

        final boolean actual = objectUnderTest.isAnySimpleCalculationRunning();

        verify(kpiDefinitionServiceMock).findAllSimpleExecutionGroups();
        verify(calculationServiceMock).isAnyCalculationRunning(executionGroupsMock);

        Assertions.assertThat(actual).isTrue();
    }

    @Test
    void shouldVerifyIsAnyComplexCalculationRunning(@Mock final Set<String> executionGroupsMock) {
        when(kpiDefinitionServiceMock.findAllComplexExecutionGroups()).thenReturn(executionGroupsMock);
        when(calculationServiceMock.isAnyCalculationRunning(executionGroupsMock)).thenReturn(true);

        final boolean actual = objectUnderTest.isAnyComplexCalculationRunning();

        verify(kpiDefinitionServiceMock).findAllComplexExecutionGroups();
        verify(calculationServiceMock).isAnyCalculationRunning(executionGroupsMock);

        Assertions.assertThat(actual).isTrue();
    }
}