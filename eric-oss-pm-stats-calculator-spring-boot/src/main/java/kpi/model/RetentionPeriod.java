/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import kpi.model.api.OptionalAttribute;
import kpi.model.api.table.definition.api.RetentionPeriodAttribute;
import kpi.model.api.validation.ValidationResult;
import kpi.model.util.Attributes;
import kpi.model.util.ValidationResults;

public class RetentionPeriod extends OptionalAttribute<Integer> implements RetentionPeriodAttribute {
    private RetentionPeriod(Integer value) {
        super(value);
    }

    @JsonCreator
    public static RetentionPeriod of(@JsonProperty(Attributes.ATTRIBUTE_RETENTION_PERIOD) final Integer value) {
        return new RetentionPeriod(value);
    }

    @Override
    public String name() {
        return Attributes.ATTRIBUTE_RETENTION_PERIOD;
    }

    @Override
    protected ValidationResult validateValue(final Integer value) {
        return super.validateValue(value)
                .andIfNotNullThen(value, () -> ValidationResults.valueIsGreaterThanZero(name(), value));
    }
}
