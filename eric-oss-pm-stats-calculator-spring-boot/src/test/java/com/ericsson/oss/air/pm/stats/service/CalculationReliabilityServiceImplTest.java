/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.model.entity.CalculationReliability;
import com.ericsson.oss.air.pm.stats.repository.api.CalculationReliabilityRepository;
import com.ericsson.oss.air.pm.stats.test_utils.DriverManagerMock;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CalculationReliabilityServiceImplTest {
    static final UUID CALCULATION_ID = UUID.fromString("b10de8fb-2417-44cd-802b-19e0b13fd3a5");
    static final String KPI_NAME = "kpi";
    static final LocalDateTime EARLIEST = LocalDateTime.of(2022, Month.SEPTEMBER, 26, 5, 0, 0);
    static final LocalDateTime LATEST = LocalDateTime.of(2022, Month.SEPTEMBER, 26, 6, 0, 0);

    @Mock
    CalculationReliabilityRepository calculationReliabilityRepositoryMock;

    @InjectMocks
    CalculationReliabilityServiceImpl objectUnderTest;

    @Test
    void shouldReturnCorrectReliabilityThreshold() {
        doReturn(Map.of(KPI_NAME, LATEST)).when(calculationReliabilityRepositoryMock).findReliabilityThresholdByCalculationId(CALCULATION_ID);

        final Map<String, LocalDateTime> actual = objectUnderTest.findReliabilityThresholdByCalculationId(CALCULATION_ID);

        verify(calculationReliabilityRepositoryMock).findReliabilityThresholdByCalculationId(CALCULATION_ID);

        Assertions.assertThat(actual).containsExactly(Map.entry(KPI_NAME, LATEST));
    }

    @Test
    void shouldReturnCorrectStartTime() {
        doReturn(Map.of(KPI_NAME, EARLIEST)).when(calculationReliabilityRepositoryMock).findCalculationStartByCalculationId(CALCULATION_ID);

        final Map<String, LocalDateTime> actual = objectUnderTest.findStartTimeByCalculationId(CALCULATION_ID);

        verify(calculationReliabilityRepositoryMock).findCalculationStartByCalculationId(CALCULATION_ID);

        Assertions.assertThat(actual).containsExactly(Map.entry(KPI_NAME, EARLIEST));
    }

    @Test
    void shouldGiveBackReliabilityThresholdsByKpiDefinition(@Mock final Map<String, LocalDateTime> reliabilitiesByKpiNameMock) {
        when(calculationReliabilityRepositoryMock.findMaxReliabilityThresholdByKpiName()).thenReturn(reliabilitiesByKpiNameMock);

        final Map<String, LocalDateTime> actual = objectUnderTest.findMaxReliabilityThresholdByKpiName();

        verify(calculationReliabilityRepositoryMock).findMaxReliabilityThresholdByKpiName();

        Assertions.assertThat(actual).isEqualTo(reliabilitiesByKpiNameMock);
    }

    @Test
    void shouldSave(@Mock final List<CalculationReliability> calculationReliabilitiesMock) {
        DriverManagerMock.prepare(connectionMock -> {
            objectUnderTest.save(calculationReliabilitiesMock);
            verify(calculationReliabilityRepositoryMock).save(connectionMock, calculationReliabilitiesMock);
        });
    }
}
