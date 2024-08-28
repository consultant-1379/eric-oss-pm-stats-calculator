/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.sqlparser.domain;

import static java.util.Collections.unmodifiableCollection;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;

@EqualsAndHashCode
public final class JsonPath {
    private final Deque<Part> parts = new LinkedList<>();

    private JsonPath(final Collection<Part> parts) {
        this.parts.addAll(parts);
    }

    public static JsonPath of(@NonNull final Collection<String> parts) {
        return new JsonPath(parts.stream().map(Part::of).collect(Collectors.toList()));
    }

    public boolean add(final Part part) {
        return parts.offerLast(part);
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public boolean isEmpty() {
        return parts.isEmpty();
    }

    public Collection<Part> parts() {
        return unmodifiableCollection(parts);
    }

    @Override
    public String toString() {
        return parts.stream().map(Part::name).collect(Collectors.joining("."));
    }

    @Accessors(fluent = true)
    @Data(staticConstructor = "of")
    @ToString(includeFieldNames = false)
    public static final class Part {
        private final String name;
    }
}
