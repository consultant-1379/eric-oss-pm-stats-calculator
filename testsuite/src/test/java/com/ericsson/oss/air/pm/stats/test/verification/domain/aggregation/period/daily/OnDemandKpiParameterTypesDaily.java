/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.daily;

import static com.ericsson.oss.air.pm.stats.test.verification.util.StringifyUtils.FieldToInspect;
import static com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils.A_DAY_AGO;
import static com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils.TWO_DAYS_AGO;

import java.time.LocalDateTime;

import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;

import com.google.gson.JsonArray;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;

/**
 * Class representing <strong>kpi_parameter_types_1440</strong> table.
 */
@Data
@Builder
public final class OnDemandKpiParameterTypesDaily implements DatabaseRow {

    @FieldToInspect(fieldName = "aggColumn0")
    private final Integer aggColumn0;

    @Default
    @FieldToInspect(fieldName = "aggregationBeginTime")
    private final LocalDateTime aggregationBeginTime = TWO_DAYS_AGO;

    @Default
    @FieldToInspect(fieldName = "aggregationEndTime")
    private final LocalDateTime aggregationEndTime = A_DAY_AGO;

    @Default
    @FieldToInspect(fieldName = "udfParameter")
    private final Float udfParameter = 2.6F;

    @FieldToInspect(fieldName = "udfTabularParameter")
    private final JsonArray udfTabularParameter;
}
