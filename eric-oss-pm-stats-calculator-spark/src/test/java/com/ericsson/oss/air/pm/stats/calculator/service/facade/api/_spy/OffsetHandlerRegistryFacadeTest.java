/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.facade.api._spy;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.service.facade.api.OffsetHandlerRegistryFacade;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.OffsetHandlerKafka;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.OffsetHandlerPostgres;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OffsetHandlerRegistryFacadeTest {
    @Spy OffsetHandlerRegistryFacade objectUnderTest;

    @Test
    void shouldCalculateOffsets(@Mock final OffsetHandlerPostgres offsetHandlerPostgresMock) {
        final Set<KpiDefinition> kpiDefinitions = Collections.emptySet();

        doReturn(offsetHandlerPostgresMock).when(objectUnderTest).offsetHandler();

        objectUnderTest.calculateOffsets(kpiDefinitions);

        verify(objectUnderTest).offsetHandler();
        verify(offsetHandlerPostgresMock).calculateOffsets(kpiDefinitions);
    }


    @Test
    void shouldSaveOffsets(@Mock final OffsetHandlerKafka offsetHandlerKafkaMock) {
        doReturn(offsetHandlerKafkaMock).when(objectUnderTest).offsetHandler();

        objectUnderTest.saveOffsets();

        verify(objectUnderTest).offsetHandler();
        verify(offsetHandlerKafkaMock).saveOffsets();
    }

}