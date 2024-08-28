/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.model;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.Queue;

import com.ericsson.oss.air.pm.stats.calculator.util.KpiCalculatorTimeSlot;
import com.ericsson.oss.air.pm.stats.calculator.util.TimeUtils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data(staticConstructor = "of")
public final class CalculationTimeWindow {
    private final Timestamp start;
    private final Timestamp end;

    public Timestamp getStart() {
        return new Timestamp(start.getTime());
    }

    public Timestamp getEnd() {
        return new Timestamp(end.getTime());
    }

    public Queue<KpiCalculatorTimeSlot> calculateTimeSlots(final int aggregationPeriod) {
        return start.after(end)
                ? new LinkedList<>()
                : new LinkedList<>(TimeUtils.getKpiCalculationTimeSlots(getStart(), getEnd(), aggregationPeriod));
    }
}
