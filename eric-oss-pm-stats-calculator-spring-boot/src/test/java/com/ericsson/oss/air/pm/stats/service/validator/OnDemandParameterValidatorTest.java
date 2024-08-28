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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.ericsson.oss.air.pm.stats._util.JsonLoaders;
import com.ericsson.oss.air.pm.stats._util.Serialization;
import com.ericsson.oss.air.pm.stats.model.entity.Parameter;
import com.ericsson.oss.air.pm.stats.model.entity.Parameter.ParameterBuilder;
import com.ericsson.oss.air.pm.stats.model.exception.ParameterValidationException;
import com.ericsson.oss.air.pm.stats.service.api.ParameterService;
import com.ericsson.oss.air.pm.stats.service.validator.helper.ParameterDefinitionHelper;

import kpi.model.KpiDefinitionRequest;
import kpi.model.ondemand.ParameterType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class OnDemandParameterValidatorTest {
    ParameterService parameterServiceMock = mock(ParameterService.class);

    OnDemandParameterValidator objectUnderTest = new OnDemandParameterValidator(parameterServiceMock, new ParameterDefinitionHelper());

    @Test
    void shouldFailParameterValidation() {
        final KpiDefinitionRequest kpiDefinitionRequest = load("json/invalid_parameters.json");

        when(parameterServiceMock.findAllSingleParameters()).thenReturn(List.of(
                parameter(1, "executionId", ParameterType.STRING),
                parameter(2, "beginTime", ParameterType.STRING)
        ));

        Assertions.assertThatThrownBy(() -> objectUnderTest.validateParametersResolved(kpiDefinitionRequest))
                .isInstanceOf(ParameterValidationException.class)
                .hasMessage("Missing declaration for following parameters: '[param.date_for_filter, nonDeclaredParam]'");

        verify(parameterServiceMock).findAllSingleParameters();
    }

    @Test
    void whenInvalidParameterName_shouldThrowValidationException() {
        final KpiDefinitionRequest kpiDefinitionRequest = load("json/invalid_parameter_names.json");

        Assertions.assertThatThrownBy(() -> objectUnderTest.validateParametersResolved(kpiDefinitionRequest))
                .isInstanceOf(ParameterValidationException.class)
                .hasMessage("Invalid format in the following parameter name(s): " +
                        "'err,name1, err:name2, param.overlyComplicatedLongExtendedNameBeyondVarcharFieldSize'. " +
                        "Format must follow the \"^[a-zA-Z][a-zA-Z0-9_.]{0,55}$\" pattern");
    }

    static Parameter parameter(final int id, final String name, final ParameterType type) {
        final ParameterBuilder builder = Parameter.builder();
        builder.withId(id);
        builder.withName(name);
        builder.withType(type);
        return builder.build();
    }

    static KpiDefinitionRequest load(final String resourceName) {
        return Serialization.deserialize(JsonLoaders.load(resourceName), KpiDefinitionRequest.class);
    }
}