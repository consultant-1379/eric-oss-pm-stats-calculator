/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model._helper;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import kpi.model.complex.element.ComplexAggregationElement;
import kpi.model.complex.element.ComplexFilterElement;
import kpi.model.ondemand.element.OnDemandAggregationElement;
import kpi.model.ondemand.element.OnDemandFilterElement;
import kpi.model.simple.element.SimpleAggregationElement;
import kpi.model.simple.element.SimpleFilterElement;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Mapper {

    public static List<SimpleAggregationElement> toAggregationElements(final List<String> aggregationElements) {
        return toAggregationElements(aggregationElements.toArray(String[]::new));
    }

    public static List<SimpleAggregationElement> toAggregationElements(final String... aggregationElements) {
        return Arrays.stream(aggregationElements).map(SimpleAggregationElement::of).collect(Collectors.toList());
    }

    public static List<ComplexAggregationElement> toComplexAggregationElements(final List<String> aggregationElements) {
        return toComplexAggregationElements(aggregationElements.toArray(String[]::new));
    }

    public static List<ComplexAggregationElement> toComplexAggregationElements(final String... aggregationElements) {
        return Arrays.stream(aggregationElements).map(ComplexAggregationElement::of).collect(Collectors.toList());
    }

    public static List<OnDemandAggregationElement> toOnDemandAggregationElements(final List<String> aggregationElements) {
        return toOnDemandAggregationElements(aggregationElements.toArray(String[]::new));
    }

    public static List<OnDemandAggregationElement> toOnDemandAggregationElements(final String... aggregationElements) {
        return Arrays.stream(aggregationElements).map(OnDemandAggregationElement::of).collect(Collectors.toList());
    }

    public static List<SimpleFilterElement> toFilterElements(final List<String> filters) {
        return toFilterElements(filters.toArray(String[]::new));
    }

    public static List<SimpleFilterElement> toFilterElements(final String... filters) {
        return Arrays.stream(filters).map(SimpleFilterElement::of).collect(Collectors.toList());
    }

    public static List<ComplexFilterElement> toComplexFilterElements(final List<String> filters) {
        return toComplexFilterElements(filters.toArray(String[]::new));
    }

    public static List<ComplexFilterElement> toComplexFilterElements(final String... filters) {
        return Arrays.stream(filters).map(ComplexFilterElement::of).collect(Collectors.toList());
    }

    public static List<OnDemandFilterElement> toOnDemandFilterElements(final List<String> filters) {
        return toOnDemandFilterElements(filters.toArray(String[]::new));
    }

    public static List<OnDemandFilterElement> toOnDemandFilterElements(final String... filters) {
        return Arrays.stream(filters).map(OnDemandFilterElement::of).collect(Collectors.toList());
    }
}
