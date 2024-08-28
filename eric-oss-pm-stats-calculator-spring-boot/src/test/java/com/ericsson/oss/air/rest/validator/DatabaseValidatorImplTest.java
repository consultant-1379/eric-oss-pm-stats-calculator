/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.validator;

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_COLLECTION_ID;
import static com.ericsson.oss.air.rest.validator._helper.MotherObject.kpiDefinition;
import static com.ericsson.oss.air.rest.validator._helper.MotherObject.table;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.adapter.KpiDefinitionAdapter;
import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.exception.KpiDefinitionValidationException;
import com.ericsson.oss.air.pm.stats.repository.ExecutionGroupGenerator;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;

import kpi.model.KpiDefinitionRequest;
import kpi.model.OnDemand;
import kpi.model.RetentionPeriod;
import kpi.model.ScheduledComplex;
import kpi.model.ScheduledSimple;
import kpi.model.api.enumeration.AggregationType;
import kpi.model.api.table.definition.ComplexKpiDefinitions.ComplexKpiDefinition;
import kpi.model.api.table.definition.OnDemandKpiDefinitions.OnDemandKpiDefinition;
import kpi.model.api.table.definition.SimpleKpiDefinitions.SimpleKpiDefinition;
import kpi.model.complex.ComplexTable;
import kpi.model.ondemand.OnDemandTable;
import kpi.model.simple.SimpleTable;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatabaseValidatorImplTest {
    @Mock private KpiDefinitionService kpiDefinitionServiceMock;
    @InjectMocks private DatabaseValidatorImpl objectUnderTest;
    @Mock private KpiDefinitionAdapter kpiDefinitionAdapterMock;
    @Mock private ExecutionGroupGenerator executionGroupGeneratorMock;

    private final String DATASPACE_CATEGORY_PARENT_SCHEMA = "dataSpace|category|parent_schema";
    private final String DATASPACE_CATEGORY_ALREADY_IN_THE_DB = "dataSpace|category|already_in_the_db";
    private final String COMPLEX = "COMPLEX";

    @Test
    void shouldValidateWithUniqueName() {
        KpiDefinitionRequest kpiDefinition = createTestSimpleKpiDefinition();

        when(kpiDefinitionServiceMock.findAllKpiNames(DEFAULT_COLLECTION_ID)).thenReturn(Sets.newHashSet());

        objectUnderTest.validateNameIsUnique(kpiDefinition, DEFAULT_COLLECTION_ID);

        verify(kpiDefinitionServiceMock).findAllKpiNames(DEFAULT_COLLECTION_ID);
    }

    @Test
    void shouldValidateExecutionGroup(@Mock final KpiDefinitionEntity kpiDefinitionEntityMock) {
        final KpiDefinitionRequest kpiDefinition = createTestKpiDefinition(DATASPACE_CATEGORY_PARENT_SCHEMA, COMPLEX);

        when(kpiDefinitionAdapterMock.toListOfEntities(kpiDefinition)).thenReturn(List.of(kpiDefinitionEntityMock));
        when(kpiDefinitionServiceMock.findAllSimpleExecutionGroups()).thenReturn(Sets.newHashSet());
        when(kpiDefinitionServiceMock.findAllComplexExecutionGroups()).thenReturn(Sets.newHashSet());

        objectUnderTest.validateExecutionGroup(kpiDefinition);

        verify(kpiDefinitionAdapterMock).toListOfEntities(kpiDefinition);
        verify(kpiDefinitionServiceMock).findAllSimpleExecutionGroups();
        verify(kpiDefinitionServiceMock).findAllComplexExecutionGroups();
    }

    @Test
    void shouldFailWhenExecutionGroupsAreTheSameInACalcRequest() {
        final KpiDefinitionRequest kpiDefinition = createTestKpiDefinition(DATASPACE_CATEGORY_PARENT_SCHEMA, DATASPACE_CATEGORY_PARENT_SCHEMA);
        final List<KpiDefinitionEntity> entities = createKpiDefinitionEntityList(DATASPACE_CATEGORY_PARENT_SCHEMA);

        when(kpiDefinitionAdapterMock.toListOfEntities(kpiDefinition)).thenReturn(entities);
        when(kpiDefinitionServiceMock.findAllSimpleExecutionGroups()).thenReturn(Sets.newHashSet());
        when(kpiDefinitionServiceMock.findAllComplexExecutionGroups()).thenReturn(Sets.newHashSet());
        when(executionGroupGeneratorMock.generateOrGetExecutionGroup(entities.get(0))).thenReturn(DATASPACE_CATEGORY_PARENT_SCHEMA);

        assertThatThrownBy(() -> objectUnderTest.validateExecutionGroup(kpiDefinition))
                .isInstanceOf(KpiDefinitionValidationException.class)
                .hasMessage("Complex execution group cannot be the same as a generated Simple execution group. " +
                        "Conflicting execution group(s): [dataSpace|category|parent_schema]");

        verify(kpiDefinitionAdapterMock).toListOfEntities(kpiDefinition);
        verify(kpiDefinitionServiceMock).findAllSimpleExecutionGroups();
        verify(kpiDefinitionServiceMock).findAllComplexExecutionGroups();
        verify(executionGroupGeneratorMock).generateOrGetExecutionGroup(entities.get(0));
    }

    @Test
    void shouldFailWhenComplexExecutionGroupIsAlreadyDeclaredAsASimpleExecutionGroup() {
        final KpiDefinitionRequest kpiDefinition = createTestKpiDefinition(DATASPACE_CATEGORY_PARENT_SCHEMA,
                DATASPACE_CATEGORY_ALREADY_IN_THE_DB);
        final List<KpiDefinitionEntity> entities = createKpiDefinitionEntityList(DATASPACE_CATEGORY_ALREADY_IN_THE_DB);
        final Set<String> simpleGroup = Sets.newHashSet();
        simpleGroup.add(DATASPACE_CATEGORY_ALREADY_IN_THE_DB);

        when(kpiDefinitionAdapterMock.toListOfEntities(kpiDefinition)).thenReturn(entities);
        when(kpiDefinitionServiceMock.findAllSimpleExecutionGroups()).thenReturn(simpleGroup);
        when(kpiDefinitionServiceMock.findAllComplexExecutionGroups()).thenReturn(Sets.newHashSet());
        when(executionGroupGeneratorMock.generateOrGetExecutionGroup(entities.get(0))).thenReturn(DATASPACE_CATEGORY_PARENT_SCHEMA);

        assertThatThrownBy(() -> objectUnderTest.validateExecutionGroup(kpiDefinition))
                .isInstanceOf(KpiDefinitionValidationException.class)
                .hasMessage("Complex execution group cannot be the same as a generated Simple execution group. " +
                        "Conflicting execution group(s): [dataSpace|category|already_in_the_db]");

        verify(kpiDefinitionAdapterMock).toListOfEntities(kpiDefinition);
        verify(kpiDefinitionServiceMock).findAllSimpleExecutionGroups();
        verify(kpiDefinitionServiceMock).findAllComplexExecutionGroups();
        verify(executionGroupGeneratorMock).generateOrGetExecutionGroup(entities.get(0));
    }

    @Test
    void shouldFailWhenSimpleExecutionGroupIsAlreadyDeclaredAsAComplexExecutionGroup() {
        final KpiDefinitionRequest kpiDefinition = createTestKpiDefinition(DATASPACE_CATEGORY_ALREADY_IN_THE_DB, COMPLEX);
        final List<KpiDefinitionEntity> entities = createKpiDefinitionEntityList(COMPLEX);
        final Set<String> complexGroup = Sets.newHashSet();
        complexGroup.add(DATASPACE_CATEGORY_ALREADY_IN_THE_DB);

        when(kpiDefinitionAdapterMock.toListOfEntities(kpiDefinition)).thenReturn(entities);
        when(kpiDefinitionServiceMock.findAllSimpleExecutionGroups()).thenReturn(Sets.newHashSet());
        when(kpiDefinitionServiceMock.findAllComplexExecutionGroups()).thenReturn(complexGroup);
        when(executionGroupGeneratorMock.generateOrGetExecutionGroup(entities.get(0))).thenReturn(DATASPACE_CATEGORY_ALREADY_IN_THE_DB);

        assertThatThrownBy(() -> objectUnderTest.validateExecutionGroup(kpiDefinition))
                .isInstanceOf(KpiDefinitionValidationException.class)
                .hasMessage("Complex execution group cannot be the same as a generated Simple execution group. " +
                        "Conflicting execution group(s): [dataSpace|category|already_in_the_db]");

        verify(kpiDefinitionAdapterMock).toListOfEntities(kpiDefinition);
        verify(kpiDefinitionServiceMock).findAllSimpleExecutionGroups();
        verify(kpiDefinitionServiceMock).findAllComplexExecutionGroups();
        verify(executionGroupGeneratorMock).generateOrGetExecutionGroup(entities.get(0));
    }

    @Test
    void shouldNotValidateWithConflictWithDatabase() {
        KpiDefinitionRequest kpiDefinition = createTestSimpleKpiDefinition();

        when(kpiDefinitionServiceMock.findAllKpiNames(DEFAULT_COLLECTION_ID)).thenReturn(Sets.set("definition_name"));

        assertThatThrownBy(() -> objectUnderTest.validateNameIsUnique(kpiDefinition, DEFAULT_COLLECTION_ID))
                .isInstanceOf(KpiDefinitionValidationException.class)
                .hasMessage("KPI name must be unique but 'definition_name' is already defined in the database");

        verify(kpiDefinitionServiceMock).findAllKpiNames(DEFAULT_COLLECTION_ID);
    }

    @Test
    void shouldNotValidateDuplicatedNameInPayload() {
        OnDemandTable onDemandTable = table(60, "alias_value", List.of("table.column1", "table.column2"), false, List.of(
                kpiDefinition(
                        "definition_one", "FROM expression_1", "INTEGER[5]", AggregationType.SUM,
                        List.of("table.column1", "table.column2"), true, Collections.emptyList()
                )));
        ComplexTable complexTable = table(1440, "alias_value1", List.of("table.column1", "table.column2"), false, 15, 7_200, false, List.of(
                kpiDefinition(
                        "definition_one", "FROM expression_1", "INTEGER", AggregationType.SUM, "COMPLEX1",
                        List.of("table.column1", "table.column2"), true, Collections.emptyList(), 15, 7_200, false
                )));

        KpiDefinitionRequest kpiDefinition = KpiDefinitionRequest.builder()
                .retentionPeriod(RetentionPeriod.of(null))
                .onDemand(OnDemand.builder().kpiOutputTables(List.of(onDemandTable)).build())
                .scheduledComplex(ScheduledComplex.builder().kpiOutputTables(List.of(complexTable)).build())
                .build();

        when(kpiDefinitionServiceMock.findAllKpiNames(DEFAULT_COLLECTION_ID)).thenReturn(Sets.newHashSet());

        assertThatThrownBy(() -> objectUnderTest.validateNameIsUnique(kpiDefinition, DEFAULT_COLLECTION_ID))
                .isInstanceOf(KpiDefinitionValidationException.class)
                .hasMessage("KPI name must be unique but 'definition_one' is duplicated in the payload");

        verify(kpiDefinitionServiceMock).findAllKpiNames(DEFAULT_COLLECTION_ID);
    }

    @Nested
    class KpiUniquenessWithWithDb {

        @Test
        void shouldFailOnSimpleWhenOnDemandHasSameTableInDatabase() {
            final KpiDefinitionRequest testKpiDefinition = createTestSimpleKpiDefinition();
            final Set<Pair<String, Integer>> expectedOnDemandAliasesFromDatabase = Set.of(
                    Pair.of("alias_one", 60),
                    Pair.of("alias_two", 60));

            when(kpiDefinitionServiceMock.findOnDemandAliasAndAggregationPeriods()).thenReturn(expectedOnDemandAliasesFromDatabase);

            Assertions.assertThatThrownBy(() -> objectUnderTest.validateKpiAliasAggregationPeriodNotConflictingWithDb(testKpiDefinition))
                    .isInstanceOf(KpiDefinitionValidationException.class)
                    .hasMessageContaining("Kpi with an alias 'alias_two' and aggregation period '60' already exists in database for a conflicting KPI type");

            verify(kpiDefinitionServiceMock).findOnDemandAliasAndAggregationPeriods();
        }

        @Test
        void shouldNotFailOnSimple() {
            final KpiDefinitionRequest testKpiDefinition = createTestSimpleKpiDefinition();

            when(kpiDefinitionServiceMock.findOnDemandAliasAndAggregationPeriods()).thenReturn(Collections.emptySet());
            assertDoesNotThrow(() -> objectUnderTest.validateKpiAliasAggregationPeriodNotConflictingWithDb(testKpiDefinition));
            verify(kpiDefinitionServiceMock).findOnDemandAliasAndAggregationPeriods();
        }

        @Test
        void shouldFailOnComplexWhenOnDemandHasSameTableInDatabase() {
            final KpiDefinitionRequest testKpiDefinition = createTestComplexKpiDefinition();
            final Set<Pair<String, Integer>> expectedOnDemandAliasesFromDatabase = Set.of(
                    Pair.of("alias_one", 60),
                    Pair.of("alias_two", 60));

            when(kpiDefinitionServiceMock.findOnDemandAliasAndAggregationPeriods()).thenReturn(expectedOnDemandAliasesFromDatabase);

            Assertions.assertThatThrownBy(() -> objectUnderTest.validateKpiAliasAggregationPeriodNotConflictingWithDb(testKpiDefinition))
                    .isInstanceOf(KpiDefinitionValidationException.class)
                    .hasMessageContaining("Kpi with an alias 'alias_two' and aggregation period '60' already exists in database for a conflicting KPI type");

            verify(kpiDefinitionServiceMock).findOnDemandAliasAndAggregationPeriods();
        }

        @Test
        void shouldNotFailOnComplex() {
            final KpiDefinitionRequest testKpiDefinition = createTestComplexKpiDefinition();

            when(kpiDefinitionServiceMock.findOnDemandAliasAndAggregationPeriods()).thenReturn(Collections.emptySet());
            assertDoesNotThrow(() -> objectUnderTest.validateKpiAliasAggregationPeriodNotConflictingWithDb(testKpiDefinition));
            verify(kpiDefinitionServiceMock).findOnDemandAliasAndAggregationPeriods();
        }

        @Test
        void shouldFailOnOnDemandWhenScheduledHasSameTableInDatabase() {
            final KpiDefinitionRequest testKpiDefinition = createTestOnDemandKpiDefinition();
            final Set<Pair<String, Integer>> expectedScheduledAliasesFromDatabase = Set.of(
                    Pair.of("alias_one", 60),
                    Pair.of("alias_two", 60));

            when(kpiDefinitionServiceMock.findScheduledAliasAndAggregationPeriods()).thenReturn(expectedScheduledAliasesFromDatabase);
            Assertions.assertThatThrownBy(() -> objectUnderTest.validateKpiAliasAggregationPeriodNotConflictingWithDb(testKpiDefinition))
                    .isInstanceOf(KpiDefinitionValidationException.class)
                    .hasMessageContaining("Kpi with an alias 'alias_two' and aggregation period '60' already exists in database for a conflicting KPI type");
            verify(kpiDefinitionServiceMock).findScheduledAliasAndAggregationPeriods();
        }

        @Test
        void shouldNotFailOnOnDemand() {
            final KpiDefinitionRequest testKpiDefinition = createTestOnDemandKpiDefinition();

            when(kpiDefinitionServiceMock.findScheduledAliasAndAggregationPeriods()).thenReturn(Collections.emptySet());
            assertDoesNotThrow(() -> objectUnderTest.validateKpiAliasAggregationPeriodNotConflictingWithDb(testKpiDefinition));
            verify(kpiDefinitionServiceMock).findScheduledAliasAndAggregationPeriods();
        }
    }

    KpiDefinitionRequest createTestKpiDefinition(final String inpDataIdentifier, final String executionGroup) {
        SimpleKpiDefinition simpleDefinition = kpiDefinition("simple_definition_name", "expression", "INTEGER", AggregationType.SUM,
                List.of("table.column1", "table.column2"), null, List.of("f_1", "f_2"), null, null, null, null);
        SimpleTable simpleTable1 = table(1440, "alias_one", List.of("table.column1", "table.column2"), null,
                inpDataIdentifier, null, null, null, List.of(simpleDefinition));
        ScheduledSimple simpleKpiDefinition = ScheduledSimple.builder().kpiOutputTables(List.of(simpleTable1)).build();

        ComplexKpiDefinition complexDefinition = kpiDefinition("complex_definition_name", "FROM expression", "INTEGER",
                AggregationType.SUM, executionGroup, List.of("table.column1", "table.column2"), null, List.of("f_1", "f_2"), null, null, null);
        ComplexTable complexTable1 = table(1440, "alias_one", List.of("table.column1", "table.column2"), null,
                null, null, null, List.of(complexDefinition));
        ScheduledComplex complexKpiDefinition = ScheduledComplex.builder().kpiOutputTables(List.of(complexTable1)).build();

        return KpiDefinitionRequest.builder()
                .retentionPeriod(RetentionPeriod.of(null))
                .scheduledComplex(complexKpiDefinition)
                .scheduledSimple(simpleKpiDefinition).build();
    }

    KpiDefinitionRequest createTestSimpleKpiDefinition() {
        SimpleKpiDefinition simpleDefinition = kpiDefinition("definition_name", "expression", "INTEGER", AggregationType.SUM,
                List.of("table.column1", "table.column2"), null, List.of("f_1", "f_2"), null, null, null, null);
        SimpleTable simpleTable1 = table(1440, "alias_one", List.of("table.column1", "table.column2"), null,
                DATASPACE_CATEGORY_PARENT_SCHEMA, null, null, null, List.of(simpleDefinition));
        SimpleTable simpleTable2 = table(60, "alias_two", List.of("table.column1", "table.column2"), null,
                DATASPACE_CATEGORY_PARENT_SCHEMA, null, null, null, List.of(simpleDefinition));
        ScheduledSimple simpleKpiDefinition = ScheduledSimple.builder().kpiOutputTables(List.of(simpleTable1, simpleTable2)).build();
        return KpiDefinitionRequest.builder()
                .retentionPeriod(RetentionPeriod.of(null))
                .scheduledSimple(simpleKpiDefinition).build();
    }

    KpiDefinitionRequest createTestComplexKpiDefinition() {
        ComplexKpiDefinition complexDefinition = kpiDefinition("definition_name", "FROM expression", "INTEGER",
                AggregationType.SUM, "COMPLEX1", List.of("table.column1", "table.column2"), null, List.of("f_1", "f_2"), null, null, null);
        ComplexTable complexTable1 = table(1440, "alias_one", List.of("table.column1", "table.column2"), null,
                null, null, null, List.of(complexDefinition));
        ComplexTable complexTable2 = table(60, "alias_two", List.of("table.column1", "table.column2"), null,
                null, null, null, List.of(complexDefinition));
        ScheduledComplex complexKpiDefinition = ScheduledComplex.builder().kpiOutputTables(List.of(complexTable1, complexTable2)).build();
        return KpiDefinitionRequest.builder()
                .retentionPeriod(RetentionPeriod.of(null))
                .scheduledComplex(complexKpiDefinition).build();
    }

    KpiDefinitionRequest createTestOnDemandKpiDefinition() {
        OnDemandKpiDefinition onDemandDefinition = kpiDefinition("definition_name", "FROM expression", "INTEGER",
                AggregationType.SUM, List.of("table.column1", "table.column2"), null, List.of("f_1", "f_2"));
        OnDemandTable onDemandTable1 = table(1440, "alias_one", List.of("table.column1", "table.column2"), null,
                List.of(onDemandDefinition));
        OnDemandTable onDemandTable2 = table(60, "alias_two", List.of("table.column1", "table.column2"), false,
                List.of(onDemandDefinition));
        OnDemand onDemandKpiDefinition = OnDemand.builder().kpiOutputTables(List.of(onDemandTable1, onDemandTable2)).build();
        return KpiDefinitionRequest.builder()
                .retentionPeriod(RetentionPeriod.of(null))
                .onDemand(onDemandKpiDefinition).build();
    }

    List<KpiDefinitionEntity> createKpiDefinitionEntityList (final String complexGroup) {
        KpiDefinitionEntity simpleDefinitionEntity = KpiDefinitionEntity.builder().withSchemaName("schema").build();

        KpiDefinitionEntity complexDefinitionEntity = KpiDefinitionEntity.builder()
                .withExecutionGroup(ExecutionGroup.builder().withName(complexGroup).build()).build();

        return List.of(simpleDefinitionEntity, complexDefinitionEntity);
    }
}