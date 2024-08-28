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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import com.ericsson.oss.air.pm.stats._helper.MotherObject;
import com.ericsson.oss.air.pm.stats.calculator.api.exception.SchemaInvalidException;
import com.ericsson.oss.air.pm.stats.service.facade.SchemaRegistryFacade;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import kpi.model.KpiDefinitionRequest;
import kpi.model.RetentionPeriod;
import kpi.model.ScheduledSimple;
import kpi.model.api.enumeration.AggregationType;
import kpi.model.api.table.definition.SimpleKpiDefinitions;
import kpi.model.simple.SimpleTable;
import kpi.model.simple.table.definition.optional.SimpleDefinitionInpDataIdentifier;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SchemaExistenceValidatorTest {

    @Mock
    SchemaRegistryFacade schemaRegistryFacadeMock;

    @InjectMocks
    SchemaExistenceValidator objectUnderTest;

    SimpleDefinitionInpDataIdentifier testDataIdentifier = SimpleDefinitionInpDataIdentifier.of("testDataSpace|testCategory|testParent_schema");
    KpiDefinitionRequest testKpiDefinition = createTestSimpleKpiDefinition();

    @Test
    @SneakyThrows
    void shouldNoErrorReturnWithValidSchema(@Mock List<ParsedSchema> parsedSchemasMock) {

        when(schemaRegistryFacadeMock.getLatestSchemaForSubject(testDataIdentifier)).thenReturn(parsedSchemasMock);
        when(parsedSchemasMock.isEmpty()).thenReturn(false);

        objectUnderTest.validate(testKpiDefinition);

        verify(schemaRegistryFacadeMock).getLatestSchemaForSubject(testDataIdentifier);
        verify(parsedSchemasMock).isEmpty();
    }

    @Test
    @SneakyThrows
    void shouldErrorReturnWithNoSchema() {

        when(schemaRegistryFacadeMock.getLatestSchemaForSubject(testDataIdentifier)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> objectUnderTest.validate(testKpiDefinition))
                .isInstanceOf(SchemaInvalidException.class)
                .hasMessage("Schema 'testParent_schema' is not registered in schema registry.");

        verify(schemaRegistryFacadeMock).getLatestSchemaForSubject(testDataIdentifier);
    }


    @Test
    void shouldReturnWithoutCheckingIfNoSimpleDefinition(@Mock KpiDefinitionRequest KpiDefinitionMock) {

        objectUnderTest.validate(KpiDefinitionMock);

        verifyNoInteractions(schemaRegistryFacadeMock);
    }

    private KpiDefinitionRequest createTestSimpleKpiDefinition() {
        SimpleKpiDefinitions.SimpleKpiDefinition simpleDefinition1 = MotherObject.kpiDefinition("definition1", "expression", "INTEGER", AggregationType.SUM,
                List.of("table.column1", "table.column2"), null, List.of("filter_1", "filter_2"), null, null, null, null);
        SimpleTable simpleTable1 = MotherObject.table(1440, "alias1", List.of("table.column1", "table.column2"), null,
                "testDataSpace|testCategory|testParent_schema", null, null, null, List.of(simpleDefinition1));

        ScheduledSimple simpleKpiDefinition = ScheduledSimple.builder().kpiOutputTables(List.of(simpleTable1)).build();

        return KpiDefinitionRequest.builder()
                .retentionPeriod(RetentionPeriod.of(null))
                .scheduledSimple(simpleKpiDefinition).build();
    }
}
