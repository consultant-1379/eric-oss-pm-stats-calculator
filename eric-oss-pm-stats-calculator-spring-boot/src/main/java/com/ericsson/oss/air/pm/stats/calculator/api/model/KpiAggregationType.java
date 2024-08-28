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

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ENUM representing the different aggregation types for KPIs.
 */
@Getter
@AllArgsConstructor
public enum KpiAggregationType {

    SUM("SUM"),
    MAX("MAX"),
    ARRAY_INDEX_SUM("ARRAY_INDEX_SUM"),
    ARRAY_INDEX_SUM_INTEGER("ARRAY_INDEX_SUM_INTEGER"),
    ARRAY_INDEX_SUM_DOUBLE("ARRAY_INDEX_SUM_DOUBLE"),
    ARRAY_INDEX_SUM_LONG("ARRAY_INDEX_SUM_LONG"),
    SUM_DOUBLE("SUM_DOUBLE"),
    PERCENTILE_INDEX_80("PERCENTILE_INDEX_80"),
    PERCENTILE_INDEX_90("PERCENTILE_INDEX_90"),
    FIRST("FIRST");

    private static final List<KpiAggregationType> VALUES_AS_LIST = Collections.unmodifiableList(Arrays.asList(values()));

    private final String jsonType;

    /**
     * Returns all {@link KpiAggregationType} values as a {@link List}.
     *
     * @return all ENUM values
     */
    public static List<KpiAggregationType> valuesAsList() {
        return VALUES_AS_LIST;
    }

}
