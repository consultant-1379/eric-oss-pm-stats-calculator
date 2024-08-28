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

import kpi.model.api.table.definition.ComplexKpiDefinitions.ComplexKpiDefinition;
import kpi.model.complex.table.definition.optional.ComplexDefinitionAggregationElements;
import kpi.model.complex.table.definition.optional.ComplexDefinitionDataLookBackLimit;
import kpi.model.complex.table.definition.optional.ComplexDefinitionDataReliabilityOffset;
import kpi.model.complex.table.definition.optional.ComplexDefinitionExportable;
import kpi.model.complex.table.definition.optional.ComplexDefinitionFilters;
import kpi.model.complex.table.definition.optional.ComplexDefinitionReexportLateData;
import kpi.model.complex.table.definition.required.ComplexDefinitionAggregationType;
import kpi.model.complex.table.definition.required.ComplexDefinitionExpression;
import kpi.model.complex.table.definition.required.ComplexDefinitionName;
import kpi.model.complex.table.definition.required.ComplexDefinitionObjectType;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;

public class ComplexKpiDefinitionAssert extends AbstractAssert<ComplexKpiDefinitionAssert, ComplexKpiDefinition> {

    protected ComplexKpiDefinitionAssert(final ComplexKpiDefinition actual, final Class<?> selfType) {
        super(actual, selfType);
    }

    public ComplexKpiDefinitionAssert isEqualTo(final ComplexKpiDefinition expected) {
        Assertions.assertThat(expected).isNotNull();
        isNotNull();

        final ComplexDefinitionName name = actual.name();
        final ComplexDefinitionExpression expression = actual.expression();
        final ComplexDefinitionObjectType objectType = actual.objectType();
        final ComplexDefinitionAggregationType aggregationType = actual.aggregationType();
        final ComplexDefinitionAggregationElements aggregationElements = actual.aggregationElements();
        final ComplexDefinitionExportable exportable = actual.exportable();
        final ComplexDefinitionFilters filters = actual.filters();
        final ComplexDefinitionDataReliabilityOffset dataReliabilityOffset = actual.dataReliabilityOffset();
        final ComplexDefinitionDataLookBackLimit dataLookBackLimit = actual.dataLookBackLimit();
        final ComplexDefinitionReexportLateData reexportLateData = actual.reexportLateData();

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
        softly.assertThat(actual.isScheduledComplex()).as("isScheduledComplex").isTrue();
        softly.assertThat(actual.isSimple()).as("isSimple").isFalse();
        softly.assertAll();

        /* Verify no attribute is missing from equality */
        Assertions.assertThat(actual).isEqualTo(expected);
        return this;
    }
}
