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

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;
import com.ericsson.oss.air.pm.stats.repository.api.ComplexReadinessLogRepository;
import com.ericsson.oss.air.pm.stats.repository.util.transaction.TransactionExecutor;
import com.ericsson.oss.air.pm.stats.service.api.ComplexReadinessLogService;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Stateless
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class ComplexReadinessLogServiceImpl implements ComplexReadinessLogService {
    @Inject
    private ComplexReadinessLogRepository complexReadinessLogRepository;

    @Override
    public boolean doesSupport(final KpiType value) {
        return value == KpiType.SCHEDULED_COMPLEX;
    }

    @Override
    public void save(final UUID complexCalculationId, final Collection<String> simpleExecutionGroups) {
        TransactionExecutor.executeSilently(connection -> complexReadinessLogRepository.save(
                connection,
                complexCalculationId,
                simpleExecutionGroups
        ));
    }

    @Override
    public List<ReadinessLog> findByCalculationId(final UUID calculationId) {
        return complexReadinessLogRepository.findByCalculationId(calculationId);
    }

    @Override
    public boolean supports(final KpiType value) {
        return doesSupport(value);
    }
}
