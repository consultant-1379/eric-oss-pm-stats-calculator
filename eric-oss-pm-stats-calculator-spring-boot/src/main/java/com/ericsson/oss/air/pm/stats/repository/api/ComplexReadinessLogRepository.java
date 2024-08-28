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

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import javax.ejb.Local;

import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;

@Local
public interface ComplexReadinessLogRepository {

    /**
     * Persist complex calculation and its related simple readiness logs.
     *
     * @param connection            {@link Connection} to the database
     * @param complexCalculationId  complex calculation id
     * @param simpleExecutionGroups simple group names that are dependencies of the provided complex definition
     */
    void save(Connection connection, UUID complexCalculationId, Collection<String> simpleExecutionGroups);

    /**
     * Fetch {@link ReadinessLog}s by calculation ID.
     *
     * @param calculationId the id to fetch by.
     * @return {@link List} of {@link ReadinessLog}s
     */
    List<ReadinessLog> findByCalculationId(UUID calculationId);
}
