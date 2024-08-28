/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.readiness.window;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;

import com.ericsson.oss.air.pm.stats.calculation.readiness.model.ReadinessWindow;
import com.ericsson.oss.air.pm.stats.calculation.readiness.model.domain.DataSource;
import com.ericsson.oss.air.pm.stats.calculation.readiness.utils.ReadinessLogMergers;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;
import com.ericsson.oss.air.pm.stats.service.util.CollectionHelpers;

import lombok.NonNull;

@ApplicationScoped
public class ReadinessWindowCalculator {

    public Map<DataSource, ReadinessWindow> calculateReadinessWindows(final Collection<? extends Set<ReadinessLog>> readinessLogs) {
        final Map<DataSource, List<ReadinessLog>> dataSourceToReadinessLogs = groupReadinessLogsByDatasource(readinessLogs);

        return CollectionHelpers.transformValue(
                dataSourceToReadinessLogs,
                ReadinessLogMergers::merge
        );
    }

    private static Map<DataSource, List<ReadinessLog>> groupReadinessLogsByDatasource(@NonNull final Collection<? extends Set<ReadinessLog>> values) {
        return values.stream().flatMap(Collection::stream).collect(Collectors.collectingAndThen(
                Collectors.groupingBy(ReadinessLog::getDatasource),
                result -> CollectionHelpers.transformKey(result, DataSource::of)
        ));
    }
}
