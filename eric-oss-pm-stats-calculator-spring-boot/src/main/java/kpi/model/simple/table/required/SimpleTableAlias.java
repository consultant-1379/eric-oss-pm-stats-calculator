/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.simple.table.required;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import kpi.model.api.table.RequiredTableAttribute;
import kpi.model.api.table.definition.api.AliasAttribute;
import kpi.model.api.validation.ValidationResult;
import kpi.model.util.Attributes;
import kpi.model.util.PatternConstants;
import kpi.model.util.ValidationResults;

public class SimpleTableAlias extends RequiredTableAttribute<String> implements AliasAttribute {

    private SimpleTableAlias(final String value) {
        super(value);
    }

    @JsonCreator
    public static SimpleTableAlias of(@JsonProperty(value = Attributes.ATTRIBUTE_ALIAS, required = true) final String value) {
        return new SimpleTableAlias(value);
    }

    @Override
    public String name() {
        return Attributes.ATTRIBUTE_ALIAS;
    }

    @Override
    protected ValidationResult validateValue(final String value) {
        return super.validateValue(value)
                .andThen(() -> ValidationResults.valueIsNotBlank(name(), value))
                .andThen(() -> ValidationResults.valueMatchesPattern(name(), value, PatternConstants.PATTERN_ALIAS_ATTRIBUTE));
    }
}
