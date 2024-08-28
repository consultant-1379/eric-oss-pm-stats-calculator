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

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Validates a given object.
 *
 * @param <V>
 *         Type of the validated object
 * @param <R>
 *         Type of the response returned on failure
 * @implNote Catches the first validation failure.
 */
@FunctionalInterface
public interface Validator<V, R> {
    ValidatorSupplier<R> on(V toValidate);

    /**
     * Validates a given object.
     *
     * @param predicate
     *         {@link Predicate} to check
     * @param responseSupplier
     *         {@link Supplier} response to return on failure
     * @return Validator
     */
    default Validator<V, R> thenValidate(final Predicate<? super V> predicate, final Supplier<? extends R> responseSupplier) {
        Objects.requireNonNull(predicate, "predicate");
        Objects.requireNonNull(responseSupplier, "responseSupplier");

        return toValidate -> {
            final ValidationResult<R> validationResult = on(toValidate).apply();
            if (validationResult.isInvalid()) {
                return () -> validationResult;
            }

            return () -> predicate.test(toValidate)
                    ? ValidationResult.valid()
                    : ValidationResult.invalid(responseSupplier.get());
        };
    }

    /**
     * Validates a given object.
     *
     * @param validationSupplier
     *         {@link Supplier} containing validation information
     * @return Validator
     */
    default Validator<V, R> thenValidate(final Supplier<Validation<V, R>> validationSupplier) {
        Objects.requireNonNull(validationSupplier, "validationSupplier");

        final Validation<V, R> validation = validationSupplier.get();
        return thenValidate(validation.getPredicate(), validation.getResponseSupplier());
    }

    /**
     * Validates a given object.
     *
     * @param predicate
     *         {@link Predicate} to check
     * @param responseSupplier
     *         {@link Supplier} response to return on failure
     * @param <V>
     *         Type of the validated object
     * @param <R>
     *         Type of the response returned on failure
     * @return Validator
     */
    static <V, R> Validator<V, R> validate(final Predicate<? super V> predicate, final Supplier<? extends R> responseSupplier) {
        Objects.requireNonNull(predicate, "predicate");
        Objects.requireNonNull(responseSupplier, "responseSupplier");

        return toValidate -> () -> predicate.test(toValidate)
                ? ValidationResult.valid()
                : ValidationResult.invalid(responseSupplier.get());
    }

    /**
     * Validates a given object.
     *
     * @param validationSupplier
     *         {@link Supplier} containing validation information
     * @param <V>
     *         Type of the validated object
     * @param <R>
     *         Type of the response returned on failure
     * @return Validator
     */
    static <V, R> Validator<V, R> validate(final Supplier<Validation<V, R>> validationSupplier) {
        Objects.requireNonNull(validationSupplier, "validationSupplier");

        final Validation<V, R> validation = validationSupplier.get();
        return validate(validation.getPredicate(), validation.getResponseSupplier());
    }

    /**
     * Evaluates the validations.
     *
     * @param validationSuppliers
     *         Validations to evaluate1
     * @param <R>
     *         Type of the response returned on failure
     * @return The first failing {@link ValidationResult}
     */
    @SafeVarargs
    static <R> ValidationResult<R> evaluateValidations(final Supplier<ValidationResult<R>>... validationSuppliers) {
        Arrays.stream(validationSuppliers)
              .forEach(supplier -> Objects.requireNonNull(supplier, "supplier"));

        return Stream.of(validationSuppliers)
                     .map(Supplier::get)
                     .filter(ValidationResult::isInvalid)
                     .findFirst()
                     .orElseGet(ValidationResult::valid);
    }

    @FunctionalInterface
    interface ValidatorSupplier<R> extends Supplier<ValidationResult<R>> {
        default ValidationResult<R> apply() {
            return get();
        }
    }
}
