/*******************************************************************************
 * COPYRIGHT Ericsson 2024
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
import com.ericsson.oss.air.pm.stats.test.verification.util.StringifyUtils.FieldToInspect;

import lombok.Builder;
import lombok.Data;

/**
 * Class representing <strong>kpi_complex_fdn_60</strong> table.
 */
@Data
@Builder
public final class ComplexFdnHourly implements DatabaseRow {

    @FieldToInspect(fieldName = "fdnParseMo")
    private final String fdnParseMo;

    @FieldToInspect(fieldName = "nodeFDN")
    private final String nodeFDN;

    @FieldToInspect(fieldName = "cellLevelAgg")
    private final Long cellLevelAgg;

    @FieldToInspect(fieldName = "aggregationBeginTime")
    private final LocalDateTime aggregationBeginTime;

    @FieldToInspect(fieldName = "aggregationEndTime")
    private final LocalDateTime aggregationEndTime;
}
