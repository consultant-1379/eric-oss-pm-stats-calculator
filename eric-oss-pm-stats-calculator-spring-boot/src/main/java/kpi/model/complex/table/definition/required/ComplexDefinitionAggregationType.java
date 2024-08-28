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
import kpi.model.api.enumeration.AggregationType;
import kpi.model.api.table.definition.RequiredDefinitionAttribute;
import kpi.model.api.table.definition.api.AggregationTypeAttribute;
import kpi.model.api.validation.ValidationResult;
import kpi.model.util.Attributes;

public class ComplexDefinitionAggregationType extends RequiredDefinitionAttribute<AggregationType> implements AggregationTypeAttribute {

    private ComplexDefinitionAggregationType(final AggregationType value) {
        super(value);
    }

    @JsonCreator
    public static ComplexDefinitionAggregationType of(
            @JsonProperty(value = Attributes.ATTRIBUTE_AGGREGATION_TYPE, required = true) final AggregationType value
    ) {
        return new ComplexDefinitionAggregationType(value);
    }

    @Override
    public String name() {
        return Attributes.ATTRIBUTE_AGGREGATION_TYPE;
    }

    @Override
    protected ValidationResult validateValue(final AggregationType value) {
        return super.validateValue(value).andThen(ValidationResult::valid);
    }
}
