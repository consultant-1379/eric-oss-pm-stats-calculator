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

import static kpi.model.util.ValidationResults.valueIsNotBlank;
import static kpi.model.util.ValidationResults.valueMatchesPattern;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import kpi.model.api.table.api.TableAttribute;
import kpi.model.api.table.definition.OptionalDefinitionAttribute;
import kpi.model.api.table.definition.api.InpDataIdentifierAttribute;
import kpi.model.api.validation.ValidationResult;
import kpi.model.simple.table.required.SimpleTableInpDataIdentifier;
import kpi.model.util.Attributes;
import kpi.model.util.PatternConstants;

public class SimpleDefinitionInpDataIdentifier extends OptionalDefinitionAttribute<String> implements InpDataIdentifierAttribute {

    private SimpleDefinitionInpDataIdentifier(final String value) {
        super(value);
    }

    @JsonCreator
    public static SimpleDefinitionInpDataIdentifier of(@JsonProperty(Attributes.ATTRIBUTE_INP_DATA_IDENTIFIER) final String value) {
        return new SimpleDefinitionInpDataIdentifier(value);
    }

    @Override
    public String name() {
        return Attributes.ATTRIBUTE_INP_DATA_IDENTIFIER;
    }

    @Override
    public Class<? extends TableAttribute<String>> parentClass() {
        return SimpleTableInpDataIdentifier.class;
    }

    @Override
    protected ValidationResult validateValue(final String value) {
        return super.validateValue(value)
                .andIfNotNullThen(value, () -> valueIsNotBlank(name(), value))
                .andIfNotNullThen(value, () -> valueMatchesPattern(name(), value, PatternConstants.PATTERN_INP_DATA_IDENTIFIER));
    }
}
