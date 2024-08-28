/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository;

import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.configuration.CalculatorProperties;
import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExecutionGroupGenerator {

    private final CalculatorProperties calculatorProperties;

    @Nullable
    public String generateOrGetExecutionGroup(final KpiDefinitionEntity kpiDefinitionEntity) {
        final ExecutionGroup executionGroup = kpiDefinitionEntity.executionGroup();
        final DataIdentifier kpiDefinitionEntityIdentifier = kpiDefinitionEntity.dataIdentifier();

        if (Objects.nonNull(kpiDefinitionEntityIdentifier)) {
            return generateExecutionGroup(kpiDefinitionEntity, kpiDefinitionEntityIdentifier);
        }

        if (Objects.nonNull(executionGroup)) {
            return executionGroup.name();
        }

        return null;
    }

    private String generateExecutionGroup(final KpiDefinitionEntity kpiDefinitionEntity, final DataIdentifier dataIdentifier) {
        StringBuilder sb = new StringBuilder();

        if (Boolean.TRUE.equals(calculatorProperties.getInputSource())) {
            sb.append(dataIdentifier.getName());
            sb.append("__");
        }

        if (Boolean.TRUE.equals(calculatorProperties.getAggregationPeriod())) {
            final int aggregationPeriod = kpiDefinitionEntity.aggregationPeriod();
            if (aggregationPeriod == -1) {
                sb.append("NO-PERIOD__");
            } else {
                sb.append(aggregationPeriod);
                sb.append("__");
            }
        }

        if (Boolean.TRUE.equals(calculatorProperties.getAggregationElements())) {
            final List<String> aggregationElementsList = kpiDefinitionEntity.aggregationElements();
            final String elements = String.join(" || ", aggregationElementsList);
            sb.append('[');
            sb.append(elements);
            sb.append("]__");
        }

        final String executionGroup = sb.toString();

        return executionGroup.substring(0, executionGroup.length() - 2);
    }
}
