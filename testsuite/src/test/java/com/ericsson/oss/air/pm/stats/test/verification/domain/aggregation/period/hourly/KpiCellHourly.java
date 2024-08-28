/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.hourly;

import java.time.LocalDateTime;

import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;
import com.ericsson.oss.air.pm.stats.test.verification.util.StringifyUtils.FieldToInspect;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;

/**
 * Class representing <strong>kpi_cell_guid_60</strong> table.
 */
@Data
@Builder
public final class KpiCellHourly implements DatabaseRow {
    @FieldToInspect(fieldName = "aggColumn0")
    private final Integer aggColumn0;

    @FieldToInspect(fieldName = "aggregationBeginTime")
    private final LocalDateTime aggregationBeginTime;

    @FieldToInspect(fieldName = "aggregationEndTime")
    private final LocalDateTime aggregationEndTime;

    @Default
    @FieldToInspect(fieldName = "firstIntegerOperator60Stage2")
    private final Float firstIntegerOperator60Stage2 = 5F;

    @Default
    @FieldToInspect(fieldName = "firstIntegerOperator60Stage3")
    private final Float firstIntegerOperator60Stage3 = 0.5F;

    @Default
    @FieldToInspect(fieldName = "firstIntegerOperator60Stage4")
    private final Float firstIntegerOperator60Stage4 = 4.5F;
}
