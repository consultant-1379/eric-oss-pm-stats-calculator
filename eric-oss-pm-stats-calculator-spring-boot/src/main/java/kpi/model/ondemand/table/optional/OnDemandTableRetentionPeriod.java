/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.ondemand.table.optional;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import kpi.model.api.table.OptionalTableAttribute;
import kpi.model.api.table.definition.api.RetentionPeriodAttribute;
import kpi.model.api.validation.ValidationResult;
import kpi.model.util.Attributes;
import kpi.model.util.ValidationResults;

public class OnDemandTableRetentionPeriod extends OptionalTableAttribute<Integer> implements RetentionPeriodAttribute {
    private OnDemandTableRetentionPeriod(Integer value) {
        super(value);
    }

    @JsonCreator
    public static OnDemandTableRetentionPeriod of(@JsonProperty(Attributes.ATTRIBUTE_RETENTION_PERIOD) final Integer value) {
        return new OnDemandTableRetentionPeriod(value);
    }

    public static OnDemandTableRetentionPeriod empty() {
        return new OnDemandTableRetentionPeriod(null);
    }

    @Nullable
    @Override
    protected Integer defaultValue() {
        return null;
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
