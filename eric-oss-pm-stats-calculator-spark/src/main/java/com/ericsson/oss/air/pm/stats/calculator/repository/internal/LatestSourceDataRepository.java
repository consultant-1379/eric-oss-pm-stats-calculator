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

import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.LatestSourceData;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.embedded.id.LatestSourceDataId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

public interface LatestSourceDataRepository extends JpaRepository<LatestSourceData, LatestSourceDataId> {
    @Query("SELECT latestSourceData.latestTimeCollected  " +
           "FROM LatestSourceData latestSourceData " +
           "WHERE latestSourceData.id.source = :source " +
           "  AND latestSourceData.id.aggregationPeriodMinutes = :aggregationPeriodMinutes " +
           "  AND latestSourceData.id.executionGroup = :executionGroup")
    Optional<LocalDateTime> findLastTimeCollected(
            @NonNull @Param("source") String source,
            @NonNull @Param("aggregationPeriodMinutes") Integer aggregationPeriodMinutes,
            @NonNull @Param("executionGroup") String executionGroup
    );
}