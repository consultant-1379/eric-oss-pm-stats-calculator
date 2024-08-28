/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.utils;

import java.util.Collection;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation.KpiCalculationJob;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KpiCalculationJobUtil {

    public static boolean doesContainExecutionGroup(final Collection<KpiCalculationJob> kpiCalculationJobs, final String executionGroup) {
        return kpiCalculationJobs.stream().anyMatch(job -> job.getExecutionGroup().equals(executionGroup));
    }
}