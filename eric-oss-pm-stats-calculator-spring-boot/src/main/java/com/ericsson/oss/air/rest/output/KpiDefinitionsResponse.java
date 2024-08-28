/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.output;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@JsonNaming(SnakeCaseStrategy.class)
public class KpiDefinitionsResponse {
    private TableListDto scheduledSimple;
    private TableListDto scheduledComplex;
    private OnDemandTableListDto onDemand;

    @Data
    @AllArgsConstructor
    @JsonNaming(SnakeCaseStrategy.class)
    public static class TableListDto {
        private List<TableDto> kpiOutputTables;
    }

    @Data
    @AllArgsConstructor
    @JsonNaming(SnakeCaseStrategy.class)
    public static class OnDemandTableListDto {
        private List<OnDemandParameterDto> parameters;
        private List<OnDemandTabularParameterDto> tabularParameters;
        private List<TableDto> kpiOutputTables;
    }

    @Data
    @AllArgsConstructor
    @JsonNaming(SnakeCaseStrategy.class)
    public static class TableDto {
        private String alias;
        private Integer aggregationPeriod;
        private Integer retentionPeriodInDays;
        private List<KpiDefinitionDto> kpiDefinitions;
    }

    @Data
    @Builder
    @Accessors(fluent = true)
    @Getter(onMethod_ = @JsonProperty)
    @JsonNaming(SnakeCaseStrategy.class)
    public static class SimpleKpiDefinitionDto implements KpiDefinitionDto {
        private String name;
        private String expression;
        private String objectType;
        private String aggregationType;
        private List<String> aggregationElements;
        private Boolean exportable;
        private List<String> filters;
        private Boolean reexportLateData;
        private Integer dataReliabilityOffset;
        private Integer dataLookbackLimit;
        private String inpDataIdentifier;
    }

    @Data
    @Builder
    @Accessors(fluent = true)
    @Getter(onMethod_ = @JsonProperty)
    @JsonNaming(SnakeCaseStrategy.class)
    public static class ComplexKpiDefinitionDto implements KpiDefinitionDto {
        private String name;
        private String expression;
        private String objectType;
        private String aggregationType;
        private List<String> aggregationElements;
        private Boolean exportable;
        private List<String> filters;
        private Boolean reexportLateData;
        private Integer dataReliabilityOffset;
        private Integer dataLookbackLimit;
        private String executionGroup;
    }

    @Data
    @Builder
    @Accessors(fluent = true)
    @Getter(onMethod_ = @JsonProperty)
    @JsonNaming(SnakeCaseStrategy.class)
    public static class OnDemandKpiDefinitionDto implements KpiDefinitionDto {
        private String name;
        private String expression;
        private String objectType;
        private String aggregationType;
        private List<String> aggregationElements;
        private Boolean exportable;
        private List<String> filters;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @JsonNaming(SnakeCaseStrategy.class)
    public static class OnDemandParameterDto {
        private String name;
        private String type;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @JsonNaming(SnakeCaseStrategy.class)
    public static class OnDemandTabularParameterDto {
        private String name;
        private List<OnDemandParameterDto> columns;
    }
}
