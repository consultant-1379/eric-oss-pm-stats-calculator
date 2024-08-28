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

import static lombok.AccessLevel.PUBLIC;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.model.entity.CalculationReliability;
import com.ericsson.oss.air.pm.stats.repository.api.CalculationReliabilityRepository;
import com.ericsson.oss.air.pm.stats.repository.util.transaction.TransactionExecutor;
import com.ericsson.oss.air.pm.stats.service.api.CalculationReliabilityService;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class CalculationReliabilityServiceImpl implements CalculationReliabilityService {

    @Inject
    private CalculationReliabilityRepository calculationReliabilityRepository;

    @Override
    public Map<String, LocalDateTime> findReliabilityThresholdByCalculationId(final UUID calculationId) {
        return calculationReliabilityRepository.findReliabilityThresholdByCalculationId(calculationId);
    }

    @Override
    public Map<String, LocalDateTime> findStartTimeByCalculationId(final UUID calculationId) {
        return calculationReliabilityRepository.findCalculationStartByCalculationId(calculationId);
    }

    @Override
    public Map<String, LocalDateTime> findMaxReliabilityThresholdByKpiName() {
        return calculationReliabilityRepository.findMaxReliabilityThresholdByKpiName();
    }

    @Override
    public void save(final List<CalculationReliability> calculationReliabilities) {
        TransactionExecutor.executeInTransactionSilently(connection -> calculationReliabilityRepository.save(
                connection,
                calculationReliabilities));
    }
}
