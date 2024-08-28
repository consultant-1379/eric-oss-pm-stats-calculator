/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.Parameter;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.ParameterRepository;

import com.google.common.base.Preconditions;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OnDemandParameterCastUtils {

    private final ParameterRepository parameterRepository;

    public Set<KpiDefinition> applyParametersToKpiDefinitions(@NonNull final Map<String, String> parameters,
                                                              final Collection<KpiDefinition> kpiDefinitions) {
        final Set<KpiDefinition> definitions = new HashSet<>(kpiDefinitions.size());

        if (parameters.isEmpty()) {
            return new HashSet<>(kpiDefinitions);
        }

        final Map<String, String> replacedParameters = replaceParameterValuesInMap(parameters);
        for (final KpiDefinition definition : kpiDefinitions) {

            final List<String> parameterizedAggregationElements = parameterizeAggregationElements(definition, replacedParameters);
            final List<Filter> parameterizedFilters = parameterizeFilters(definition, replacedParameters);
            final String parameterizedExpression = parameterizeExpression(definition, replacedParameters);

            definition.setExpression(parameterizedExpression);
            definition.setAggregationElements(parameterizedAggregationElements);
            definition.setFilter(parameterizedFilters);

            definitions.add(definition);
        }
        return definitions;
    }

    private Map<String, String> replaceParameterValuesInMap(final Map<String, String> parameters) {
        final List<Parameter> databaseParameters = parameterRepository.findByNameIn(parameters.keySet());
        final Set<String> dbParamKeys = databaseParameters.stream().map(Parameter::getName).collect(Collectors.toSet());

        Preconditions.checkArgument(dbParamKeys.containsAll(parameters.keySet()), "All parameter keys must be included");

        final Map<String, String> modifiedParameters = new HashMap<>(parameters.size());
        databaseParameters.forEach(parameter -> {
            if (parameter.getTabularParameterId() == null) {
                String parameterType = parameter.getType();
                final String parameterValue = parameters.get(parameter.getName());
                if (parameter.getType().equals("INTEGER")) {
                    parameterType = "INT";
                }
                modifiedParameters.put(parameter.getName(), modifyParameterString(parameterValue, parameterType));
            }
        });

        return modifiedParameters;
    }

    private String modifyParameterString(final String value, final String type) {
        if (type.equals("STRING")) {
            return String.format("'%s'", value);
        }
        return String.format("CAST('%s' AS %s)", value, type);
    }

    private String parameterizeExpression(final KpiDefinition definition, final Map<String, String> parameters) {
        final String expression = definition.getExpression();
        return replaceStringForParameters(expression, parameters);
    }

    private List<String> parameterizeAggregationElements(final KpiDefinition definition, final Map<String, String> parameters) {
        final List<String> aggregationElements = definition.getAggregationElements();
        final List<String> parameterizedAggregationElements = new ArrayList<>();

        aggregationElements.forEach(
                aggregationElement -> parameterizedAggregationElements.add(replaceStringForParameters(aggregationElement, parameters)));

        return parameterizedAggregationElements;
    }

    private List<Filter> parameterizeFilters(final KpiDefinition definition, final Map<String, String> parameters) {
        final List<String> filters = definition.getFilter()
                                               .stream()
                                               .map(Filter::getName)
                                               .collect(Collectors.toList());
        final List<Filter> parameterizedFilters = new ArrayList<>();

        filters.forEach(filter -> parameterizedFilters.add(Filter.of(replaceStringForParameters(filter, parameters))));

        return parameterizedFilters;
    }

    private String replaceStringForParameters(final String element, final Map<String, String> parameters) {
        return StringSubstitutor.replace(element, parameters, "'${", "}'");
    }
}
