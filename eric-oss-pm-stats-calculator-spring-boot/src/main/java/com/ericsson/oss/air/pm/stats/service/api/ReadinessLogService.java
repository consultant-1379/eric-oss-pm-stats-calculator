/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.api;

import java.util.Collection;
import java.util.Set;
import javax.ejb.Local;

import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;
import com.ericsson.oss.air.pm.stats.service.api.registry.ReadinessLogRegistry;

import lombok.NonNull;

@Local
public interface ReadinessLogService extends ReadinessLogRegistry {
    /**
     * Collects all {@link ReadinessLog}s by the provided {@link ExecutionGroup}s from the start time specified by complexExecutionGroup
     *
     * @param complexExecutionGroup determined by the start of the search
     * @param executionGroups       {@link ExecutionGroup}s to collect {@link ReadinessLog}s for
     * @return {@link ReadinessLog}s for the provided {@link ExecutionGroup}s
     */
    Set<ReadinessLog> collectLatestReadinessLogs(@NonNull String complexExecutionGroup, @NonNull Collection<ExecutionGroup> executionGroups);
}
