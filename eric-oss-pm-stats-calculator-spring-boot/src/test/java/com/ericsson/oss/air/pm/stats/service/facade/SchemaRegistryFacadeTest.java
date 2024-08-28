/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.SchemaRegistryUnreachableException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity.KpiDefinitionEntityBuilder;
import com.ericsson.oss.air.pm.stats.service.util.SchemaSubjectHelper;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import kpi.model.simple.table.definition.optional.SimpleDefinitionInpDataIdentifier;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SchemaRegistryFacadeTest {

    @Mock
    SchemaRegistryClient schemaRegistryClientMock;
    @Mock
    SchemaSubjectHelper schemaSubjectHelperMock;

    @InjectMocks
    SchemaRegistryFacade objectUnderTest = Mockito.spy(new SchemaRegistryFacade());

    @Test
    @SneakyThrows
    void givesBackSchemasForDefinitions() {
        final DataIdentifier dataIdentifier = DataIdentifier.of("dataSpace|category|schema");
        final ParsedSchema schema = new AvroSchema("{\"type\":\"record\",\"name\":\"schemaName\",\"fields\":[]}");

        when(schemaSubjectHelperMock.createSubjectRepresentation(dataIdentifier)).thenReturn("namespace.schema");
        doReturn(List.of(schema)).when(objectUnderTest).getLatestSchemaForSubject("namespace.schema");

        Set<ParsedSchema> actual = objectUnderTest.getSchemasForDefinitions(List.of(entity("dataSpace", "category", "schema")));

        verify(schemaSubjectHelperMock).createSubjectRepresentation(dataIdentifier);
        verify(objectUnderTest).getLatestSchemaForSubject("namespace.schema");

        assertThat(actual).containsExactly(schema);
    }

    @Test
    @SneakyThrows
    void throwsExceptionWhenCantReach() {
        when(schemaRegistryClientMock.getSchemas("subject", false, true)).thenThrow(IOException.class);

        assertThatThrownBy(() -> objectUnderTest.getLatestSchemaForSubject("subject"))
                .hasRootCauseExactlyInstanceOf(IOException.class)
                .isInstanceOf(SchemaRegistryUnreachableException.class)
                .hasMessage("Schema Registry is not reachable");

        verify(schemaRegistryClientMock).getSchemas("subject", false, true);
    }

    @Test
    @SneakyThrows
    void givesBackLatestSchemaForSubject() {
        when(schemaRegistryClientMock.getSchemas("subject", false, true)).thenReturn(Collections.emptyList());

        List<ParsedSchema> actual = objectUnderTest.getLatestSchemaForSubject("subject");

        verify(schemaRegistryClientMock).getSchemas("subject", false, true);

        assertThat(actual).isEmpty();
    }

    @Test
    @SneakyThrows
    void givesBackLatestSchemaForDataIdentifier(@Mock SimpleDefinitionInpDataIdentifier dataIdentifierMock) {
        when(schemaSubjectHelperMock.createSubjectRepresentation(dataIdentifierMock)).thenReturn("subject");
        when(schemaRegistryClientMock.getSchemas("subject", false, true)).thenReturn(Collections.emptyList());

        List<ParsedSchema> actual = objectUnderTest.getLatestSchemaForSubject(dataIdentifierMock);

        verify(schemaSubjectHelperMock).createSubjectRepresentation(dataIdentifierMock);
        verify(schemaRegistryClientMock).getSchemas("subject", false, true);

        assertThat(actual).isEmpty();
    }

    static KpiDefinitionEntity entity(final String dataSpace, final String catalog, final String schema) {
        final KpiDefinitionEntityBuilder builder = KpiDefinitionEntity.builder();
        builder.withSchemaDataSpace(dataSpace);
        builder.withSchemaCategory(catalog);
        builder.withSchemaName(schema);
        return builder.build();
    }
}