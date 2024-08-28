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

import static org.mockito.Mockito.verify;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload;
import com.ericsson.oss.air.pm.stats.service.validator.ParameterValidator;
import com.ericsson.oss.air.pm.stats.service.validator.TabularParameterValidator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KpiCalculationValidatorImplTest {
    @Mock ParameterValidator parameterValidatorMock;
    @Mock TabularParameterValidator tabularParameterValidatorMock;
    @InjectMocks KpiCalculationValidatorImpl objectUnderTest;

    @Test
    void shouldValidateParameters(@Mock final KpiCalculationRequestPayload payloadMock) {
        objectUnderTest.validate(payloadMock);
        verify(parameterValidatorMock).validateParameterTypes(payloadMock);
        verify(tabularParameterValidatorMock).checkTabularParameterTableExistsInDatabase(payloadMock.getTabularParameters());
        verify(tabularParameterValidatorMock).checkRequiredTabularParameterSourcesPresent(payloadMock);
        verify(tabularParameterValidatorMock).checkColumnsInCaseHeaderPresent(payloadMock);
    }
}

