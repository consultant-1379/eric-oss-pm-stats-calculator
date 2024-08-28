/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.simple.table.definition.optional;

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
import kpi.model.simple.element.SimpleAggregationElement;
import kpi.model.simple.table.required.SimpleTableAggregationElements;
import kpi.model.util.Attributes;
import kpi.model.util.Strings;
import kpi.model.util.ValidationResults;

public class SimpleDefinitionAggregationElements
        extends OptionalDefinitionAttribute<List<SimpleAggregationElement>>
        implements AggregationElementsAttribute<SimpleAggregationElement> {

    private SimpleDefinitionAggregationElements(final List<SimpleAggregationElement> value) {
        super(Objects.isNull(value) ? Collections.emptyList() : value);
    }

    @JsonCreator
    public static SimpleDefinitionAggregationElements of(
            @JsonSetter(contentNulls = Nulls.FAIL)
            @JsonProperty(Attributes.ATTRIBUTE_AGGREGATION_ELEMENTS) final List<SimpleAggregationElement> value
    ) {
        return new SimpleDefinitionAggregationElements(value);
    }

    @Override
    public String name() {
        return Attributes.ATTRIBUTE_AGGREGATION_ELEMENTS;
    }

    @Override
    public boolean canOverride(final List<SimpleAggregationElement> value) {
        return isEmpty() && isNotEmpty(value);
    }

    @Override
    public String toString() {
        return Strings.stringifyIterable(this);
    }

    @Override
    public Class<? extends TableAttribute<List<SimpleAggregationElement>>> parentClass() {
        return SimpleTableAggregationElements.class;
    }

    private static boolean isNotEmpty(final List<SimpleAggregationElement> value) {
        return !ValidationResults.isEmpty(value);
    }

}
