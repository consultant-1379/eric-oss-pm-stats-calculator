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


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.dataset.readinesslog.ReadinessLogCache;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.Calculation;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.ReadinessLog;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.ReadinessLogRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReadinessLogFacadeImplTest {
    @Mock ReadinessLogCache readinessLogCacheMock;
    @Mock ReadinessLogRepository readinessLogRepositoryMock;

    @InjectMocks ReadinessLogFacadeImpl objectUnderTest;

    @Test
    void shouldSaveDataIntoDatabase(){
        final UUID calculationId1 = UUID.fromString("19f4401b-710c-42c6-beb2-dbb712fecf28");
        final UUID calculationId2 = UUID.fromString("e057617c-a1dd-415b-bf1e-94fb0fe34331");

        final List<ReadinessLog> readinessLogs = List.of(
                readinessLog(1, 15L, "datasource-1", testTime(12, 5), testTime(12, 20), calculationId1),
                readinessLog(2, 25L, "datasource-2", testTime(12, 15), testTime(12, 30), calculationId2)
        );

        when(readinessLogCacheMock.fetchAlLReadinessLogs()).thenReturn(readinessLogs);
        when(readinessLogRepositoryMock.saveAll(readinessLogs)).thenReturn(readinessLogs);

        final List<ReadinessLog> actual = objectUnderTest.persistReadinessLogs();

        verify(readinessLogCacheMock).fetchAlLReadinessLogs();
        verify(readinessLogRepositoryMock).saveAll(readinessLogs);

        assertThat(actual).isEqualTo(readinessLogs);
    }

    static ReadinessLog readinessLog(
            final int id, final long collectedRows, final String datasource, final LocalDateTime earliest, final LocalDateTime latest,
            final UUID calculationId
    ) {
        final Calculation calculation = new Calculation();
        calculation.setId(calculationId);
        return new ReadinessLog(id, collectedRows, datasource, earliest, latest, calculation);
    }

    static LocalDateTime testTime(final int hour, final int minute) {
        return LocalDateTime.of(2_023, Month.SEPTEMBER, 4, hour, minute);
    }
}