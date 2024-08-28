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

import com.ericsson.oss.air.pm.stats.common.model.attribute.ExpressionAttribute;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import kpi.model.api.table.definition.RequiredDefinitionAttribute;
import kpi.model.api.validation.ValidationResult;
import kpi.model.util.Attributes;
import kpi.model.util.ValidationResults;

public class OnDemandDefinitionExpression extends RequiredDefinitionAttribute<String> implements ExpressionAttribute {

    private OnDemandDefinitionExpression(final String value) {
        super(value);
    }

    @JsonCreator
    public static OnDemandDefinitionExpression of(@JsonProperty(value = Attributes.ATTRIBUTE_EXPRESSION, required = true) final String value) {
        return new OnDemandDefinitionExpression(value);
    }

    @Override
    public String name() {
        return Attributes.ATTRIBUTE_EXPRESSION;
    }

    @Override
    protected ValidationResult validateValue(final String value) {
        return super.validateValue(value)
                .andThen(() -> ValidationResults.valueIsNotBlank(name(), value))
                .andThen(() -> ValidationResults.valueContainsExactlyOneFrom(name(), value))
                .andThen(() -> ValidationResults.valueNotContainsOtherDatasourceWhenContains(Datasource.KPI_POST_AGGREGATION, name(), value))
                .andThen(() -> ValidationResults.valueNotContainsOtherDatasourceWhenContains(Datasource.KPI_IN_MEMORY, name(), value));
    }
}
