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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload;
import com.ericsson.oss.air.pm.stats.model.exception.ParameterValidationException;
import com.ericsson.oss.air.pm.stats.service.validator.helper.ParameterValidatorHelper;

import kpi.model.ondemand.ParameterType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ParameterValidatorTest {
    @Mock
    ParameterValidatorHelper parameterValidatorHelperMock;
    @Mock
    KpiCalculationRequestPayload kpiCalculationRequestPayloadMock;
    @InjectMocks
    ParameterValidator objectUnderTest;

    @MethodSource("provideValidParameters")
    @ParameterizedTest(name = "[{index}] Requested parameters: ''{0}'', Defined parameters: ''{1}''")
    void validateParameterTypes(final Map<String, Object> requestedParameters, final Map<String, ParameterType> definedParameters) {
        when(parameterValidatorHelperMock.getParameterValueByName(any())).thenReturn(requestedParameters);
        when(parameterValidatorHelperMock.getParameterTypeByNameFromDatabase()).thenReturn(definedParameters);

        objectUnderTest.validateParameterTypes(kpiCalculationRequestPayloadMock);

        verify(parameterValidatorHelperMock).getParameterValueByName(any());
        verify(parameterValidatorHelperMock).getParameterTypeByNameFromDatabase();
    }

    @Test
    void validateWithInvalidParameterType() {

        final Map<String, Object> requested = Map.of("param", 1);
        final Map<String, ParameterType> defined = Map.of("param", ParameterType.STRING);

        when(parameterValidatorHelperMock.getParameterValueByName(any())).thenReturn(requested);
        when(parameterValidatorHelperMock.getParameterTypeByNameFromDatabase()).thenReturn(defined);

        assertThatThrownBy(() -> objectUnderTest.validateParameterTypes(kpiCalculationRequestPayloadMock))
                .isInstanceOf(ParameterValidationException.class)
                .hasMessage("Parameter(s): [param] do(es) not match with the declared type(s).");

        verify(parameterValidatorHelperMock).getParameterValueByName(any());
        verify(parameterValidatorHelperMock).getParameterTypeByNameFromDatabase();
    }

    @Test
    void validateWithNotFoundParameter() {

        final Map<String, Object> requested = Map.of("wrong_param", 1);
        final Map<String, ParameterType> defined = Map.of("param", ParameterType.INTEGER);

        when(parameterValidatorHelperMock.getParameterValueByName(any())).thenReturn(requested);
        when(parameterValidatorHelperMock.getParameterTypeByNameFromDatabase()).thenReturn(defined);

        assertThatThrownBy(() -> objectUnderTest.validateParameterTypes(kpiCalculationRequestPayloadMock))
                .isInstanceOf(ParameterValidationException.class)
                .hasMessage("Parameter(s): [wrong_param] not found.");

        verify(parameterValidatorHelperMock).getParameterValueByName(any());
        verify(parameterValidatorHelperMock).getParameterTypeByNameFromDatabase();
    }

    static Stream<Arguments> provideValidParameters() {
        return Stream.of(
                Arguments.of(Map.of("param", 1), Map.of("param", ParameterType.INTEGER)),
                Arguments.of(Map.of("param", 1L), Map.of("param", ParameterType.LONG)),
                Arguments.of(Map.of("param", 1.0), Map.of("param", ParameterType.DOUBLE)),
                Arguments.of(Map.of("param", true), Map.of("param", ParameterType.BOOLEAN)),
                Arguments.of(Map.of("param", "a"), Map.of("param", ParameterType.STRING))
        );
    }
}