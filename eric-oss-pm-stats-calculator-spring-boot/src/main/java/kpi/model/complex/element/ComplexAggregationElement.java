/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.complex.element;

import com.ericsson.oss.air.pm.stats.common.model.element.AggregationElement;

import com.fasterxml.jackson.annotation.JsonCreator;
import kpi.model.api.element.ElementBase;
import kpi.model.api.validation.ValidationResult;
import kpi.model.util.Attributes;
import kpi.model.util.PatternConstants;
import kpi.model.util.ValidationResults;

public class ComplexAggregationElement extends ElementBase<String> implements AggregationElement {

    private ComplexAggregationElement(final String value) {
        super(value);
    }

    @JsonCreator
    public static ComplexAggregationElement of(final String value) {
        return new ComplexAggregationElement(value);
    }

    @Override
    public String name() {
        return Attributes.AGGREGATION_ELEMENT;
    }

    @Override
    protected ValidationResult validateValue(final String value) {
        return super.validateValue(value)
                .andThen(() -> ValidationResults.aggregationElementsValueIsNotBlank(value))
                .andThen(() -> ValidationResults.valueIsNotParameterized(name(), value))
                .andThen(() -> ValidationResults.valueMatchesPattern(name(), value, PatternConstants.PATTERN_NON_SIMPLE_AGGREGATION_ELEMENT));
    }
}
