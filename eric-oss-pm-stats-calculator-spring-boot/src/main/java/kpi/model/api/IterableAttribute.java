/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.api;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.common.model.attribute.Attribute;

public interface IterableAttribute<T> extends Attribute<List<T>>, Iterable<T> {
    @Override
    default Iterator<T> iterator() {
        final List<T> value = value();
        return value.iterator();
    }

    default boolean isEmpty() {
        final List<T> value = value();
        return value.isEmpty();
    }

    default boolean isNotEmpty() {
        return !isEmpty();
    }

    default Stream<T> stream() {
        return value().stream();
    }
}
