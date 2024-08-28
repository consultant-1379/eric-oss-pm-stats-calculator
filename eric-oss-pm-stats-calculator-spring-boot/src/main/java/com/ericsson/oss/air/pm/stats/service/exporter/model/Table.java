/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.exporter.model;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(setterPrefix = "with")
public class Table {
    @JsonProperty("name")
    private String name;
    @JsonProperty("aggregation_period")
    private Integer aggregationPeriod;
    @JsonProperty("kpis")
    private List<Kpi> kpis;
    @JsonProperty("list_of_kpis")
    private List<String> listOfKpis;
    @JsonProperty("list_of_dimensions")
    private List<String> listOfDimensions;

    public void addKpi(final Kpi kpi) {
        kpis.add(kpi);
    }

    public static Table of(@NonNull final KpiDefinitionEntity kpiDefinition) {
        return builder().withName(kpiDefinition.tableName())
                .withAggregationPeriod(kpiDefinition.aggregationPeriod())
                .withKpis(new ArrayList<>())
                .build();
    }
}
