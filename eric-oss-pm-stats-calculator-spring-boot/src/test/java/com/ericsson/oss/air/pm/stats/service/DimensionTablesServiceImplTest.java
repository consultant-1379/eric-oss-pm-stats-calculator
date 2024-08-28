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

import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.repository.DimensionTablesRepositoryImpl;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DimensionTablesServiceImplTest {

    @Mock
    DimensionTablesRepositoryImpl dimensionTablesRepositoryMock;

    @InjectMocks
    DimensionTablesServiceImpl objectUnderTest;

    @Test
    @SneakyThrows
    void shouldSave(@Mock Connection connectionMock) {
        final UUID calculationId = UUID.fromString("812f4d8a-6c8f-4dcd-b278-e4dfadbacc7f");

        objectUnderTest.save(connectionMock, List.of(), calculationId);

        verify(dimensionTablesRepositoryMock).save(connectionMock, List.of(), calculationId);
    }

    @Test
    void shouldFind() {
        final UUID calculationId = UUID.fromString("812f4d8a-6c8f-4dcd-b278-e4dfadbacc7f");

        objectUnderTest.findTableNamesForCalculation(calculationId);

        verify(dimensionTablesRepositoryMock).findTableNamesForCalculation(calculationId);
    }

    @Test
    void shouldFindAllTableNames() {
        objectUnderTest.findLostTableNames();
        verify(dimensionTablesRepositoryMock).findLostTableNames();
    }
}
