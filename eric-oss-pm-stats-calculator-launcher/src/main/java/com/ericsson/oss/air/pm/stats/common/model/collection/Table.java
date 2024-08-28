/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.model.collection;


import com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants;
import lombok.Data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class Table {
    public static final String DEFAULT_SCHEMA = "kpi";

    private static final Pattern CONTAINS_NUMBER = Pattern.compile(".*_\\d+$");
    private static final Pattern ALIAS_AND_AGGREGATION_PERIOD_PATTERN = Pattern.compile("^(kpi_)(.*)_(\\d+)*$");

    private final String schema;
    private final String name;

    public static Table of(final String name) {
        return new Table(DEFAULT_SCHEMA, name);
    }

    public static Table of(final String schema, final String name) {
        return new Table(schema, name);
    }

    public boolean doesNotContainNumber() {
        return !doesContainNumber();
    }

    public boolean doesContainNumber() {
        return CONTAINS_NUMBER.matcher(name).matches();
    }

    public boolean isNameEqualsAnotherTableName(Table table) {
        return name.equalsIgnoreCase(table.getName());
    }

    public int getAggregationPeriod() {
        final Matcher matcher = ALIAS_AND_AGGREGATION_PERIOD_PATTERN.matcher(name);
        if (matcher.find() && matcher.group(3) != null) {
            return Integer.parseInt(matcher.group(3));
        }
        return KpiCalculatorConstants.DEFAULT_AGGREGATION_PERIOD_INT;
    }

    public String getAlias() {
        final Matcher matcher = ALIAS_AND_AGGREGATION_PERIOD_PATTERN.matcher(name);
        if (matcher.find()) {
            return matcher.group(2);
        }
        throw new IllegalArgumentException(String.format("Alias could not be parsed from table name: %s", name));
    }
}
