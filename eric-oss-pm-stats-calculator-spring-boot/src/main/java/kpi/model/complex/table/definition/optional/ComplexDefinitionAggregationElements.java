/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.complex.table.definition.optional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import kpi.model.api.table.api.TableAttribute;
import kpi.model.api.table.definition.OptionalDefinitionAttribute;
import kpi.model.api.table.definition.api.AggregationElementsAttribute;
import kpi.model.complex.element.ComplexAggregationElement;
import kpi.model.complex.table.required.ComplexTableAggregationElements;
import kpi.model.util.Attributes;
import kpi.model.util.Strings;
import kpi.model.util.ValidationResults;

public class ComplexDefinitionAggregationElements
        extends OptionalDefinitionAttribute<List<ComplexAggregationElement>>
        implements AggregationElementsAttribute<ComplexAggregationElement> {

    private ComplexDefinitionAggregationElements(final List<ComplexAggregationElement> value) {
        super(Objects.isNull(value) ? Collections.emptyList() : value);
    }

    @JsonCreator
    public static ComplexDefinitionAggregationElements of(
            @JsonSetter(contentNulls = Nulls.FAIL)
            @JsonProperty(Attributes.ATTRIBUTE_AGGREGATION_ELEMENTS) final List<ComplexAggregationElement> value
    ) {
        return new ComplexDefinitionAggregationElements(value);
    }

    @Override
    public String name() {
        return Attributes.ATTRIBUTE_AGGREGATION_ELEMENTS;
    }

    @Override
    public boolean canOverride(final List<ComplexAggregationElement> value) {
        return isEmpty() && isNotEmpty(value);
    }

    @Override
    public String toString() {
        return Strings.stringifyIterable(this);
    }

    @Override
    public Class<? extends TableAttribute<List<ComplexAggregationElement>>> parentClass() {
        return ComplexTableAggregationElements.class;
    }

    private static boolean isNotEmpty(final List<ComplexAggregationElement> value) {
        return !ValidationResults.isEmpty(value);
    }

}
