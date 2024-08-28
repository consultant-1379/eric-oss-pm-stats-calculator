/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.api;

import java.util.List;
import java.util.UUID;
import javax.ejb.Local;

import com.ericsson.oss.air.pm.stats.model.entity.Calculation;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;

@Local
public interface ReadinessLogRepository {

    /**
     * Finds the all {@link ReadinessLog}s by the provided execution groups from the start time specified by complexExecutionGroup.
     * <br>
     * <strong>NOTE</strong>: One {@link Calculation} can have multiple data sources read creating multiple readiness logs.
     *
     * @param complexExecutionGroup determined by the start of the search
     * @param executionGroups       array of execution groups to find the {@link ReadinessLog} latest readiness logs by
     * @return {@link ReadinessLog} latest readiness logs by execution group
     */
    List<ReadinessLog> findLatestReadinessLogsByExecutionGroup(String complexExecutionGroup, List<String> executionGroups);

    /**
     * Fetch {@link ReadinessLog}s by calculation ID.
     *
     * @param calculationId the id to fetch by.
     * @return {@link List} of {@link ReadinessLog}s
     */
    List<ReadinessLog> findByCalculationId(UUID calculationId);
}
