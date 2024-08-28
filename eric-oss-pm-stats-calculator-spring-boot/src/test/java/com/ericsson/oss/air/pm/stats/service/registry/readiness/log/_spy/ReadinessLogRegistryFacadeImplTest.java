/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.registry.readiness.log._spy;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats._util.TestHelpers;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;
import com.ericsson.oss.air.pm.stats.service.api.registry.ReadinessLogRegistry;
import com.ericsson.oss.air.pm.stats.service.registry.readiness.log.ReadinessLogRegistryFacadeImpl;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReadinessLogRegistryFacadeImplTest {

    @Spy
    ReadinessLogRegistryFacadeImpl objectUnderTest;

    @Test
    void findByCalculationId(@Mock final ReadinessLogRegistry readinessLogRegistryMock, @Mock final List<ReadinessLog> readinessLogsMock) {
        final UUID uuid = TestHelpers.uuid("51547f26-d761-40fa-9755-29fa4bb28c3e");

        doReturn(readinessLogRegistryMock).when(objectUnderTest).readinessLogRegistry(uuid);
        doReturn(readinessLogsMock).when(readinessLogRegistryMock).findByCalculationId(uuid);

        final List<ReadinessLog> actual = objectUnderTest.findByCalculationId(uuid);

        verify(objectUnderTest).readinessLogRegistry(uuid);
        verify(readinessLogRegistryMock).findByCalculationId(uuid);

        Assertions.assertThat(actual).isEqualTo(readinessLogsMock);
    }
}