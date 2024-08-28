/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.api;

import java.util.Optional;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.model.entity.Metric;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetricRepository extends JpaRepository<Metric, UUID> {

    /**
     * Finds a metric by its name.
     *
     * @param name Name of the metric to find.
     * @return Metric with the given name.
     */
    Optional<Metric> findByName(String name);
}
