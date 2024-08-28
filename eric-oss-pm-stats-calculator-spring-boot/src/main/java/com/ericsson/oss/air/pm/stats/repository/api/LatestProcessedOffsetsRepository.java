/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.api;

import java.util.Collection;
import java.util.List;
import javax.ejb.Local;

import com.ericsson.oss.air.pm.stats.model.entity.LatestProcessedOffset;

@Local
public interface LatestProcessedOffsetsRepository {

    /**
     * Finds all {@link LatestProcessedOffset}.
     *
     * @return the list of {@link LatestProcessedOffset}s
     */
    List<LatestProcessedOffset> findAll();

    /**
     * Finds all {@link LatestProcessedOffset} for an execution group.
     *
     * @return the list of {@link LatestProcessedOffset}s
     */
    List<LatestProcessedOffset> findAllForExecutionGroup(String executionGroup);

    /**
     * Deletes all entry which contains the given topics.
     *
     * @param topics the list of topic names needs to be truncated
     */
    void deleteOffsetsByTopic(Collection<String> topics);
}
