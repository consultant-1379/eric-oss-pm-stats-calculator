/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.schema.util;

import static lombok.AccessLevel.PRIVATE;
import static org.apache.avro.Schema.Type.RECORD;
import static org.apache.avro.Schema.Type.UNION;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.JsonPath;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.JsonPath.Part;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;

@NoArgsConstructor(access = PRIVATE)
public final class Schemas {

    public static List<Field> computeFields(@NonNull final Schema schema) {
        final List<Field> fields = new ArrayList<>();

        for (final Field field : schema.getFields()) {
            if (field.schema().getType() == RECORD) {
                fields.addAll(computeFields(field.schema()));
            } else {
                fields.add(field);
            }
        }

        return fields;
    }

    public static boolean isPathContained(final Schema schema, @NonNull final JsonPath jsonPath) {
        if (jsonPath.isNotEmpty()) {
            final Deque<Part> parts = new LinkedList<>(jsonPath.parts());
            final Part schemaPart = parts.removeFirst();
            return schemaPart.name().equals(schema.getName()) && consumeParts(schema, parts);
        }

        return false;
    }

    public static boolean consumeParts(final Schema schema, @NonNull final Deque<Part> parts) {
        if (parts.isEmpty()) {
            /* Parts are consumed (path matches), but we need to check if the last element is a closing one [RECORD is not closed] */
            return schema.getType() != RECORD;
        }

        if (schema.getType() == RECORD) {
            return consumePartsForRecord(schema, parts);
        } else if (schema.getType() == UNION) {
            return consumePartsForUnion(schema, parts);
        }
        return false;
    }

    private static boolean consumePartsForRecord(final Schema schema, @NonNull final Deque<Part> parts) {
        final Part part = parts.removeFirst();
        for (final Field field : schema.getFields()) {
            if (field.name().equals(part.name())) {
                return consumeParts(field.schema(), parts);
            }
        }
        return false;
    }

    private static boolean consumePartsForUnion(final Schema schema, @NonNull final Deque<Part> parts) {
        Deque<Part> copy = new LinkedList<>(parts);
        for (final Schema childSchema : schema.getTypes()) {
            if (!childSchema.getName().equals("null") && consumeParts(childSchema, copy)) {
                return true;
            }
            copy = new LinkedList<>(parts);
        }
        return false;
    }
}
