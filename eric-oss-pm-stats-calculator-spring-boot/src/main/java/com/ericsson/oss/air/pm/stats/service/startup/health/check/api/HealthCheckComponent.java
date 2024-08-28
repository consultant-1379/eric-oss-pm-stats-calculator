/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.startup.health.check.api;

public interface HealthCheckComponent {
    /**
     * Returns the {@link Component}
     *
     * @return The {@link Component}
     */
    Component getComponent();

    /**
     * Called periodically. It checks the health status of the dependent component and updates the
     * service health status accordingly.
     */
    void execute();

    enum Component {
        KPI_DATABASE,
        SPARK_MASTER,
        SCHEMA_REGISTRY,
        KAFKA
    }
}
