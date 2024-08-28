/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition;

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_AGGREGATION_PERIOD;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * Class to internally map to KPI Definitions from source JSON file.
 */
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class KpiDefinition implements Comparable<KpiDefinition> {

    private static final char FUNCTION_SYNTAX_LEADING_BRACKET_CHAR = '(';
    private static final String WHERE_CLAUSE = " WHERE ";
    private static final String ON_EXPRESSION = " ON ";
    private static final String AS_EXPRESSION = " AS ";

    private String name;
    private String alias;
    private String expression;

    @JsonProperty("collection_id")
    private UUID collectionId;

    @JsonProperty("object_type")
    private String objectType;
    @JsonProperty("aggregation_type")
    private String aggregationType;
    @JsonProperty("aggregation_period")
    private String aggregationPeriod;
    @Builder.Default
    @JsonProperty("aggregation_elements")
    private List<String> aggregationElements = Collections.emptyList();
    @JsonProperty("exportable")
    private Boolean exportable = true;
    @Builder.Default
    @JsonProperty("filter")
    private List<Filter> filter = Collections.emptyList();
    @JsonProperty("inp_data_identifier")
    private DataIdentifier inpDataIdentifier;
    @JsonProperty("execution_group")
    private String executionGroup;
    @JsonProperty("data_reliability_offset")
    private Integer dataReliabilityOffset;
    @JsonProperty("data_lookback_limit")
    private Integer dataLookbackLimit;
    @Builder.Default
    @JsonProperty("reexport_late_data")
    private Boolean reexportLateData = false;
    @JsonIgnore
    private SchemaDetail schemaDetail;

    public boolean isScheduled() {
        return !(StringUtils.isBlank(executionGroup) || "null".equalsIgnoreCase(executionGroup));
    }

    public boolean isSimple() {
        return Objects.nonNull(inpDataIdentifier) && !inpDataIdentifier.isEmpty();
    }

    public String datasource() {
        return inpDataIdentifier.getName();
    }

    public boolean isDefaultAggregationPeriod() {
        return DEFAULT_AGGREGATION_PERIOD.equals(aggregationPeriod);
    }

    public boolean hasSameDataIdentifier(@NonNull final DataIdentifier identifier) {
        return identifier.equals(inpDataIdentifier);
    }

    public boolean hasInputDataIdentifier() {
        return !inpDataIdentifier.isEmpty();
    }

    @Override
    public int compareTo(final KpiDefinition o) {
        final int nameComparison = name.compareTo(o.name);
        final int aggregationComparison = nameComparison == 0 ? aggregationPeriod.compareTo(o.aggregationPeriod) : nameComparison;
        return aggregationComparison == 0 ? alias.compareTo(o.alias) : aggregationComparison;
    }

    public List<String> getAggregationElements() {
        return new ArrayList<>(this.aggregationElements);
    }

    public List<Filter> getFilter() {
        return Collections.unmodifiableList(this.filter);
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setExpression(final String expression) {
        this.expression = expression;
    }

    @JsonProperty("aggregation_elements")
    public void setAggregationElements(final List<String> aggregationElements) {
        this.aggregationElements = Collections.unmodifiableList(aggregationElements);
    }

    @JsonProperty("execution_group")
    public void setExecutionGroup(final String executionGroup) {
        this.executionGroup = executionGroup;
    }

    @JsonProperty("filter")
    public void setFilter(final List<Filter> filter) {
        this.filter = Collections.unmodifiableList(filter);
    }
}
