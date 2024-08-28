/*******************************************************************************
 * COPYRIGHT Ericsson 2023
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
 * Class representing <strong>kpi_execution_id_1440</strong> table.
 */
@Data
@Builder
public final class KpiExecutionIdDaily implements DatabaseRow {
    @FieldToInspect(fieldName = "aggColumn0")
    private final Integer aggColumn0;

    @Default
    @FieldToInspect(fieldName = "executionId")
    private final String executionId = "TEST_1";

    @Default
    @FieldToInspect(fieldName = "aggregationBeginTime")
    private final LocalDateTime aggregationBeginTime = TimeUtils.TWO_DAYS_AGO;

    @Default
    @FieldToInspect(fieldName = "aggregationEndTime")
    private final LocalDateTime aggregationEndTime = TimeUtils.A_DAY_AGO;

    @Default
    @FieldToInspect(fieldName = "executionIdSumInteger1440")
    private final Integer executionIdSumInteger1440 = 1;
}
