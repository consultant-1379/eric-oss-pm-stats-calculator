/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.readinesslog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.ReadinessLog;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReadinessLogCache {
    private final ConcurrentHashMap<CacheKey, ReadinessLog> cache = new ConcurrentHashMap<>();

    public ReadinessLog merge(final ReadinessLog value, final Integer aggregationPeriod) {
        return cache.merge(CacheKey.of(aggregationPeriod, value.getDatasource()), value, ReadinessLogMergers::merge);
    }

    public ReadinessLog put(final ReadinessLog value, final Integer aggregationPeriod) {
        return cache.put(CacheKey.of(aggregationPeriod, value.getDatasource()), value);
    }

    public Collection<ReadinessLog> fetchAlLReadinessLogs() {
        final Collection<ReadinessLog> readinessLogs = new ArrayList<>();
        final Collection<String> visitedDataSources = new HashSet<>();

        cache.forEach((cacheKey, readinessLog) -> {
            final String datasource = cacheKey.getDatasource();
            if (visitedDataSources.contains(datasource)) {
                log.info("Datasource '{}' already visited so '{}' is ignored", datasource, readinessLog);
            } else {
                visitedDataSources.add(datasource);
                readinessLogs.add(readinessLog);
            }
        });

        return Collections.unmodifiableCollection(readinessLogs);
    }

    @Data(staticConstructor = "of")
    public static final class CacheKey {
        private final Integer aggregationPeriod;
        private final String datasource;
    }
}