/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.bragent.model;

import lombok.Data;

@Data(staticConstructor = "of")
public final class AgentReturnState {
    private final boolean isSuccessful;
    private final String message;

    public static AgentReturnState success(final String message) {
        return of(true, message);
    }

    public static AgentReturnState failure(final String message) {
        return of(false, message);
    }
}

