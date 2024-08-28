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

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

/**
 * ENUM representing the different SQL data types.
 */
public enum SqlDataType {

    BOOLEAN("boolean", "boolean"),
    BYTE("byte", "byte"),
    DOUBLE("double", "double precision"),
    FLOAT("float", "real"),
    INT("int", "integer"),
    LONG("long", "bigint"),
    SERIAL("index", "serial"),
    SHORT("short", "smallint"),
    STRING("String", "varchar(255)"),
    STRING_ARRAY("String[]", "varchar(255)[]");

    private static final List<SqlDataType> VALUES_AS_LIST = Collections.unmodifiableList(Arrays.asList(values()));

    @Getter
    private final String jsonType;
    @Getter
    private final String sqlType;

    SqlDataType(final String jsonType, final String sqlType) {
        this.jsonType = jsonType;
        this.sqlType = sqlType;
    }

    /**
     * Finds a matching {@link SqlDataType} based on the input string.
     *
     * @param wantedValue the string representation of the {@link SqlDataType} to find taken from the JSON schema
     * @return the {@link SqlDataType} which is double precision by default
     */
    @JsonCreator
    public static SqlDataType forValue(final String wantedValue) {
        for (final SqlDataType sqlDataType : SqlDataType.valuesAsList()) {
            if (sqlDataType.getJsonType().equalsIgnoreCase(wantedValue)) {
                return sqlDataType;
            }
        }
        return SqlDataType.DOUBLE;
    }

    /**
     * Returns all {@link SqlDataType} values as a {@link List}.
     * <p>
     * To be used instead of Enum.values as it does not create a new array for each invocation.
     *
     * @return all ENUM values
     */
    public static List<SqlDataType> valuesAsList() {
        return VALUES_AS_LIST;
    }

    public boolean isNumeric() {
        return equals(LONG) || equals(INT) || equals(SHORT) || equals(DOUBLE) || equals(FLOAT);
    }
}
