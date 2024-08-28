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
 * Class representing <strong>relation_guid_source_guid_target_guid_60_kpis</strong> table.
 */
@Data
@Builder
public final class KpiRelationHourly implements DatabaseRow {
    @FieldToInspect(fieldName = "aggColumn0")
    private final Integer aggColumn0;

    @FieldToInspect(fieldName = "aggColumn1")
    private final Integer aggColumn1;

    @FieldToInspect(fieldName = "aggregationBeginTime")
    private final LocalDateTime aggregationBeginTime;

    @FieldToInspect(fieldName = "aggregationEndTime")
    private final LocalDateTime aggregationEndTime;

    @Default
    @FieldToInspect(fieldName = "firstFloatDivideBy0_60")
    private final Float firstFloatDivideBy0_60 = null;
}
