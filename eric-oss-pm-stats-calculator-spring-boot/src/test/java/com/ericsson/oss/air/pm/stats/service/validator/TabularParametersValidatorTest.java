/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator;

import static com.ericsson.oss.air.pm.stats.calculator.api.model.Format.CSV;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.TABULAR_PARAMETERS;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.pm.stats._util.JsonLoaders;
import com.ericsson.oss.air.pm.stats._util.Serialization;
import com.ericsson.oss.air.pm.stats.calculator.api.model.Format;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload.TabularParameters;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.References;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.TabularParameter;
import com.ericsson.oss.air.pm.stats.model.exception.TabularParameterValidationException;
import com.ericsson.oss.air.pm.stats.service.api.TabularParameterService;
import com.ericsson.oss.air.pm.stats.service.validator.helper.TabularParameterHelper;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TabularParametersValidatorTest {

    final KpiCalculationRequestPayload kpiCalculationRequestPayload =
            Serialization.deserialize(JsonLoaders.load("json/calculationRequest.json"), KpiCalculationRequestPayload.class);

    @Mock
    TabularParameterService tabularParameterServiceMock;
    @Mock
    TabularParameterHelper tabularParameterHelperMock;

    @InjectMocks
    TabularParameterValidator objectUnderTest;

    @Test
    void shouldPassTabularParameterTableInDBValidation() {
        when(tabularParameterServiceMock.findAllTabularParameters()).thenReturn(List.of(tabularParameter("tabular_parameter1"), tabularParameter("tabular_parameter2")));

        assertDoesNotThrow(() -> objectUnderTest.checkTabularParameterTableExistsInDatabase(kpiCalculationRequestPayload.getTabularParameters()));

        verify(tabularParameterServiceMock).findAllTabularParameters();
    }

    @Test
    void shouldThrowExceptionOnTabularParameterTableInDBValidation() {
        when(tabularParameterServiceMock.findAllTabularParameters()).thenReturn(List.of(tabularParameter("tabularName1"), tabularParameter("tabularName2")));

        Assertions.assertThatThrownBy(() -> objectUnderTest.checkTabularParameterTableExistsInDatabase(kpiCalculationRequestPayload.getTabularParameters()))
                .isInstanceOf(TabularParameterValidationException.class)
                .hasMessage("The following tabular parameters are not present in the KPI definition database: '[tabular_parameter1, tabular_parameter2]'");

        verify(tabularParameterServiceMock).findAllTabularParameters();
    }

    @Test
    void shouldPassTablesInRequestValidation() {
        final Set<String> kpiNames = kpiCalculationRequestPayload.getKpiNames();

        when(tabularParameterHelperMock.filterRelations(kpiNames, TABULAR_PARAMETERS)).thenReturn(getRelations());

        assertDoesNotThrow(() -> objectUnderTest.checkRequiredTabularParameterSourcesPresent(kpiCalculationRequestPayload));

        verify(tabularParameterHelperMock).filterRelations(kpiNames, TABULAR_PARAMETERS);
    }

    @Test
    void shouldThrowExceptionOnTablesInRequestValidation() {
        final Set<String> kpiNames = kpiCalculationRequestPayload.getKpiNames();

        final MultiValuedMap<KpiDefinitionEntity, Relation> relations = getRelations();
        relations.put(entity("rolling_max_integer_1440"), relation("not_present"));

        when(tabularParameterHelperMock.filterRelations(kpiNames, TABULAR_PARAMETERS)).thenReturn(relations);

        Assertions.assertThatThrownBy(() -> objectUnderTest.checkRequiredTabularParameterSourcesPresent(kpiCalculationRequestPayload))
                .isInstanceOf(TabularParameterValidationException.class)
                .hasMessage("The following tabular parameters are not present for the triggered KPIs: '[[rolling_max_integer_1440: [not_present]]]'");

        verify(tabularParameterHelperMock).filterRelations(kpiNames, TABULAR_PARAMETERS);
    }


    @Test
    void shouldPassColumnInCaseHeaderValidation() {
        final TabularParameters tabularParameter1 = TabularParameters.builder().name("table1").format(CSV).header("tabular_parameter2,tabular_parameter1").value("12").build();
        final List<String> splitHeaders = List.of("tabular_parameter2", "tabular_parameter1");

        when(tabularParameterHelperMock.splitHeader(tabularParameter1)).thenReturn(splitHeaders);
        when(tabularParameterHelperMock.getTabularParametersWithHeader(kpiCalculationRequestPayload)).thenReturn(List.of(tabularParameter1));
        when(tabularParameterHelperMock.getReference(any())).thenReturn(Set.of(reference("table1", "tabular_parameter1"),
                reference("kpi_db_table", "column")));

        assertDoesNotThrow(() -> objectUnderTest.checkColumnsInCaseHeaderPresent(kpiCalculationRequestPayload));

        verify(tabularParameterHelperMock).getTabularParametersWithHeader(kpiCalculationRequestPayload);
        verify(tabularParameterHelperMock).getReference(any());
    }

    @Test
    void shouldPassColumnInCaseHeaderValidation_NoHeader() {
        when(tabularParameterHelperMock.getTabularParametersWithHeader(kpiCalculationRequestPayload)).thenReturn(List.of());

        assertDoesNotThrow(() -> objectUnderTest.checkColumnsInCaseHeaderPresent(kpiCalculationRequestPayload));

        verify(tabularParameterHelperMock).getTabularParametersWithHeader(kpiCalculationRequestPayload);
    }

    @Test
    void shouldThrowExceptionOnColumnInCaseHeaderValidation() {
        final TabularParameters tabularParameter1 = TabularParameters.builder().name("table1").format(Format.CSV).header("tabular_parameter1").value("12").build();
        final TabularParameters tabularParameter2 = TabularParameters.builder().name("table2").format(Format.CSV).header("tabular_parameter1,tabular_parameter3").value("12").build();

        when(tabularParameterHelperMock.splitHeader(tabularParameter1)).thenReturn(List.of("tabular_parameter1"));
        when(tabularParameterHelperMock.splitHeader(tabularParameter2)).thenReturn(List.of("tabular_parameter1", "tabular_parameter3"));
        when(tabularParameterHelperMock.getTabularParametersWithHeader(kpiCalculationRequestPayload)).thenReturn(List.of(tabularParameter1, tabularParameter2));
        when(tabularParameterHelperMock.getReference(any())).thenReturn(Set.of(reference("table1", "tabular_parameter1"),
                reference("table2", "tabular_parameter2")));

        Assertions.assertThatThrownBy(() -> objectUnderTest.checkColumnsInCaseHeaderPresent(kpiCalculationRequestPayload))
                .isInstanceOf(TabularParameterValidationException.class)
                .hasMessage("Not all of the required columns are present in the header of following tabular parameters: [table2 - tabular_parameter2]");

        verify(tabularParameterHelperMock).getTabularParametersWithHeader(kpiCalculationRequestPayload);
        verify(tabularParameterHelperMock).getReference(any());

    }

    static TabularParameter tabularParameter(final String name) {
        return TabularParameter.builder()
                .withName(name)
                .build();
    }

    static Reference reference(final String table, final String column) {
        return References.reference(References.datasource("TEST"),
                References.table(table),
                References.column(column),
                null);
    }

    static MultiValuedMap<KpiDefinitionEntity, Relation> getRelations() {
        final MultiValuedMap<KpiDefinitionEntity, Relation> relations = new HashSetValuedHashMap<>();
        relations.put(entity("rolling_sum_integer_1440"), relation("tabular_parameter1"));
        return relations;
    }

    static Relation relation(final String tableName) {
        return Relation.of(null, Table.of(tableName), null);
    }

    static KpiDefinitionEntity entity(final String name) {
        return KpiDefinitionEntity.builder()
                .withName(name)
                .build();
    }
}