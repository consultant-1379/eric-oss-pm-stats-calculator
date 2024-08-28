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

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static lombok.AccessLevel.PUBLIC;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.SchemaInvalidException;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.JsonPath;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.JsonPathReference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.service.facade.SchemaRegistryFacade;
import com.ericsson.oss.air.pm.stats.service.validator.schema.util.Schemas;
import com.ericsson.oss.air.pm.stats.service.validator.sql.SqlExtractorService;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import kpi.model.ScheduledSimple;
import kpi.model.api.table.definition.SimpleKpiDefinitions.SimpleKpiDefinition;
import kpi.model.api.table.definition.api.InpDataIdentifierAttribute;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class SchemaFieldValidator {
    @Inject
    private SqlExtractorService sqlExtractorService;
    @Inject
    private SchemaRegistryFacade schemaRegistryFacade;

    public void validateReferencedSchema(@NonNull final ScheduledSimple scheduledSimple) {
        scheduledSimple.definitions().forEach(kpiDefinition -> {
            final InpDataIdentifierAttribute inpDataIdentifier = kpiDefinition.inpDataIdentifier();

            final List<ParsedSchema> parsedSchemas = schemaRegistryFacade.getLatestSchemaForSubject(inpDataIdentifier);
            final JsonPathReference jsonPathReference = sqlExtractorService.extractColumns((SimpleKpiDefinition) kpiDefinition);

            final Set<JsonPath> nonMatchingJsonPaths = validateJsonPaths(parsedSchemas, jsonPathReference.jsonPaths());
            final Set<Reference> nonMatchingReferences = validateTableColumns(inpDataIdentifier.schema(), parsedSchemas, jsonPathReference.references());

            if (isNotEmpty(nonMatchingJsonPaths) || isNotEmpty(nonMatchingReferences)) {
                throw new SchemaInvalidException(format(
                        "KPI Definition with name '%s' for schema '%s' the following references not found: '%s'",
                        kpiDefinition.name().value(), inpDataIdentifier.schema(), merge(nonMatchingJsonPaths, nonMatchingReferences)
                ));
            }

            log.info(
                    "KPI Definition with name '{}' has valid references for '{}' (checked references: '{}')",
                    kpiDefinition.name().value(), inpDataIdentifier.value(), merge(jsonPathReference.jsonPaths(), jsonPathReference.references())
            );
        });
    }

    private static String merge(final Collection<JsonPath> jsonPaths, final Collection<Reference> references) {
        final Stream<String> jsonPathsStream = jsonPaths.stream().map(Object::toString);
        final Stream<String> referenceStream = references.stream().map(reference -> reference.toString(false));

        return Stream.concat(jsonPathsStream, referenceStream)
                .distinct().sorted()
                .collect(joining(", ", "[", "]"));
    }

    private Set<JsonPath> validateJsonPaths(final List<ParsedSchema> parsedSchemas, @NonNull final Collection<JsonPath> jsonPaths) {
        final Set<JsonPath> nonMatchingReferences = new HashSet<>();

        for (final JsonPath jsonPath : jsonPaths) {
            if (!isPathInSchemas(parsedSchemas, jsonPath)) {
                nonMatchingReferences.add(jsonPath);
            }
        }

        return nonMatchingReferences;
    }

    private static boolean isPathInSchemas(@NonNull final List<ParsedSchema> parsedSchemas, final JsonPath jsonPath) {
        return parsedSchemas.stream()
                .map(ParsedSchema::rawSchema)
                .map(Schema.class::cast)
                .anyMatch(schema -> Schemas.isPathContained(schema, jsonPath));
    }

    private Set<Reference> validateTableColumns(final String schema, final List<ParsedSchema> parsedSchemas, final Collection<Reference> references) {
        final Set<Reference> nonMatchingReferences = new HashSet<>();
        final Set<String> fieldNames = extractFieldNames(parsedSchemas);

        for (final Reference reference : references) {
            reference.column().ifPresent(column -> {
                if (fieldNames.contains(column.getName())) {
                    reference.table().ifPresent(table -> {
                        if (!table.getName().equals(schema)) {
                            nonMatchingReferences.add(reference);
                        }
                    });
                } else {
                    nonMatchingReferences.add(reference);
                }
            });
        }

        return nonMatchingReferences;
    }

    private static Set<String> extractFieldNames(@NonNull final List<ParsedSchema> parsedSchemas) {
        return parsedSchemas.stream()
                .map(ParsedSchema::rawSchema)
                .map(Schema.class::cast)
                .map(Schemas::computeFields)
                .flatMap(Collection::stream)
                .map(Field::name)
                .collect(toSet());
    }
}
