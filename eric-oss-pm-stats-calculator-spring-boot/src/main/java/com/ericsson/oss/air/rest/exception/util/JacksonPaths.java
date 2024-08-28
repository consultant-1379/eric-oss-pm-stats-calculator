/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.exception.util;

import java.util.Deque;
import java.util.LinkedList;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JacksonPaths {

    public static String normalizePath(@NonNull final JsonMappingException jsonMappingException) {
        final Deque<String> pathParts = new LinkedList<>();

        for (final Reference reference : jsonMappingException.getPath()) {
            final String fieldName = reference.getFieldName();

            if (fieldName == null) {
                /* Field is a Collection */
                final int collectionIndex = reference.getIndex();
                final String collectionName = pathParts.removeLast();
                pathParts.add(String.format("%s[%s]", collectionName, collectionIndex));
            } else {
                pathParts.add(fieldName);
            }
        }

        return String.join(" -> ", pathParts);
    }

}
