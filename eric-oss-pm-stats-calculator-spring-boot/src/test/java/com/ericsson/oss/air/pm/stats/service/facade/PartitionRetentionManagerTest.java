/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.facade;

import static java.time.Month.MARCH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.repository.util.partition.Partition;
import com.ericsson.oss.air.pm.stats.repository.util.partition.PartitionNameEncoder;
import com.ericsson.oss.air.pm.stats.service.api.PartitionService;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class PartitionRetentionManagerTest {

    @Mock
    PartitionService partitionServiceMock;
    @Mock
    PartitionNameEncoder partitionNameEncoder;
    @Spy
    Clock clock = fixedClock();

    @InjectMocks
    PartitionRetentionManager objectUnderTest;

    @Test
    void testGetRetentionDate() {
        LocalDateTime actual = objectUnderTest.getRetentionDate(4);

        assertThat(actual).isEqualTo(LocalDateTime.parse("2024-02-28T00:00:00"));
    }

    @Test
    @SneakyThrows
    void testRunCleanUp(@Mock Connection connectionMock) {
        when(partitionServiceMock.getPartitionNamesForTable("table")).thenReturn(List.of(
                "partition1_p_2024_02_27", "partition2_p_2024_02_29"));

        when(partitionNameEncoder.decodePartitionDateFromName("partition1_p_2024_02_27")).thenReturn(LocalDate.parse("2024-02-27"));
        when(partitionNameEncoder.decodePartitionDateFromName("partition2_p_2024_02_29")).thenReturn(LocalDate.parse("2024-02-29"));

        objectUnderTest.runCleanUpForPartitionedTable(connectionMock, "table", LocalDateTime.parse("2024-02-28T00:00:00"));

        verify(partitionServiceMock).dropPartitions(connectionMock, List.of("partition1_p_2024_02_27"));
    }

    @Test
    @SneakyThrows
    void testRunCleanUpWithoutDroppingPartition(@Mock Connection connectionMock) {
        when(partitionServiceMock.getPartitionNamesForTable("table")).thenReturn(List.of(
                "partition1_p_2024_03_01", "partition2_p_2024_02_29"));

        when(partitionNameEncoder.decodePartitionDateFromName("partition1_p_2024_03_01")).thenReturn(LocalDate.parse("2024-03-01"));
        when(partitionNameEncoder.decodePartitionDateFromName("partition2_p_2024_02_29")).thenReturn(LocalDate.parse("2024-02-29"));


        objectUnderTest.runCleanUpForPartitionedTable(connectionMock, "table", LocalDateTime.parse("2024-02-28T00:00:00"));

        verify(partitionServiceMock, never()).dropPartitions(any(), anyList());
    }

    @Test
    @SneakyThrows
    void testCreateNewPartitionForTable(@Mock Connection connectionMock) {
        List<Partition> partitions = List.of(
                new Partition("table_p_2024_03_01", "table", LocalDate.parse("2024-03-01"), LocalDate.parse("2024-03-02"), Set.of("noFdn")),
                new Partition("table_p_2024_03_02", "table", LocalDate.parse("2024-03-02"), LocalDate.parse("2024-03-03"), Set.of("noFdn")),
                new Partition("table_p_2024_03_03", "table", LocalDate.parse("2024-03-03"), LocalDate.parse("2024-03-04"), Set.of("noFdn")),
                new Partition("table_p_2024_03_04", "table", LocalDate.parse("2024-03-04"), LocalDate.parse("2024-03-05"), Set.of("noFdn")),
                new Partition("table_p_2024_03_05", "table", LocalDate.parse("2024-03-05"), LocalDate.parse("2024-03-06"), Set.of("noFdn"))
        );

        when(partitionNameEncoder.encodePartitionNameFromDate("table", LocalDate.parse("2024-03-01"))).thenReturn("table_p_2024_03_01");
        when(partitionNameEncoder.encodePartitionNameFromDate("table", LocalDate.parse("2024-03-02"))).thenReturn("table_p_2024_03_02");
        when(partitionNameEncoder.encodePartitionNameFromDate("table", LocalDate.parse("2024-03-03"))).thenReturn("table_p_2024_03_03");
        when(partitionNameEncoder.encodePartitionNameFromDate("table", LocalDate.parse("2024-03-04"))).thenReturn("table_p_2024_03_04");
        when(partitionNameEncoder.encodePartitionNameFromDate("table", LocalDate.parse("2024-03-05"))).thenReturn("table_p_2024_03_05");

        objectUnderTest.createNewPartitionForTable(connectionMock, "table", Set.of("noFdn"), LocalDateTime.parse("2024-03-01T00:00:00"));

        verify(partitionServiceMock).createPartitions(connectionMock, partitions);
    }

    static Clock fixedClock() {
        return Clock.fixed(
                LocalDateTime.of(2_024, MARCH, 3, 0, 0).toInstant(ZoneOffset.UTC),
                ZoneId.systemDefault()
        );
    }
}