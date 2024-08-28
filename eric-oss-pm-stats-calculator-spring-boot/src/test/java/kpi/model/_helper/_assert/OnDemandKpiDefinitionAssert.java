/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model._helper._assert;

import kpi.model.api.table.definition.OnDemandKpiDefinitions.OnDemandKpiDefinition;
import kpi.model.ondemand.table.definition.optional.OnDemandDefinitionAggregationElements;
import kpi.model.ondemand.table.definition.optional.OnDemandDefinitionExportable;
import kpi.model.ondemand.table.definition.optional.OnDemandDefinitionFilters;
import kpi.model.ondemand.table.definition.required.OnDemandDefinitionAggregationType;
import kpi.model.ondemand.table.definition.required.OnDemandDefinitionExpression;
import kpi.model.ondemand.table.definition.required.OnDemandDefinitionName;
import kpi.model.ondemand.table.definition.required.OnDemandDefinitionObjectType;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;

public class OnDemandKpiDefinitionAssert extends AbstractAssert<OnDemandKpiDefinitionAssert, OnDemandKpiDefinition> {
    protected OnDemandKpiDefinitionAssert(final OnDemandKpiDefinition actual, final Class<?> selfType) {
        super(actual, selfType);
    }

    public OnDemandKpiDefinitionAssert isEqualTo(final OnDemandKpiDefinition expected) {
        Assertions.assertThat(expected).isNotNull();
        isNotNull();

        final OnDemandDefinitionName name = actual.name();
        final OnDemandDefinitionExpression expression = actual.expression();
        final OnDemandDefinitionObjectType objectType = actual.objectType();
        final OnDemandDefinitionAggregationType aggregationType = actual.aggregationType();
        final OnDemandDefinitionAggregationElements aggregationElements = actual.aggregationElements();
        final OnDemandDefinitionExportable exportable = actual.exportable();
        final OnDemandDefinitionFilters filters = actual.filters();

        /* Exhaustive assertion to see on what fields we have failed */
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(name).as(name.name()).isEqualTo(expected.name());
        softly.assertThat(expression).as(expression.name()).isEqualTo(expected.expression());
        softly.assertThat(objectType).as(objectType.name()).isEqualTo(expected.objectType());
        softly.assertThat(aggregationType).as(aggregationType.name()).isEqualTo(expected.aggregationType());
        softly.assertThat(aggregationElements).as(aggregationElements.name()).isEqualTo(expected.aggregationElements());
        softly.assertThat(exportable).as(exportable.name()).isEqualTo(expected.exportable());
        softly.assertThat(filters).as(filters.name()).isEqualTo(expected.filters());
        softly.assertThat(actual.isScheduledComplex()).as("isScheduledComplex").isFalse();
        softly.assertThat(actual.isSimple()).as("isSimple").isFalse();
        softly.assertAll();

        /* Verify no attribute is missing from equality */
        Assertions.assertThat(actual).isEqualTo(expected);
        return this;
    }
}
