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
import java.util.Optional;

import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.ReadinessLog;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReadinessLogRepository extends JpaRepository<ReadinessLog, Integer> {
    @Query("SELECT max(readiness.latestCollectedData) " +
            "FROM ReadinessLog readiness " +
            "INNER JOIN readiness.kpiCalculationId calc " +
            "WHERE readiness.datasource = :datasource " +
            "AND calc.executionGroup = :executionGroup")
    Optional<LocalDateTime> findLatestCollectedTimeByDataSourceAndExecutionGroup(
            @NonNull @Param("datasource") String datasource,
            @NonNull @Param("executionGroup") String executionGroup
    );
}
