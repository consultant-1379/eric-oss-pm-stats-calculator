/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model;

import static java.util.Collections.emptyList;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import kpi.model.ondemand.OnDemandParameter;
import kpi.model.ondemand.OnDemandTable;
import kpi.model.ondemand.OnDemandTabularParameter;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.collections4.CollectionUtils;

@Data
@Builder
@Jacksonized
@Accessors(fluent = true)
@ToString(includeFieldNames = false)
@JsonNaming(SnakeCaseStrategy.class)
public class OnDemand implements KpiDefinitionTable<OnDemandTable> {
    private final List<OnDemandTable> kpiOutputTables;
    private final List<OnDemandParameter> parameters;
    private final List<OnDemandTabularParameter> tabularParameters;

    @Builder
    private OnDemand(
            final List<OnDemandTable> kpiOutputTables,
            final List<OnDemandParameter> parameters,
            final List<OnDemandTabularParameter> tabularParameters
    ) {
        this.kpiOutputTables = CollectionUtils.isEmpty(kpiOutputTables) ? emptyList() : kpiOutputTables;
        this.parameters = CollectionUtils.isEmpty(parameters) ? emptyList() : parameters;
        this.tabularParameters = CollectionUtils.isEmpty(tabularParameters) ? emptyList() : tabularParameters;
    }

    public static OnDemand empty() {
        return new OnDemand(emptyList(), emptyList(), emptyList());
    }
}
