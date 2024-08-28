/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.hourly.complex;

import java.time.LocalDateTime;

import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;
import com.ericsson.oss.air.pm.stats.test.verification.util.JsonArrayUtils;
import com.ericsson.oss.air.pm.stats.test.verification.util.StringifyUtils.FieldToInspect;

import com.google.gson.JsonArray;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;

 /**
 * Class representing <strong>kpi_complex_60</strong> table.
 */
@Data
@Builder
public final class KpiComplexHourly implements DatabaseRow {
    @FieldToInspect(fieldName = "aggColumn0")
    private final Integer aggColumn0;

    @FieldToInspect(fieldName = "aggColumn1")
    private final Integer aggColumn1;

    @Default
    @FieldToInspect(fieldName = "aggregationBeginTime")
    private final LocalDateTime aggregationBeginTime;

    @Default
    @FieldToInspect(fieldName = "aggregationEndTime")
    private final LocalDateTime aggregationEndTime;

    @FieldToInspect(fieldName = "sumInteger60Complex")
    private final Integer sumInteger60Complex;

    @Default
    @FieldToInspect(fieldName = "sumFloat60Complex")
    private final Float sumFloat60Complex = 0.2F;

    @Default
    @FieldToInspect(fieldName = "integerArrayComplex")
    private final JsonArray integerArrayComplex = JsonArrayUtils.create(1, 2, 3, 4, 5);

    @Default
    @FieldToInspect(fieldName = "sumFloatCount60")
    private final Integer sumFloatCount60 = 1;

    @FieldToInspect(fieldName = "sumIntegerFloatComplex")
    private final Float sumIntegerFloatComplex;
}