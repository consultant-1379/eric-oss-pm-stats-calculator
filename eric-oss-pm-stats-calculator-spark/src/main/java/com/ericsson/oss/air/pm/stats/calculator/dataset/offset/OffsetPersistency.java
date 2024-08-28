/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.offset;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatasourceTables;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.LatestProcessedOffset;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;

import org.springframework.plugin.core.Plugin;

public interface OffsetPersistency extends Plugin<Collection<KpiDefinition>> {
    default void calculateMaxOffsetsForSourceTables(
            Map<Integer, List<KpiDefinition>> kpisByStage,
            DatasourceTables datasourceTables,
            int aggregationPeriodInMinutes) {
        throw new UnsupportedOperationException();
    }

    default void persistMaxOffsets(int aggregationPeriodInMinutes) {
        throw new UnsupportedOperationException();
    }

    default void upsertOffset(final LatestProcessedOffset latestProcessedOffset) {
        throw new UnsupportedOperationException();
    }

}
