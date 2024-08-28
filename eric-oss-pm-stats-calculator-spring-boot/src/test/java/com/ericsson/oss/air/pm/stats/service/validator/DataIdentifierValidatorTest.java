/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Set;

import com.ericsson.oss.air.pm.stats.cache.SchemaDetailCache;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail;
import com.ericsson.oss.air.pm.stats.model.exception.DataCatalogException;
import com.ericsson.oss.air.pm.stats.model.exception.DataCatalogUnreachableException;
import com.ericsson.oss.air.pm.stats.service.helper.DataCatalogReader;

import kpi.model.ScheduledSimple;
import kpi.model.api.table.definition.SimpleKpiDefinitions.SimpleKpiDefinition;
import kpi.model.simple.table.definition.optional.SimpleDefinitionInpDataIdentifier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataIdentifierValidatorTest {
    @Mock
    DataCatalogReader dataCatalogReaderMock;
    @Mock
    SchemaDetailCache schemaDetailCacheMock;

    @InjectMocks
    DataIdentifierValidator objectUnderTest;

    SimpleDefinitionInpDataIdentifier dataIdentifierAttribute1;
    SimpleDefinitionInpDataIdentifier dataIdentifierAttribute2;
    SimpleDefinitionInpDataIdentifier dataIdentifierAttribute3;
    SimpleDefinitionInpDataIdentifier dataIdentifierAttribute4;
    ScheduledSimple scheduledSimpleTablesMock;

    @BeforeEach
    void setUp() {
        final SimpleKpiDefinition simpleKpiDefinitionMock1 = mock(SimpleKpiDefinition.class);
        final SimpleKpiDefinition simpleKpiDefinitionMock2 = mock(SimpleKpiDefinition.class);
        final SimpleKpiDefinition simpleKpiDefinitionMock3 = mock(SimpleKpiDefinition.class);
        final SimpleKpiDefinition simpleKpiDefinitionMock4 = mock(SimpleKpiDefinition.class);

        dataIdentifierAttribute1 = SimpleDefinitionInpDataIdentifier.of("dataSpace|category|schema1");
        dataIdentifierAttribute2 = SimpleDefinitionInpDataIdentifier.of("dataSpace|category|schema2");
        dataIdentifierAttribute3 = SimpleDefinitionInpDataIdentifier.of("dataSpace|category|schema3");
        dataIdentifierAttribute4 = SimpleDefinitionInpDataIdentifier.of("dataSpace|category|schema4");

        scheduledSimpleTablesMock = mock(ScheduledSimple.class);

        when(scheduledSimpleTablesMock.definitions()).thenReturn(Set.of(
                simpleKpiDefinitionMock1, simpleKpiDefinitionMock2, simpleKpiDefinitionMock3, simpleKpiDefinitionMock4
        ));

        when(simpleKpiDefinitionMock1.inpDataIdentifier()).thenReturn(dataIdentifierAttribute1);
        when(simpleKpiDefinitionMock2.inpDataIdentifier()).thenReturn(dataIdentifierAttribute2);
        when(simpleKpiDefinitionMock3.inpDataIdentifier()).thenReturn(dataIdentifierAttribute3);
        when(simpleKpiDefinitionMock4.inpDataIdentifier()).thenReturn(dataIdentifierAttribute4);
    }

    @Test
    void shouldValidateWithoutCache(@Mock final SchemaDetail schemaDetailMock) {
        when(schemaDetailCacheMock.hasValue(dataIdentifierAttribute1)).thenReturn(false);
        when(schemaDetailCacheMock.hasValue(dataIdentifierAttribute2)).thenReturn(false);
        when(schemaDetailCacheMock.hasValue(dataIdentifierAttribute3)).thenReturn(false);
        when(schemaDetailCacheMock.hasValue(dataIdentifierAttribute4)).thenReturn(false);
        when(dataCatalogReaderMock.getDetails(DataIdentifier.of(dataIdentifierAttribute1.value()))).thenReturn(schemaDetailMock);
        when(dataCatalogReaderMock.getDetails(DataIdentifier.of(dataIdentifierAttribute2.value()))).thenReturn(schemaDetailMock);
        when(dataCatalogReaderMock.getDetails(DataIdentifier.of(dataIdentifierAttribute3.value()))).thenReturn(schemaDetailMock);
        when(dataCatalogReaderMock.getDetails(DataIdentifier.of(dataIdentifierAttribute4.value()))).thenReturn(schemaDetailMock);

        assertDoesNotThrow(() -> objectUnderTest.validateDataIdentifiers(scheduledSimpleTablesMock));

        verify(schemaDetailCacheMock).hasValue(dataIdentifierAttribute1);
        verify(schemaDetailCacheMock).hasValue(dataIdentifierAttribute2);
        verify(schemaDetailCacheMock).hasValue(dataIdentifierAttribute3);
        verify(schemaDetailCacheMock).hasValue(dataIdentifierAttribute4);
        verify(dataCatalogReaderMock).getDetails(DataIdentifier.of(dataIdentifierAttribute1.value()));
        verify(dataCatalogReaderMock).getDetails(DataIdentifier.of(dataIdentifierAttribute2.value()));
        verify(dataCatalogReaderMock).getDetails(DataIdentifier.of(dataIdentifierAttribute3.value()));
        verify(dataCatalogReaderMock).getDetails(DataIdentifier.of(dataIdentifierAttribute4.value()));
        verify(schemaDetailCacheMock).put(dataIdentifierAttribute1, schemaDetailMock);
        verify(schemaDetailCacheMock).put(dataIdentifierAttribute2, schemaDetailMock);
        verify(schemaDetailCacheMock).put(dataIdentifierAttribute3, schemaDetailMock);
        verify(schemaDetailCacheMock).put(dataIdentifierAttribute4, schemaDetailMock);
    }

    @Test
    void shouldValidateWithCache() {
        when(schemaDetailCacheMock.hasValue(dataIdentifierAttribute1)).thenReturn(true);
        when(schemaDetailCacheMock.hasValue(dataIdentifierAttribute2)).thenReturn(true);
        when(schemaDetailCacheMock.hasValue(dataIdentifierAttribute3)).thenReturn(true);
        when(schemaDetailCacheMock.hasValue(dataIdentifierAttribute4)).thenReturn(true);

        assertDoesNotThrow(() -> objectUnderTest.validateDataIdentifiers(scheduledSimpleTablesMock));

        verify(schemaDetailCacheMock).hasValue(dataIdentifierAttribute1);
        verify(schemaDetailCacheMock).hasValue(dataIdentifierAttribute2);
        verify(schemaDetailCacheMock).hasValue(dataIdentifierAttribute3);
        verify(schemaDetailCacheMock).hasValue(dataIdentifierAttribute4);
        verifyNoInteractions(dataCatalogReaderMock);
        verifyNoMoreInteractions(schemaDetailCacheMock);
    }

    @Test
    void shouldThrowDataCatalogException_whenDataCatalogIsUnreachable() {
        when(schemaDetailCacheMock.hasValue(dataIdentifierAttribute1)).thenReturn(false);
        when(dataCatalogReaderMock.getDetails(DataIdentifier.of(dataIdentifierAttribute1.value()))).thenThrow(DataCatalogException.class);

        Assertions.assertThatThrownBy(() -> objectUnderTest.validateDataIdentifiers(scheduledSimpleTablesMock))
                .isInstanceOf(DataCatalogUnreachableException.class)
                .hasMessage("DataCatalog is not reachable, data identifiers cannot be validated");

        verify(schemaDetailCacheMock).hasValue(dataIdentifierAttribute1);
        verify(dataCatalogReaderMock).getDetails(DataIdentifier.of(dataIdentifierAttribute1.value()));
        verifyNoMoreInteractions(schemaDetailCacheMock);
    }

    @Test
    void shouldThrowDataCatalogException_whenDataCatalogHasOneOrMoreMissingIdentifier() {
        when(schemaDetailCacheMock.hasValue(dataIdentifierAttribute1)).thenReturn(false);
        when(schemaDetailCacheMock.hasValue(dataIdentifierAttribute2)).thenReturn(false);
        when(schemaDetailCacheMock.hasValue(dataIdentifierAttribute3)).thenReturn(true);
        when(schemaDetailCacheMock.hasValue(dataIdentifierAttribute4)).thenReturn(false);
        when(dataCatalogReaderMock.getDetails(DataIdentifier.of(dataIdentifierAttribute1.value()))).thenThrow(RuntimeException.class);
        when(dataCatalogReaderMock.getDetails(DataIdentifier.of(dataIdentifierAttribute2.value()))).thenThrow(RuntimeException.class);
        when(dataCatalogReaderMock.getDetails(DataIdentifier.of(dataIdentifierAttribute4.value()))).thenThrow(RuntimeException.class);

        Assertions.assertThatThrownBy(() -> objectUnderTest.validateDataIdentifiers(scheduledSimpleTablesMock))
                .isInstanceOf(DataCatalogException.class).hasMessage(
                        "There is no entry available in the DataCatalog for identifier(s): " +
                                "'dataSpace|category|schema1', " +
                                "'dataSpace|category|schema2', " +
                                "'dataSpace|category|schema4'"
                );

        verify(schemaDetailCacheMock).hasValue(dataIdentifierAttribute1);
        verify(schemaDetailCacheMock).hasValue(dataIdentifierAttribute2);
        verify(schemaDetailCacheMock).hasValue(dataIdentifierAttribute3);
        verify(schemaDetailCacheMock).hasValue(dataIdentifierAttribute4);
        verify(dataCatalogReaderMock).getDetails(DataIdentifier.of(dataIdentifierAttribute1.value()));
        verify(dataCatalogReaderMock).getDetails(DataIdentifier.of(dataIdentifierAttribute2.value()));
        verify(dataCatalogReaderMock).getDetails(DataIdentifier.of(dataIdentifierAttribute4.value()));
        verifyNoMoreInteractions(schemaDetailCacheMock);
    }
}
