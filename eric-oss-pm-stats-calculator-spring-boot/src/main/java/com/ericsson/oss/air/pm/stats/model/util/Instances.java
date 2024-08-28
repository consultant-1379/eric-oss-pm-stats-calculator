/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.model.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Instances {

    public static <T> List<T> cast(@NonNull final Collection<?> collection, final Class<T> expectedType) {
        return collection.isEmpty()
                ? Collections.emptyList()
                : collection.stream().map(expectedType::cast).collect(Collectors.toList());
    }

    public static <T> boolean isInstanceOf(@NonNull final Collection<?> collection, final Class<T> expectedType) {
        return isNotEmpty(collection) && expectedType.isInstance(Iterables.getFirst(collection, null));
    }

    private static boolean isNotEmpty(final Collection<?> collection) {
        return !collection.isEmpty();
    }
}