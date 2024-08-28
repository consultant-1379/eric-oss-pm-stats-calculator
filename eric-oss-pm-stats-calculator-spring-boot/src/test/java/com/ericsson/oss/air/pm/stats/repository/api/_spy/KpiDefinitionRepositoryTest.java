/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.api._spy;

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_COLLECTION_ID;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.exception.EntityNotFoundException;
import com.ericsson.oss.air.pm.stats.repository.api.KpiDefinitionRepository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KpiDefinitionRepositoryTest {
    @Spy
    KpiDefinitionRepository objectUnderTest;

    @Nested
    class ForceFindByName {
        @Test
        void shouldFind(@Mock final KpiDefinitionEntity kpiDefinitionEntityMock) {
            doReturn(Optional.of(kpiDefinitionEntityMock)).when(objectUnderTest).findByName("definitionName", DEFAULT_COLLECTION_ID);

            final KpiDefinitionEntity actual = objectUnderTest.forceFindByName("definitionName", DEFAULT_COLLECTION_ID);

            verify(objectUnderTest).forceFindByName("definitionName", DEFAULT_COLLECTION_ID);

            Assertions.assertThat(actual).isEqualTo(kpiDefinitionEntityMock);
        }

        @Test
        void shouldRaiseException_whenEntityIsNotFoundByName() {
            doReturn(Optional.empty()).when(objectUnderTest).findByName("definitionName", DEFAULT_COLLECTION_ID);

            Assertions.assertThatThrownBy(() -> objectUnderTest.forceFindByName("definitionName", DEFAULT_COLLECTION_ID))
                    .isExactlyInstanceOf(EntityNotFoundException.class)
                    .hasMessage("KPI was not found by name 'definitionName'");

            verify(objectUnderTest).forceFindByName("definitionName", DEFAULT_COLLECTION_ID);
        }
    }
}