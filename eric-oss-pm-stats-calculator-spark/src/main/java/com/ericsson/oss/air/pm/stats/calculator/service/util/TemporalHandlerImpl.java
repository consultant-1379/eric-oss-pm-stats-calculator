/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import com.ericsson.oss.air.pm.stats.calculator.configuration.property.CalculationProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TemporalHandlerImpl {
    private final CalculationProperties calculationProperties;

    public Timestamp getOffsetTimestamp() {
        final Instant nowTruncatedToMinutes = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        final long incrementToMillis = nowTruncatedToMinutes.toEpochMilli() % TimeUnit.MINUTES.toMillis(calculationProperties.getScheduleIncrement().toMinutes());

        return new Timestamp(
                nowTruncatedToMinutes.minus(calculationProperties.getEndOfExecutionOffset())
                                     .minusMillis(incrementToMillis)
                                     .minusMillis(10)
                                     .toEpochMilli()
        );
    }
}
