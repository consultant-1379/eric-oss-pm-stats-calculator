/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql;

import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_DB;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_IN_MEMORY;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_POST_AGGREGATION;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.column;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.reference;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.relation;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.table;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.service.validator.sql.exception.UnscopedComplexInMemoryResolutionException;
import com.ericsson.oss.air.pm.stats.service.validator.sql.reference.RelationReferenceResolution;
import com.ericsson.oss.air.pm.stats.service.validator.sql.reference.ResolutionResult;

import kpi.model.api.table.definition.KpiDefinition;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SqlDatasourceValidatorTest {
    @Mock
    KpiDefinitionService kpiDefinitionServiceMock;

    @InjectMocks
    SqlDatasourceValidator objectUnderTest;

    @Nested
    class ValidateComplexInMemoryScope {

        @Test
        void shouldDoNothing_whenProvidedResultsAreEmpty() {
            objectUnderTest.validateDatasources(List.of());
            verifyNoInteractions(kpiDefinitionServiceMock);
        }

        @Test
        void shouldDoNothing_whenProvidedResultsContainNoScheduledComplex() {
            final KpiDefinition kpiDefinitionMock1 = mock(KpiDefinition.class, RETURNS_DEEP_STUBS);

            when(kpiDefinitionMock1.isScheduledComplex()).thenReturn(false);

            objectUnderTest.validateDatasources(List.of(
                    resolutionResult(kpiDefinitionMock1, resolvedResolutions(
                            resolution(relation(KPI_DB, table("kpi_cell_guid_60"), null), reference(
                                    KPI_DB, table("kpi_cell_guid_60"), column("sum_integer_60"), null
                            ))
                    ))
            ));

            verify(kpiDefinitionMock1).isScheduledComplex();
            verifyNoInteractions(kpiDefinitionServiceMock);
        }

        @Test
        void shouldValidateComplexExecutionGroup() {
            final KpiDefinition kpiDefinitionMock1 = mock(KpiDefinition.class, RETURNS_DEEP_STUBS);
            final KpiDefinition kpiDefinitionMock2 = mock(KpiDefinition.class, RETURNS_DEEP_STUBS);
            final KpiDefinition kpiDefinitionMock3 = mock(KpiDefinition.class, RETURNS_DEEP_STUBS);
            final KpiDefinition kpiDefinitionMock4 = mock(KpiDefinition.class, RETURNS_DEEP_STUBS);
            final KpiDefinition kpiDefinitionMock5 = mock(KpiDefinition.class, RETURNS_DEEP_STUBS);
            final KpiDefinition kpiDefinitionMock6 = mock(KpiDefinition.class, RETURNS_DEEP_STUBS);

            when(kpiDefinitionMock1.isScheduledComplex()).thenReturn(true);
            when(kpiDefinitionMock2.isScheduledComplex()).thenReturn(true);
            when(kpiDefinitionMock3.isScheduledComplex()).thenReturn(true);
            when(kpiDefinitionMock4.isScheduledComplex()).thenReturn(true);
            when(kpiDefinitionMock5.isScheduledComplex()).thenReturn(true);
            when(kpiDefinitionMock6.isScheduledComplex()).thenReturn(true);

            when(kpiDefinitionMock1.name().value()).thenReturn("rolling_sum_integer_1440");
            when(kpiDefinitionMock2.name().value()).thenReturn("rolling_max_integer_1440");
            when(kpiDefinitionMock3.name().value()).thenReturn("first_float_operator_1440_post_aggregation");
            when(kpiDefinitionMock4.name().value()).thenReturn("first_integer_operator_60_stage2");
            when(kpiDefinitionMock5.name().value()).thenReturn("first_integer_operator_60_stage3");
            when(kpiDefinitionMock6.name().value()).thenReturn("first_integer_operator_60_stage4");

            when(kpiDefinitionMock1.executionGroup().value()).thenReturn("rolling_execution_group");
            when(kpiDefinitionMock2.executionGroup().value()).thenReturn("rolling_execution_group");
            when(kpiDefinitionMock3.executionGroup().value()).thenReturn("rolling_execution_group");
            when(kpiDefinitionMock4.executionGroup().value()).thenReturn("in_memory_group");
            when(kpiDefinitionMock5.executionGroup().value()).thenReturn("in_memory_group");
            when(kpiDefinitionMock6.executionGroup().value()).thenReturn("in_memory_group");

            when(kpiDefinitionServiceMock.findAllKpiNames()).thenReturn(Set.of(
                    "integer_simple", "float_simple", "sum_Integer_1440_simple",
                    "rolling_sum_integer_1440", "rolling_max_integer_1440", "first_float_operator_1440_post_aggregation",
                    "first_integer_operator_60_stage2", "first_integer_operator_60_stage3"
            ));
            when(kpiDefinitionServiceMock.findAllComplexKpiNamesGroupedByExecGroups()).thenReturn(Map.of(
                    "rolling_execution_group", List.of(
                            "rolling_sum_integer_1440", "rolling_max_integer_1440", "first_float_operator_1440_post_aggregation"
                    ),
                    "in_memory_group", List.of(
                            "first_integer_operator_60_stage2", "first_integer_operator_60_stage3"
                    )
            ));

            final List<ResolutionResult> resolutionResults = List.of(
                    resolutionResult(kpiDefinitionMock1, resolvedResolutions(
                            resolution(
                                    relation(KPI_DB, table("kpi_cell_guid_simple_1440"), null),
                                    reference(KPI_DB, table("kpi_cell_guid_simple_1440"), column("sum_Integer_1440_simple"), null
                                    ))
                    )),
                    resolutionResult(kpiDefinitionMock2, resolvedResolutions(
                            resolution(
                                    relation(KPI_DB, table("kpi_cell_guid_simple_1440"), null),
                                    reference(KPI_DB, table("kpi_cell_guid_simple_1440"), column("sum_Integer_1440_simple"), null
                                    ))
                    )),
                    resolutionResult(kpiDefinitionMock3, resolvedResolutions(
                            resolution(
                                    relation(KPI_POST_AGGREGATION, table("rolling_aggregation"), null),
                                    reference(KPI_POST_AGGREGATION, table("rolling_aggregation"), column("rolling_sum_integer_1440"), null
                                    )),
                            resolution(
                                    relation(KPI_POST_AGGREGATION, table("rolling_aggregation"), null),
                                    reference(KPI_POST_AGGREGATION, table("rolling_aggregation"), column("rolling_max_integer_1440"), null
                                    ))
                    )),
                    resolutionResult(kpiDefinitionMock4, resolvedResolutions(
                            resolution(
                                    relation(KPI_DB, table("kpi_simple_60"), null),
                                    reference(KPI_DB, table("kpi_simple_60"), column("integer_simple"), null
                                    )),
                            resolution(
                                    relation(KPI_DB, table("kpi_simple_60"), null),
                                    reference(KPI_DB, table("kpi_simple_60"), column("float_simple"), null
                                    ))
                    )),
                    resolutionResult(kpiDefinitionMock5, resolvedResolutions(
                            resolution(
                                    relation(KPI_IN_MEMORY, table("cell_guid"), null),
                                    reference(KPI_IN_MEMORY, table("cell_guid"), column("first_integer_operator_60_stage2"), null
                                    ))
                    )),
                    resolutionResult(kpiDefinitionMock6, resolvedResolutions(
                            resolution(
                                    relation(KPI_IN_MEMORY, table("cell_guid"), null),
                                    reference(KPI_IN_MEMORY, table("cell_guid"), column("first_integer_operator_60_stage2"), null
                                    )),
                            resolution(
                                    relation(KPI_IN_MEMORY, table("cell_guid"), null),
                                    reference(KPI_IN_MEMORY, table("cell_guid"), column("first_integer_operator_60_stage3"), null
                                    ))
                    ))
            );

            Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.validateDatasources(resolutionResults));
        }

        @Test
        void shouldFailValidation_whenDependencyIsOutsideOfComplexExecutionGroup() {
            final KpiDefinition kpiDefinitionMock1 = mock(KpiDefinition.class, RETURNS_DEEP_STUBS);
            final KpiDefinition kpiDefinitionMock2 = mock(KpiDefinition.class, RETURNS_DEEP_STUBS);
            final KpiDefinition kpiDefinitionMock3 = mock(KpiDefinition.class, RETURNS_DEEP_STUBS);

            when(kpiDefinitionMock1.isScheduledComplex()).thenReturn(true);
            when(kpiDefinitionMock2.isScheduledComplex()).thenReturn(true);
            when(kpiDefinitionMock3.isScheduledComplex()).thenReturn(true);

            when(kpiDefinitionMock1.name().value()).thenReturn("rolling_sum_integer_1440");
            when(kpiDefinitionMock2.name().value()).thenReturn("rolling_max_integer_1440");
            when(kpiDefinitionMock3.name().value()).thenReturn("first_float_operator_1440_post_aggregation");

            when(kpiDefinitionMock1.executionGroup().value()).thenReturn("rolling_execution_group_1");
            when(kpiDefinitionMock2.executionGroup().value()).thenReturn("rolling_execution_group_1");
            when(kpiDefinitionMock3.executionGroup().value()).thenReturn("rolling_execution_group_2");

            when(kpiDefinitionServiceMock.findAllKpiNames()).thenReturn(emptySet());
            when(kpiDefinitionServiceMock.findAllComplexKpiNamesGroupedByExecGroups()).thenReturn(emptyMap());

            final List<ResolutionResult> resolutionResults = List.of(
                    resolutionResult(
                            kpiDefinitionMock1, resolvedResolutions(resolution(
                                    relation(KPI_DB, table("kpi_cell_guid_simple_1440"), null),
                                    reference(KPI_DB, table("kpi_cell_guid_simple_1440"), column("sum_Integer_1440_simple"), null)
                            ))
                    ),
                    resolutionResult(
                            kpiDefinitionMock2, resolvedResolutions(resolution(
                                    relation(KPI_DB, table("kpi_cell_guid_simple_1440"), null),
                                    reference(KPI_DB, table("kpi_cell_guid_simple_1440"), column("sum_Integer_1440_simple"), null)
                            ))
                    ),
                    resolutionResult(
                            kpiDefinitionMock3, resolvedResolutions(
                                    resolution(
                                            relation(KPI_POST_AGGREGATION, table("rolling_aggregation"), null),
                                            reference(KPI_POST_AGGREGATION, table("rolling_aggregation"), column("rolling_sum_integer_1440"), null)),
                                    resolution(
                                            relation(KPI_POST_AGGREGATION, table("rolling_aggregation"), null),
                                            reference(KPI_POST_AGGREGATION, table("rolling_aggregation"), column("rolling_max_integer_1440"), null)
                                    )
                            )
                    )
            );

            Assertions.assertThatThrownBy(() -> objectUnderTest.validateDatasources(resolutionResults))
                    .asInstanceOf(InstanceOfAssertFactories.throwable(UnscopedComplexInMemoryResolutionException.class))
                    .satisfies(exception -> {
                        final HashSetValuedHashMap<KpiDefinition, RelationReferenceResolution> actual = exception.unscopedResolutions();

                        final MultiValuedMap<KpiDefinition, RelationReferenceResolution> expected = new HashSetValuedHashMap<>();
                        final RelationReferenceResolution resolution1 = resolution(
                                relation(KPI_POST_AGGREGATION, table("rolling_aggregation"), null),
                                reference(KPI_POST_AGGREGATION, table("rolling_aggregation"), column("rolling_sum_integer_1440"), null)
                        );
                        final RelationReferenceResolution resolution2 = resolution(
                                relation(KPI_POST_AGGREGATION, table("rolling_aggregation"), null),
                                reference(KPI_POST_AGGREGATION, table("rolling_aggregation"), column("rolling_max_integer_1440"), null)
                        );

                        resolution1.resolve();
                        resolution2.resolve();

                        expected.put(kpiDefinitionMock3, resolution1);
                        expected.put(kpiDefinitionMock3, resolution2);

                        Assertions.assertThat(actual).isEqualTo(expected);
                    });

        }
    }

    static List<RelationReferenceResolution> resolvedResolutions(final RelationReferenceResolution... resolutions) {
        return List.of(resolutions);
    }

    static RelationReferenceResolution resolution(final Relation relation, final Reference reference) {
        return new RelationReferenceResolution(relation, reference);
    }

    static ResolutionResult resolutionResult(final KpiDefinition kpiDefinition, final List<RelationReferenceResolution> resolved) {
        final ResolutionResult resolutionResult = new ResolutionResult(kpiDefinition);

        resolved.forEach(resolution -> {
            resolutionResult.addResolvedResolution(resolution.relation().orElse(null), resolution.reference());
        });


        return resolutionResult;
    }
}