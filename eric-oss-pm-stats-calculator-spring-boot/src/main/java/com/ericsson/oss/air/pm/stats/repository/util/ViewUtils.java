/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util;

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_AGGREGATION_PERIOD_INT;

import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ViewUtils {

    /**
     * Creates reliability view name.
     *
     * @param table the {@link Table} to create view name
     * @return the reliability view name
     */
    public static String constructReliabilityViewName(final Table table) {
        return table.getName() + (table.getAggregationPeriod() == DEFAULT_AGGREGATION_PERIOD_INT ? "view" : "_view");
    }

}
