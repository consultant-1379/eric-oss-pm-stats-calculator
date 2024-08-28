/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.facade;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.dataset.readinesslog.ReadinessLogCache;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.ReadinessLog;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.facade.api.ReadinessLogFacade;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.ReadinessLogRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReadinessLogFacadeImpl implements ReadinessLogFacade {
    private final ReadinessLogRepository readinessLogRepository;
    private final ReadinessLogCache readinessLogCache;

    @Override
    public List<ReadinessLog> persistReadinessLogs() {
        final List<ReadinessLog> readinessLogs = readinessLogRepository.saveAll(readinessLogCache.fetchAlLReadinessLogs());
        logReadinessLogs(readinessLogs);
        return readinessLogs;
    }

    private static void logReadinessLogs(@NonNull final List<ReadinessLog> readinessLogs) {
        readinessLogs.forEach(readinessLog -> {
            final String datasource = readinessLog.getDatasource();
            final Long rows = readinessLog.getCollectedRowsCount();
            final LocalDateTime earliest = readinessLog.getEarliestCollectedData();
            final LocalDateTime latest = readinessLog.getLatestCollectedData();
            final UUID calculationId = readinessLog.getKpiCalculationId().getId();

            log.info(
                    "For calculation '{}' on '{}' datasource '{}' rows collected between '{}' and '{}'",
                    calculationId, datasource, rows, earliest, latest
            );
        });
    }
}
