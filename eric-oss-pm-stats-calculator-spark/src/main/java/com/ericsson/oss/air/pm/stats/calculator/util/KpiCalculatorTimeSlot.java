/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.util;

import java.sql.Timestamp;

/**
 * Time slot for KPI calculation defining the start and end time for the calculation period.
 */
public class KpiCalculatorTimeSlot {
    private final Timestamp startTimestamp;
    private final Timestamp endTimestamp;

    public KpiCalculatorTimeSlot(final Timestamp startTimestamp, final Timestamp endTimestamp) {
        this.startTimestamp = Timestamps.copy(startTimestamp);
        this.endTimestamp = Timestamps.copy(endTimestamp);
    }

    public Timestamp getStartTimestamp() {
        return Timestamps.copy(startTimestamp);
    }

    public Timestamp getEndTimestamp() {
        return Timestamps.copy(endTimestamp);
    }
}
