/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.scheduler;

import static lombok.AccessLevel.PUBLIC;

import java.util.UUID;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.scheduler.api.KpiCalculationMediator;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Stateless
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class KpiCalculationMediatorImpl implements KpiCalculationMediator {

    @Inject
    private KpiCalculationExecutionController kpiCalculationExecutionController;

    @Override
    public void removeRunningCalculation(final UUID calculationId) {
        kpiCalculationExecutionController.removeRunningCalculationAndStartNext(calculationId);
    }
}
