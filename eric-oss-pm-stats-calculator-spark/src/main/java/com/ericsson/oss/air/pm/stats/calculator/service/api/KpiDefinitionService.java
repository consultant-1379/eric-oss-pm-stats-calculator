/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.api;

import java.util.Collection;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;

public interface KpiDefinitionService {
    /**
     * Return the KPI Definitions to be calculated.
     *
     * @return {@link Set} of KPI Definitions to be calculated.
     */
    Set<KpiDefinition> loadDefinitionsToCalculate();

    /**
     * Verifies if the provided KPI Definitions are scheduled or not.
     *
     * @param kpiDefinitions
     *         {@link Collection} of KPI Definitions to check.
     * @return true if the provided KPI Definitions are scheduled otherwise false.
     */
    boolean areScheduled(Collection<? extends KpiDefinition> kpiDefinitions);

    /**
     * Checks if the provided KPI Definitions are type of <strong>Scheduled Simple</strong>.
     *
     * @param definitions
     *         {@link Collection} of KPI Definitions to check.
     * @return true if the provided KPI Definitions are type of <strong>Scheduled Simple</strong> otherwise false.
     */
    boolean areScheduledSimple(Collection<KpiDefinition> definitions);

    /**
     * Checks if the provided KPI Definitions are not type of <strong>Scheduled Simple</strong>.
     *
     * @param definitions
     *         {@link Collection} of KPI Definitions to check.
     * @return true if the provided KPI Definitions are not type of <strong>Scheduled Simple</strong> otherwise false.
     */
    boolean areNonScheduledSimple(Collection<KpiDefinition> definitions);

    /**
     * Checks if the provided aggregation period is default or not.
     * <br>
     * Aggregation period is default if it is equals to <strong>-1</strong>.
     *
     * @param aggregationPeriod
     *         aggregation period to check,
     * @return true if the provided aggregation period is default otherwise false.
     */
    boolean isDefaultAggregationPeriod(Integer aggregationPeriod);
}
