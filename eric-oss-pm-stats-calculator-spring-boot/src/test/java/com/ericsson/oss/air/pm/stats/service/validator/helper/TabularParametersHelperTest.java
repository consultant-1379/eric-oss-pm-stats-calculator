/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.helper;

import static com.ericsson.oss.air.pm.stats._util.JsonLoaders.load;
import static com.ericsson.oss.air.pm.stats._util.Serialization.deserialize;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_DB;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.TABULAR_PARAMETERS;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.alias;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.column;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.reference;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.table;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.Format;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload.TabularParameters;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload.TabularParameters.TabularParametersBuilder;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.service.validator.sql.SqlExtractorService;
import com.ericsson.oss.air.pm.stats.service.validator.sql.SqlRelationExtractor;

import org.apache.commons.collections4.MultiValuedMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TabularParametersHelperTest {
    @Mock
    KpiDefinitionService kpiDefinitionServiceMock;
    @Mock
    SqlRelationExtractor sqlRelationExtractorMock;
    @Mock
    SqlExtractorService sqlExtractorServiceMock;

    @InjectMocks
    TabularParameterHelper objectUnderTest;

    @Test
    void shouldSplitHeader(@Mock final TabularParameters tabularParameterMock) {
        when(tabularParameterMock.getHeader()).thenReturn("tabular_agg,tabular_dimension");

        final List<String> actual = objectUnderTest.splitHeader(tabularParameterMock);

        verify(tabularParameterMock).getHeader();

        assertThat(actual).containsExactlyElementsOf(List.of("tabular_agg", "tabular_dimension"));
    }

    @Test
    void shouldGetTabularParameterWithHeader() {
        final KpiCalculationRequestPayload request = deserialize(load("json/calculationRequest.json"), KpiCalculationRequestPayload.class);
        final List<TabularParameters> actual = objectUnderTest.getTabularParametersWithHeader(request);

        assertThat(actual).containsExactly(tabularParameter("tabular_parameter2", "header1", "10"));
    }

    @Test
    void shouldGetReferences(@Mock final KpiDefinitionEntity entityMock) {
        final Reference reference = reference(TABULAR_PARAMETERS, table("tabular_parameter2"), column("agg_column_0"), alias("tabular_parameter2"));

        when(sqlExtractorServiceMock.extractColumnsFromOnDemand(entityMock)).thenReturn(singleton(reference));

        final Set<Reference> actual = objectUnderTest.getReference(List.of(entityMock));

        verify(sqlExtractorServiceMock).extractColumnsFromOnDemand(entityMock);

        assertThat(actual).containsExactly(reference);
    }

    @Test
    void shouldGetTabularParameterDatasourceDefinitions(@Mock final KpiDefinitionEntity entityMock1, @Mock final KpiDefinitionEntity entityMock2) {
        final KpiCalculationRequestPayload request = deserialize(load("json/calculationRequest.json"), KpiCalculationRequestPayload.class);

        when(kpiDefinitionServiceMock.findByNames(any())).thenReturn(List.of(entityMock1, entityMock2));
        when(sqlRelationExtractorMock.extractRelations(entityMock1)).thenReturn(Set.of(Relation.of(KPI_DB, Table.of("fact_table_1"), null)));
        final Relation tabularParameterRelation = Relation.of(TABULAR_PARAMETERS, Table.of("tabular_parameter2"), null);
        when(sqlRelationExtractorMock.extractRelations(entityMock2)).thenReturn(Set.of(tabularParameterRelation));

        final MultiValuedMap<KpiDefinitionEntity, Relation> actual = objectUnderTest.filterRelations(request.getKpiNames(), TABULAR_PARAMETERS);

        assertThat(actual.asMap()).containsExactly(entry(entityMock2, Set.of(tabularParameterRelation)));

        verify(kpiDefinitionServiceMock).findByNames(any());
        verify(sqlRelationExtractorMock).extractRelations(entityMock1);
        verify(sqlRelationExtractorMock).extractRelations(entityMock2);
    }

    @Test
    void shouldFetchTabularParameterEntities(@Mock final KpiDefinitionEntity kpiDefinition1, @Mock final KpiDefinitionEntity kpiDefinition2) {
        final KpiCalculationRequestPayload request = deserialize(load("json/calculationRequest.json"), KpiCalculationRequestPayload.class);

        when(kpiDefinitionServiceMock.findByNames(any())).thenReturn(List.of(kpiDefinition1, kpiDefinition2));
        when(sqlRelationExtractorMock.extractRelations(kpiDefinition1)).thenReturn(Set.of(Relation.of(KPI_DB, Table.of("fact_table_1"), null)));
        when(sqlRelationExtractorMock.extractRelations(kpiDefinition2)).thenReturn(Set.of(Relation.of(TABULAR_PARAMETERS, Table.of("tabular_parameter2"), null)));

        final List<KpiDefinitionEntity> actual = objectUnderTest.fetchTabularParameterEntities(request);

        assertThat(actual).containsExactly(kpiDefinition2);

        verify(kpiDefinitionServiceMock).findByNames(any());
        verify(sqlRelationExtractorMock).extractRelations(kpiDefinition1);
        verify(sqlRelationExtractorMock).extractRelations(kpiDefinition2);
    }


    static TabularParameters tabularParameter(final String name, final String header, final String value) {
        final TabularParametersBuilder builder = TabularParameters.builder();
        builder.name(name);
        builder.format(Format.CSV);
        builder.header(header);
        builder.value(value);
        return builder.build();
    }
}