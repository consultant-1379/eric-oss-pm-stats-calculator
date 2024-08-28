/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.api.enumeration;

import java.util.Objects;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ObjectType {
    POSTGRES_INTEGER_ARRAY_SIZE("INTEGER\\[[0-9]+\\]", "_int4", "_int4", "{}"),
    POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE("INTEGER\\[\\]", "_int4", "_int4", "{}"),
    POSTGRES_INTEGER("INTEGER", "int4", "int4", "0"),
    POSTGRES_FLOAT_ARRAY_SIZE("FLOAT\\[[0-9]+\\]", "_float8", "_float8", "{}"),
    POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE("FLOAT\\[\\]", "_float8", "_float8", "{}"),
    POSTGRES_FLOAT("FLOAT", "double precision", "float8", "0.0"),
    POSTGRES_LONG_ARRAY_SIZE("LONG\\[[0-9]+\\]", "_int8", "_int8", "{}"),
    POSTGRES_LONG_ARRAY_UNDEFINED_SIZE("LONG\\[\\]", "_int8", "_int8", "{}"),
    POSTGRES_LONG("LONG", "int8", "int8", "0"),
    POSTGRES_REAL("REAL", "real", "float4", "0"),
    POSTGRES_BOOLEAN("BOOLEAN", "boolean", "bool", "false"),
    POSTGRES_STRING("STRING", "varchar(255)", "varchar", "''"),
    POSTGRES_UNLIMITED_STRING("TEXT", "varchar", "varchar", "''"),
    POSTGRES_TIMESTAMP("TIMESTAMP", "TIMESTAMP WITHOUT TIME ZONE", "timestamp", "current_timestamp");

    private final String regEx;
    private final String postgresType;
    private final String columnType;
    private final String defaultValue;

    @Nullable
    public static ObjectType from(final String wantedValue) {
        if (Objects.isNull(wantedValue)) {
            return null;
        }

        for (final ObjectType objectType : values()) {
            final Pattern objectTypePattern = Pattern.compile(objectType.getRegEx());
            if (objectTypePattern.matcher(wantedValue).find()) {
                return objectType;
            }
        }

        throw new IllegalStateException(String.format("'%s' is not a valid %s", wantedValue, ObjectType.class.getSimpleName()));
    }
}
