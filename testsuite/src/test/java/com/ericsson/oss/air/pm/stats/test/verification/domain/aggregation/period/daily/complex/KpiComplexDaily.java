/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.daily.complex;

import java.time.LocalDateTime;

import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;
import com.ericsson.oss.air.pm.stats.test.verification.util.StringifyUtils.FieldToInspect;
import com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;

/**
 * Class representing <strong>kpi_complex_1440</strong> table.
 */

@Data
@Builder
public class KpiComplexDaily implements DatabaseRow {

    @FieldToInspect(fieldName = "aggColumn0")
    private final Integer aggColumn0;

    @FieldToInspect(fieldName = "aggColumn1")
    private final Integer aggColumn1;

    @Default
    @FieldToInspect(fieldName = "sumFloat1440Complex")
    private final Float sumFloat1440Complex = 0.2F;

    @Default
    @FieldToInspect(fieldName = "sumInteger1440ComplexNonTriggered")
    private final Integer sumInteger1440ComplexNonTriggered = null;

    @Default
    @FieldToInspect(fieldName = "aggregationBeginTime")
    private final LocalDateTime aggregationBeginTime = TimeUtils.daysAgo(1, 0);

    @Default
    @FieldToInspect(fieldName = "aggregationEndTime")
    private final LocalDateTime aggregationEndTime = TimeUtils.daysAgo(0, 0);

}
