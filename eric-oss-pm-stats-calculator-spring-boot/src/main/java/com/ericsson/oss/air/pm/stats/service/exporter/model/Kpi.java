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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(setterPrefix = "with")
public class Kpi {
    @JsonProperty("name")
    private String name;
    @JsonProperty("reexport_late_data")
    private boolean reexportLateData;
    @JsonProperty("exportable")
    private boolean exportable;
    @JsonProperty("reliability_threshold")
    private long reliabilityThreshold;
    @JsonProperty("calculation_start_time")
    private long calculationStartTime;

    public static Kpi of(
            final String name,
            final long reliabilityThreshold,
            final long calculationStartTime,
            final boolean reexportLateData,
            final boolean exportable) {
        final KpiBuilder builder = builder();
        builder.withName(name);
        builder.withReexportLateData(reexportLateData);
        builder.withExportable(exportable);
        builder.withReliabilityThreshold(reliabilityThreshold);
        builder.withCalculationStartTime(calculationStartTime);
        return builder.build();
    }
}
