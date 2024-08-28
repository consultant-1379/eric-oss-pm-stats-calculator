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

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.EXECUTION_GROUP;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

/**
 * Used as a wrapper for KPI and counter definitions, contains a map of attributes that could represent either of these objects.
 */
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class Definition {
    public static final String KPI_NAME = "name";
    public static final String KPI_EXPRESSION = "expression";
    public static final String AGGREGATION_ELEMENTS = "aggregation_elements";
    public static final String INP_DATA_IDENTIFIER = "inp_data_identifier";
    public static final String FILTER = "filter";
    public static final String SCHEMA_DETAIL = "schema_detail";
    private final Map<String, Object> attributes = new HashMap<>();

    public Definition(final Map<String, Object> attributes) {
        this.attributes.putAll(attributes == null ? Collections.emptyMap() : attributes);
    }

    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    @Nullable
    @JsonIgnore
    public <T> T attribute(final String name, @NonNull final Class<T> target) {
        final Object attribute = attributes.get(name);
        return Optional.ofNullable(attribute).map(target::cast).orElse(null);
    }

    public Object getAttributeByName(final String attributeName) {
        return attributes.get(attributeName);
    }

    public void setAttribute(final String attributeName, final Object attributeDefaultValue) {
        attributes.put(attributeName, attributeDefaultValue);
    }

    public boolean doesAttributeExist(final String attributeName) {
        return attributes.containsKey(attributeName);
    }

    //  If the definition is not normalized this will lead to ClassCastException
    public List<Filter> getFilters() {
        @SuppressWarnings("unchecked") final List<Filter> filter = (List<Filter>) attributes.get(FILTER);

        return filter;
    }

    public boolean doesExecutionGroupExist() {
        return Objects.nonNull(getExecutionGroup());
    }

    @JsonIgnore
    public String getName() {
        return getAttributeByName(KPI_NAME).toString();
    }

    @JsonIgnore
    public String getExecutionGroup() {
        return (String) getAttributeByName(EXECUTION_GROUP);
    }

    @JsonIgnore
    public String getExpression() {
        return getAttributeByName(KPI_EXPRESSION).toString();
    }

    @JsonIgnore
    public List<String> getAggregationElements() {
        @SuppressWarnings("unchecked") final List<String> aggregationElements = (List<String>) getAttributeByName(AGGREGATION_ELEMENTS);
        return aggregationElements;
    }

    public boolean isComplexDefinition() {
        return !isSimpleKpiDefinition() && doesExecutionGroupExist();
    }


    private boolean doesInputDataIdentifierExists() {
        return Objects.nonNull(getInputDataIdentifier());
    }

    @JsonIgnore
    public DataIdentifier getInputDataIdentifier() {
        final Object dataIdentifier = getAttributeByName(INP_DATA_IDENTIFIER);
        if (dataIdentifier instanceof DataIdentifier) {
            return (DataIdentifier) getAttributeByName(INP_DATA_IDENTIFIER);
        } else if (dataIdentifier instanceof String) {
            return DataIdentifier.of((String) getAttributeByName(INP_DATA_IDENTIFIER));
        }
        return null;
    }

    @JsonIgnore
    public SchemaDetail getSchemaDetail() {
        return (SchemaDetail) getAttributeByName(SCHEMA_DETAIL);
    }

    public KpiType getKpiType() {
        if (isComplexDefinition()) {
            return KpiType.SCHEDULED_COMPLEX;
        }

        return isSimpleKpiDefinition()
                ? KpiType.SCHEDULED_SIMPLE
                : KpiType.ON_DEMAND;
    }

    public boolean isSimpleKpiDefinition() {
        return doesInputDataIdentifierExists();
    }

}
