/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.offset;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.model.CalculationTimeWindow;

import org.springframework.plugin.core.Plugin;

public interface OffsetHandler extends Plugin<Set<KpiDefinition>> {
    void calculateOffsets(Set<KpiDefinition> defaultFilterKpis);

    default Timestamp getStartTimestamp(final Integer aggregationPeriodInMinutes) {
        throw new UnsupportedOperationException();
    }

    default Timestamp getEndTimestamp(final Integer aggregationPeriodInMinutes) {
        throw new UnsupportedOperationException();
    }

    default CalculationTimeWindow getCalculationTimeWindow(final Integer aggregationPeriod) {
        return CalculationTimeWindow.of(
                getStartTimestamp(aggregationPeriod),
                getEndTimestamp(aggregationPeriod)
        );
    }

    List<KpiDefinition> getKpisForAggregationPeriodWithTimestampParameters(Integer aggPeriodInMinutes, Collection<KpiDefinition> kpiDefinitions);

    void saveOffsets();

}
