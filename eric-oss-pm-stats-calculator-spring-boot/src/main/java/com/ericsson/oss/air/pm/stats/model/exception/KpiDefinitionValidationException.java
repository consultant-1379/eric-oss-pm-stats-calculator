/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.model.exception;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import lombok.Getter;

@Getter
public class KpiDefinitionValidationException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final transient StatusType statusType;

    private KpiDefinitionValidationException(final String message, final StatusType statusType) {
        super(message);
        this.statusType = statusType;
    }

    public static KpiDefinitionValidationException conflict(final String message) {
        return new KpiDefinitionValidationException(message, Status.CONFLICT);
    }

    public static KpiDefinitionValidationException badRequest(final String message) {
        return new KpiDefinitionValidationException(message, Status.BAD_REQUEST);
    }
}