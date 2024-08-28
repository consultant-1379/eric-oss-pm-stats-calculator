/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.complex.table.definition.required;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import kpi.model.api.table.definition.RequiredDefinitionAttribute;
import kpi.model.api.table.definition.api.ExecutionGroupAttribute;
import kpi.model.api.validation.ValidationResult;
import kpi.model.util.Attributes;
import kpi.model.util.ValidationResults;

public class ComplexDefinitionExecutionGroup extends RequiredDefinitionAttribute<String> implements ExecutionGroupAttribute {

    private ComplexDefinitionExecutionGroup(final String value) {
        super(value);
    }

    @JsonCreator
    public static ComplexDefinitionExecutionGroup of(@JsonProperty(value = Attributes.ATTRIBUTE_EXECUTION_GROUP, required = true) final String value) {
        return new ComplexDefinitionExecutionGroup(value);
    }

    @Override
    protected ValidationResult validateValue(final String value) {
        return super.validateValue(value)
                .andThen(() -> ValidationResults.valueIsNotBlank(name(), value));
    }

    @Override
    public String name() {
        return Attributes.ATTRIBUTE_EXECUTION_GROUP;
    }
}
