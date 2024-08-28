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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Data(staticConstructor = "of")
public final class JsonPathReference {
    private final Set<JsonPath> jsonPaths = new HashSet<>();
    private final Set<Reference> references = new HashSet<>();

    public void addAllJsonPaths(final Collection<JsonPath> collection) {
        jsonPaths.addAll(collection);
    }

    public void addAllReferences(final Collection<Reference> collection) {
        references.addAll(collection);
    }

}
