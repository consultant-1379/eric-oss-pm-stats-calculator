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
import kpi.model.api.table.definition.api.DataLookBackLimitAttribute;
import kpi.model.api.validation.ValidationResult;
import kpi.model.simple.table.optional.SimpleTableDataLookBackLimit;
import kpi.model.util.Attributes;
import kpi.model.util.ValidationResults;

public class SimpleDefinitionDataLookBackLimit extends OptionalDefinitionAttribute<Integer> implements DataLookBackLimitAttribute {

    private SimpleDefinitionDataLookBackLimit(final Integer value) {
        super(value);
    }

    @JsonCreator
    public static SimpleDefinitionDataLookBackLimit of(@JsonProperty(Attributes.ATTRIBUTE_DATA_LOOK_BACK_LIMIT) final Integer value) {
        return new SimpleDefinitionDataLookBackLimit(value);
    }

    @Override
    public String name() {
        return Attributes.ATTRIBUTE_DATA_LOOK_BACK_LIMIT;
    }

    @Override
    public Class<? extends TableAttribute<Integer>> parentClass() {
        return SimpleTableDataLookBackLimit.class;
    }

    @Override
    protected ValidationResult validateValue(final Integer value) {
        return super.validateValue(value)
                .andIfNotNullThen(value, () -> ValidationResults.valueIsGreaterThanZero(name(), value));
    }
}
