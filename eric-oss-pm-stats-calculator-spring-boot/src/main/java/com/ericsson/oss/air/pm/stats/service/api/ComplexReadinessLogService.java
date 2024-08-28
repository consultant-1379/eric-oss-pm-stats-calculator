/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.api;

import java.util.Collection;
import java.util.UUID;
import javax.ejb.Local;

import com.ericsson.oss.air.pm.stats.service.api.registry.ReadinessLogRegistry;

@Local
public interface ComplexReadinessLogService extends ReadinessLogRegistry {
    /**
     * Persist complex calculation and its related simple readiness logs.
     *
     * @param complexCalculationId  complex calculation id
     * @param simpleExecutionGroups simple group names that are dependencies of the provided complex definition
     */
    void save(UUID complexCalculationId, Collection<String> simpleExecutionGroups);
}
