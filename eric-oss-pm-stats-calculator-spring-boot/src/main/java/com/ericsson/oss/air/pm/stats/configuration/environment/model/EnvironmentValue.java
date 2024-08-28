/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.configuration.environment.model;

import java.util.NoSuchElementException;
import java.util.Objects;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Data(staticConstructor = "of")
@EqualsAndHashCode(doNotUseGetters = true)
public final class EnvironmentValue<T> {
    private static final EnvironmentValue<?> EMPTY = new EnvironmentValue<>(null);

    private final T value;

    public T value() {
        if (isPresent()) {
            return value;
        }
        throw new NoSuchElementException("value is not present");
    }

    public static <T> EnvironmentValue<T> ofNullable(final T value) {
        return Objects.isNull(value)
                ? empty()
                : of(value);
    }

    public boolean isPresent() {
        return !isMissing();
    }

    public boolean isMissing() {
        return equals(EMPTY);
    }

    private static <T> EnvironmentValue<T> empty() {
        @SuppressWarnings("unchecked") final EnvironmentValue<T> empty = (EnvironmentValue<T>) EMPTY;
        return empty;
    }
}
