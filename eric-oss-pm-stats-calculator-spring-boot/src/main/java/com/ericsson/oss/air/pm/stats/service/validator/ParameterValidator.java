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

import static lombok.AccessLevel.PUBLIC;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload;
import com.ericsson.oss.air.pm.stats.model.exception.ParameterValidationException;
import com.ericsson.oss.air.pm.stats.service.validator.helper.ParameterValidatorHelper;

import kpi.model.ondemand.ParameterType;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class ParameterValidator {
    @Inject
    private ParameterValidatorHelper parameterValidatorHelper;

    public void validateParameterTypes(final KpiCalculationRequestPayload kpiCalculationRequestPayload) {
        final Map<String, Object> requestedParameters = parameterValidatorHelper.getParameterValueByName(kpiCalculationRequestPayload);
        final Map<String, ParameterType> definedParameters = parameterValidatorHelper.getParameterTypeByNameFromDatabase();

        validate(requestedParameters, definedParameters);
    }

    private void validate(final Map<String, Object> requestedParameters, final Map<String, ParameterType> definedParameters) {
        List<String> invalidParameterTypes = new ArrayList<>();
        List<String> parametersNotFound = new ArrayList<>();

        requestedParameters.forEach((parameterName, parameterValue) -> {
            if (!definedParameters.containsKey(parameterName)) {
                parametersNotFound.add(parameterName);
            } else {
                final ParameterType expectedType = definedParameters.get(parameterName);

                if (!expectedType.valueIsTypeOf(parameterValue)) {
                    invalidParameterTypes.add(parameterName);
                }
            }
        });

        if (!invalidParameterTypes.isEmpty()) {
            throw new ParameterValidationException(String.format("Parameter(s): %s do(es) not match with the declared type(s).", invalidParameterTypes));
        }
        if (!parametersNotFound.isEmpty()) {
            throw new ParameterValidationException(String.format("Parameter(s): %s not found.", parametersNotFound));
        }
    }
}
