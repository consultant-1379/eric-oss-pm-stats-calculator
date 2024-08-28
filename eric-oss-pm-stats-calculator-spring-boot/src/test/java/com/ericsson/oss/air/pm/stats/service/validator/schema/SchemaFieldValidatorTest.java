/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.schema;

import static com.ericsson.oss.air.pm.stats._helper.MotherObject.kpiDefinition;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.column;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.reference;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.table;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.SchemaInvalidException;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.JsonPath;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.JsonPathReference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.service.facade.SchemaRegistryFacade;
import com.ericsson.oss.air.pm.stats.service.validator.sql.SqlExtractorService;

import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import kpi.model.ScheduledSimple;
import kpi.model.api.enumeration.AggregationType;
import kpi.model.api.table.definition.SimpleKpiDefinitions.SimpleKpiDefinition;
import kpi.model.simple.table.definition.optional.SimpleDefinitionInpDataIdentifier;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SchemaFieldValidatorTest {
    @Mock
    SqlExtractorService sqlExtractorServiceMock;
    @Mock
    SchemaRegistryFacade schemaRegistryFacadeMock;

    @InjectMocks
    SchemaFieldValidator objectUnderTest;

    @Nested
    class ValidateReferencedSchema {
        @Test
        void shouldPassValidation(@Mock final ScheduledSimple scheduledSimpleMock) {
            final SimpleDefinitionInpDataIdentifier dataIdentifier = SimpleDefinitionInpDataIdentifier.of("dataSpace|category|fact_table");
            final SimpleKpiDefinition simple = simple("kpi_definition", dataIdentifier.value());

            final JsonPathReference jsonPathReference = jsonPathTableColumn(
                    List.of(
                            jsonPath("fact_table", "nodeFDN"),
                            jsonPath("fact_table", "pmCounters", "floatArrayColumn0")
                    ),
                    List.of(
                            reference(null, null, column("nodeFDN"), null),
                            reference(null, table("fact_table"), column("integerColumn0"), null)
                    )
            );

            when(scheduledSimpleMock.definitions()).thenReturn(Set.of(simple));
            when(schemaRegistryFacadeMock.getLatestSchemaForSubject(dataIdentifier)).thenReturn(List.of(schema()));
            when(sqlExtractorServiceMock.extractColumns(simple)).thenReturn(jsonPathReference);

            objectUnderTest.validateReferencedSchema(scheduledSimpleMock);

            verify(scheduledSimpleMock).definitions();
            verify(schemaRegistryFacadeMock).getLatestSchemaForSubject(dataIdentifier);
            verify(sqlExtractorServiceMock).extractColumns(simple);
        }

        @Test
        void shouldFailWhenReferencesAreNotAvailable(@Mock final ScheduledSimple scheduledSimpleMock) {
            final SimpleDefinitionInpDataIdentifier dataIdentifier = SimpleDefinitionInpDataIdentifier.of("dataSpace|category|fact_table");
            final SimpleKpiDefinition simple = simple("kpi_definition", dataIdentifier.value());

            final JsonPathReference jsonPathReference = jsonPathTableColumn(
                    List.of(
                            jsonPath("fact_table", "nodeFDN"), // OK
                            jsonPath("fact_table", "pmCounters", "floatArrayColumn0"), // OK
                            jsonPath("unknown-schema", "unknown-field-1"), // NOK
                            jsonPath("fact_table", "unknown-field") // NOK
                    ),
                    List.of(
                            reference(null, null, column("nodeFDN"), null),    // OK
                            reference(null, table("fact_table"), column("integerColumn0"), null), // OK
                            reference(null, table("fact_table"), column("unknown-field-2"), null), // NOK
                            reference(null, null, column("unknown-field"), null), // NOK
                            reference(null, table("fact_table"), column("unknown-field"), null), // NOK
                            reference(null, table("unknown-table"), column("unknown-field"), null) // NOK
                    )
            );

            when(scheduledSimpleMock.definitions()).thenReturn(Set.of(simple));
            when(schemaRegistryFacadeMock.getLatestSchemaForSubject(dataIdentifier)).thenReturn(List.of(schema()));
            when(sqlExtractorServiceMock.extractColumns(simple)).thenReturn(jsonPathReference);

            Assertions.assertThatThrownBy(() -> objectUnderTest.validateReferencedSchema(scheduledSimpleMock))
                    .isInstanceOf(SchemaInvalidException.class).hasMessage(
                            "KPI Definition with name '%s' for schema '%s' the following references not found: '%s'",
                            simple.name().value(), dataIdentifier.schema(), List.of(
                                    jsonPath("fact_table", "unknown-field"),
                                    reference(null, table("fact_table"), column("unknown-field-2"), null).toString(false),
                                    reference(null, null, column("unknown-field"), null).toString(false),
                                    jsonPath("unknown-schema", "unknown-field-1"),
                                    reference(null, table("unknown-table"), column("unknown-field"), null).toString(false)
                            )
                    );

            verify(scheduledSimpleMock).definitions();
            verify(schemaRegistryFacadeMock).getLatestSchemaForSubject(dataIdentifier);
            verify(sqlExtractorServiceMock).extractColumns(simple);
        }
    }

    static AvroSchema schema() {
        final Schema schema = new Parser().parse(
                "{ " +
                        "  \"namespace\": \"kpi-service\", \"type\": \"record\", \"name\": \"fact_table\", \"fields\": [ " +
                        "    {\"name\": \"nodeFDN\", \"type\": \"int\"}, " +
                        "    {\"name\": \"ropBeginTime\", \"type\": \"string\"}, " +
                        "    {\"name\": \"ropEndTime\", \"type\": \"string\"}, " +
                        "    {\"name\": \"pmCounters\", \"type\": { " +
                        "      \"name\":\"pmCounters\", \"type\": \"record\", \"fields\": [ " +
                        "        {\"name\": \"integerArrayColumn0\", \"type\": [\"null\", {\"type\": \"array\", \"items\": \"int\" }]}, " +
                        "        {\"name\": \"integerArrayColumn1\", \"type\": [\"null\", {\"type\": \"array\", \"items\": \"int\" }]}, " +
                        "        {\"name\": \"integerArrayColumn2\", \"type\": [\"null\", {\"type\": \"array\", \"items\": \"int\" }]}, " +
                        "        {\"name\": \"floatArrayColumn0\", \"type\": [ \"null\", {\"type\": \"array\", \"items\": \"double\" }]}, " +
                        "        {\"name\": \"floatArrayColumn1\", \"type\": [ \"null\", {\"type\": \"array\", \"items\": \"double\" }]}, " +
                        "        {\"name\": \"floatArrayColumn2\", \"type\": [ \"null\", {\"type\": \"array\", \"items\": \"double\" }]}, " +
                        "        {\"name\": \"integerColumn0\", \"type\": \"int\"}, " +
                        "        {\"name\": \"floatColumn0\", \"type\": \"double\"} " +
                        "     ]" +
                        "    }}" +
                        "  ]" +
                        "} "
        );

        return new AvroSchema(schema.toString(true));
    }

    static JsonPathReference jsonPathTableColumn(final List<JsonPath> jsonPaths, final List<Reference> references) {
        final JsonPathReference jsonPathReference = JsonPathReference.of();
        jsonPathReference.addAllJsonPaths(jsonPaths);
        jsonPathReference.addAllReferences(references);
        return jsonPathReference;
    }

    static SimpleKpiDefinition simple(final String name, final String identifier) {
        return kpiDefinition(
                name, "expression", "INTEGER", AggregationType.SUM, List.of("table.column1", "table.column2"),
                null, List.of("filter_1", "filter_2"), null, null, null, identifier
        );
    }

    static JsonPath jsonPath(final String... parts) {
        return JsonPath.of(List.of(parts));
    }
}