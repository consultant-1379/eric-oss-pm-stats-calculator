/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.repository.internal;

import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.CalculationReliability;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnDemandReliabilityRepository extends JpaRepository<CalculationReliability, Integer> {
}
