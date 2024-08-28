/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.api.registry;

import java.util.List;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;
import com.ericsson.oss.air.pm.stats.registry.Registry;

import org.springframework.plugin.core.Plugin;

public interface ReadinessLogRegistry extends Registry<KpiType>, Plugin<KpiType> {
    /**
     * Fetch {@link ReadinessLog}s by calculation ID.
     *
     * @param calculationId the id to fetch by.
     * @return {@link List} of {@link ReadinessLog}s
     */
    List<ReadinessLog> findByCalculationId(UUID calculationId);
}
