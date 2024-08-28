/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.repository.internal;

import java.util.Optional;

import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.KpiExecutionGroup;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecutionGroupRepository extends JpaRepository<KpiExecutionGroup, Integer> {

    Optional<KpiExecutionGroup> findByExecutionGroup(String executionGroup);
}
