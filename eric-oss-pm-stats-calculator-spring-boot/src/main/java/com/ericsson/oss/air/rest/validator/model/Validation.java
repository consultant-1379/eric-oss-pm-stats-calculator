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

import java.util.function.Predicate;
import java.util.function.Supplier;

import lombok.Data;

@Data(staticConstructor = "of")
public final class Validation<V, R> {
    private final Predicate<V> predicate;
    private final Supplier<R> responseSupplier;
}
