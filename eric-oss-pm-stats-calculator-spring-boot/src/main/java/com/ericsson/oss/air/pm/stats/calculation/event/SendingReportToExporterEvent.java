/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.event;

import java.util.UUID;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Data(staticConstructor = "of")
public class SendingReportToExporterEvent {
    private final UUID calculationId;
}
