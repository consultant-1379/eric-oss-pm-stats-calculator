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

import java.util.Collections;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class DefinitionNameErrorResponseTest {

    @Test
    void shouldConstructObject() {
        final String definitionName = "definitionName";
        final String errorCause = "errorCause";

        final DefinitionNameErrorResponse definitionNameErrorResponse = new DefinitionNameErrorResponse(Collections.singletonList(errorCause), definitionName);

        Assertions.assertThat(definitionNameErrorResponse.getDefinitionName()).isEqualTo(definitionName);
        Assertions.assertThat(definitionNameErrorResponse.getErrorCauses()).containsExactly(errorCause);
    }
}