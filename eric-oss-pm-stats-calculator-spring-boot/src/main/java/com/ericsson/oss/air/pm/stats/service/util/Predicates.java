/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.util;

import java.util.Collection;
import java.util.function.Predicate;

import com.google.common.collect.Iterables;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Predicates {

    public static <T> Predicate<T> isMissing(final Collection<T> collection) {
        return not(contains(collection));
    }

    public static <T> Predicate<T> not(@NonNull final Predicate<T> predicate) {
        return predicate.negate();
    }

    private static @NonNull <T> Predicate<T> contains(final Collection<T> collection) {
        return element -> Iterables.contains(collection, element);
    }
}
