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

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_COLLECTION_ID;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiPersistenceException;
import com.ericsson.oss.air.pm.stats.repository.api.TabularParameterRepository;
import com.ericsson.oss.air.pm.stats.test_utils.DriverManagerMock;

import kpi.model.KpiDefinitionRequest;
import kpi.model.OnDemand;
import kpi.model.ondemand.OnDemandTabularParameter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TabularParametersServiceImplTest {

    @Mock TabularParameterRepository parameterRepositoryMock;
    @InjectMocks TabularParameterServiceImpl objectUnderTest;

    @Test
    void shouldSaveTabularParameter(@Mock final KpiDefinitionRequest kpiDefinitionMock, @Mock OnDemand onDemandMock,
                                    @Mock final OnDemandTabularParameter onDemandTabularParameterMock) {
        DriverManagerMock.prepare(connectionMock -> {
            when(kpiDefinitionMock.onDemand()).thenReturn(onDemandMock);
            when(onDemandMock.tabularParameters()).thenReturn(List.of(onDemandTabularParameterMock));

            Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.insert(kpiDefinitionMock, DEFAULT_COLLECTION_ID));

            verify(kpiDefinitionMock).onDemand();
            verify(onDemandMock).tabularParameters();
            verify(parameterRepositoryMock).saveTabularParameter(connectionMock, onDemandTabularParameterMock, DEFAULT_COLLECTION_ID);
        });
    }

    @Test
    void shouldThrowException_whenCannotSave(@Mock final KpiDefinitionRequest kpiDefinitionMock, @Mock OnDemand onDemandMock,
                                             @Mock final OnDemandTabularParameter onDemandTabularParameterMock) {
        DriverManagerMock.prepare(connectionMock -> {
            when(kpiDefinitionMock.onDemand()).thenReturn(onDemandMock);
            when(onDemandMock.tabularParameters()).thenReturn(List.of(onDemandTabularParameterMock));
            doThrow(new SQLException("Already exists")).when(parameterRepositoryMock).saveTabularParameter(connectionMock, onDemandTabularParameterMock, DEFAULT_COLLECTION_ID);

            Assertions.assertThatThrownBy(() -> objectUnderTest.insert(kpiDefinitionMock, DEFAULT_COLLECTION_ID))
                      .hasRootCauseInstanceOf(SQLException.class)
                      .isInstanceOf(KpiPersistenceException.class)
                      .hasMessage("Tabular parameters could not be saved into the table.");

            verify(kpiDefinitionMock).onDemand();
            verify(onDemandMock).tabularParameters();
            verify(parameterRepositoryMock).saveTabularParameter(connectionMock, onDemandTabularParameterMock, DEFAULT_COLLECTION_ID);
        });
    }

    @Test
    void shouldCallRepositoryForFindAllParameters() {
        when(parameterRepositoryMock.findAllTabularParameters()).thenReturn(Collections.emptyList());

        objectUnderTest.findAllTabularParameters();

        verify(parameterRepositoryMock).findAllTabularParameters();
    }
}