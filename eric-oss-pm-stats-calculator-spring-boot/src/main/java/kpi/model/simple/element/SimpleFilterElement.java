/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.simple.element;

import static kpi.model.util.ValidationResults.valueDoesNotContain;
import static kpi.model.util.ValidationResults.valueIsNotBlank;
import static kpi.model.util.ValidationResults.valueIsNotParameterized;
import static kpi.model.util.ValidationResults.valueMatchesPattern;

import com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants;
import com.ericsson.oss.air.pm.stats.common.model.element.FilterElement;

import com.fasterxml.jackson.annotation.JsonCreator;
import kpi.model.api.element.ElementBase;
import kpi.model.api.validation.ValidationResult;
import kpi.model.util.Attributes;
import kpi.model.util.PatternConstants;

public class SimpleFilterElement extends ElementBase<String> implements FilterElement {

    private SimpleFilterElement(final String value) {
        super(value);
    }

    @JsonCreator
    public static SimpleFilterElement of(final String value) {
        return new SimpleFilterElement(value);
    }

    @Override
    public String name() {
        return Attributes.FILTER_ELEMENT;
    }

    @Override
    protected ValidationResult validateValue(final String value) {
        return super.validateValue(value)
                .andThen(() -> valueIsNotBlank(name(), value))
                .andThen(() -> valueIsNotParameterized(name(), value))
                .andThen(() -> valueMatchesPattern(name(), value, PatternConstants.PATTERN_TO_FIND_MORE_THAN_ONE_DATASOURCE))
                .andThen(() -> valueDoesNotContain(name(), value, KpiCalculatorConstants.SQL_WHERE))
                .andThen(() -> valueDoesNotContain(name(), value, KpiCalculatorConstants.SQL_FROM));
    }
}
