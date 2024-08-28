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

import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;

class KpiDefinitionValidationExceptionTest {

    @Test
    void testConflict() {
        String errorMessage = "This KPI definition conflicts with an existing one.";
        KpiDefinitionValidationException exception = KpiDefinitionValidationException.conflict(errorMessage);
        assertThat(exception.getMessage()).isEqualTo(errorMessage);
        assertThat(exception.getStatusType()).isEqualTo(Status.CONFLICT);
    }

    @Test
    void testBadRequest() {
        String errorMessage = "The request to create a KPI definition is invalid.";
        KpiDefinitionValidationException exception = KpiDefinitionValidationException.badRequest(errorMessage);
        assertThat(exception.getMessage()).isEqualTo(errorMessage);
        assertThat(exception.getStatusType()).isEqualTo(Status.BAD_REQUEST);
    }
}
