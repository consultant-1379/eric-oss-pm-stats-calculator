/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.repository.internal;

import java.util.List;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.DimensionTable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DimensionTablesRepository extends JpaRepository<DimensionTable, Integer> {

    @Query("SELECT dt.tableName FROM DimensionTable dt WHERE dt.calculation.id = :calculationId")
    List<String> findTableNamesByCalculationId(@Param("calculationId") UUID calculationId);
}
