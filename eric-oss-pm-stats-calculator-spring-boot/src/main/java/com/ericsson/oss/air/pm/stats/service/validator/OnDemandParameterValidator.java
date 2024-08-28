/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator;

import static kpi.model.util.PatternConstants.PATTERN_PARAMETER_NAME;
import static lombok.AccessLevel.PUBLIC;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections4.SetUtils.difference;
import static org.apache.commons.collections4.SetUtils.union;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.model.entity.Parameter;
import com.ericsson.oss.air.pm.stats.model.exception.ParameterValidationException;
import com.ericsson.oss.air.pm.stats.service.api.ParameterService;
import com.ericsson.oss.air.pm.stats.service.validator.helper.ParameterDefinitionHelper;

import kpi.model.KpiDefinitionRequest;
import kpi.model.api.table.definition.OnDemandKpiDefinitions.OnDemandKpiDefinition;
import kpi.model.ondemand.OnDemandParameter;
import kpi.model.ondemand.OnDemandTable;
import kpi.model.ondemand.element.OnDemandAggregationElement;
import kpi.model.ondemand.element.OnDemandFilterElement;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetUtils.SetView;

@Slf4j
@ApplicationScoped
@NoArgsConstructor /* EJB definition requires no-arg constructor */
@AllArgsConstructor(access = PUBLIC) /* Internal testing only */
public class OnDemandParameterValidator {
    @Inject
    private ParameterService parameterService;
    @Inject
    private ParameterDefinitionHelper parameterDefinitionHelper;

    public void validateParametersResolved(final KpiDefinitionRequest kpiDefinitionRequest) {
        final Set<String> requestParameters = loadRequestParameterNames(kpiDefinitionRequest);
        final Set<String> storedParameters = loadStoredParameterNames();
        final SetView<String> availableParameters = union(storedParameters, requestParameters);

        final Set<String> parameterReferences = computeParameterReferences(kpiDefinitionRequest);

        final SetView<String> unresolvedParameterReferences = difference(parameterReferences, availableParameters);

        if (isNotEmpty(unresolvedParameterReferences)) {
            throw new ParameterValidationException(String.format("Missing declaration for following parameters: '%s'", unresolvedParameterReferences));
        }
    }

    private static Set<String> loadRequestParameterNames(final KpiDefinitionRequest kpiDefinitionRequest) {
        final Set<String> requestParametersNames = getOnDemandParameterNames(kpiDefinitionRequest);

        final String nameErrors = requestParametersNames.stream().filter(s -> !PATTERN_PARAMETER_NAME.matcher(s).matches()).collect(Collectors.joining(", "));
        if (!nameErrors.isEmpty()) {
            throw new ParameterValidationException(String.format(
                    "Invalid format in the following parameter name(s): '%s'. Format must follow the \"%s\" pattern",
                    nameErrors, PATTERN_PARAMETER_NAME
            ));
        }

        return requestParametersNames;
    }

    private Set<String> computeParameterReferences(final KpiDefinitionRequest kpiDefinitionRequest) {
        final Set<String> definitionParameters = new HashSet<>();

        filterOnDemandDefinitions(kpiDefinitionRequest).forEach(definition -> {
            for (final OnDemandFilterElement filter : definition.filters()) {
                definitionParameters.addAll(parameterDefinitionHelper.collectParameterNames(filter));
            }

            for (final OnDemandAggregationElement aggregationElement : definition.aggregationElements()) {
                definitionParameters.addAll(parameterDefinitionHelper.collectParameterNames(aggregationElement));
            }

            definitionParameters.addAll(parameterDefinitionHelper.collectParameterNames(definition.expression().value()));
        });

        return definitionParameters;
    }

    private Set<String> loadStoredParameterNames() {
        return parameterService.findAllSingleParameters().stream().map(Parameter::name).collect(Collectors.toSet());
    }

    private List<OnDemandKpiDefinition> filterOnDemandDefinitions(final KpiDefinitionRequest kpiDefinitionRequest) {
        final List<OnDemandTable> outputTables = kpiDefinitionRequest.onDemand().kpiOutputTables();
        return outputTables.stream()
                .flatMap(onDemandTable -> onDemandTable.kpiDefinitions().value().stream())
                .collect(Collectors.toList());
    }

    private static Set<String> getOnDemandParameterNames(final KpiDefinitionRequest kpiDefinitionRequest) {
        return kpiDefinitionRequest.onDemand().parameters().stream().map(OnDemandParameter::name).collect(Collectors.toSet());
    }

}
