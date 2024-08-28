/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model;

import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload.Parameter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload.TabularParameters;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import kpi.model._helper.JsonLoaders;
import kpi.model._helper.Serialization;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class KpiCalculationRequestPayloadTest {

    @Test
    void shouldDeserialize() {
        final KpiCalculationRequestPayload actual = Serialization.deserialize(JsonLoaders.load("json/calcRequest.json"), KpiCalculationRequestPayload.class);

        Assertions.assertThat(actual.getSource()).isEqualTo("TEST");
        Assertions.assertThat(actual.getKpiNames()).containsExactlyInAnyOrder(
                "rolling_sum_integer_1440",
                "rolling_max_integer_1440",
                "first_float_operator_1440_post_aggregation",
                "executionid_sum_integer_1440",
                "first_integer_aggregate_slice_1440",
                "first_float_divideby0_60",
                "sum_integer_60_join_kpidb");

        Assertions.assertThat(actual.getParameters()).containsExactlyInAnyOrder(
                buildParameter("param.execution_id", "TEST_1"),
                buildParameter("param.date_for_filter", "2023-06-06"),
                buildParameter("param.fdn", "ManagedElement=NR101NodeBRadio00016,GNBCUUPFunction=9")
        );

        Assertions.assertThat(actual.getTabularParameters()).containsExactly(
                TabularParameters.builder().name("test").format(Format.CSV).header("something").value("12").build()
        );
    }


    @Test
    void shouldGiveBackParameterString() {
        final KpiCalculationRequestPayload actual = Serialization.deserialize(JsonLoaders.load("json/calcRequest.json"), KpiCalculationRequestPayload.class);

        Assertions.assertThat(actual.parameterString()).isEqualTo(
                "{\"param.date_for_filter\":\"2023-06-06\",\"param.execution_id\":\"TEST_1\",\"param.fdn\":\"ManagedElement=NR101NodeBRadio00016,GNBCUUPFunction=9\"}");
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class DeserializationFail {

        @MethodSource("provideMissingFieldForPayload")
        @ParameterizedTest(name = "[{index}] Testing missing property: ''{1}''")
        void shouldFailDeserializationForMissingPayloadProperty(final String content, final String missing) {
            Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, KpiCalculationRequestPayload.class))
                    .isInstanceOf(MismatchedInputException.class)
                    .hasMessageContaining("Missing required creator property '%s'", missing);
        }

        @MethodSource("provideMissingFieldForParameter")
        @ParameterizedTest(name = "[{index}] Testing missing property: ''{1}''")
        void shouldFailDeserializationForMissingParameterProperty(final String content, final String missing) {
            Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, Parameter.class))
                    .isInstanceOf(MismatchedInputException.class)
                    .hasMessageContaining("Missing required creator property '%s'", missing);
        }

        @MethodSource("provideMissingFieldForTabularParameter")
        @ParameterizedTest(name = "[{index}] Testing missing property: ''{1}''")
        void shouldFailDeserializationForMissingTabularParameterProperty(final String content, final String missing) {
            Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, TabularParameters.class))
                    .isInstanceOf(MismatchedInputException.class)
                    .hasMessageContaining("Missing required creator property '%s'", missing);
        }

        @Test
        void shouldFailWithUnknownValue() {
            final String payload =
                    "{\n" +
                            "  \"name\": \"name\",\n" +
                            "  \"format\": \"unknown\",\n" +
                            "  \"header\": \"header\",\n" +
                            "  \"value\": \"value\"\n" +
                            "}";
            Assertions.assertThatThrownBy(() -> Serialization.deserialize(payload, TabularParameters.class))
                    .isInstanceOf(InvalidFormatException.class)
                    .hasMessageContaining("\"unknown\": not one of the values accepted for Enum class: [JSON, CSV]");
        }

        Stream<Arguments> provideMissingFieldForPayload() {
            final String missingKpiNames =
                    "{\n" +
                            "  \"source\": \"TEST\"\n" +
                            "}";
            return Stream.of(
                    Arguments.of(missingKpiNames, "kpiNames")
            );
        }

        Stream<Arguments> provideMissingFieldForParameter() {
            final String missingName =
                    "{\n" +
                            "  \"value\": \"value\"\n" +
                            "}";
            final String missingValue =
                    "{\n" +
                            "  \"name\": \"name\"\n" +
                            "}";
            return Stream.of(
                    Arguments.of(missingName, "name"),
                    Arguments.of(missingValue, "value")
            );
        }

        Stream<Arguments> provideMissingFieldForTabularParameter() {
            final String missingName =
                    "{\n" +
                            "  \"format\": \"CSV\",\n" +
                            "  \"value\": \"value\"\n" +
                            "}";
            final String missingFormat =
                    "{\n" +
                            "  \"name\": \"name\",\n" +
                            "  \"value\": \"value\"\n" +
                            "}";
            final String missingValue =
                    "{\n" +
                            "  \"name\": \"name\",\n" +
                            "  \"format\": \"CSV\"\n" +
                            "}";
            return Stream.of(
                    Arguments.of(missingName, "name"),
                    Arguments.of(missingFormat, "format"),
                    Arguments.of(missingValue, "value")
            );
        }
    }

    static Parameter buildParameter(final String name, final String value) {
        return Parameter.builder().name(name).value(value).build();
    }

}
