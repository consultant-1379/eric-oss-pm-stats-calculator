/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;

import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.repository.api.PartitionRepository;
import com.ericsson.oss.air.pm.stats.repository.util.partition.PartitionUniqueIndex;
import com.ericsson.oss.air.pm.stats.test_utils.DriverManagerMock;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PartitionServiceImplTest {
    private static final List<String> PARTITION_NAMES = Collections.singletonList("partition1");

    @Mock
    PartitionRepository partitionRepositoryMock;
    @InjectMocks
    PartitionServiceImpl objectUnderTest;

    @Test
    void shouldGetPartitionNamesForTable() {
        final String tableName = "tableName";

        when(partitionRepositoryMock.getPartitionNamesForTable(anyString())).thenReturn(PARTITION_NAMES);

        final List<String> actual = objectUnderTest.getPartitionNamesForTable(tableName);

        verify(partitionRepositoryMock).getPartitionNamesForTable(tableName);

        Assertions.assertThat(actual).isEqualTo(PARTITION_NAMES);
    }

    @Test
    void shouldDropPartitions() {
        DriverManagerMock.prepare(connectionMock -> {
            objectUnderTest.dropPartitions(PARTITION_NAMES);

            verify(partitionRepositoryMock).dropPartitions(any(Connection.class), eq(PARTITION_NAMES));
        });
    }

    @Test
    @SneakyThrows
    void shouldCreatePartition() {
        DriverManagerMock.prepare(connectionMock -> {
            objectUnderTest.createPartitions(Collections.emptyList());

            verify(partitionRepositoryMock).createPartitions(connectionMock, Collections.emptyList());
        });
    }

    @Test
    void shouldGetUniqueIndexForTablePartitions() {
        final Table table = Table.of("table");

        when(partitionRepositoryMock.findAllPartitionUniqueIndexes(table)).thenReturn(Collections.emptyList());

        final List<PartitionUniqueIndex> actual = objectUnderTest.getUniqueIndexForTablePartitions(table);

        verify(partitionRepositoryMock).findAllPartitionUniqueIndexes(table);

        Assertions.assertThat(actual).isEmpty();
    }

    @Nested
    @DisplayName("should create partition")
    class ShouldCreatePartition {
        @Test
        @SneakyThrows
        void withoutConnection() {
            DriverManagerMock.prepare(connectionMock -> {
                objectUnderTest.createPartitions(Collections.emptyList());

                verify(partitionRepositoryMock).createPartitions(connectionMock, Collections.emptyList());
            });
        }

        @Test
        @SneakyThrows
        void withConnection() {
            final Connection connectionMock = Mockito.mock(Connection.class);

            objectUnderTest.createPartitions(connectionMock, Collections.emptyList());
            verify(partitionRepositoryMock).createPartitions(connectionMock, Collections.emptyList());
        }
    }
}