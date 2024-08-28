/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.scheduler.activity;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import com.ericsson.oss.air.pm.stats.service.api.CalculationService;
import com.ericsson.oss.air.pm.stats.service.facade.PartitionRetentionFacade;
import com.ericsson.oss.air.pm.stats.service.facade.PartitionRetentionManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDataMap;

@ExtendWith(MockitoExtension.class)
@SetEnvironmentVariable(key = "RETENTION_PERIOD_DAYS", value = "5")
class KpiCalculatorRetentionActivityTest {

    @Mock
    PartitionRetentionFacade partitionRetentionFacadeMock;
    @Mock
    CalculationService calculationServiceMock;
    @Mock
    PartitionRetentionManager partitionRetentionManagerMock;

    @InjectMocks
    private KpiCalculatorRetentionActivity objectUnderTest;

    @Test
    void shouldTestDefaultConstructor() {
        final KpiCalculatorRetentionActivity kpiCalculatorRetentionActivity = new KpiCalculatorRetentionActivity();
        assertThat(kpiCalculatorRetentionActivity).isNotNull();
    }

    @Test
    void testCalculationsAreCleanedUp() {
        final Integer retentionPeriod = 5;
        KpiCalculatorRetentionActivity kpiCalculatorRetentionActivity = KpiCalculatorRetentionActivity.of(retentionPeriod);

        objectUnderTest.run(new JobDataMap(kpiCalculatorRetentionActivity.getContext()));

        final LocalDate now = LocalDate.now();
        verify(partitionRetentionFacadeMock).runRetention();
        verify(partitionRetentionManagerMock).getRetentionDate(retentionPeriod);
        verify(calculationServiceMock, atMost(1)).deleteByTimeCreatedLessThen(now.minusDays(3).atStartOfDay());
        verify(calculationServiceMock, atMost(1)).deleteByTimeCreatedLessThen(now.minusDays(4).atStartOfDay());
        verify(calculationServiceMock, times(1)).deleteByTimeCreatedLessThen(any());
    }
}
