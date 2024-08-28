/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.validator.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ValidationResultTest {
    @Test
    void shouldReturnValidValidationResult() {
        final ValidationResult<Object> objectUnderTest = ValidationResult.valid();

        Assertions.assertThat(objectUnderTest.isValid()).isTrue();
        Assertions.assertThat(objectUnderTest.getErrorResponse()).isNull();
    }

    @Test
    void shouldReturnInvalidValidationResult() {
        final Object response = new Object();
        final ValidationResult<Object> objectUnderTest = ValidationResult.invalid(response);

        Assertions.assertThat(objectUnderTest.isInvalid()).isTrue();
        Assertions.assertThat(objectUnderTest.getErrorResponse()).isEqualTo(response);
    }
}