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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic representation of required kpi definitions passed to KPI Service.
 */
@NoArgsConstructor
@Data
public class KpiDefinitionPayload {
    @JsonAlias({"kpiAttributes", "kpi_definitions"})
    private final List<Map<String, Object>> kpiDefinitions = new ArrayList<>();

    private String source = "";

    public KpiDefinitionPayload(final String source, final List<Map<String, Object>> kpiDefinitions) {
        this.source = source;
        this.kpiDefinitions.addAll(kpiDefinitions == null ? Collections.emptyList() : kpiDefinitions);
    }

    public List<Map<String, Object>> getKpiDefinitions() {
        return Collections.unmodifiableList(kpiDefinitions);
    }
}
