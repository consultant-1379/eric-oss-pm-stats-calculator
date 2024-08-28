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

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;

/**
 * Class representing <strong>kpi_complex_example_1440</strong> table.
 */
@Data
@Builder
public class ComplexExampleDaily implements DatabaseRow {

    @FieldToInspect(fieldName = "nodeFDN")
    private final String nodeFDN;

    @FieldToInspect(fieldName = "moFdn")
    private final String moFdn;

    @FieldToInspect(fieldName = "aggregationBeginTime")
    private final LocalDateTime aggregationBeginTime;

    @FieldToInspect(fieldName = "aggregationEndTime")
    private final LocalDateTime aggregationEndTime;

    @FieldToInspect(fieldName = "max1440Complex")
    private final Float max1440Complex;
}