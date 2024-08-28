/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service;

import static com.ericsson.oss.air.pm.stats.common.env.DatabaseProperties.getKpiServiceJdbcConnection;
import static com.ericsson.oss.air.pm.stats.common.env.DatabaseProperties.getKpiServiceJdbcProperties;
import static com.ericsson.oss.air.pm.stats.service.util.TabularParameterUtils.aggregationElementPredicate;
import static lombok.AccessLevel.PUBLIC;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiPersistenceException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.AggregationElement;
import com.ericsson.oss.air.pm.stats.model.entity.Parameter;
import com.ericsson.oss.air.pm.stats.repository.api.ParameterRepository;
import com.ericsson.oss.air.pm.stats.service.api.ParameterService;

import kpi.model.KpiDefinitionRequest;
import kpi.model.ondemand.OnDemandParameter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Stateless
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class ParameterServiceImpl implements ParameterService {

    @Inject
    private ParameterRepository parameterRepository;

    @Override
    public void insert(final KpiDefinitionRequest kpiDefinition, final UUID collectionId) {
        final List<OnDemandParameter> parameters = kpiDefinition.onDemand().parameters();
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties())) {
            parameterRepository.saveAllParameters(connection, parameters, collectionId);
        } catch (final SQLException e) {
            throw new KpiPersistenceException("Parameters could not be saved into the table.", e);
        }
    }

    @Override
    public List<Parameter> findAllSingleParameters() {
        return parameterRepository.findAllSingleParameters();
    }

    @Override
    public Map<String, KpiDataType> findAggregationElementTypeForTabularParameter(final List<AggregationElement> aggregationElements) {
        final Map<String, KpiDataType> aggregationElementsAndTypesForTabularParameter = new HashMap<>(aggregationElements.size());

        final List<String> tabularParameterDataSourceTableNames = aggregationElements.stream()
                .map(AggregationElement::getSourceTable)
                .collect(Collectors.toList());

        final List<Parameter> parameters = parameterRepository.findParametersForListOfTabularParameter(tabularParameterDataSourceTableNames);

        for (final AggregationElement aggregationElement : aggregationElements) {
            final Parameter parameter = parameters.stream()
                    .filter(aggregationElementPredicate(aggregationElement))
                    .findFirst()
                    .orElseThrow(() -> new KpiPersistenceException(
                            String.format("Invalid aggregation element. Parameter declaration not found for: '%s'", aggregationElement.getExpression())
                    ));

            aggregationElementsAndTypesForTabularParameter.put(aggregationElement.getTargetColumn(), KpiDataType.forValue(parameter.type().name()));
        }

        return aggregationElementsAndTypesForTabularParameter;
    }

    @Override
    public List<Parameter> findAllParameters() {
        return parameterRepository.findAllParameters();
    }

    @Override
    public List<Parameter> findAllParameters(final UUID collectionId) {
        return parameterRepository.findAllParameters(collectionId);
    }
}
