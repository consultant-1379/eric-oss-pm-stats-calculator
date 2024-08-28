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

import lombok.Data;

@Data
public final class ValidationResult<R> {
    private final boolean isValid;
    private final R errorResponse;

    public static <R> ValidationResult<R> valid() {
        return new ValidationResult<>(true, null);
    }

    public static <R> ValidationResult<R> invalid(final R response) {
        return new ValidationResult<>(false, response);
    }

    public boolean isInvalid() {
        return !isValid;
    }

    public boolean isValid() {
        return isValid;
    }
}
