/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util;

import static com.ericsson.oss.air.pm.stats._util.TestHelpers.readinessLog;
import static com.ericsson.oss.air.pm.stats._util.TestHelpers.testMinute;
import static com.ericsson.oss.air.pm.stats._util.TestHelpers.uuid;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EntityMappersTest {

    @Test
    void successfully_parseReadinessLogResult(
            @Mock final PreparedStatement preparedStatementMock,
            @Mock final ResultSet resultSetMock) throws Exception {
        when(preparedStatementMock.executeQuery()).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(true).thenReturn(false);
        when(resultSetMock.getInt("id")).thenReturn(1);
        when(resultSetMock.getString("datasource")).thenReturn("datasource");
        when(resultSetMock.getLong("collected_rows_count")).thenReturn(10L);
        when(resultSetMock.getTimestamp("earliest_collected_data")).thenReturn(Timestamp.valueOf(testMinute(10)));
        when(resultSetMock.getTimestamp("latest_collected_data")).thenReturn(Timestamp.valueOf(testMinute(20)));
        when(resultSetMock.getObject("kpi_calculation_id", UUID.class)).thenReturn(uuid("b2531c89-8513-4a88-aa1a-b99484321628"));

        final List<ReadinessLog> actual = EntityMappers.parseReadinessLogResult(preparedStatementMock);

        verify(preparedStatementMock).executeQuery();
        verify(resultSetMock, times(2)).next();
        verify(resultSetMock).getInt("id");
        verify(resultSetMock).getString("datasource");
        verify(resultSetMock).getLong("collected_rows_count");
        verify(resultSetMock).getTimestamp("earliest_collected_data");
        verify(resultSetMock).getTimestamp("latest_collected_data");
        verify(resultSetMock).getObject("kpi_calculation_id", UUID.class);

        Assertions.assertThat(actual).containsExactlyInAnyOrder(readinessLog(
                1,
                "datasource",
                10L,
                testMinute(10),
                testMinute(20),
                uuid("b2531c89-8513-4a88-aa1a-b99484321628")
        ));
    }
}