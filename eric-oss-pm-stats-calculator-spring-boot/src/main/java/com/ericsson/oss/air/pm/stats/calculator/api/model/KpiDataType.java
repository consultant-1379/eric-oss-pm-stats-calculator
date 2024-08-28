/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * ENUM representing the relationship between data types in the KPI Model and SQL data types.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum KpiDataType {

    POSTGRES_INTEGER_ARRAY_SIZE("INTEGER\\[[0-9]+\\]", "_int4", "_int4", "{}"),
    POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE("INTEGER\\[\\]", "_int4", "_int4", "{}"),
    POSTGRES_INTEGER("INT(EGER)?", "int4", "int4", "0"),
    POSTGRES_FLOAT_ARRAY_SIZE("FLOAT\\[[0-9]+\\]", "_float8", "_float8", "{}"),
    POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE("FLOAT\\[\\]", "_float8", "_float8", "{}"),
    POSTGRES_FLOAT("FLOAT|DOUBLE", "double precision", "float8", "0.0"),
    POSTGRES_LONG_ARRAY_SIZE("LONG\\[[0-9]+\\]", "_int8", "_int8", "{}"),
    POSTGRES_LONG_ARRAY_UNDEFINED_SIZE("LONG\\[\\]", "_int8", "_int8", "{}"),
    POSTGRES_LONG("LONG", "int8", "int8", "0"),
    POSTGRES_REAL("REAL", "real", "float4", "0"),
    POSTGRES_BOOLEAN("BOOLEAN", "boolean", "bool", "false"),
    POSTGRES_STRING("STRING", "varchar(255)", "varchar", "''"),
    POSTGRES_UNLIMITED_STRING("TEXT", "varchar", "varchar", "''"),
    POSTGRES_TIMESTAMP("TIMESTAMP", "TIMESTAMP WITHOUT TIME ZONE", "timestamp", "current_timestamp");

    private static final List<KpiDataType> VALUES_AS_LIST = Collections.unmodifiableList(Arrays.asList(values()));
    private static final List<String> VALID_VALUES_AS_LIST = createValidValuesList();

    private final String regEx;
    private final String postgresType;
    private final String columnType;
    private final String defaultValue;

    /**
     * Finds a matching {@link KpiDataType} based on the input string.
     *
     * @param wantedValue the string representation of the {@link KpiDataType} to find taken from the JSON schema
     * @return the {@link KpiDataType} which is double precision by default.
     * @throws IllegalArgumentException if no matching {@link KpiDataType} could be found.
     */
    @JsonCreator
    public static KpiDataType forValue(final String wantedValue) {
        for (final KpiDataType kpiDataType : KpiDataType.valuesAsList()) {
            final Pattern objectTypePattern = Pattern.compile(kpiDataType.getRegEx());
            if (objectTypePattern.matcher(wantedValue).find()) {
                return kpiDataType;
            }
        }
        throw new IllegalArgumentException(String.format("Invalid KpiDataType: '%s'", wantedValue));
    }

    /**
     * Finds a matching {@link KpiDataType} based on the avro schema.
     *
     * @param schema the schema of the field for which we need the type
     * @return {@link KpiDataType} representation of the schema type
     */
    public static KpiDataType forSchemaType(final Schema schema) {
        if (schema.isUnion()) {
            final List<Type> nonNullSchemaType = schema.getTypes()
                    .stream()
                    .map(Schema::getType)
                    .filter(type -> type != Type.NULL)
                    .collect(Collectors.toList());

            Preconditions.checkArgument(nonNullSchemaType.size() == 1, "union type should only have one non null exact type");

            final Type schemaType = IterableUtils.first(nonNullSchemaType);
            return forValue(schemaType.toString());
        }
        return forValue(schema.getType().toString());
    }

    /**
     * Returns all {@link KpiDataType} values as a {@link List}.
     * <p>
     * To be used instead Enum.values of as it does not create a new array for each invocation.
     *
     * @return all ENUM values
     */
    public static List<KpiDataType> valuesAsList() {
        return Collections.unmodifiableList(VALUES_AS_LIST);
    }

    /**
     * Returns all {@link KpiDataType} regex as a {@link List} of strings.
     * <p>
     * Removes escape characters before returning.
     *
     * @return all regex values with escape characters removed.
     */
    public static List<String> getValidValuesAsList() {
        return Collections.unmodifiableList(VALID_VALUES_AS_LIST);
    }

    private static List<String> createValidValuesList() {
        return VALUES_AS_LIST.stream()
                .map(kpiDataType -> kpiDataType.getRegEx().replace("\\", StringUtils.EMPTY))
                .collect(Collectors.toList());
    }
}
