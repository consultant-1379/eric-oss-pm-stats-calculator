/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation;

import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculation.event.SendingReportToExporterEvent;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExecutionReportEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void pushEvent(final UUID uuid) {
        applicationEventPublisher.publishEvent(SendingReportToExporterEvent.of(uuid));
    }
}