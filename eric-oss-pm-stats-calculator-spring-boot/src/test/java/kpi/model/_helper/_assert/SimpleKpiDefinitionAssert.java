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

import kpi.model.api.table.definition.SimpleKpiDefinitions.SimpleKpiDefinition;
import kpi.model.simple.table.definition.optional.SimpleDefinitionAggregationElements;
import kpi.model.simple.table.definition.optional.SimpleDefinitionDataLookBackLimit;
import kpi.model.simple.table.definition.optional.SimpleDefinitionDataReliabilityOffset;
import kpi.model.simple.table.definition.optional.SimpleDefinitionExportable;
import kpi.model.simple.table.definition.optional.SimpleDefinitionFilters;
import kpi.model.simple.table.definition.optional.SimpleDefinitionInpDataIdentifier;
import kpi.model.simple.table.definition.optional.SimpleDefinitionReexportLateData;
import kpi.model.simple.table.definition.required.SimpleDefinitionAggregationType;
import kpi.model.simple.table.definition.required.SimpleDefinitionExpression;
import kpi.model.simple.table.definition.required.SimpleDefinitionName;
import kpi.model.simple.table.definition.required.SimpleDefinitionObjectType;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;

public class SimpleKpiDefinitionAssert extends AbstractAssert<SimpleKpiDefinitionAssert, SimpleKpiDefinition> {
    protected SimpleKpiDefinitionAssert(final SimpleKpiDefinition actual, final Class<?> selfType) {
        super(actual, selfType);
    }

    public SimpleKpiDefinitionAssert isEqualTo(final SimpleKpiDefinition expected) {
        Assertions.assertThat(expected).isNotNull();
        isNotNull();

        final SimpleDefinitionName name = actual.name();
        final SimpleDefinitionExpression expression = actual.expression();
        final SimpleDefinitionObjectType objectType = actual.objectType();
        final SimpleDefinitionAggregationType aggregationType = actual.aggregationType();
        final SimpleDefinitionAggregationElements aggregationElements = actual.aggregationElements();
        final SimpleDefinitionExportable exportable = actual.exportable();
        final SimpleDefinitionFilters filters = actual.filters();
        final SimpleDefinitionDataReliabilityOffset dataReliabilityOffset = actual.dataReliabilityOffset();
        final SimpleDefinitionDataLookBackLimit dataLookBackLimit = actual.dataLookBackLimit();
        final SimpleDefinitionReexportLateData reexportLateData = actual.reexportLateData();
        final SimpleDefinitionInpDataIdentifier inpDataIdentifier = actual.inpDataIdentifier();

        /* Exhaustive assertion to see on what fields we have failed */
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(name).as(name.name()).isEqualTo(expected.name());
        softly.assertThat(expression).as(expression.name()).isEqualTo(expected.expression());
        softly.assertThat(objectType).as(objectType.name()).isEqualTo(expected.objectType());
        softly.assertThat(aggregationType).as(aggregationType.name()).isEqualTo(expected.aggregationType());
        softly.assertThat(aggregationElements).as(aggregationElements.name()).isEqualTo(expected.aggregationElements());
        softly.assertThat(exportable).as(exportable.name()).isEqualTo(expected.exportable());
        softly.assertThat(filters).as(filters.name()).isEqualTo(expected.filters());
        softly.assertThat(dataReliabilityOffset).as(dataReliabilityOffset.name()).isEqualTo(expected.dataReliabilityOffset());
        softly.assertThat(dataLookBackLimit).as(dataLookBackLimit.name()).isEqualTo(expected.dataLookBackLimit());
        softly.assertThat(reexportLateData).as(reexportLateData.name()).isEqualTo(expected.reexportLateData());
        softly.assertThat(inpDataIdentifier).as(inpDataIdentifier.name()).isEqualTo(expected.inpDataIdentifier());
        softly.assertThat(actual.isScheduledComplex()).as("isScheduledComplex").isFalse();
        softly.assertThat(actual.isSimple()).as("isSimple").isTrue();
        softly.assertAll();

        /* Verify no attribute is missing from equality */
        Assertions.assertThat(actual).isEqualTo(expected);
        return this;
    }
}
