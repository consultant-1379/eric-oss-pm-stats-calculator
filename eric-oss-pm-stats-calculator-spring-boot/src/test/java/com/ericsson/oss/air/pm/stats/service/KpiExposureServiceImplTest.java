/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service;

import static org.mockito.Mockito.verify;

import com.ericsson.oss.air.pm.stats.calculator.api.dto.kafka.query.service.KpiTableExposureConfig.Type;
import com.ericsson.oss.air.pm.stats.service.helper.KpiExposureUpdater;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KpiExposureServiceImplTest {
    static final String KPI = "kpi";

    @Mock
    KpiExposureUpdater kpiExposureUpdater;

    @InjectMocks
    private KpiExposureServiceImpl objectUnderTest;

    @Test
    void shouldSendCorrectMessage_whenExposingTable() {
        objectUnderTest.exposeCalculationTable("tableName");

        verify(kpiExposureUpdater).updateVisibility(KPI, "tableName", Type.TABLE, true);
    }

    @Test
    void shouldSendCorrectMessage_whenUpdatingExposure() {
        objectUnderTest.updateExposure();

        verify(kpiExposureUpdater).updateExposureInfo(KPI);
    }

}
