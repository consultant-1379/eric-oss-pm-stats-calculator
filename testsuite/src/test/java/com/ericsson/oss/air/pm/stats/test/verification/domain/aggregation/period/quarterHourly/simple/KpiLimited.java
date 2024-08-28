/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.quarterHourly.simple;

import java.time.LocalDateTime;

import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;
import com.ericsson.oss.air.pm.stats.test.verification.util.JsonArrayUtils;
import com.ericsson.oss.air.pm.stats.test.verification.util.StringifyUtils.FieldToInspect;

import com.google.gson.JsonArray;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;

/**
 * Class representing <strong>kpi_limited_15</strong> table.
 */
@Data
@Builder
public class KpiLimited implements DatabaseRow {
    @FieldToInspect(fieldName = "ossID")
    private final Integer ossID;

    @FieldToInspect(fieldName = "aggregationBeginTime")
    private final LocalDateTime aggregationBeginTime;

    @FieldToInspect(fieldName = "aggregationEndTime")
    private final LocalDateTime aggregationEndTime;

    @FieldToInspect(fieldName = "sumInteger15")
    private final Integer sumInteger15;

    @Default
    @FieldToInspect(fieldName = "countFloat15")
    private final Integer countFloat15 = 3;

    @Default
    @FieldToInspect(fieldName = "transformArray15")
    private final JsonArray transformArray15 = JsonArrayUtils.create(8, 16, 24, 32, 40);

    @Default
    @FieldToInspect(fieldName = "notCalculatedSimple15")
    private final Integer notCalculatedSimple15 = null;
}