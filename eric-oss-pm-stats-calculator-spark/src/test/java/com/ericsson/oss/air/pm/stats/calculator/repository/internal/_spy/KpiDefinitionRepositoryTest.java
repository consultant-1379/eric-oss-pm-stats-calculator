/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.repository.internal._spy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.util.Optional;

import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.model.exception.EntityNotFoundException;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.KpiDefinitionRepository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KpiDefinitionRepositoryTest {

    @Spy KpiDefinitionRepository objectUnderTest;

    @Test
    void shouldFind(@Mock KpiDefinition kpiDefinitionMock) {
        doReturn(Optional.of(kpiDefinitionMock)).when(objectUnderTest).findByName("kpiName");

        KpiDefinition actual = objectUnderTest.forceFindByName("kpiName");

        assertThat(actual).isEqualTo(kpiDefinitionMock);
    }

    @Test
    void shouldThrowException() {
        doReturn(Optional.empty()).when(objectUnderTest).findByName("kpiName");

        Assertions.assertThatThrownBy(() ->objectUnderTest.forceFindByName("kpiName"))
                .isExactlyInstanceOf(EntityNotFoundException.class)
                .hasMessage("KpiDefinition with name: kpiName not found");
    }
}
