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
import kpi.model.api.table.definition.api.InpDataIdentifierAttribute;
import kpi.model.api.validation.ValidationResult;
import kpi.model.util.Attributes;
import kpi.model.util.PatternConstants;
import kpi.model.util.ValidationResults;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class SimpleTableInpDataIdentifier extends RequiredTableAttribute<String> implements InpDataIdentifierAttribute {

    private SimpleTableInpDataIdentifier(final String value) {
        super(value);
    }

    @JsonCreator
    public static SimpleTableInpDataIdentifier of(@JsonProperty(value = Attributes.ATTRIBUTE_INP_DATA_IDENTIFIER, required = true) final String value) {
        return new SimpleTableInpDataIdentifier(value);
    }

    @Override
    public String name() {
        return Attributes.ATTRIBUTE_INP_DATA_IDENTIFIER;
    }

    @Override
    protected ValidationResult validateValue(final String value) {
        return super.validateValue(value)
                .andThen(() -> ValidationResults.valueIsNotBlank(name(), value))
                .andThen(() -> ValidationResults.valueMatchesPattern(name(), value, PatternConstants.PATTERN_INP_DATA_IDENTIFIER));
    }
}
