/*******************************************************************************
 * COPYRIGHT Ericsson 2023
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
import com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;

/**
 * Class representing <strong>kpi_cell_guid_simple_1440</strong> table.
 */
@Data
@Builder
public final class KpiCellSimpleDaily implements DatabaseRow {
    @FieldToInspect(fieldName = "nodeFDN")
    private final Integer nodeFDN;

    @Default
    @FieldToInspect(fieldName = "aggregationBeginTime")
    private final LocalDateTime aggregationBeginTime = TimeUtils.TWO_DAYS_AGO;

    @Default
    @FieldToInspect(fieldName = "aggregationEndTime")
    private final LocalDateTime aggregationEndTime = TimeUtils.A_DAY_AGO;

    @Default
    @FieldToInspect(fieldName = "sumInteger1440Simple")
    private final Integer sumInteger1440Simple = 1;

    @Default
    @FieldToInspect(fieldName = "sumIntegerArrayIndex1440Simple")
    private final Integer sumIntegerArrayIndex1440Simple = 2;

}
