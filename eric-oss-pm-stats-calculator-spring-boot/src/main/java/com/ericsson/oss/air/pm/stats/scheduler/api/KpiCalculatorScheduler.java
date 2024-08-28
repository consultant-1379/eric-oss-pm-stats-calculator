/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.scheduler.api;

import javax.ejb.Local;

import com.ericsson.oss.air.pm.stats.common.scheduler.ActivitySchedulerException;

/**
 * Exposes methods related to the scheduling of <code>eric-oss-pm-stats-calculator</code>.
 */
@Local
public interface KpiCalculatorScheduler {

    /**
     * Schedule KPI calculation.
     *
     * @param executionPeriod the executionPeriod for KPI calculation.
     * @throws ActivitySchedulerException thrown if the Activity cannot be scheduled
     */
    void scheduleKpiCalculation(String executionPeriod) throws ActivitySchedulerException;
}
