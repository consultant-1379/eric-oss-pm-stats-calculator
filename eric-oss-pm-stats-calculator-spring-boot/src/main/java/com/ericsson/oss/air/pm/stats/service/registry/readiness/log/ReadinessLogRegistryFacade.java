/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.registry.readiness.log;

import java.util.List;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;
import com.ericsson.oss.air.pm.stats.service.api.registry.ReadinessLogRegistry;

public interface ReadinessLogRegistryFacade {

    List<ReadinessLog> findByCalculationId(final UUID calculationId);

    ReadinessLogRegistry readinessLogRegistry(final UUID calculationId);
}
