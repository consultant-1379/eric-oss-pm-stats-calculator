/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.start.time.adjuster;

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_AGGREGATION_PERIOD_INT;

import java.time.LocalDateTime;
import javax.enterprise.context.ApplicationScoped;

import com.ericsson.oss.air.pm.stats.common.util.LocalDateTimeTruncates;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class StartTimeTemporalAdjuster {

    public LocalDateTime calculate(@NonNull final ReadinessLog readinessLog, @NonNull final KpiDefinitionEntity kpiDefinition) {
        final LocalDateTime earliestCollectedData = readinessLog.getEarliestCollectedData();
        final int aggregationPeriod = kpiDefinition.aggregationPeriod();

        return aggregationPeriod == DEFAULT_AGGREGATION_PERIOD_INT
                ? earliestCollectedData
                : LocalDateTimeTruncates.truncateToAggregationPeriod(earliestCollectedData, aggregationPeriod);
    }
}
