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
import kpi.model.api.table.definition.OptionalDefinitionAttribute;
import kpi.model.api.table.definition.api.FiltersAttribute;
import kpi.model.simple.element.SimpleFilterElement;
import kpi.model.util.Attributes;
import kpi.model.util.Strings;

public class SimpleDefinitionFilters extends OptionalDefinitionAttribute<List<SimpleFilterElement>> implements FiltersAttribute<SimpleFilterElement> {

    private SimpleDefinitionFilters(final List<SimpleFilterElement> value) {
        super(Objects.isNull(value) ? Collections.emptyList() : value);
    }

    @JsonCreator
    public static SimpleDefinitionFilters of(
            @JsonSetter(contentNulls = Nulls.FAIL)
            @JsonProperty(Attributes.ATTRIBUTE_FILTERS) final List<SimpleFilterElement> value
    ) {
        return new SimpleDefinitionFilters(value);
    }

    @Override
    public String name() {
        return Attributes.ATTRIBUTE_FILTERS;
    }

    @Override
    public String toString() {
        return Strings.stringifyIterable(this);
    }

}
