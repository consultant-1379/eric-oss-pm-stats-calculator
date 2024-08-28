/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.helper;

import static lombok.AccessLevel.PUBLIC;

import java.util.Map;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload;
import com.ericsson.oss.air.pm.stats.model.entity.Parameter;
import com.ericsson.oss.air.pm.stats.service.api.ParameterService;

import kpi.model.ondemand.ParameterType;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class ParameterValidatorHelper {

    @Inject
    private ParameterService parameterService;

    public Map<String, Object> getParameterValueByName(final KpiCalculationRequestPayload kpiCalculationRequestPayload) {
        return kpiCalculationRequestPayload.getParameters()
                .stream()
                .collect(Collectors.toMap(KpiCalculationRequestPayload.Parameter::getName, KpiCalculationRequestPayload.Parameter::getValue));
    }

    public Map<String, ParameterType> getParameterTypeByNameFromDatabase() {
        return parameterService.findAllSingleParameters()
                .stream()
                .collect(Collectors.toMap(Parameter::name, Parameter::type));
    }
}
