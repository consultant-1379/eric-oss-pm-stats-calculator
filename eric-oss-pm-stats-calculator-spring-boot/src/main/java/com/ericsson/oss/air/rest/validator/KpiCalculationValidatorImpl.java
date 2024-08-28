/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.validator;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload;
import com.ericsson.oss.air.pm.stats.service.validator.ParameterValidator;
import com.ericsson.oss.air.pm.stats.service.validator.TabularParameterValidator;
import com.ericsson.oss.air.rest.api.KpiCalculationValidator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@RequiredArgsConstructor
@Component
public class KpiCalculationValidatorImpl implements KpiCalculationValidator {

    private final ParameterValidator parameterValidator;
    private final TabularParameterValidator tabularParameterValidator;

    @Override
    public void validate(final KpiCalculationRequestPayload kpiCalculationRequestPayload) {
        parameterValidator.validateParameterTypes(kpiCalculationRequestPayload);
        tabularParameterValidator.checkTabularParameterTableExistsInDatabase(kpiCalculationRequestPayload.getTabularParameters());
        tabularParameterValidator.checkRequiredTabularParameterSourcesPresent(kpiCalculationRequestPayload);
        tabularParameterValidator.checkColumnsInCaseHeaderPresent(kpiCalculationRequestPayload);
    }
}
