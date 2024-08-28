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

import static com.ericsson.oss.air.pm.stats.repository.KpiDefinitionRepositoryImpl.STRING_TYPE;
import static com.ericsson.oss.air.pm.stats.repository._util.RepositoryHelpers.database;
import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats._util.AssertionHelpers;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity.KpiDefinitionEntityBuilder;
import com.ericsson.oss.air.pm.stats.model.exception.UncheckedSqlException;
import com.ericsson.oss.air.pm.stats.repository._util.RepositoryHelpers;
import com.ericsson.oss.air.pm.stats.repository.api.ExecutionGroupRepository;
import com.ericsson.oss.air.pm.stats.repository.api.SchemaDetailRepository;

import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.TemporalUnitOffset;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;

@DisplayNameGeneration(ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class KpiDefinitionRepositoryImplTest {
    private static final String NAME_KPI_DEFINITION_1 = "kpiDefinition1";

    private static final String ALIAS_1 = "alias1";
    private static final String TABLE_1 = "kpi_alias1_60";
    private static final String COMPLEX = "complex";
    private static final String EXPRESSION_1 = "expression1";
    private static final String OBJECT_TYPE_1 = "objectType1";
    private static final String AGGREGATION_TYPE_1 = "aggregationType1";
    private static final String EXECUTION_GROUP_1 = "executionGroup1";
    private static final Filter FILTER_1 = new Filter("filter1");
    private static final Filter FILTER_2 = new Filter("filter2");
    private static final String AGGREGATION_ELEMENT_1 = "aggregationElement1";
    private static final String AGGREGATION_ELEMENT_2 = "'${param.execution_id}' AS execution_id";
    public static final UUID CALCULATION_ID = UUID.fromString("84edfb50-95d5-4afb-b1e8-103ee4acbeb9");
    public static final UUID CALCULATION_ID2 = UUID.fromString("56e4365a-4567-4afb-b1e8-103ee4acbeb9");
    public static final UUID DEFAULT_COLLECTION_ID = UUID.fromString("29dc1bbf-7cdf-421b-8fc9-e363889ada79");
    public static final UUID OTHER_COLLECTION_ID =  UUID.fromString("0c68de29-fdd6-4190-960e-1971808a1898");
    private static final SchemaDetail SCHEMA_DETAIL = SchemaDetail.builder().withTopic("topic").build();

    @Mock ExecutionGroupRepository executionGroupRepositoryMock;
    @Mock SchemaDetailRepository schemaDetailRepositoryMock;

    @InjectMocks KpiDefinitionRepositoryImpl objectUnderTest;

    @Nested
    @DisplayName("Given an invalid JDBC URL")
    class GivenAnInvalidJdbcUrl {

        @Test
        void shouldThrowUncheckedSqlException_onCounting() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.count());
        }

        @Test
        void shouldThrowUncheckedSqlException_onFindByName() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findByName("name", UUID.randomUUID()));
        }

        @Test
        void shouldThrowUncheckedSqlException_onFindByNames() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findByNames(Set.of("name1", "name2")));
        }

        @Test
        void shouldThrowUncheckedSqlException_onFindAllSimpleKpiNames() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findAllSimpleKpiNamesGroupedByExecGroups());
        }

        @Test
        void shouldThrowUncheckedSqlException_onFindAll() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findAll());
        }

        @Test
        void shouldThrowUncheckedSqlException_onFindAllIncludingSoftDeleted() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findAllIncludingSoftDeleted());
        }

        @Test
        void shouldThrowUncheckedSqlException_onFindComplexKpis() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findComplexKpis());
        }

        @Test
        void shouldThrowUncheckedSqlException_onFindByExecutionGroup() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findKpiDefinitionsByExecutionGroup("Execution_group"));
        }

        @Test
        void shouldThrowUncheckedSqlException_onFindAllKpiNames() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findAllKpiNames());
        }
        @Test
        void shouldThrowUncheckedSqlException_onFindAllKpiNamesWithCollectionId() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findAllKpiNames(DEFAULT_COLLECTION_ID));
        }


        @Test
        void shouldThrowUncheckedException_onFindByCalculationId() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findOnDemandKpiDefinitionsByCalculationId(CALCULATION_ID));
        }

        @Test
        void shouldThrowUncheckedSqlException_onFindOnAliasAndAggregationPeriod() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findAllKpiNamesWithAliasAndAggregationPeriod("alias", 60));
        }

        @Test
        void shouldThrowUncheckedSqlException_onFindAllOnDemandAliasAndAggregationPeriods() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findOnDemandAliasAndAggregationPeriods());
        }

        @Test
        void shouldThrowUncheckedSqlException_onFindAllScheduledAliasAndAggregationPeriods() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findScheduledAliasAndAggregationPeriods());
        }

        @Test
        void shouldThrowUncheckedSqlException_onSaveRelations() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.saveOnDemandCalculationRelation(
                    Collections.emptySet(),
                    CALCULATION_ID
            ));
        }

        @Test
        void shouldThrowUncheckedSqlException_onFindAllSimpleExecutionGroups() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findAllSimpleExecutionGroups());
        }

        @Test
        void shouldThrowUncheckedSqlException_onFindAllComplexExecutionGroups() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findAllComplexExecutionGroups());
        }

        @Test
        void shouldThrowUncheckedSqlException_onCountComplexKpi() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.countComplexKpi());
        }

        @Test
        @SneakyThrows
        void shouldThrowUncheckedSqlException_onDeleteKpiDefinitions(@Mock Connection connectionMock) {
            when(connectionMock.prepareStatement(anyString())).thenThrow(SQLException.class);
            assertThatThrownBy(() -> objectUnderTest.deleteKpiDefinitionsByName(connectionMock, Collections.emptySet()))
                    .isInstanceOf(UncheckedSqlException.class)
                    .hasRootCauseInstanceOf(SQLException.class);
        }


        @Test
        @SneakyThrows
        void shouldThrowUncheckedSqlException_onCountKpisWithAliasAndAggregationPeriod(@Mock Connection connectionMock) {
            when(connectionMock.prepareStatement(anyString())).thenThrow(SQLException.class);
            assertThatThrownBy(() -> objectUnderTest.countKpisWithAliasAndAggregationPeriod(connectionMock, Table.of(TABLE_1)))
                    .isInstanceOf(UncheckedSqlException.class)
                    .hasRootCauseInstanceOf(SQLException.class);
        }
    }

    @Nested
    @DisplayName("Given an available database")
    class GivenAnAvailableDatabase {
        EmbeddedDatabase embeddedDatabase;

        KpiDefinitionEntity kpiDefinitionEntityToSave;

        @BeforeEach
        void setUp() {
            embeddedDatabase = database("sql/initialize_kpi_definition.sql");

            kpiDefinitionEntityToSave = entity(
                    NAME_KPI_DEFINITION_1, ALIAS_1, EXPRESSION_1, OBJECT_TYPE_1, AGGREGATION_TYPE_1, 60,
                    Arrays.asList(AGGREGATION_ELEMENT_1, AGGREGATION_ELEMENT_2), false, executionGroup(1, EXECUTION_GROUP_1),
                    1, 10, Arrays.asList(FILTER_1.getName(), FILTER_2.getName()), false, SCHEMA_DETAIL,
                    "dataSpace", "category", "schema", 1, DEFAULT_COLLECTION_ID
            );
        }

        @AfterEach
        void tearDown() {
            embeddedDatabase.shutdown();
        }

        @Nested
        @DisplayName("When database is populated")
        class WhenDatabaseIsPopulated {
            private static final String NAME_KPI_DEFINITION_2 = "kpiDefinition2";
            private static final String NAME_KPI_DEFINITION_3 = "kpiDefinition3";
            private static final String NAME_KPI_DEFINITION_4 = "kpiDefinition4";
            private static final String NAME_KPI_DEFINITION_5 = "kpiDefinition5";
            private static final String NAME_KPI_DEFINITION_6 = "kpiDefinition6";
            private static final String EXECUTION_GROUP_2 = "executionGroup2";
            private static final String EXECUTION_GROUP_3 = "executionGroup3";

            private KpiDefinitionEntity kpiDefinitionEntity2;
            private KpiDefinitionEntity kpiDefinitionEntity3;
            private KpiDefinitionEntity kpiDefinitionEntity4;
            private KpiDefinitionEntity kpiDefinitionEntity5;
            private KpiDefinitionEntity kpiDefinitionEntity6;

            @BeforeEach
            void setUp() throws SQLException {
                kpiDefinitionEntity2 = entity(
                        NAME_KPI_DEFINITION_2, ALIAS_1, EXPRESSION_1, OBJECT_TYPE_1, AGGREGATION_TYPE_1, 60,
                        Arrays.asList(AGGREGATION_ELEMENT_1, AGGREGATION_ELEMENT_2), true, executionGroup(1, EXECUTION_GROUP_1),
                        1, 10, Arrays.asList(FILTER_1.getName(), FILTER_2.getName()), false, SCHEMA_DETAIL,
                        "dataSpace", "category", "schema", 2, DEFAULT_COLLECTION_ID
                );

                kpiDefinitionEntity3 = entity(
                        NAME_KPI_DEFINITION_3, ALIAS_1, EXPRESSION_1, OBJECT_TYPE_1, AGGREGATION_TYPE_1, 60,
                        Arrays.asList(AGGREGATION_ELEMENT_1, AGGREGATION_ELEMENT_2), false, executionGroup(1, EXECUTION_GROUP_1),
                        1, 10, Arrays.asList(FILTER_1.getName(), FILTER_2.getName()), false, SCHEMA_DETAIL,
                        "dataSpace", "category", "schema", 3, OTHER_COLLECTION_ID
                );

                kpiDefinitionEntity4 = entity(
                        NAME_KPI_DEFINITION_4, ALIAS_1, EXPRESSION_1, OBJECT_TYPE_1, AGGREGATION_TYPE_1, 60,
                        Arrays.asList(AGGREGATION_ELEMENT_1, AGGREGATION_ELEMENT_2), false, executionGroup(2, EXECUTION_GROUP_2),
                        1, 10, Arrays.asList(FILTER_1.getName(), FILTER_2.getName()), false, SCHEMA_DETAIL,
                        "dataSpace", "category", "schema", 4, DEFAULT_COLLECTION_ID
                );

                kpiDefinitionEntity5 = entity(
                        NAME_KPI_DEFINITION_5, ALIAS_1, EXPRESSION_1, OBJECT_TYPE_1, AGGREGATION_TYPE_1, 60,
                        Arrays.asList(AGGREGATION_ELEMENT_1, AGGREGATION_ELEMENT_2), false, executionGroup(2, EXECUTION_GROUP_2),
                        1, 10, Arrays.asList(FILTER_1.getName(), FILTER_2.getName()), false, SCHEMA_DETAIL,
                        "dataSpace", "category", "schema", 5, OTHER_COLLECTION_ID
                );

                kpiDefinitionEntity6 = entity(
                        NAME_KPI_DEFINITION_6, ALIAS_1, EXPRESSION_1, OBJECT_TYPE_1, AGGREGATION_TYPE_1, 60,
                        Arrays.asList(AGGREGATION_ELEMENT_1, AGGREGATION_ELEMENT_2), false, executionGroup(3, EXECUTION_GROUP_3),
                        1, 10, Arrays.asList(FILTER_1.getName(), FILTER_2.getName()), false, SCHEMA_DETAIL,
                        "dataSpace", "category", "schema", 6, DEFAULT_COLLECTION_ID
                );

                when(executionGroupRepositoryMock.getOrSave(any(Connection.class), eq(kpiDefinitionEntityToSave))).thenReturn(1L);
                when(executionGroupRepositoryMock.getOrSave(any(Connection.class), eq(kpiDefinitionEntity2))).thenReturn(1L);
                when(executionGroupRepositoryMock.getOrSave(any(Connection.class), eq(kpiDefinitionEntity3))).thenReturn(1L);
                when(executionGroupRepositoryMock.getOrSave(any(Connection.class), eq(kpiDefinitionEntity4))).thenReturn(2L);
                when(executionGroupRepositoryMock.getOrSave(any(Connection.class), eq(kpiDefinitionEntity5))).thenReturn(2L);
                when(executionGroupRepositoryMock.getOrSave(any(Connection.class), eq(kpiDefinitionEntity6))).thenReturn(3L);
                when(schemaDetailRepositoryMock.getOrSave(any(Connection.class), eq(SCHEMA_DETAIL))).thenReturn(1);

                final List<KpiDefinitionEntity> toSave = List.of(
                        kpiDefinitionEntityToSave,
                        kpiDefinitionEntity2,
                        kpiDefinitionEntity3,
                        kpiDefinitionEntity4,
                        kpiDefinitionEntity5,
                        kpiDefinitionEntity6
                );
                objectUnderTest.saveAll(embeddedDatabase.getConnection(), toSave);

                verify(executionGroupRepositoryMock, times(6)).getOrSave(any(Connection.class), any(KpiDefinitionEntity.class));
                verify(schemaDetailRepositoryMock, times(6)).getOrSave(any(Connection.class), eq(SCHEMA_DETAIL));
            }

            @Test
            void shouldCountRows() {
                RepositoryHelpers.prepare(embeddedDatabase, () -> {
                    final long actual = objectUnderTest.count();
                    assertThat(actual).isEqualTo(6);
                });
            }

            @Test
            void shouldFindAll() throws SQLException {
                when(executionGroupRepositoryMock.findExecutionGroupById(any(Connection.class), eq(1))).thenReturn(executionGroup(1, EXECUTION_GROUP_1));
                when(executionGroupRepositoryMock.findExecutionGroupById(any(Connection.class), eq(2))).thenReturn(executionGroup(2, EXECUTION_GROUP_2));
                when(executionGroupRepositoryMock.findExecutionGroupById(any(Connection.class), eq(3))).thenReturn(executionGroup(3, EXECUTION_GROUP_3));
                when(schemaDetailRepositoryMock.findById(1)).thenReturn(Optional.of(SCHEMA_DETAIL));

                RepositoryHelpers.prepare(embeddedDatabase, () -> {
                    final List<KpiDefinitionEntity> actual = objectUnderTest.findAll();
                    assertThat(actual).containsExactlyInAnyOrder(
                            kpiDefinitionEntityToSave,
                            kpiDefinitionEntity2,
                            kpiDefinitionEntity3,
                            kpiDefinitionEntity4,
                            kpiDefinitionEntity5,
                            kpiDefinitionEntity6
                    );
                });

                verify(executionGroupRepositoryMock, times(3)).findExecutionGroupById(any(Connection.class), eq(1));
                verify(executionGroupRepositoryMock, times(2)).findExecutionGroupById(any(Connection.class), eq(2));
                verify(executionGroupRepositoryMock).findExecutionGroupById(any(Connection.class), eq(3));
                verify(schemaDetailRepositoryMock, times(6)).findById(1);
            }

            @Test
            void shouldFindAllWithCollectionId() throws SQLException{
                when(executionGroupRepositoryMock.findExecutionGroupById(any(Connection.class), eq(1))).thenReturn(executionGroup(1, EXECUTION_GROUP_1));
                when(executionGroupRepositoryMock.findExecutionGroupById(any(Connection.class), eq(2))).thenReturn(executionGroup(2, EXECUTION_GROUP_2));
                when(schemaDetailRepositoryMock.findById(1)).thenReturn(Optional.of(SCHEMA_DETAIL));

                RepositoryHelpers.prepare(embeddedDatabase, () -> {
                    final List<KpiDefinitionEntity> actual = objectUnderTest.findAll(OTHER_COLLECTION_ID);
                    assertThat(actual).containsExactlyInAnyOrder(
                            kpiDefinitionEntity3,
                            kpiDefinitionEntity5
                    );
                });

                verify(executionGroupRepositoryMock, times(1)).findExecutionGroupById(any(Connection.class), eq(1));
                verify(executionGroupRepositoryMock, times(1)).findExecutionGroupById(any(Connection.class), eq(2));
                verify(schemaDetailRepositoryMock, times(2)).findById(1);
            }


            @Test
            void shouldFindAllIncludingSoftDeleted() throws SQLException {
                when(executionGroupRepositoryMock.findExecutionGroupById(any(Connection.class), eq(1))).thenReturn(executionGroup(1, EXECUTION_GROUP_1));
                when(executionGroupRepositoryMock.findExecutionGroupById(any(Connection.class), eq(2))).thenReturn(executionGroup(2, EXECUTION_GROUP_2));
                when(executionGroupRepositoryMock.findExecutionGroupById(any(Connection.class), eq(3))).thenReturn(executionGroup(3, EXECUTION_GROUP_3));
                when(schemaDetailRepositoryMock.findById(1)).thenReturn(Optional.of(SCHEMA_DETAIL));

                RepositoryHelpers.prepare(embeddedDatabase, () -> {
                    objectUnderTest.softDelete(embeddedDatabase.getConnection(), List.of(NAME_KPI_DEFINITION_6), DEFAULT_COLLECTION_ID);
                    final List<KpiDefinitionEntity> actual = objectUnderTest.findAllIncludingSoftDeleted();
                    assertThat(actual).extracting(KpiDefinitionEntity::name).containsExactlyInAnyOrder(
                            kpiDefinitionEntityToSave.name(),
                            kpiDefinitionEntity2.name(),
                            kpiDefinitionEntity3.name(),
                            kpiDefinitionEntity4.name(),
                            kpiDefinitionEntity5.name(),
                            kpiDefinitionEntity6.name()
                    );

                    assertThat(actual).filteredOn(entity -> entity.name().equals(NAME_KPI_DEFINITION_6)).first().satisfies(entity -> {
                        assertThat(entity.timeDeleted()).isNotNull();
                    });
                });

                verify(executionGroupRepositoryMock, times(3)).findExecutionGroupById(any(Connection.class), eq(1));
                verify(executionGroupRepositoryMock, times(2)).findExecutionGroupById(any(Connection.class), eq(2));
                verify(executionGroupRepositoryMock).findExecutionGroupById(any(Connection.class), eq(3));
                verify(schemaDetailRepositoryMock, times(6)).findById(1);
            }

            @SneakyThrows
            @Test
            void shouldFindByName() {
                when(executionGroupRepositoryMock.findExecutionGroupById(any(Connection.class), eq(1))).thenReturn(executionGroup(1, EXECUTION_GROUP_1));
                when(schemaDetailRepositoryMock.findById(1)).thenReturn(Optional.of(SCHEMA_DETAIL));

                RepositoryHelpers.prepare(embeddedDatabase, () -> {
                    final Optional<KpiDefinitionEntity> actual = objectUnderTest.findByName(NAME_KPI_DEFINITION_2, DEFAULT_COLLECTION_ID);
                    assertThat(actual).hasValue(kpiDefinitionEntity2);
                });

                verify(executionGroupRepositoryMock).findExecutionGroupById(any(Connection.class), eq(1));
                verify(schemaDetailRepositoryMock).findById(1);
            }

            @SneakyThrows
            @Test
            void shouldNotFindByName() {
                RepositoryHelpers.prepare(embeddedDatabase, () -> {
                    final Optional<KpiDefinitionEntity> actual = objectUnderTest.findByName("unknownDefinitionName", DEFAULT_COLLECTION_ID);
                    assertThat(actual).isEmpty();
                });
            }

            @SneakyThrows
            @Test
            void shouldFindByNames() {
                when(executionGroupRepositoryMock.findExecutionGroupById(any(Connection.class), eq(1))).thenReturn(executionGroup(1, EXECUTION_GROUP_1));
                when(schemaDetailRepositoryMock.findById(1)).thenReturn(Optional.of(SCHEMA_DETAIL));

                RepositoryHelpers.prepare(embeddedDatabase, () -> {
                    final List<KpiDefinitionEntity> actual = objectUnderTest.findByNames(Set.of(NAME_KPI_DEFINITION_2, NAME_KPI_DEFINITION_3));
                    assertThat(actual).containsExactly(kpiDefinitionEntity2, kpiDefinitionEntity3);
                });

                verify(executionGroupRepositoryMock, times(2)).findExecutionGroupById(any(Connection.class), eq(1));
                verify(schemaDetailRepositoryMock, times(2)).findById(1);
            }

            @SneakyThrows
            @Test
            void shouldNotFindByNames() {
                RepositoryHelpers.prepare(embeddedDatabase, () -> {
                    final List<KpiDefinitionEntity> actual = objectUnderTest.findByNames(Set.of("unknownDefinitionName"));
                    assertThat(actual).isEmpty();
                });
            }

            @Test
            void shouldUpdateKpiDefinition() throws SQLException {
                final SchemaDetail updated = SchemaDetail.builder().withTopic("new").build();
                final KpiDefinitionEntity update = entity(
                        NAME_KPI_DEFINITION_6, "newAlias", "newExpression", "newObjectType", "newAggregationType", 1_440,
                        Arrays.asList(AGGREGATION_ELEMENT_2, "aggregationElement3"), false, executionGroup(4, "newExecutionGroup"),
                        10, 15, Arrays.asList(FILTER_2.getName(),"filter3"), true, updated,
                        "dataSpace", "new", "identifier", 6,DEFAULT_COLLECTION_ID
                );

                when(executionGroupRepositoryMock.getOrSave(any(Connection.class), eq((update)))).thenReturn(4L);
                when(schemaDetailRepositoryMock.getOrSave(any(Connection.class), eq(updated))).thenReturn(2);
                when(executionGroupRepositoryMock.findExecutionGroupById(any(Connection.class), eq(1))).thenReturn(executionGroup(1, EXECUTION_GROUP_1));
                when(executionGroupRepositoryMock.findExecutionGroupById(any(Connection.class), eq(2))).thenReturn(executionGroup(2, EXECUTION_GROUP_2));
                when(executionGroupRepositoryMock.findExecutionGroupById(any(Connection.class), eq(4))).thenReturn(executionGroup(4, "newExecutionGroup"));
                when(schemaDetailRepositoryMock.findById(1)).thenReturn(Optional.of(SCHEMA_DETAIL));
                when(schemaDetailRepositoryMock.findById(2)).thenReturn(Optional.of(updated));

                Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.update(embeddedDatabase.getConnection(), update, DEFAULT_COLLECTION_ID));

                RepositoryHelpers.prepare(embeddedDatabase, () -> {
                    assertThat(objectUnderTest.findAll())
                            .filteredOn(updatedDefinition -> updatedDefinition.name().equals(NAME_KPI_DEFINITION_6))
                            .first()
                            .satisfies(updatedDefinition -> {
                                assertThat(updatedDefinition).isEqualTo(update);
                            });
                });

                verify(executionGroupRepositoryMock).getOrSave(any(Connection.class), eq((update)));
                verify(schemaDetailRepositoryMock).getOrSave(any(Connection.class), eq(updated));
                verify(executionGroupRepositoryMock, times(3)).findExecutionGroupById(any(Connection.class), eq(1));
                verify(executionGroupRepositoryMock, times(2)).findExecutionGroupById(any(Connection.class), eq(2));
                verify(executionGroupRepositoryMock).findExecutionGroupById(any(Connection.class), eq(4));
                verify(schemaDetailRepositoryMock, times(5)).findById(1);
                verify(schemaDetailRepositoryMock).findById(2);
            }

            @Test
            void shouldUpdateTimeDeletedFieldForSoftDeletedKpiDefinition() throws SQLException {
                final List<String> kpiDefinitionNames = List.of(
                        NAME_KPI_DEFINITION_1,
                        NAME_KPI_DEFINITION_2,
                        NAME_KPI_DEFINITION_4,
                        NAME_KPI_DEFINITION_6
                );

                Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.softDelete(embeddedDatabase.getConnection(), kpiDefinitionNames, DEFAULT_COLLECTION_ID));
                assertRecentlyDeleted(kpiDefinitionNames, within(5, ChronoUnit.SECONDS));
            }

            @Test
            void shouldContainsNullValues() throws SQLException {
                final KpiDefinitionEntity entity = entity(
                        "newKpiDefinition", ALIAS_1, EXPRESSION_1, OBJECT_TYPE_1, AGGREGATION_TYPE_1, 60,
                        Arrays.asList(AGGREGATION_ELEMENT_1, AGGREGATION_ELEMENT_2), true, null,
                        0, 0, Arrays.asList(FILTER_1.getName(), FILTER_2.getName()), false,null,
                        null, null, null, 1, DEFAULT_COLLECTION_ID
                );

                objectUnderTest.save(embeddedDatabase.getConnection(), entity);

                RepositoryHelpers.prepare(embeddedDatabase, () -> {
                    final List<KpiDefinitionEntity> actual = objectUnderTest.findAll();
                    assertThat(actual)
                            .filteredOn(definitionEntity -> "newKpiDefinition".equals(definitionEntity.name()))
                            .first()
                            .satisfies(kpiDefinitionEntity -> {
                                assertThat(kpiDefinitionEntity.schemaCategory()).isNull();
                                assertThat(kpiDefinitionEntity.schemaDataSpace()).isNull();
                                assertThat(kpiDefinitionEntity.schemaName()).isNull();
                                assertThat(kpiDefinitionEntity.executionGroup()).isNull();
                                assertThat(kpiDefinitionEntity.dataReliabilityOffset()).isEqualTo(0);
                                assertThat(kpiDefinitionEntity.dataLookbackLimit()).isEqualTo(0);
                            });
                });
            }

            @Test
            @SneakyThrows
            void shouldDeleteDefinitions() {
                final List<String> kpisToDelete = List.of(NAME_KPI_DEFINITION_1, NAME_KPI_DEFINITION_2, NAME_KPI_DEFINITION_3);

                RepositoryHelpers.prepare(embeddedDatabase, () -> {
                    final Set<String> actual = objectUnderTest.findAllKpiNames();
                    assertThat(actual).containsAnyElementsOf(kpisToDelete);
                });

                objectUnderTest.deleteKpiDefinitionsByName(embeddedDatabase.getConnection(), kpisToDelete);

                RepositoryHelpers.prepare(embeddedDatabase, () -> {
                    final Set<String> actual = objectUnderTest.findAllKpiNames();
                    assertThat(actual).isNotEmpty().doesNotContainAnyElementsOf(kpisToDelete);
                });
            }

            @Test
            @SneakyThrows
            void shouldCountKpisWithAliasAndAggregationPeriod() {
                final int actual = objectUnderTest.countKpisWithAliasAndAggregationPeriod(embeddedDatabase.getConnection(), Table.of(TABLE_1));

                assertThat(actual).isEqualTo(6);
            }
        }

        void assertRecentlyDeleted(final List<String> kpiDefinitionNames, final TemporalUnitOffset offset) throws SQLException {
            assertThat(kpiDefinitionNames).isNotEmpty();

            final String sql = "SELECT time_deleted " +
                    "FROM kpi_service_db.kpi.kpi_definition " +
                    "WHERE name = ANY(?)";

            try (final Connection connection = embeddedDatabase.getConnection();
                 final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setArray(1, connection.createArrayOf(STRING_TYPE, kpiDefinitionNames.toArray()));
                try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                    final LocalDateTime now = now();
                    while (resultSet.next()) {
                        final Timestamp timeDeleted = resultSet.getTimestamp("time_deleted");
                        assertThat(timeDeleted).isNotNull();
                        assertThat(timeDeleted.toLocalDateTime()).isCloseTo(now, offset);
                    }
                }
            }
        }
    }

    @Nested
    @DisplayName("Given an available definition and execution group database")
    class GivenAnAvailableDefinitionAndExecutionGroupDatabase {

        private static final String NAME_KPI_DEFINITION_2 = "kpiDefinition2";
        private static final String NAME_KPI_DEFINITION_3 = "kpiDefinition3";
        private static final String NAME_KPI_DEFINITION_4 = "kpiDefinition4";
        private static final String NAME_KPI_DEFINITION_5 = "kpiDefinition5";
        private static final String NAME_KPI_DEFINITION_6 = "kpiDefinition6";
        private static final String NAME_KPI_DEFINITION_7 = "kpiDefinition7";
        private static final String NAME_KPI_DEFINITION_8 = "kpiDefinition8";
        private static final String NAME_KPI_DEFINITION_9 = "kpiDefinition9";
        private static final String NAME_KPI_DEFINITION_10 = "kpiDefinition10";
        private static final String NAME_KPI_DEFINITION_COMPLEX_1 = "kpiDefinitionComplex1";
        private static final String NAME_KPI_DEFINITION_DELETED = "kpiDefinitionDeleted";
        private static final String EXECUTION_GROUP_2 = "executionGroup2";
        private static final String EXECUTION_GROUP_3 = "executionGroup3";
        private static final String EXECUTION_GROUP_COMPLEX_1 = "COMPLEX";

        EmbeddedDatabase embeddedDatabase;

        KpiDefinitionEntity kpiDefinition1;
        KpiDefinitionEntity kpiDefinition2;
        KpiDefinitionEntity kpiDefinition3;
        KpiDefinitionEntity kpiDefinition7;
        KpiDefinitionEntity kpiDefinition8;
        KpiDefinitionEntity kpiDefinitionComplex1;

        @BeforeEach
        void setUp() {
            embeddedDatabase = database(
                    "sql/initialize_kpi_definition.sql",
                    "sql/initialize_kpi_definition_and_execution_groups.sql"
            );

            kpiDefinition1 = makeSimpleKpiDefinitionEntity(NAME_KPI_DEFINITION_1, EXECUTION_GROUP_1, 1);
            kpiDefinition2 = makeSimpleKpiDefinitionEntity(NAME_KPI_DEFINITION_2, EXECUTION_GROUP_1, 2);
            kpiDefinition3 = makeSimpleKpiDefinitionEntity(NAME_KPI_DEFINITION_3, EXECUTION_GROUP_1, 3);
            kpiDefinition7 = makeComplexKpiDefinitionEntity(NAME_KPI_DEFINITION_7, 3, EXECUTION_GROUP_3, 8);
            kpiDefinition8 = makeComplexKpiDefinitionEntity(NAME_KPI_DEFINITION_8, 3, EXECUTION_GROUP_3, 9);
            kpiDefinitionComplex1 = makeComplexKpiDefinitionEntity(NAME_KPI_DEFINITION_COMPLEX_1, 4, EXECUTION_GROUP_COMPLEX_1, 7);
        }

        @AfterEach
        void tearDown() {
            embeddedDatabase.shutdown();
        }

        @Test
        void shouldGiveBackAllKpiDefinition() throws SQLException {
            when(executionGroupRepositoryMock.findExecutionGroupById(any(Connection.class), eq(1))).thenReturn(executionGroup(1, EXECUTION_GROUP_1));

            RepositoryHelpers.prepare(embeddedDatabase, () -> {
                List<KpiDefinitionEntity> actual = objectUnderTest.findKpiDefinitionsByExecutionGroup(EXECUTION_GROUP_1);
                verify(executionGroupRepositoryMock, times(3)).findExecutionGroupById(any(Connection.class), eq(1));
                assertThat(actual)
                        .hasSize(3)
                        .containsExactlyInAnyOrder(kpiDefinition1, kpiDefinition2, kpiDefinition3);
            });
        }

        @Test
        void shouldFindAllSimpleExecutionGroups() {
            RepositoryHelpers.prepare(embeddedDatabase, () -> {
                final Set<String> actual = objectUnderTest.findAllSimpleExecutionGroups();
                assertThat(actual).containsExactlyInAnyOrder(
                        EXECUTION_GROUP_1,
                        EXECUTION_GROUP_2,
                        EXECUTION_GROUP_3
                );
            });
        }

        @Test
        void shouldFindAllComplexExecutionGroups() {
            RepositoryHelpers.prepare(embeddedDatabase, () -> {
                final Set<String> actual = objectUnderTest.findAllComplexExecutionGroups();
                assertThat(actual).containsExactlyInAnyOrder(
                        EXECUTION_GROUP_COMPLEX_1, EXECUTION_GROUP_3
                );
            });
        }

        @Test
        void shouldFindAllKpiNames() {
            RepositoryHelpers.prepare(embeddedDatabase, () -> {
                final Set<String> actual = objectUnderTest.findAllKpiNames();
                assertThat(actual).containsExactlyInAnyOrder(
                        NAME_KPI_DEFINITION_1, NAME_KPI_DEFINITION_2, NAME_KPI_DEFINITION_3,
                        NAME_KPI_DEFINITION_4, NAME_KPI_DEFINITION_5, NAME_KPI_DEFINITION_6,
                        NAME_KPI_DEFINITION_7, NAME_KPI_DEFINITION_8, NAME_KPI_DEFINITION_9,
                        NAME_KPI_DEFINITION_10, NAME_KPI_DEFINITION_COMPLEX_1, NAME_KPI_DEFINITION_DELETED);
            });
        }

        @Test
        void shouldFindAllKpiNamesWithCollectionId() {
            RepositoryHelpers.prepare(embeddedDatabase, () -> {
                KpiDefinitionEntity entityWithOtherCollectionId = entity(
                        "other_kpi", ALIAS_1, EXPRESSION_1, OBJECT_TYPE_1, AGGREGATION_TYPE_1, 60,
                        Arrays.asList(AGGREGATION_ELEMENT_1, AGGREGATION_ELEMENT_2), false, executionGroup(1, EXECUTION_GROUP_1),
                        1, 10, Arrays.asList(FILTER_1.getName(), FILTER_2.getName()), false, SCHEMA_DETAIL,
                        "dataSpace", "category", "schema", 3, OTHER_COLLECTION_ID
                );
                objectUnderTest.saveAll(embeddedDatabase.getConnection(), List.of(entityWithOtherCollectionId));
                final Set<String> actual = objectUnderTest.findAllKpiNames(OTHER_COLLECTION_ID);
                assertThat(actual).containsExactlyInAnyOrder("other_kpi");
            });
        }

        @Test
        void shouldFindAllSimpleKpiNamesByExecutionGroups() {
            RepositoryHelpers.prepare(embeddedDatabase, () -> {
                final Map<String, List<String>> actual = objectUnderTest.findAllSimpleKpiNamesGroupedByExecGroups();
                assertThat(actual)
                        .hasSize(3)
                        .containsExactly(entry(EXECUTION_GROUP_3, Collections.singletonList(NAME_KPI_DEFINITION_5)),
                                entry(EXECUTION_GROUP_2, Collections.singletonList(NAME_KPI_DEFINITION_4)),
                                entry(EXECUTION_GROUP_1, List.of(NAME_KPI_DEFINITION_1, NAME_KPI_DEFINITION_2, NAME_KPI_DEFINITION_3)));
            });
        }

        @Test
        void shouldFindComplexKpis() throws SQLException {
            when(executionGroupRepositoryMock.findExecutionGroupById(any(Connection.class), eq(4))).thenReturn(executionGroup(4, EXECUTION_GROUP_COMPLEX_1));
            when(executionGroupRepositoryMock.findExecutionGroupById(any(Connection.class), eq(3))).thenReturn(executionGroup(3, EXECUTION_GROUP_3));

            RepositoryHelpers.prepare(embeddedDatabase, () -> {
                final List<KpiDefinitionEntity> actual = objectUnderTest.findComplexKpis();
                assertThat(actual).first().usingRecursiveComparison().isEqualTo(kpiDefinitionComplex1);
                assertThat(actual).containsExactly(
                        kpiDefinitionComplex1,
                        kpiDefinition7,
                        kpiDefinition8
                );
            });
        }

        @Test
        void shouldFindAllComplexKpiNames() {
            RepositoryHelpers.prepare(embeddedDatabase, () -> {
                final Map<String, List<String>> actualResult = objectUnderTest.findAllComplexKpiNamesGroupedByExecGroups();
                assertThat(actualResult)
                        .hasSize(2)
                        .containsExactlyInAnyOrderEntriesOf(Map.of(EXECUTION_GROUP_COMPLEX_1, Collections.singletonList(NAME_KPI_DEFINITION_COMPLEX_1),
                                EXECUTION_GROUP_3, List.of(NAME_KPI_DEFINITION_7, NAME_KPI_DEFINITION_8)));
            });
        }

        @Test
        void shouldCountComplexKpis() {
            RepositoryHelpers.prepare(embeddedDatabase, () -> {
                final long actualResult = objectUnderTest.countComplexKpi();
                assertThat(actualResult).isEqualTo(3);
            });
        }

        @Test
        void shouldFindAllKpiNamesWithAliasAndAggregationPeriod() {
            RepositoryHelpers.prepare(embeddedDatabase, () -> {
                final List<String> actualResult = objectUnderTest.findAllKpiNamesWithAliasAndAggregationPeriod(ALIAS_1, 60);
                assertThat(actualResult)
                        .hasSize(5)
                        .containsExactlyInAnyOrder(NAME_KPI_DEFINITION_1,
                                NAME_KPI_DEFINITION_2,
                                NAME_KPI_DEFINITION_3,
                                NAME_KPI_DEFINITION_4,
                                NAME_KPI_DEFINITION_5);
            });
        }

        @Test
        void shouldFindAllOnDemandAliasAndAggregationPeriods() {
            RepositoryHelpers.prepare(embeddedDatabase, () -> {
                final Set<Pair<String, Integer>> expected = Set.of(
                        Pair.of("onDemandAlias1", 60),
                        Pair.of("onDemandAlias1", 1440),
                        Pair.of("onDemandAlias2", 60));
                final Set<Pair<String, Integer>> actualResult = objectUnderTest.findOnDemandAliasAndAggregationPeriods();
                assertThat(actualResult).containsExactlyInAnyOrderElementsOf(expected);
            });
        }

        @Test
        void shouldFindAllScheduledAliasAndAggregationPeriods() {
            RepositoryHelpers.prepare(embeddedDatabase, () -> {
                final Set<Pair<String, Integer>> expected = new HashSet<>(
                        List.of(
                                Pair.of("alias1", 60),
                                Pair.of("complex", 60)

                        ));
                final Set<Pair<String, Integer>> actualResult = objectUnderTest.findScheduledAliasAndAggregationPeriods();
                assertThat(actualResult).containsExactlyInAnyOrderElementsOf(expected);
            });
        }

        private KpiDefinitionEntity makeSimpleKpiDefinitionEntity(final String name, final String executionGroup, final int id) {
            return entity(
                    name, ALIAS_1, EXPRESSION_1, OBJECT_TYPE_1, AGGREGATION_TYPE_1, 60,
                    Arrays.asList(AGGREGATION_ELEMENT_1, "aggregationElement2"), true, executionGroup(1, executionGroup),
                    0, 0, List.of(FILTER_1.getName(), FILTER_2.getName()), false, null,
                    "dataSpace", "category", "schema", id, DEFAULT_COLLECTION_ID
            );
        }

        private KpiDefinitionEntity makeComplexKpiDefinitionEntity(final String name, final Integer groupId, final String executionGroup, final int id) {
            return entity(
                    name, COMPLEX, EXPRESSION_1, OBJECT_TYPE_1, AGGREGATION_TYPE_1, 60,
                    Arrays.asList(AGGREGATION_ELEMENT_1, "aggregationElement2"), true, executionGroup(groupId, executionGroup),
                    0, 0, List.of(FILTER_1.getName(), FILTER_2.getName()), false, null,
                    null, null, null, id, DEFAULT_COLLECTION_ID
            );
        }
    }

    @Nested
    @DisplayName("Given an available definition and calculation database")
    class GivenAnAvailableDefinitionAndCalculationDatabase {

        EmbeddedDatabase embeddedDatabase;

        @BeforeEach
        void setUp() {
            embeddedDatabase = database(
                    "sql/initialize_kpi_definition.sql",
                    "sql/initialize_calculation.sql",
                    "sql/initialize_kpi_definition_and_calculation.sql"
            );
        }

        @AfterEach
        void tearDown() {
            embeddedDatabase.shutdown();
        }

        @Test
        void shouldSaveRelationsBetweenDefinitionAndCalculation() {
            final Set<String> kpiNames = Set.of("kpiDefinition1", "kpiDefinition2");
            RepositoryHelpers.prepare(embeddedDatabase, () -> {
                objectUnderTest.saveOnDemandCalculationRelation(kpiNames, CALCULATION_ID);
            });
            assertJunctionTable(kpiNames.size());
        }

        @SneakyThrows
        private void assertJunctionTable(final int expectedRows) {
            final String sql = "SELECT * FROM kpi.on_demand_definitions_per_calculation";
            try (final Statement stmt = embeddedDatabase.getConnection().createStatement();
                 final ResultSet rs = stmt.executeQuery(sql)) {

                final List<Integer> ids = new ArrayList<>();
                final Set<UUID> calculationIds = new HashSet<>();
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                    calculationIds.add(rs.getObject(2, UUID.class));
                }
                assertThat(ids).hasSize(expectedRows);
                assertThat(calculationIds).containsExactly(CALCULATION_ID);
            }
        }
    }

    @Nested
    @DisplayName("Given an available definition and on demand relations database")
    class GivenAnAvailableDefinitionAndOnDemandRelationsDatabase {

        EmbeddedDatabase embeddedDatabase;

        @BeforeEach
        void setUp() {
            embeddedDatabase = database(
                    "sql/initialize_kpi_definition.sql",
                    "sql/initialize_calculation.sql",
                    "sql/initialize_kpi_definition_and_calculation.sql",
                    "sql/initialize_kpi_definition_and_on_demand_relations.sql"
            );
        }

        @AfterEach
        void tearDown() {
            embeddedDatabase.shutdown();
        }

        @Test
        void shouldGiveBackCorrectKpiDefinitionsForCalculation() {
            RepositoryHelpers.prepare(embeddedDatabase, () -> {
                final KpiDefinitionEntity expected1 = makeKpiDefinitionEntity("kpiDefinition3", 3);
                final KpiDefinitionEntity expected2 = makeKpiDefinitionEntity("kpiDefinition4", 4);

                final List<KpiDefinitionEntity> expected = List.of(expected1, expected2);
                final List<KpiDefinitionEntity> actual = objectUnderTest.findOnDemandKpiDefinitionsByCalculationId(CALCULATION_ID2);

                assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
            });
        }

        private KpiDefinitionEntity makeKpiDefinitionEntity(final String name, final int id) {
            return entity(
                    name, "alias2", "expression1", "objectType1", "aggregationType1", 60,
                    List.of("aggregationElement1", "aggregationElement2"), true, null,
                    0,0, List.of("filter1", "filter2"), false, null,
                    null, null, null, id, DEFAULT_COLLECTION_ID
            );
        }
    }

    static KpiDefinitionEntity entity(
            final String name, final String alias, final String expression, final String objectType,
            final String aggregationType, final int aggregationPeriod, final List<String> elements,
            final boolean exportable, final ExecutionGroup executionGroup, final int reliabilityOffset,
            final int lookbackLimit, final List<String> filters, final boolean reexportLateData,
            final SchemaDetail schemaDetail, final String dataSpace, final String category, final String schema, final int id,
            final UUID collectionId
    ) {
        final KpiDefinitionEntityBuilder builder = KpiDefinitionEntity.builder();
        builder.withName(name);
        builder.withAlias(alias);
        builder.withExpression(expression);
        builder.withObjectType(objectType);
        builder.withAggregationType(aggregationType);
        builder.withAggregationPeriod(aggregationPeriod);
        builder.withAggregationElements(elements);
        builder.withExportable(exportable);
        builder.withExecutionGroup(executionGroup);
        builder.withDataReliabilityOffset(reliabilityOffset);
        builder.withDataLookbackLimit(lookbackLimit);
        builder.withFilters(filters);
        builder.withReexportLateData(reexportLateData);
        builder.withSchemaDetail(schemaDetail);
        builder.withSchemaDataSpace(dataSpace);
        builder.withSchemaCategory(category);
        builder.withSchemaName(schema);
        builder.withId(id);
        builder.withCollectionId(collectionId);
        return builder.build();
    }

    static ExecutionGroup executionGroup(final int id, final String executionGroup1) {
        return ExecutionGroup.builder().withId(id).withName(executionGroup1).build();
    }
}
