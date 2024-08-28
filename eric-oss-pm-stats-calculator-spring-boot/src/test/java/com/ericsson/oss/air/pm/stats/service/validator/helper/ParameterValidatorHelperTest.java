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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import com.ericsson.oss.air.pm.stats._util.JsonLoaders;
import com.ericsson.oss.air.pm.stats._util.Serialization;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload;
import com.ericsson.oss.air.pm.stats.model.entity.Parameter;
import com.ericsson.oss.air.pm.stats.service.api.ParameterService;

import kpi.model.ondemand.ParameterType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ParameterValidatorHelperTest {

    @Mock
    ParameterService parameterServiceMock;

    @InjectMocks
    ParameterValidatorHelper objectUnderTest;

    @Test
    void shouldGetParameterDetailsFromPayload() {
        final KpiCalculationRequestPayload payload = Serialization.deserialize(JsonLoaders.load("json/calculationRequest.json"), KpiCalculationRequestPayload.class);

        Map<String, Object> actual = objectUnderTest.getParameterValueByName(payload);
        Map<String, Object> expected = Map.of("param.execution_id", "TEST_1", "param.date_for_filter", "2023-06-06");

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldGetParameterDetailsFromDatabase() {
        final Parameter parameter = Parameter.builder().withName("parameter").withType(ParameterType.INTEGER).withTabularParameter(null).build();

        when(parameterServiceMock.findAllSingleParameters()).thenReturn(List.of(parameter));

        final Map<String, ParameterType> actual = objectUnderTest.getParameterTypeByNameFromDatabase();

        verify(parameterServiceMock).findAllSingleParameters();

        Assertions.assertThat(actual).containsExactly(Map.entry("parameter", ParameterType.INTEGER));
    }
}