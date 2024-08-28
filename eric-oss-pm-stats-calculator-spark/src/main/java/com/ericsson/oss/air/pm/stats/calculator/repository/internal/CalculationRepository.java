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

import java.time.LocalDateTime;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.Calculation;
import com.ericsson.oss.air.pm.stats.calculator.repository.custom.ForceFetchRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

public interface CalculationRepository extends ForceFetchRepository<Calculation, UUID> {
    @Modifying
    @Transactional
    @Query("UPDATE Calculation c SET c.state = :kpiCalculationState WHERE c.id = :id")
    void updateStateById(@NonNull @Param("kpiCalculationState") KpiCalculationState kpiCalculationState, @NonNull @Param("id") UUID id);

    @Modifying
    @Transactional
    @Query("UPDATE Calculation c SET c.state = :kpiCalculationState, c.timeCompleted = :timeCompleted WHERE c.id = :id")
    void updateStateAndTimeCompletedById(@NonNull @Param("kpiCalculationState") KpiCalculationState kpiCalculationState,
                                         @NonNull @Param("timeCompleted") LocalDateTime now,
                                         @NonNull @Param("id") UUID calculationId);
}