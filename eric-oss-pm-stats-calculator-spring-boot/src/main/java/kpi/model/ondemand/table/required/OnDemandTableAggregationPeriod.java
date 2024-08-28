/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.ondemand.table.required;

import static kpi.model.api.validation.ValidationResult.invalid;
import static kpi.model.api.validation.ValidationResult.valid;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import kpi.model.api.table.RequiredTableAttribute;
import kpi.model.api.table.definition.api.AggregationPeriodAttribute;
import kpi.model.api.validation.ValidationResult;
import kpi.model.util.Attributes;
import kpi.model.util.Defaults;

public class OnDemandTableAggregationPeriod extends RequiredTableAttribute<Integer> implements AggregationPeriodAttribute {

    private OnDemandTableAggregationPeriod(final Integer value) {
        super(value);
    }

    @JsonCreator
    public static OnDemandTableAggregationPeriod of(
            @JsonProperty(value = Attributes.ATTRIBUTE_AGGREGATION_PERIOD, required = true) final Integer value
    ) {
        return new OnDemandTableAggregationPeriod(value);
    }

    @Override
    public boolean isNotDefault() {
        return value != Defaults.AGGREGATION_PERIOD;
    }

    @Override
    public String name() {
        return Attributes.ATTRIBUTE_AGGREGATION_PERIOD;
    }

    @Override
    protected ValidationResult validateValue(final Integer value) {
        return super.validateValue(value).andThen(() -> {
            if (!List.of(15, 60, 1_440).contains(value)) {
                return invalid("value '%s' for '%s' is not valid", value, name());
            }

            return valid();
        });
    }
}
