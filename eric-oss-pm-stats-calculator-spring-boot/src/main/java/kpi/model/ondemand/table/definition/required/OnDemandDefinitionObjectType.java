/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.ondemand.table.definition.required;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import kpi.model.api.enumeration.ObjectType;
import kpi.model.api.table.definition.RequiredDefinitionAttribute;
import kpi.model.api.table.definition.api.ObjectTypeAttribute;
import kpi.model.api.validation.ValidationResult;
import kpi.model.util.Attributes;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
public class OnDemandDefinitionObjectType extends RequiredDefinitionAttribute<ObjectType> implements ObjectTypeAttribute {
    private final String originalValue;

    private OnDemandDefinitionObjectType(final String value) {
        super(ObjectType.from(value));
        originalValue = value;
    }

    @JsonCreator
    public static OnDemandDefinitionObjectType of(@JsonProperty(value = Attributes.ATTRIBUTE_OBJECT_TYPE, required = true) final String value) {
        return new OnDemandDefinitionObjectType(value);
    }

    @Override
    public String name() {
        return Attributes.ATTRIBUTE_OBJECT_TYPE;
    }

    @Override
    protected ValidationResult validateValue(final ObjectType value) {
        return super.validateValue(value).andThen(ValidationResult::valid);
    }
}
