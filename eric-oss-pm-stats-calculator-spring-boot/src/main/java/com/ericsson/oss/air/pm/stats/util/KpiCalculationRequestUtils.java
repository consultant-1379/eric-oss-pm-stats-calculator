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

import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.DEFINITION_FILTER_KEY;
import static com.ericsson.oss.air.pm.stats.util.KpiDefinitionUtils.DEFINITION_NAME_KEY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.calculator.api.model.Definition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload;
import com.ericsson.oss.air.pm.stats.calculator.api.model.SchemaAttribute;
import com.ericsson.oss.air.pm.stats.calculator.api.model.SchemaModel;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.KpiModel;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.model.KpiSchemaValidator;
import com.ericsson.oss.air.pm.stats.model.metadata.JsonSchemaLoader;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;

/**
 * Utility class to support KPI calculation request validation and submission.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KpiCalculationRequestUtils {

    private static final String KPI_SCHEMA = "kpiSchema.json";

    /**
     * Verifies a {@link KpiCalculationRequestPayload} contains a valid collection of KPI names.
     *
     * @param kpiCalculationRequestPayload a {@link KpiCalculationRequestPayload} that has been proposed for calculation.
     * @return true if the KPI names collection is not null empty or contains empty string.
     */
    public static boolean payloadContainsEmptyKpiNames(final KpiCalculationRequestPayload kpiCalculationRequestPayload) {
        final Set<String> kpiNames = kpiCalculationRequestPayload.getKpiNames();
        return kpiNames.isEmpty() || kpiNames.contains(StringUtils.EMPTY);
    }

    /**
     * Verifies a {@link KpiCalculationRequestPayload} contains all required attributes.
     *
     * @param kpiCalculationRequestPayload a {@link KpiCalculationRequestPayload} that has been proposed for calculation.
     * @return true if the KPI names collection null or empty or if no source is provided
     */
    public static boolean payloadContainsMissingAttributes(final KpiCalculationRequestPayload kpiCalculationRequestPayload) {
        return kpiCalculationRequestPayload.getKpiNames().isEmpty()
                || kpiCalculationRequestPayload.getSource().isEmpty();
    }

    /**
     * Verifies a {@link KpiCalculationRequestPayload} corresponds to persisted {@link KpiDefinition}.
     *
     * @param kpiNames       a {@link Set} of strings that represent proposed KPIs for calculation.
     * @param kpiDefinitions a {@link List} of {@link String}s that represent the KPIs proposed for calculation.
     * @return a {@link Set} of {@link String}s that represent proposed KPI names that do not exist in the database.
     */
    public static Set<String> checkThatKpisRequestedForCalculationExist(final Set<String> kpiNames,
                                                                        final Collection<KpiDefinition> kpiDefinitions) {
        final Set<String> validKpiCalculationRequestNames = kpiDefinitions.stream()
                .map(KpiDefinition::getName)
                .collect(Collectors.toSet());

        final Set<String> requestsNotFound = kpiNames.stream()
                .collect(Collectors.toSet());

        requestsNotFound.removeAll(validKpiCalculationRequestNames);

        return requestsNotFound;
    }

    /**
     * Retrieves KPI Definitions from existing persisted KPIs.
     *
     * @param kpiCalculationRequestPayload a {@link KpiCalculationRequestPayload} to match against KPI Definitions.
     * @param kpiDefinitions               a {@link List} of currently persisted {@link KpiDefinition}s.
     * @return a {@link List} of {@link KpiDefinition}s that match KPI Calculation Requests.
     */
    public static List<KpiDefinition> getRequestedKpiDefinitionsFromExistingKpiDefinitions(
            final KpiCalculationRequestPayload kpiCalculationRequestPayload, final Collection<KpiDefinition> kpiDefinitions) {
        final Set<String> kpiNames = kpiCalculationRequestPayload.getKpiNames();
        return kpiDefinitions.stream()
                .filter(def -> kpiNames.contains(def.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Replace parameter tokens in KPI definitions with provided parameter values.
     *
     * @param parameters     a {@link Map} of parameters keyed by parameter token
     * @param kpiDefinitions a {@link Set} of {@link KpiDefinition} to be decorated with parameters
     * @return a {@link Set} of {@link Definition}s that have had parameters applied.
     */
    public static Set<Definition> applyParameterValuesToKpiDefinitions(final Map<String, String> parameters,
                                                                       final Collection<KpiDefinition> kpiDefinitions) {
        final Set<Definition> definitions = KpiDefinitionUtils.convertKpiDefinitionsToRawDefinitionSet(kpiDefinitions);
        if (parameters == null || parameters.isEmpty()) {
            return definitions;
        }
        final Map<String, SchemaAttribute> schemaAttributeMap = getSchemaAttributeMap();
        for (final Definition definition : definitions) {
            for (final SchemaAttribute schemaAttribute : schemaAttributeMap.values()) {
                if (schemaAttribute.isParameterizable()) {
                    applyParameterValuesToKpiDefinitions(parameters, definition, schemaAttribute.getName());
                }
            }
        }
        return definitions;
    }

    private static void applyParameterValuesToKpiDefinitions(final Map<String, String> parameters, final Definition definition,
                                                             final String schemaAttributeName) {
        final Object value = definition.getAttributeByName(schemaAttributeName);
        if (!Objects.isNull(value)) {
            if (value instanceof List) {
                final List<String> arrayValue = (List) value;
                final List<String> parameterDecoratedArrayValue = new ArrayList<>();
                for (final Object element : arrayValue) {
                    final String parameterDecoratedElement = decorateParametersElement(parameters, element);
                    parameterDecoratedArrayValue.add(parameterDecoratedElement);
                }
                definition.setAttribute(schemaAttributeName,
                        schemaAttributeName.equals(DEFINITION_FILTER_KEY)
                                ? parameterDecoratedArrayValue.stream().map(Filter::new).collect(Collectors.toList())
                                : parameterDecoratedArrayValue);
            } else {
                final String stringValue = String.valueOf(value);
                final String parameterDecoratedValue = StringSubstitutor.replace(stringValue, parameters);
                if (parameterDecoratedValue != null) {
                    definition.setAttribute(schemaAttributeName, parameterDecoratedValue);
                }
            }
        }
    }

    private static String decorateParametersElement(Map<String, String> parameters, Object element) {
        final Object source = element instanceof Filter
                ? ((Filter) element).getName()
                : element;
        return StringSubstitutor.replace(source, parameters);
    }

    /**
     * Get the names of definitions that have parameter tokens.
     *
     * @param kpiDefinitions a {@link Set} of {@link Definition}s to check for presence of parameter tokens
     * @return a {@link Set} of {@link String}s that represent the names of KPIs with parameter tokens
     */
    public static Set<String> getNamesOfKpisWithUnresolvedParameters(final Set<Definition> kpiDefinitions) {
        final Set<String> requestedKpiNamesMissingParameters = new HashSet<>();
        for (final Definition definition : kpiDefinitions) {
            for (final Map.Entry<String, Object> attribute : definition.getAttributes().entrySet()) {
                if (KpiSchemaValidator.valueContainsParameterToken(attribute.getValue())) {
                    requestedKpiNamesMissingParameters.add(String.valueOf(definition.getAttributeByName(DEFINITION_NAME_KEY)));
                }
            }
        }
        return requestedKpiNamesMissingParameters;
    }

    /**
     * Get a map of {@link SchemaAttribute} names mapped to {@link SchemaAttribute}.
     *
     * @return a {@link Map} of attribute names to attribute objects
     */
    public static Map<String, SchemaAttribute> getSchemaAttributeMap() {
        final Map<String, SchemaAttribute> schemaAttributeMap = new HashMap<>();

        final SchemaModel kpiSchemaModel = JsonSchemaLoader.getModelConfig(KPI_SCHEMA, KpiModel.class);
        for (final SchemaAttribute attribute : kpiSchemaModel.getElement().getAttributes()) {
            schemaAttributeMap.put(attribute.getName(), attribute);
        }
        return schemaAttributeMap;
    }
}