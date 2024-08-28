/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.table.output;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiPersistenceException;
import com.ericsson.oss.air.pm.stats.configuration.environment.model.EnvironmentValue;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.repository.util.partition.Partition;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.TableCreationInformation;
import com.ericsson.oss.air.pm.stats.service.api.DatabaseService;
import com.ericsson.oss.air.pm.stats.service.api.KpiExposureService;
import com.ericsson.oss.air.pm.stats.service.api.PartitionService;
import com.ericsson.oss.air.pm.stats.test_utils.DriverManagerMock;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.Index;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OutputTableCreatorImplTest {

    @Mock
    DatabaseService databaseServiceMock;
    @Mock
    PartitionService partitionServiceMock;
    @Mock
    KpiExposureService kpiExposureServiceMock;

    @Mock(name = "retentionPeriod")
    EnvironmentValue<Duration> retentionPeriodMock;

    @InjectMocks
    OutputTableCreatorImpl objectUnderTest;

    @Captor
    ArgumentCaptor<List<Partition>> partitionsCaptor;

    @Test
    void shouldCreateDefaultOutputTable() {
        DriverManagerMock.prepare(connectionMock -> {
            final TableCreationInformation tableCreationInformation = TableCreationInformation.of("tableName", "-1", List.of(entity()));
            objectUnderTest.createOutputTable(tableCreationInformation, new HashMap<>());

            verify(databaseServiceMock).createOutputTable(connectionMock, tableCreationInformation, new HashMap<>());
            verifyNoInteractions(partitionServiceMock);
            verify(kpiExposureServiceMock).exposeCalculationTable("tableName");
        });
    }

    @Test
    void shouldThrowException_whenSomethingGoesWrong_onCreateOutputTable() {
        DriverManagerMock.prepare(connectionMock -> {
            final TableCreationInformation tableCreationInformation = TableCreationInformation.of("tableName", "-1", List.of(entity()));

            doThrow(SQLException.class).when(databaseServiceMock).createOutputTable(connectionMock, tableCreationInformation, new HashMap<>());

            Assertions.assertThatThrownBy(() -> objectUnderTest.createOutputTable(tableCreationInformation, new HashMap<>()))
                    .isInstanceOf(KpiPersistenceException.class)
                    .hasMessage("Error creating KPI output table: '%s'", tableCreationInformation.getTableName());

            verify(databaseServiceMock).createOutputTable(connectionMock, tableCreationInformation, new HashMap<>());
            verifyNoInteractions(partitionServiceMock);
            verifyNoInteractions(kpiExposureServiceMock);
        });
    }

    @Test
    void shouldCreateNonDefaultOutputTable() {
        DriverManagerMock.prepare(connectionMock -> {
            final TableCreationInformation tableCreationInformation = TableCreationInformation.of("tableName", "60", List.of(entity()));
            when(retentionPeriodMock.value()).thenReturn(Duration.ofDays(5));

            objectUnderTest.createOutputTable(tableCreationInformation, new HashMap<>());

            verify(databaseServiceMock).createOutputTable(connectionMock, tableCreationInformation, new HashMap<>());
            verify(partitionServiceMock).createPartitions(eq(connectionMock), partitionsCaptor.capture());
            verify(retentionPeriodMock).value();
            verify(kpiExposureServiceMock).exposeCalculationTable("tableName");

            Assertions.assertThat(partitionsCaptor.getValue())
                    .hasSize(8)   /* Singleton lazy initialization of com.ericsson.oss.air.pm.stats.util.RetentionUtils.definedRetentionPeriodDays */
                    .satisfies(partition -> assertPartition(partition, LocalDate.now().minusDays(5)), Index.atIndex(0))
                    .satisfies(partition -> assertPartition(partition, LocalDate.now().minusDays(4)), Index.atIndex(1))
                    .satisfies(partition -> assertPartition(partition, LocalDate.now().minusDays(3)), Index.atIndex(2))
                    .satisfies(partition -> assertPartition(partition, LocalDate.now().minusDays(2)), Index.atIndex(3))
                    .satisfies(partition -> assertPartition(partition, LocalDate.now().minusDays(1)), Index.atIndex(4))
                    .satisfies(partition -> assertPartition(partition, LocalDate.now()), Index.atIndex(5))
                    .satisfies(partition -> assertPartition(partition, LocalDate.now().plusDays(1)), Index.atIndex(6))
                    .satisfies(partition -> assertPartition(partition, LocalDate.now().plusDays(2)), Index.atIndex(7));
        });
    }

    private static void assertPartition(final Partition partition, final LocalDate localDate) {
        Assertions.assertThat(partition.getPartitionName()).matches("tableName_p_\\d{4}_\\d{2}_\\d{2}");
        Assertions.assertThat(partition.getTableName()).isEqualTo("tableName");
        Assertions.assertThat(partition.getStartDate()).as("startDate").isEqualTo(localDate);
        Assertions.assertThat(partition.getEndDate()).as("endDate").isEqualTo(localDate.plusDays(1));
        Assertions.assertThat(partition.getUniqueIndexColumns()).containsExactlyInAnyOrder("aggregation_begin_time");
    }

    static KpiDefinitionEntity entity() {
        return KpiDefinitionEntity.builder().build();
    }
}