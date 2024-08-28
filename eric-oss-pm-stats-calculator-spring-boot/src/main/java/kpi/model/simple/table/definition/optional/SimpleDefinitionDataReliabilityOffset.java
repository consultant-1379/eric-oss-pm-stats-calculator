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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import kpi.model.api.table.api.TableAttribute;
import kpi.model.api.table.definition.OptionalDefinitionAttribute;
import kpi.model.api.table.definition.api.DataReliabilityOffsetAttribute;
import kpi.model.api.validation.ValidationResult;
import kpi.model.simple.table.optional.SimpleTableDataReliabilityOffset;
import kpi.model.util.Attributes;

public class SimpleDefinitionDataReliabilityOffset extends OptionalDefinitionAttribute<Integer> implements DataReliabilityOffsetAttribute {

    private SimpleDefinitionDataReliabilityOffset(final Integer value) {
        super(value);
    }

    @JsonCreator
    public static SimpleDefinitionDataReliabilityOffset of(@JsonProperty(Attributes.ATTRIBUTE_DATA_RELIABILITY_OFFSET) final Integer value) {
        return new SimpleDefinitionDataReliabilityOffset(value);
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
        return SimpleTableDataReliabilityOffset.class;
    }
}
