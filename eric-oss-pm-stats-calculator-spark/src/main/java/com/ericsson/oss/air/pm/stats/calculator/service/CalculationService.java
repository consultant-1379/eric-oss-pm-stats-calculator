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

import java.time.LocalDateTime;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.CalculationRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalculationService {
    private final SparkServiceImpl sparkService;

    private final CalculationRepository calculationRepository;

    public void updateCurrentCalculationState(final KpiCalculationState kpiCalculationState) {
        final UUID calculationId = sparkService.getCalculationId();
        calculationRepository.updateStateById(kpiCalculationState, calculationId);
    }

    public void updateCurrentCalculationStateAndTime(final KpiCalculationState kpiCalculationState, final LocalDateTime localDateTime) {
        final UUID calculationId = sparkService.getCalculationId();
        calculationRepository.updateStateAndTimeCompletedById(kpiCalculationState, localDateTime, calculationId);
    }
}
