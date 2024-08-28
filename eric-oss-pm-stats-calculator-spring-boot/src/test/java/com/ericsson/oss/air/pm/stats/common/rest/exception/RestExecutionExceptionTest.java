/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.rest.exception;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class RestExecutionExceptionTest {
    @Test
    void shouldCreateRestExecutionException() {
        final RuntimeException cause = new RuntimeException();
        final RestExecutionException restExecutionException = new RestExecutionException(cause);

        Assertions.assertThat(restExecutionException.getStatusCode()).isEqualTo(-1);
    }

    @Test
    void shouldCreateRestExecutionExceptionWithMessage() {
        final String message = "Test message";
        final RestExecutionException restExecutionException = new RestExecutionException(message);

        Assertions.assertThat(restExecutionException.getStatusCode()).isEqualTo(-1);
    }
}