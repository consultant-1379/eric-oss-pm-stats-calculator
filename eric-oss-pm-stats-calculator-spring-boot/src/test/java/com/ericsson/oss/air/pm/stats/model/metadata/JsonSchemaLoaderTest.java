/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.model.metadata;


import java.io.IOException;
import java.io.UncheckedIOException;

import com.ericsson.oss.air.pm.stats.calculator.api.model.SqlDataType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.Kpi;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.KpiModel;
import com.ericsson.oss.air.pm.stats.common.resources.utils.ResourceLoaderUtils;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.Verification;
import org.mockito.Mockito;


class JsonSchemaLoaderTest {
    private static final String KPI_SCHEMA_RESOURCE_PATH = "kpiSchema.json";

    @Test
    void shouldThrowIllegalArgumentException_whenConfigDoesNotExist() {
        final String resourceFile = "nonExistingFile.json";
        Assertions.assertThatThrownBy(() -> JsonSchemaLoader.getModelConfig(resourceFile, KpiModel.class))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("InputStream is null for path '%s'", resourceFile);
    }

    @Test
    void shouldThrowUncheckedIOException_whenJsonIsMalformed() {
        final String resourceFile = "malformedFile.json";
        try (final MockedStatic<ResourceLoaderUtils> resourceLoaderUtilsMockedStatic = Mockito.mockStatic(ResourceLoaderUtils.class)) {
            final Verification verification = () -> ResourceLoaderUtils.getClasspathResourceAsString(resourceFile);
            resourceLoaderUtilsMockedStatic.when(verification).thenReturn("{ malformedJson");

            Assertions.assertThatThrownBy(() -> JsonSchemaLoader.getModelConfig(resourceFile, KpiModel.class))
                    .hasRootCauseInstanceOf(IOException.class)
                    .isInstanceOf(UncheckedIOException.class)
                    .hasMessage("Unable to create SchemaModel for resource file of '%s'", resourceFile);

            resourceLoaderUtilsMockedStatic.verify(verification);
        }
    }

    @Test
    void whenLoadingKpiModel_thenTheCorrectConfigurationsAreLoaded() {
        final KpiModel kpiModel = JsonSchemaLoader.getModelConfig(KPI_SCHEMA_RESOURCE_PATH, KpiModel.class);
        Assertions.assertThat(kpiModel.getName()).as("name").isEqualTo("son-kpi-model");
        Assertions.assertThat(kpiModel.getNamespace()).isEqualTo("son");
        Assertions.assertThat(kpiModel.getVersion()).isEqualTo("0.0.4");

        final Kpi element = kpiModel.getElement();
        Assertions.assertThat(element.getAttributes()).satisfiesExactly(attributeName -> {
            Assertions.assertThat(attributeName.getName()).as("name").isEqualTo("name");
            Assertions.assertThat(attributeName.getType()).as("type").isEqualTo(SqlDataType.STRING);
            Assertions.assertThat(attributeName.isOptional()).as("optional").isFalse();
            Assertions.assertThat(attributeName.isParameterizable()).as("parameterizable").isFalse();
        }, attributeAlias -> {
            Assertions.assertThat(attributeAlias.getName()).as("name").isEqualTo("alias");
            Assertions.assertThat(attributeAlias.getType()).as("type").isEqualTo(SqlDataType.STRING);
            Assertions.assertThat(attributeAlias.isOptional()).as("optional").isFalse();
            Assertions.assertThat(attributeAlias.isParameterizable()).as("parameterizable").isFalse();
        }, attributeExpression -> {
            Assertions.assertThat(attributeExpression.getName()).as("name").isEqualTo("expression");
            Assertions.assertThat(attributeExpression.getType()).as("type").isEqualTo(SqlDataType.STRING);
            Assertions.assertThat(attributeExpression.isOptional()).as("optional").isFalse();
            Assertions.assertThat(attributeExpression.isParameterizable()).as("parameterizable").isTrue();
        }, attributeObjectType -> {
            Assertions.assertThat(attributeObjectType.getName()).as("name").isEqualTo("object_type");
            Assertions.assertThat(attributeObjectType.getType()).as("type").isEqualTo(SqlDataType.STRING);
            Assertions.assertThat(attributeObjectType.isOptional()).as("optional").isFalse();
            Assertions.assertThat(attributeObjectType.isParameterizable()).as("parameterizable").isFalse();
        }, attributeAggregationType -> {
            Assertions.assertThat(attributeAggregationType.getName()).as("name").isEqualTo("aggregation_type");
            Assertions.assertThat(attributeAggregationType.getType()).as("type").isEqualTo(SqlDataType.STRING);
            Assertions.assertThat(attributeAggregationType.isOptional()).as("optional").isFalse();
            Assertions.assertThat(attributeAggregationType.isParameterizable()).as("parameterizable").isTrue();
        }, attributeAggregationPeriod -> {
            Assertions.assertThat(attributeAggregationPeriod.getName()).as("name").isEqualTo("aggregation_period");
            Assertions.assertThat(attributeAggregationPeriod.getType()).as("type").isEqualTo(SqlDataType.INT);
            Assertions.assertThat(attributeAggregationPeriod.isOptional()).as("optional").isTrue();
            Assertions.assertThat(attributeAggregationPeriod.isParameterizable()).as("parameterizable").isFalse();
        }, attributeAggregationElements -> {
            Assertions.assertThat(attributeAggregationElements.getName()).as("name").isEqualTo("aggregation_elements");
            Assertions.assertThat(attributeAggregationElements.getType()).as("type").isEqualTo(SqlDataType.STRING_ARRAY);
            Assertions.assertThat(attributeAggregationElements.isOptional()).as("optional").isFalse();
            Assertions.assertThat(attributeAggregationElements.isParameterizable()).as("parameterizable").isTrue();
        }, attributeExportable -> {
            Assertions.assertThat(attributeExportable.getName()).as("name").isEqualTo("exportable");
            Assertions.assertThat(attributeExportable.getType()).as("type").isEqualTo(SqlDataType.BOOLEAN);
            Assertions.assertThat(attributeExportable.isOptional()).as("optional").isTrue();
            Assertions.assertThat(attributeExportable.isParameterizable()).as("parameterizable").isFalse();
        }, attributeFilter -> {
            Assertions.assertThat(attributeFilter.getName()).as("name").isEqualTo("filter");
            Assertions.assertThat(attributeFilter.getType()).as("type").isEqualTo(SqlDataType.STRING_ARRAY);
            Assertions.assertThat(attributeFilter.isOptional()).as("optional").isTrue();
            Assertions.assertThat(attributeFilter.isParameterizable()).as("parameterizable").isTrue();
        }, attributeInpDataIdentifier -> {
            Assertions.assertThat(attributeInpDataIdentifier.getName()).as("name").isEqualTo("inp_data_identifier");
            Assertions.assertThat(attributeInpDataIdentifier.getType()).as("type").isEqualTo(SqlDataType.STRING);
            Assertions.assertThat(attributeInpDataIdentifier.isOptional()).as("optional").isTrue();
            Assertions.assertThat(attributeInpDataIdentifier.isParameterizable()).as("parameterizable").isFalse();
        }, attributeExecutionGroup -> {
            Assertions.assertThat(attributeExecutionGroup.getName()).as("name").isEqualTo("execution_group");
            Assertions.assertThat(attributeExecutionGroup.getType()).as("type").isEqualTo(SqlDataType.STRING);
            Assertions.assertThat(attributeExecutionGroup.isOptional()).as("optional").isTrue();
            Assertions.assertThat(attributeExecutionGroup.isParameterizable()).as("parameterizable").isFalse();
        }, attributeDataReliabilityOffset -> {
            Assertions.assertThat(attributeDataReliabilityOffset.getName()).as("name").isEqualTo("data_reliability_offset");
            Assertions.assertThat(attributeDataReliabilityOffset.getType()).as("type").isEqualTo(SqlDataType.INT);
            Assertions.assertThat(attributeDataReliabilityOffset.isOptional()).as("optional").isTrue();
            Assertions.assertThat(attributeDataReliabilityOffset.isParameterizable()).as("parameterizable").isFalse();
        }, attributeDataLookBackLimit -> {
            Assertions.assertThat(attributeDataLookBackLimit.getName()).as("name").isEqualTo("data_lookback_limit");
            Assertions.assertThat(attributeDataLookBackLimit.getType()).as("type").isEqualTo(SqlDataType.INT);
            Assertions.assertThat(attributeDataLookBackLimit.isOptional()).as("optional").isTrue();
            Assertions.assertThat(attributeDataLookBackLimit.isParameterizable()).as("parameterizable").isFalse();
        }, attributeReexportLateData -> {
            Assertions.assertThat(attributeReexportLateData.getName()).as("name").isEqualTo("reexport_late_data");
            Assertions.assertThat(attributeReexportLateData.getType()).as("type").isEqualTo(SqlDataType.BOOLEAN);
            Assertions.assertThat(attributeReexportLateData.isOptional()).as("optional").isTrue();
            Assertions.assertThat(attributeReexportLateData.isParameterizable()).as("parameterizable").isFalse();
        });
    }
}
