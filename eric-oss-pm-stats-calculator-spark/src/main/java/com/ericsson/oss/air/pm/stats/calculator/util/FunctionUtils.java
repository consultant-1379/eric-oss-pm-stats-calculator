/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.util;


import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FunctionUtils {

    /**
     * Utility function to reduce necessity to create collection for ignored input {@link Function} typically used part of the
     * {@link java.util.Map#computeIfAbsent(Object, Function)} method call.
     *
     * @param collectionFactory
     *         {@link Supplier} providing new a new empty {@link Collection}
     * @param <I>
     *         type of the input for the {@link Function}.
     * @param <E>
     *         type of the elements contained by the returned collection.
     * @param <C>
     *         type of the collection
     * @return {@link Function} to construct a {@link Collection}
     */
    public static <I, E, C extends Collection<E>> Function<I, C> newCollection(@NonNull final Supplier<? extends C> collectionFactory) {
        return input -> collectionFactory.get();
    }

}
