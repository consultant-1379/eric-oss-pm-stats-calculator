/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.configuration.CalculatorProperties;
import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExecutionGroupGeneratorTest {

    @Mock CalculatorProperties calculatorPropertiesMock;

    @InjectMocks ExecutionGroupGenerator objectUnderTest;

    static final DataIdentifier INP_DATA_IDENTIFIER = DataIdentifier.of("server|topic|schema");
    static final String EXECUTION_GROUP_1 = INP_DATA_IDENTIFIER.getName();
    static final String EXECUTION_GROUP_2 = INP_DATA_IDENTIFIER.getName();

    @Test
    void whenExecutionGroupIsReady_shouldReturnTheCorrectExecutionGroup() {
        final KpiDefinitionEntity entity = KpiDefinitionEntity.builder().withExecutionGroup(ExecutionGroup.builder().withId(1).withName(EXECUTION_GROUP_1).build()).build();

        final String actual = objectUnderTest.generateOrGetExecutionGroup(entity);

        assertThat(actual).isEqualTo(EXECUTION_GROUP_1);
    }

    @Nested
    @DisplayName("Given grouping flags")
    class GivenGroupingFlags {
        static final String KPI_DEFINITION_1 = "kpiDefinition1";
        static final String AGGREGATION_ELEMENT_1 = "aggregationElement1";
        static final String AGGREGATION_ELEMENT_2 = "aggregationElement2";
        static final int AGGREGATION_PERIOD = 60;
        static final String DATA_SPACE = "server";
        static final String SCHEMA_CATEGORY = "topic";
        static final String SCHEMA_NAME = "schema";
        KpiDefinitionEntity kpiDefinitionEntityToSaveWithAggregationPeriod;
        KpiDefinitionEntity kpiDefinitionEntityToSaveWithoutAggregationPeriod;

        @BeforeEach
        void setUp() {
            kpiDefinitionEntityToSaveWithAggregationPeriod = KpiDefinitionEntity.builder()
                    .withName(KPI_DEFINITION_1)
                    .withAggregationPeriod(AGGREGATION_PERIOD)
                    .withAggregationElements(Arrays.asList(AGGREGATION_ELEMENT_1, AGGREGATION_ELEMENT_2))
                    .withSchemaDataSpace(DATA_SPACE)
                    .withSchemaCategory(SCHEMA_CATEGORY)
                    .withSchemaName(SCHEMA_NAME)
                    .withExecutionGroup(ExecutionGroup.builder().withId(1).withName(EXECUTION_GROUP_1).build()).build();


            kpiDefinitionEntityToSaveWithoutAggregationPeriod = KpiDefinitionEntity.builder()
                    .withName(KPI_DEFINITION_1)
                    .withAggregationPeriod(-1)
                    .withAggregationElements(Arrays.asList(AGGREGATION_ELEMENT_1, AGGREGATION_ELEMENT_2))
                    .withSchemaDataSpace(DATA_SPACE)
                    .withSchemaCategory(SCHEMA_CATEGORY)
                    .withSchemaName(SCHEMA_NAME)
                    .withExecutionGroup(ExecutionGroup.builder().withId(2).withName(EXECUTION_GROUP_2).build())
                    .build();
        }

        @Test
        void whenAllGroupingFlagIsTrue_shouldReturnCorrectExecutionGroup() {
            when(calculatorPropertiesMock.getInputSource()).thenReturn(true);
            when(calculatorPropertiesMock.getAggregationPeriod()).thenReturn(true);
            when(calculatorPropertiesMock.getAggregationElements()).thenReturn(true);

            final String actual = objectUnderTest.generateOrGetExecutionGroup(kpiDefinitionEntityToSaveWithAggregationPeriod);
            final String expected = String.format("%s__%d__[%s || %s]", INP_DATA_IDENTIFIER, AGGREGATION_PERIOD, AGGREGATION_ELEMENT_1,
                    AGGREGATION_ELEMENT_2
            );

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void whenAllGroupingFlagIsTrueNoPeriod_shouldReturnCorrectExecutionGroup() {
            when(calculatorPropertiesMock.getInputSource()).thenReturn(true);
            when(calculatorPropertiesMock.getAggregationPeriod()).thenReturn(true);
            when(calculatorPropertiesMock.getAggregationElements()).thenReturn(true);

            final String actual = objectUnderTest.generateOrGetExecutionGroup(kpiDefinitionEntityToSaveWithoutAggregationPeriod);
            final String expected = String.format("%s__NO-PERIOD__[%s || %s]", INP_DATA_IDENTIFIER, AGGREGATION_ELEMENT_1, AGGREGATION_ELEMENT_2);

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void whenInputSourceAndAggregationPeriodFlagIsTrue_shouldReturnCorrectExecutionGroup() {
            when(calculatorPropertiesMock.getInputSource()).thenReturn(true);
            when(calculatorPropertiesMock.getAggregationPeriod()).thenReturn(true);
            when(calculatorPropertiesMock.getAggregationElements()).thenReturn(false);

            final String actual = objectUnderTest.generateOrGetExecutionGroup(kpiDefinitionEntityToSaveWithAggregationPeriod);
            final String expected = String.format("%s__%d", INP_DATA_IDENTIFIER, AGGREGATION_PERIOD);

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void whenInputSourceAndAggregationPeriodFlagIsTrueNoPeriod_shouldReturnCorrectExecutionGroup() {
            when(calculatorPropertiesMock.getInputSource()).thenReturn(true);
            when(calculatorPropertiesMock.getAggregationPeriod()).thenReturn(true);
            when(calculatorPropertiesMock.getAggregationElements()).thenReturn(false);

            final String actual = objectUnderTest.generateOrGetExecutionGroup(kpiDefinitionEntityToSaveWithoutAggregationPeriod);
            final String expected = String.format("%s__NO-PERIOD", INP_DATA_IDENTIFIER);

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void WhenInputSourceAndAggregationElementsFlagIsTrue_shouldReturnCorrectExecutionGroup() {
            when(calculatorPropertiesMock.getInputSource()).thenReturn(true);
            when(calculatorPropertiesMock.getAggregationPeriod()).thenReturn(false);
            when(calculatorPropertiesMock.getAggregationElements()).thenReturn(true);

            final String actual = objectUnderTest.generateOrGetExecutionGroup(kpiDefinitionEntityToSaveWithAggregationPeriod);
            final String expected = String.format("%s__[%s || %s]", INP_DATA_IDENTIFIER, AGGREGATION_ELEMENT_1, AGGREGATION_ELEMENT_2);

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void whenAggregationPeriodAndAggregationElementsFlagIsTrue_shouldReturnCorrectExecutionGroup() {
            when(calculatorPropertiesMock.getInputSource()).thenReturn(false);
            when(calculatorPropertiesMock.getAggregationPeriod()).thenReturn(true);
            when(calculatorPropertiesMock.getAggregationElements()).thenReturn(true);

            final String actual = objectUnderTest.generateOrGetExecutionGroup(kpiDefinitionEntityToSaveWithAggregationPeriod);
            final String expected = String.format("%d__[%s || %s]", AGGREGATION_PERIOD, AGGREGATION_ELEMENT_1, AGGREGATION_ELEMENT_2);

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void whenAggregationPeriodAndAggregationElementsFlagIsTrueNoPeriod_shouldReturnCorrectExecutionGroup() {
            when(calculatorPropertiesMock.getInputSource()).thenReturn(false);
            when(calculatorPropertiesMock.getAggregationPeriod()).thenReturn(true);
            when(calculatorPropertiesMock.getAggregationElements()).thenReturn(true);

            final String actual = objectUnderTest.generateOrGetExecutionGroup(kpiDefinitionEntityToSaveWithoutAggregationPeriod);
            final String expected = String.format("NO-PERIOD__[%s || %s]", AGGREGATION_ELEMENT_1, AGGREGATION_ELEMENT_2);

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void whenOnlyAggregationPeriodFlagIsTrue_shouldReturnCorrectExecutionGroup() {
            when(calculatorPropertiesMock.getInputSource()).thenReturn(false);
            when(calculatorPropertiesMock.getAggregationPeriod()).thenReturn(true);
            when(calculatorPropertiesMock.getAggregationElements()).thenReturn(false);

            final String actual = objectUnderTest.generateOrGetExecutionGroup(kpiDefinitionEntityToSaveWithAggregationPeriod);
            final String expected = String.valueOf(AGGREGATION_PERIOD);

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void whenOnlyAggregationElementsFlagIsTrue_shouldReturnCorrectExecutionGroup() {
            when(calculatorPropertiesMock.getInputSource()).thenReturn(false);
            when(calculatorPropertiesMock.getAggregationPeriod()).thenReturn(false);
            when(calculatorPropertiesMock.getAggregationElements()).thenReturn(true);

            final String actual = objectUnderTest.generateOrGetExecutionGroup(kpiDefinitionEntityToSaveWithAggregationPeriod);
            final String expected = String.format("[%s || %s]", AGGREGATION_ELEMENT_1, AGGREGATION_ELEMENT_2);

            assertThat(actual).isEqualTo(expected);
        }

        @AfterEach
        void verifyMocking() {
            verify(calculatorPropertiesMock).getInputSource();
            verify(calculatorPropertiesMock).getAggregationPeriod();
            verify(calculatorPropertiesMock).getAggregationElements();
        }
    }
}
