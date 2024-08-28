/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.complex.table.definition.optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import kpi.model.api.table.api.TableAttribute;
import kpi.model.api.table.definition.OptionalDefinitionAttribute;
import kpi.model.api.table.definition.api.DataReliabilityOffsetAttribute;
import kpi.model.api.validation.ValidationResult;
import kpi.model.complex.table.optional.ComplexTableDataReliabilityOffset;
import kpi.model.util.Attributes;

public class ComplexDefinitionDataReliabilityOffset extends OptionalDefinitionAttribute<Integer> implements DataReliabilityOffsetAttribute {

    private ComplexDefinitionDataReliabilityOffset(final Integer value) {
        super(value);
    }

    @JsonCreator
    public static ComplexDefinitionDataReliabilityOffset of(@JsonProperty(Attributes.ATTRIBUTE_DATA_RELIABILITY_OFFSET) final Integer value) {
        return new ComplexDefinitionDataReliabilityOffset(value);
    }

    @Override
    public String name() {
        return Attributes.ATTRIBUTE_DATA_RELIABILITY_OFFSET;
    }

    @Override
    protected ValidationResult validateValue(final Integer value) {
        return super.validateValue(value).andThen(ValidationResult::valid);
    }

    @Override
    public Class<? extends TableAttribute<Integer>> parentClass() {
        return ComplexTableDataReliabilityOffset.class;
    }
}
