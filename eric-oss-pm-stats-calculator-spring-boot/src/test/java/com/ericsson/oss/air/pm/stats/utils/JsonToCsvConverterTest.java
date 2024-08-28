/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.utils;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload.TabularParameters;
import com.ericsson.oss.air.pm.stats.model.exception.TabularParameterValidationException;

import com.fasterxml.jackson.core.JsonParseException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class JsonToCsvConverterTest {
    final String JSON_STRING =
            "{ \"cell_configuration_test_1\": [" +
                    "   {\"field1\": 11,\"field2\": 12,\"field3\": 13}," +
                    "   {\"field1\": 21,\"field2\": 22,\"field3\": 23}," +
                    "   {\"field1\": 31,\"field2\": 32,\"field3\": 33}" +
                    "]}";
    final String MALFORMED_JSON_STRING = "{ cell_configuration_test_1: [ malformed ]}";

    @Test
    void convertJsonToCsv() {
        final String expectedValue = "11,12,13\n21,22,23\n31,32,33\n";
        final String expectedHeader = "field1,field2,field3";
        final TabularParameters expected = TabularParameters.builder().value(expectedValue).header(expectedHeader).build();

        final TabularParameters actual = JsonToCsvConverter.convertJsonToCsv(JSON_STRING);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void convertJsonToCsvShouldThrowException() {
        Assertions.assertThatThrownBy(() -> JsonToCsvConverter.convertJsonToCsv(MALFORMED_JSON_STRING))
                .hasRootCauseInstanceOf(JsonParseException.class)
                .isInstanceOf(TabularParameterValidationException.class)
                .hasMessage("Error occurred while processing json. Unable to save tabular parameters");
    }
}