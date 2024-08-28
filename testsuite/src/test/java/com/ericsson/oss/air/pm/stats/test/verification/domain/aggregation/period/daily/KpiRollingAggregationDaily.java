/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.daily;

import java.time.LocalDateTime;

import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;
import com.ericsson.oss.air.pm.stats.test.verification.util.StringifyUtils.FieldToInspect;
import com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;

/**
 * Class representing <strong>kpi_rolling_aggregation_1440</strong> table.
 */
@Data
@Builder
public final class KpiRollingAggregationDaily implements DatabaseRow {
    @FieldToInspect(fieldName = "aggColumn0")
    private final Integer aggColumn0;

    @Default
    @FieldToInspect(fieldName = "aggregationBeginTime")
    private final LocalDateTime aggregationBeginTime = TimeUtils.TWO_DAYS_AGO;

    @Default
    @FieldToInspect(fieldName = "aggregationEndTime")
    private final LocalDateTime aggregationEndTime = TimeUtils.A_DAY_AGO;

    @FieldToInspect(fieldName = "rollingSumInteger1440")
    private final Integer rollingSumInteger1440;

    @FieldToInspect(fieldName = "rollingMaxInteger1440")
    private final Integer rollingMaxInteger1440;

    @Default
    @FieldToInspect(fieldName = "firstFloatOperator1440PostAggregation")
    private final Float firstFloatOperator1440PostAggregation = 1.0F;
}
