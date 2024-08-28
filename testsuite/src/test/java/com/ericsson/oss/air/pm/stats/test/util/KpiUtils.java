/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.util;

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DAILY_AGGREGATION_PERIOD_IN_MINUTES;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class used to provide KPI json payloads to be sent in REST endpoint testing
 */
public final class KpiUtils {

    private static final String[] VALID_KPI_KEYS = { "name", "element", "expression", "object_type", "aggregation_type",
            "aggregation_period", "aggregation_elements" };
    private static final Object[] VALID_KPI_VALUES = { "num_calls_daily", "cell", "pm_stats://counters_cell.pmErabEstabSuccInit", "INTEGER",
            "SUM", DAILY_AGGREGATION_PERIOD_IN_MINUTES, new String[] { "counters_cell.guid" } };

    private KpiUtils() {

    }

    public static List<Map<String, Object>> getInvalidKpis() {
        final List<Map<String, Object>> invalidKpis = getObjectList(VALID_KPI_KEYS, VALID_KPI_VALUES);
        invalidKpis.get(0).remove("name");
        return invalidKpis;
    }

    private static List<Map<String, Object>> getObjectList(final String[] keys, final Object[] values) {
        final Map<String, Object> objectMap = new HashMap<>(keys.length);
        for (int i = 0; i < keys.length; i++) {
            objectMap.put(keys[i], values[i]);
        }
        return Collections.singletonList(objectMap);
    }
}
