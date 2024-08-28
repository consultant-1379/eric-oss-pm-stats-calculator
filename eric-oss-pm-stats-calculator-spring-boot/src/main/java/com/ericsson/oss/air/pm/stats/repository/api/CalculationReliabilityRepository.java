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

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ejb.Local;

import com.ericsson.oss.air.pm.stats.model.entity.CalculationReliability;

@Local
public interface CalculationReliabilityRepository {

    /**
     * Finds the reliability threshold for every on demand and complex definition in the calculation.
     *
     * @param calculationId The calculation id of the on demand calculation.
     * @return {@link Map} of the kpi definition names and its reliabilities
     */
    Map<String, LocalDateTime> findReliabilityThresholdByCalculationId(UUID calculationId);

    /**
     * Finds the calculation start for every on demand and complex definition in the calculation.
     *
     * @param calculationId The calculation id of the on demand calculation.
     * @return {@link Map} of the kpi definition names and its start times
     */
    Map<String, LocalDateTime> findCalculationStartByCalculationId(UUID calculationId);

    /**
     * Finds the max reliability threshold for every on demand and complex definition across all calculation.
     *
     * @return {@link Map} of the kpi definition names and its reliabilities
     */
    Map<String, LocalDateTime> findMaxReliabilityThresholdByKpiName();


    /**
     * Persist complex calculation reliabilities.
     *
     * @param connection             {@link Connection} to the database
     * @param calculationReliability {@link List} of calculation reliabilities
     */
    void save(Connection connection, List<CalculationReliability> calculationReliability);
}
