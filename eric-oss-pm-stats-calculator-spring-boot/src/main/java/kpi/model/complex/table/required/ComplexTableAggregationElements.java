/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.complex.table.required;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import kpi.model.api.table.RequiredTableAttribute;
import kpi.model.api.table.definition.api.AggregationElementsAttribute;
import kpi.model.api.validation.ValidationResult;
import kpi.model.complex.element.ComplexAggregationElement;
import kpi.model.util.Attributes;
import kpi.model.util.Strings;

public class ComplexTableAggregationElements
        extends RequiredTableAttribute<List<ComplexAggregationElement>>
        implements AggregationElementsAttribute<ComplexAggregationElement> {

    private ComplexTableAggregationElements(final List<ComplexAggregationElement> value) {
        super(value);
    }

    @JsonCreator
    public static ComplexTableAggregationElements of(
            @JsonSetter(contentNulls = Nulls.FAIL)
            @JsonProperty(value = Attributes.ATTRIBUTE_AGGREGATION_ELEMENTS, required = true) final List<ComplexAggregationElement> value
    ) {
        return new ComplexTableAggregationElements(value);
    }

    @Override
    public String name() {
        return Attributes.ATTRIBUTE_AGGREGATION_ELEMENTS;
    }

    @Override
    public String toString() {
        return Strings.stringifyIterable(this);
    }

    @Override
    protected ValidationResult validateValue(List<ComplexAggregationElement> value) {
        return super.validateValue(value).andThen(() -> value.isEmpty()
                ? ValidationResult.invalid("Table attribute '%s' is empty, but this attribute is \"required\", must not be empty", name())
                : ValidationResult.valid()
        );
    }
}
