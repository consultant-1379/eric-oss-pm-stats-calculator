/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.simple.table.optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import kpi.model.api.table.OptionalTableAttribute;
import kpi.model.api.table.definition.api.DataLookBackLimitAttribute;
import kpi.model.api.validation.ValidationResult;
import kpi.model.util.Attributes;
import kpi.model.util.Defaults;
import kpi.model.util.ValidationResults;

public class SimpleTableDataLookBackLimit extends OptionalTableAttribute<Integer> implements DataLookBackLimitAttribute {

    private SimpleTableDataLookBackLimit(final Integer value) {
        super(value);
    }

    @JsonCreator
    public static SimpleTableDataLookBackLimit of(@JsonProperty(Attributes.ATTRIBUTE_DATA_LOOK_BACK_LIMIT) final Integer value) {
        return new SimpleTableDataLookBackLimit(value);
    }

    @Override
    public String name() {
        return Attributes.ATTRIBUTE_DATA_LOOK_BACK_LIMIT;
    }

    @Override
    protected Integer defaultValue() {
        return Defaults.DATA_LOOK_BACK_LIMIT;
    }

    @Override
    protected ValidationResult validateValue(final Integer value) {
        return super.validateValue(value)
                .andIfNotNullThen(value, () -> ValidationResults.valueIsGreaterThanZero(name(), value));
    }
}
