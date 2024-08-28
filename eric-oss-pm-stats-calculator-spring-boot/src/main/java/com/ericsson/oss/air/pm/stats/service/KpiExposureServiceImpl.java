/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service;

import com.ericsson.oss.air.pm.stats.calculator.api.dto.kafka.query.service.KpiTableExposureConfig.Type;
import com.ericsson.oss.air.pm.stats.service.api.KpiExposureService;
import com.ericsson.oss.air.pm.stats.service.helper.KpiExposureUpdater;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class KpiExposureServiceImpl implements KpiExposureService {
    private static final String SCHEMA = "kpi";

    private KpiExposureUpdater kpiExposureUpdater;

    @Override
    public void exposeCalculationTable(final String name) {
        kpiExposureUpdater.updateVisibility(SCHEMA, name, Type.TABLE, true);
    }

    @Override
    public void updateExposure() {
        kpiExposureUpdater.updateExposureInfo(SCHEMA);
    }
}
