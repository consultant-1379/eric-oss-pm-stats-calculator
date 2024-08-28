/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service;

import java.sql.Timestamp;

import com.ericsson.oss.air.pm.stats.calculator.repository.internal.api.DataSourceRepository;
import com.ericsson.oss.air.pm.stats.calculator.service.api.DataSourceService;
import com.ericsson.oss.air.pm.stats.calculator.util.Timestamps;
import com.ericsson.oss.air.pm.stats.common.model.collection.Database;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataSourceServiceImpl implements DataSourceService {
    private final DataSourceRepository dataSourceRepository;

    @Override
    public Timestamp findMaxUtcTimestamp(final Database database, final Table table) {
        return dataSourceRepository.findMaxAggregationBeginTime(database, table).orElseGet(Timestamps::initialTimeStamp);
    }

    @Override
    public Timestamp findMinUtcTimestamp(final Database database, final Table table) {
        return dataSourceRepository.findAggregationBeginTime(database, table).orElseGet(Timestamps::initialTimeStamp);
    }

}
