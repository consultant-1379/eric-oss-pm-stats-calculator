/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service;

import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.FINISHED;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.CalculationRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CalculationServiceTest {
    @Mock SparkServiceImpl sparkServiceMock;
    @Mock CalculationRepository calculationRepositoryMock;

    @InjectMocks CalculationService objectUnderTest;

    @Test
    void updateCurrentCalculationState() {
        final UUID calculationId = UUID.fromString("802ea9db-e49b-41ec-8a8f-ae0a9fa340da");

        when(sparkServiceMock.getCalculationId()).thenReturn(calculationId);

        objectUnderTest.updateCurrentCalculationState(FINISHED);

        verify(sparkServiceMock).getCalculationId();
        verify(calculationRepositoryMock).updateStateById(FINISHED, calculationId);
    }

    @Test
    void updateCurrentCalculationStateAndTimeCompleted() {
        final UUID calculationId = UUID.fromString("802ea9db-e49b-41ec-8a8f-ae0a9fa340da");
        when(sparkServiceMock.getCalculationId()).thenReturn(calculationId);

        LocalDateTime updatedTime = LocalDateTime.of(LocalDate.of(2_022, Month.JUNE, 1), LocalTime.NOON);
        objectUnderTest.updateCurrentCalculationStateAndTime(KpiCalculationState.NOTHING_CALCULATED, updatedTime);

        verify(sparkServiceMock).getCalculationId();
        verify(calculationRepositoryMock).updateStateAndTimeCompletedById(KpiCalculationState.NOTHING_CALCULATED, updatedTime, calculationId);
    }
}