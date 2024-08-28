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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.calculator.api.util.Serializations;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
public class KpiCalculationRequestPayload {
    @Schema(deprecated = true, description = "unused field")
    private final String source;
    @JsonAlias("kpi_names")
    private final Set<String> kpiNames;
    private final List<Parameter> parameters;
    @JsonAlias("tabular_parameters")
    private final List<TabularParameters> tabularParameters;

    @JsonCreator
    private KpiCalculationRequestPayload(@JsonProperty("source") final String source,
                                         @JsonProperty(required = true, value = "kpiNames") final Set<String> kpiNames,
                                         @JsonProperty("parameters") final List<Parameter> parameters,
                                         @JsonProperty("tabularParameters") final List<TabularParameters> tabularParameters) {
        this.source = source;
        this.kpiNames = new HashSet<>(kpiNames);
        this.parameters = parameters == null ? Collections.emptyList() : parameters;
        this.tabularParameters = tabularParameters == null ? Collections.emptyList() : tabularParameters;
    }

    public String parameterString() {
        return Serializations.writeValueAsString(parameters.stream().collect(Collectors.toMap(
                Parameter::getName, Parameter::getValue
        )));
    }

    @Data
    @Builder
    public static final class Parameter {
        @Schema(description = "Name of the parameter", example = "date_for_filter", required = true)
        private final String name;
        @Schema(description = "Value of the parameter", example = "2023-06-06", required = true)
        private final Object value;

        @JsonCreator
        private Parameter(@JsonProperty(required = true, value = "name") final String name,
                          @JsonProperty(required = true, value = "value") final Object value) {
            this.name = name;
            this.value = value;
        }
    }

    @Data
    @Builder
    public static final class TabularParameters {
        @Schema(description = "The name of the tabular parameter", example = "cell_configuration", required = true)
        private final String name;
        @Schema(description = "The type of the value field", example = "CSV", required = true)
        private final Format format;
        @Schema(description = "The header of the values (applicable in case of CSV format)", example = "date_for_filter")
        private final String header;
        @ToString.Exclude
        @Schema(description = "The value of the tabular parameter, in rows matching with the given format and header", example = "FDN1,0.2,2 \n FDN2,1.2,13", required = true)
        private final String value;

        @JsonCreator
        private TabularParameters(@JsonProperty(required = true, value = "name") final String name,
                                  @JsonProperty(required = true, value = "format") final Format format,
                                  @JsonProperty(value = "header") final String header,
                                  @JsonProperty(required = true, value = "value") final String value) {
            this.name = name;
            this.format = format;
            this.header = header;
            this.value = value;
        }
    }
}