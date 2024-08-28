/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service;

import static org.mockito.Mockito.verify;

import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.repository.internal.DimensionTablesRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DimensionTablesServiceImplTest {

    @Mock DimensionTablesRepository dimensionRepositoryMock;

    @InjectMocks DimensionTablesServiceImpl objectUnderTest;

    @Test
    void shouldCallRepository() {
        final UUID calculationId = UUID.fromString("0fe30eba-71f5-4265-9584-35fecc65a0d1");

        objectUnderTest.getTabularParameterTableNamesByCalculationId(calculationId);

        verify(dimensionRepositoryMock).findTableNamesByCalculationId(calculationId);
    }

}