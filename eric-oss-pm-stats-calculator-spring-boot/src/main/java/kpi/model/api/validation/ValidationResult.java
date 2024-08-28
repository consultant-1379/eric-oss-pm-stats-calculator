/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.api.validation;

import java.util.Objects;
import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValidationResult {
    private static final ValidationResult VALID = new ValidationResult(true, null);

    private final boolean isValid;
    private final Supplier<RuntimeException> exceptionSupplier;

    public static ValidationResult valid() {
        return VALID;
    }

    public static ValidationResult invalid(final String format, final Object... args) {
        return new ValidationResult(false, () -> new IllegalArgumentException(String.format(format, args)));
    }

    public boolean isInvalid() {
        return !isValid;
    }

    public ValidationResult andThen(@NonNull final Supplier<ValidationResult> validationFunction) {
        raiseExceptionIfInvalid();

        return validationFunction.get();
    }

    public ValidationResult andIfNotNullThen(final Object value, @NonNull final Supplier<ValidationResult> validationFunction) {
        raiseExceptionIfInvalid();

        return Objects.isNull(value) ? valid() : validationFunction.get();
    }

    public RuntimeException exception() {
        return exceptionSupplier.get();
    }

    private void raiseExceptionIfInvalid() {
        if (isInvalid()) {
            throw exception();
        }
    }
}
