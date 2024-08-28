/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.AggregationElement;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.TableCreationInformation;
import com.ericsson.oss.air.pm.stats.service.api.DatabaseService;
import com.ericsson.oss.air.pm.stats.service.api.ParameterService;
import com.ericsson.oss.air.pm.stats.service.facade.SchemaRegistryFacade;
import com.ericsson.oss.air.pm.stats.utils.DefinitionUtils;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class AggregationElementUtils {

    private final SchemaRegistryFacade schemaRegistryFacade;
    private final ParameterService parameterService;
    private final DatabaseService databaseService;

    public Map<String, KpiDataType> collectValidAggregationElements(final TableCreationInformation tableCreationInformation) {
        final Set<KpiDefinitionEntity> simpleDefinitions = tableCreationInformation.collectSimpleDefinitions();
        final Set<KpiDefinitionEntity> notSimpleDefinitions = tableCreationInformation.collectNotSimpleDefinitions();
        final Set<String> aggregationElements = tableCreationInformation.collectAggregationElements();
        final Set<String> simpleAggregationElements = new HashSet<>();

        Map<String, KpiDataType> aggregationsElementAndTypes = new HashMap<>();
        if (!simpleDefinitions.isEmpty()){
            final Map<String, KpiDataType> aggregationElementDataFromSimples = collectSimpleAggregationElementColumnNameDataType(simpleDefinitions, aggregationElements);
            simpleAggregationElements.addAll(aggregationElementDataFromSimples.keySet());
            aggregationsElementAndTypes.putAll(aggregationElementDataFromSimples);
        }
        if (!notSimpleDefinitions.isEmpty()) {
            aggregationsElementAndTypes.putAll(collectNotSimpleAggregationElementColumnNameDataType(notSimpleDefinitions, simpleAggregationElements));
        }

        return aggregationsElementAndTypes;
    }

    //TODO: should be private
    public Map<String, KpiDataType> collectSimpleAggregationElementColumnNameDataType(final Collection<KpiDefinitionEntity> simpleDefinitions, final Set<String> aggregationElements) {
        final Set<ParsedSchema> parsedSchemas = schemaRegistryFacade.getSchemasForDefinitions(simpleDefinitions);
        final Map<String, KpiDataType> validSimpleAggregationElements = new HashMap<>();

        for (final KpiDefinitionEntity definition : simpleDefinitions) {
            ParsedSchema schema = parsedSchemas.stream()
                    .filter(parsedSchema -> definition.schemaName().equals(((Schema) parsedSchema.rawSchema()).getName()))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Could not find the schema"));

            ((Schema) schema.rawSchema()).getFields().stream()
                    .filter(field -> aggregationElements.contains(field.name()))
                    .forEach(field -> validSimpleAggregationElements.put(field.name(), KpiDataType.forSchemaType(field.schema())));
        }
        return validSimpleAggregationElements;
    }

    //TODO: should be private
    public Map<String, KpiDataType> collectNotSimpleAggregationElementColumnNameDataType(final Collection<KpiDefinitionEntity> definitions, final Set<String> simpleElements) {
        final Map<String, KpiDataType> result = DefinitionUtils.getDefaultElementsAndTypes(definitions);

        final List<AggregationElement> aggregationElementsWithKpiSource = DefinitionUtils.getAggregationElementsWithKpiSource(definitions, simpleElements);
        result.putAll(databaseService.findAggregationElementColumnType(aggregationElementsWithKpiSource));

        final List<AggregationElement> aggregationElementsWithTabularParameterDataSource = DefinitionUtils.getAggregationElementsWithTabularParameterDataSource(definitions);
        result.putAll(parameterService.findAggregationElementTypeForTabularParameter(aggregationElementsWithTabularParameterDataSource));

        return result;
    }
}
