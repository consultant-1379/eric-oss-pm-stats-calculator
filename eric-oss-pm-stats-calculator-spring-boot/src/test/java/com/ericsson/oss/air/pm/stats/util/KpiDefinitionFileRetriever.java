/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiRetrievalException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.common.resources.utils.ResourceLoaderUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Class that loads all KPI definitions from definitions file defined in KpiDefinitionParser.java.
 */
@Slf4j
public class KpiDefinitionFileRetriever {

    private static final String KPI_DEFINITIONS_SOURCE_FILE = "RequiredKpis.json";
    private static final String FAILURE_LOADING_JSON_FILE_ERROR = "Failure loading json file";

    final ObjectMapper objectMapper = new ObjectMapper();

    public Set<KpiDefinition> retrieveAllKpiDefinitions() throws KpiRetrievalException {
        final String definitionsAsString;
        try {
            definitionsAsString = ResourceLoaderUtils.getClasspathResourceAsString(KPI_DEFINITIONS_SOURCE_FILE);
        } catch (final IOException e) {
            log.error("Failure loading json file", e);
            throw new KpiRetrievalException(FAILURE_LOADING_JSON_FILE_ERROR, e);
        }

        return parseAllKpiDefinitions(definitionsAsString);
    }

    public Set<KpiDefinition> retrieveNamedKpiDefinitions(final Collection<String> kpiDefinitionNames) throws KpiRetrievalException {
        final String definitionsAsString;
        try {
            definitionsAsString = ResourceLoaderUtils.getClasspathResourceAsString(KPI_DEFINITIONS_SOURCE_FILE);
        } catch (final IOException e) {
            log.error("Failure loading json file", e);
            throw new KpiRetrievalException(FAILURE_LOADING_JSON_FILE_ERROR, e);
        }

        return parseNamedKpiDefinitions(definitionsAsString, kpiDefinitionNames);
    }

    Set<KpiDefinition> parseNamedKpiDefinitions(final String definitionsAsString, final Collection<String> kpiDefinitionNames) {
        final Set<KpiDefinition> definitions = new HashSet<>();
        try {
            final JsonElement kpiDefinitionJsonElement = JsonParser.parseString(definitionsAsString);
            final JsonArray jsonArray = kpiDefinitionJsonElement.getAsJsonObject().get("kpi_definitions").getAsJsonArray();

            for (final JsonElement jsonElement : jsonArray) {
                final Optional<KpiDefinition> namedDefinition = parseNamedDefinition(jsonElement, kpiDefinitionNames);
                namedDefinition.ifPresent(definitions::add);
            }
        } catch (final Exception e) { //NOSONAR Exception is suitably logged
            log.warn("Error parsing JSON - {}", e.getClass());
        }

        return definitions;
    }

    Set<KpiDefinition> parseAllKpiDefinitions(final String definitionsAsString) {
        final Set<KpiDefinition> definitions = new HashSet<>();
        try {
            final JsonElement kpiDefinitionJsonElement = JsonParser.parseString(definitionsAsString);
            final JsonArray jsonArray = kpiDefinitionJsonElement.getAsJsonObject().get("kpi_definitions").getAsJsonArray();

            for (final JsonElement jsonElement : jsonArray) {
                final Optional<KpiDefinition> definition = parseDefinition(jsonElement);
                definition.ifPresent(definitions::add);
            }
        } catch (final Exception e) { //NOSONAR Exception is suitably logged
            log.warn("Error parsing JSON - {}", e.getClass());
        }

        return definitions;
    }

    Optional<KpiDefinition> parseDefinition(final JsonElement jsonElement) {
        try {
            final String jsonElementString = jsonElement.toString();
            final KpiDefinition kpiDef = objectMapper.readValue(jsonElementString, KpiDefinition.class);
            final Optional<KpiDefinition> kpiDefinition = Optional.of(kpiDef);
            kpiDefinition.get().setAggregationElements(removeSingleQuoteEscapeCharacterFromAggregationElements(kpiDefinition.get().getAggregationElements()));
            return kpiDefinition;
        } catch (final IllegalArgumentException | IOException e) { //NOSONAR Exception is suitably logged
            log.warn("Failed to parse KPI - {}", e.getClass());
        }
        return Optional.empty();
    }

    Optional<KpiDefinition> parseNamedDefinition(final JsonElement jsonElement, final Collection<String> kpiDefinitionNames) {
        try {
            final Optional<KpiDefinition> kpiDefinition = Optional.of(objectMapper.readValue(jsonElement.toString(), KpiDefinition.class));
            if (kpiDefinitionNames.contains(kpiDefinition.get().getName())) {
                return kpiDefinition;
            }
        } catch (final IllegalArgumentException | IOException e) { //NOSONAR Exception is suitably logged
            log.warn("Failed to parse KPI - {}", e.getClass());
        }
        return Optional.empty();
    }

    static List<String> removeSingleQuoteEscapeCharacterFromAggregationElements(final List<String> aggregationElements) {
        final List<String> aggregationElementsWithSingleQuoteEscapeRemoved = new ArrayList<>();
        for (final String aggregationElement : aggregationElements) {
            if (aggregationElement.contains("''")) {
                final String stringMinusSingleQuoteEscapeCharacter = aggregationElement.replace("''", "'");
                aggregationElementsWithSingleQuoteEscapeRemoved.add(stringMinusSingleQuoteEscapeCharacter);
            } else {
                aggregationElementsWithSingleQuoteEscapeRemoved.add(aggregationElement);
            }
        }
        return aggregationElementsWithSingleQuoteEscapeRemoved;
    }
}