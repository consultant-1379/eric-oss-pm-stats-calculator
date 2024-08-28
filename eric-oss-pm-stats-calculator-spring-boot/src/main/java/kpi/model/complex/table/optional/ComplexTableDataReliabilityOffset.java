/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.complex.table.optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import kpi.model.api.table.OptionalTableAttribute;
import kpi.model.api.table.definition.api.DataReliabilityOffsetAttribute;
import kpi.model.api.validation.ValidationResult;
import kpi.model.util.Attributes;
import kpi.model.util.Defaults;

public class ComplexTableDataReliabilityOffset extends OptionalTableAttribute<Integer> implements DataReliabilityOffsetAttribute {

    private ComplexTableDataReliabilityOffset(final Integer value) {
        super(value);
    }

    @JsonCreator
    public static ComplexTableDataReliabilityOffset of(@JsonProperty(Attributes.ATTRIBUTE_DATA_RELIABILITY_OFFSET) final Integer value) {
        return new ComplexTableDataReliabilityOffset(value);
    }


    @Override
    public String name() {
        return Attributes.ATTRIBUTE_DATA_RELIABILITY_OFFSET;
    }

    @Override
    protected Integer defaultValue() {
        return Defaults.DATA_RELIABILITY_OFFSET;
    }

    @Override
    protected ValidationResult validateValue(final Integer value) {
        return super.validateValue(value).andIfNotNullThen(value, ValidationResult::valid);
    }
}
