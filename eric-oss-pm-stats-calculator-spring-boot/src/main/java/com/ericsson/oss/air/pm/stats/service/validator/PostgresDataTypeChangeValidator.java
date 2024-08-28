/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator;

import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType.POSTGRES_BOOLEAN;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType.POSTGRES_FLOAT;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType.POSTGRES_FLOAT_ARRAY_SIZE;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType.POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType.POSTGRES_INTEGER;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType.POSTGRES_INTEGER_ARRAY_SIZE;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType.POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType.POSTGRES_LONG;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType.POSTGRES_LONG_ARRAY_SIZE;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType.POSTGRES_LONG_ARRAY_UNDEFINED_SIZE;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType.POSTGRES_REAL;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType.POSTGRES_STRING;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType.POSTGRES_TIMESTAMP;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType.POSTGRES_UNLIMITED_STRING;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class PostgresDataTypeChangeValidator {
    private final Map<KpiDataType, Set<KpiDataType>> validDataTypeChanges = new EnumMap<>(KpiDataType.class);

    public PostgresDataTypeChangeValidator() {
        validConversion(POSTGRES_INTEGER_ARRAY_SIZE, POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE);
        validConversion(POSTGRES_INTEGER_ARRAY_SIZE, POSTGRES_FLOAT_ARRAY_SIZE);
        validConversion(POSTGRES_INTEGER_ARRAY_SIZE, POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE);
        validConversion(POSTGRES_INTEGER_ARRAY_SIZE, POSTGRES_STRING);
        validConversion(POSTGRES_INTEGER_ARRAY_SIZE, POSTGRES_UNLIMITED_STRING);
        validConversion(POSTGRES_INTEGER_ARRAY_SIZE, POSTGRES_LONG_ARRAY_SIZE);
        validConversion(POSTGRES_INTEGER_ARRAY_SIZE, POSTGRES_LONG_ARRAY_UNDEFINED_SIZE);

        validConversion(POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, POSTGRES_INTEGER_ARRAY_SIZE);
        validConversion(POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, POSTGRES_FLOAT_ARRAY_SIZE);
        validConversion(POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE);
        validConversion(POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, POSTGRES_STRING);
        validConversion(POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, POSTGRES_UNLIMITED_STRING);
        validConversion(POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, POSTGRES_LONG_ARRAY_SIZE);
        validConversion(POSTGRES_INTEGER_ARRAY_UNDEFINED_SIZE, POSTGRES_LONG_ARRAY_UNDEFINED_SIZE);

        validConversion(POSTGRES_INTEGER, POSTGRES_FLOAT);
        validConversion(POSTGRES_INTEGER, POSTGRES_LONG);
        validConversion(POSTGRES_INTEGER, POSTGRES_REAL);
        validConversion(POSTGRES_INTEGER, POSTGRES_STRING);
        validConversion(POSTGRES_INTEGER, POSTGRES_UNLIMITED_STRING);
        validConversion(POSTGRES_INTEGER, POSTGRES_TIMESTAMP);

        validConversion(POSTGRES_FLOAT_ARRAY_SIZE, POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE);
        validConversion(POSTGRES_FLOAT_ARRAY_SIZE, POSTGRES_STRING);
        validConversion(POSTGRES_FLOAT_ARRAY_SIZE, POSTGRES_UNLIMITED_STRING);

        validConversion(POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, POSTGRES_FLOAT_ARRAY_SIZE);
        validConversion(POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, POSTGRES_STRING);
        validConversion(POSTGRES_FLOAT_ARRAY_UNDEFINED_SIZE, POSTGRES_UNLIMITED_STRING);

        validConversion(POSTGRES_FLOAT, POSTGRES_LONG);
        validConversion(POSTGRES_FLOAT, POSTGRES_STRING);
        validConversion(POSTGRES_FLOAT, POSTGRES_UNLIMITED_STRING);

        validConversion(POSTGRES_LONG_ARRAY_SIZE, POSTGRES_LONG_ARRAY_UNDEFINED_SIZE);
        validConversion(POSTGRES_LONG_ARRAY_SIZE, POSTGRES_STRING);
        validConversion(POSTGRES_LONG_ARRAY_SIZE, POSTGRES_UNLIMITED_STRING);

        validConversion(POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, POSTGRES_LONG_ARRAY_SIZE);
        validConversion(POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, POSTGRES_STRING);
        validConversion(POSTGRES_LONG_ARRAY_UNDEFINED_SIZE, POSTGRES_UNLIMITED_STRING);

        validConversion(POSTGRES_LONG, POSTGRES_FLOAT);
        validConversion(POSTGRES_LONG, POSTGRES_STRING);
        validConversion(POSTGRES_LONG, POSTGRES_UNLIMITED_STRING);

        validConversion(POSTGRES_REAL, POSTGRES_INTEGER);
        validConversion(POSTGRES_REAL, POSTGRES_FLOAT);
        validConversion(POSTGRES_REAL, POSTGRES_STRING);
        validConversion(POSTGRES_REAL, POSTGRES_UNLIMITED_STRING);

        validConversion(POSTGRES_BOOLEAN, POSTGRES_INTEGER);
        validConversion(POSTGRES_BOOLEAN, POSTGRES_STRING);
        validConversion(POSTGRES_BOOLEAN, POSTGRES_UNLIMITED_STRING);

        validConversion(POSTGRES_STRING, POSTGRES_TIMESTAMP);
        validConversion(POSTGRES_STRING, POSTGRES_UNLIMITED_STRING);

        validConversion(POSTGRES_UNLIMITED_STRING, POSTGRES_STRING);
        validConversion(POSTGRES_UNLIMITED_STRING, POSTGRES_TIMESTAMP);

        validConversion(POSTGRES_TIMESTAMP, POSTGRES_STRING);
        validConversion(POSTGRES_TIMESTAMP, POSTGRES_UNLIMITED_STRING);
    }

    public boolean isInvalidChange(final KpiDataType from, final KpiDataType to) {
        return !canChangeType(from, to);
    }

    private boolean canChangeType(final KpiDataType from, final KpiDataType to) {
        final Set<KpiDataType> validChanges = validDataTypeChanges.get(from);
        return validChanges.contains(to);
    }

    private void validConversion(final KpiDataType from, final KpiDataType to) {
        validDataTypeChanges.computeIfAbsent(from, v -> new HashSet<>()).add(to);
    }
}
