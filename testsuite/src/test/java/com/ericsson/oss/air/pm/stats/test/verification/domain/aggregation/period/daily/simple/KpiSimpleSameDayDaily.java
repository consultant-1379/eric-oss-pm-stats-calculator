/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.daily.simple;

import java.time.LocalDateTime;

import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;
import com.ericsson.oss.air.pm.stats.test.verification.util.StringifyUtils.FieldToInspect;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;

/**
 * Class representing <strong>kpi_simple_same_day_60</strong> table.
 */
@Data
@Builder
public class KpiSimpleSameDayDaily implements DatabaseRow {
    @FieldToInspect(fieldName = "aggColumn0")
    private final Integer aggColumn0;

    @FieldToInspect(fieldName = "aggColumn1")
    private final Integer aggColumn1;

    @Default
    @FieldToInspect(fieldName = "floatSimpleSameDay")
    private final Float floatSimpleSameDay = 0.2F;

    @Default
    @FieldToInspect(fieldName = "integerSimpleSameDay")
    private final Integer integerSimpleSameDay = 1;

    @FieldToInspect(fieldName = "aggregationBeginTime")
    private final LocalDateTime aggregationBeginTime;

    @FieldToInspect(fieldName = "aggregationEndTime")
    private final LocalDateTime aggregationEndTime;


}
