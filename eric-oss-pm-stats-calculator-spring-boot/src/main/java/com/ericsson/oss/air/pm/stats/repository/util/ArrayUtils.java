/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util;

import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArrayUtils {
    public static Array successfullyFinishedStates(@NonNull final Connection connection) throws SQLException {
        return connection.createArrayOf(
                "VARCHAR",
                KpiCalculationState.getSuccessfulDoneKpiCalculationStates().stream().map(KpiCalculationState::name).toArray()
        );
    }
}
