/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class DefinitionErrorResponseTest {

    private static final String ERROR_CAUSE_1 = "errorCause1";

    @Test
    void shouldConstructObject() {
        final Definition definition = new Definition();

        final DefinitionErrorResponse definitionErrorResponse = new DefinitionErrorResponse(ERROR_CAUSE_1, definition);

        Assertions.assertThat(definitionErrorResponse.getDefinition()).isEqualTo(definition);
        Assertions.assertThat(definitionErrorResponse.getErrorCauses()).containsExactly(ERROR_CAUSE_1);
    }

    @Test
    void shouldAddErrorCause() {
        final String errorCause2 = "errorCause2";
        final Definition definition = new Definition();

        final DefinitionErrorResponse definitionErrorResponse = new DefinitionErrorResponse(ERROR_CAUSE_1, definition);
        definitionErrorResponse.addErrorCause(errorCause2);

        Assertions.assertThat(definitionErrorResponse.getDefinition()).isEqualTo(definition);
        Assertions.assertThat(definitionErrorResponse.getErrorCauses()).containsExactly(ERROR_CAUSE_1, errorCause2);
    }
}