/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model;

import java.sql.Timestamp;

import lombok.Data;

/**
 * Parameter object combining start and end time for Postgres offset handling.
 */
@Data(staticConstructor = "of")
public final class AggregationPeriodWindow {
    private final Timestamp start;
    private final Timestamp end;

    public Timestamp getStart() {
        return new Timestamp(start.getTime());
    }

    public Timestamp getEnd() {
        return new Timestamp(end.getTime());
    }
}
