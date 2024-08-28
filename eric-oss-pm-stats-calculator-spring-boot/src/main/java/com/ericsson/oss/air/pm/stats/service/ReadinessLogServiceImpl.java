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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;
import com.ericsson.oss.air.pm.stats.repository.api.ReadinessLogRepository;
import com.ericsson.oss.air.pm.stats.service.api.ReadinessLogService;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Stateless
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class ReadinessLogServiceImpl implements ReadinessLogService {
    @Inject
    private ReadinessLogRepository readinessLogRepository;

    @Override
    public boolean doesSupport(final KpiType value) {
        return value == KpiType.SCHEDULED_SIMPLE;
    }

    @Override
    public Set<ReadinessLog> collectLatestReadinessLogs(@NonNull final String complexExecutionGroup,
                                                        @NonNull final Collection<ExecutionGroup> executionGroups) {
        return new HashSet<>(readinessLogRepository.findLatestReadinessLogsByExecutionGroup(
                complexExecutionGroup,
                executionGroups.stream().map(ExecutionGroup::name).collect(Collectors.toList())));
    }

    @Override
    public List<ReadinessLog> findByCalculationId(final UUID calculationId) {
        return readinessLogRepository.findByCalculationId(calculationId);
    }

    @Override
    public boolean supports(final KpiType value) {
        return doesSupport(value);
    }
}
