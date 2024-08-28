/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.util;

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_AGGREGATION_PERIOD_INT;
import static com.ericsson.oss.air.rest.validator._helper.Mapper.toAggregationElements;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.CalculationRequestSuccessResponse;
import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.VerificationSuccessResponse;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity.KpiDefinitionEntityBuilder;

import kpi.model.KpiDefinitionRequest;
import kpi.model.RetentionPeriod;
import kpi.model.ScheduledSimple;
import kpi.model.ScheduledSimple.ScheduledSimpleBuilder;
import kpi.model.api.enumeration.AggregationType;
import kpi.model.api.table.definition.SimpleKpiDefinitions;
import kpi.model.api.table.definition.SimpleKpiDefinitions.SimpleKpiDefinition;
import kpi.model.api.table.definition.SimpleKpiDefinitions.SimpleKpiDefinition.SimpleKpiDefinitionBuilder;
import kpi.model.simple.SimpleTable;
import kpi.model.simple.SimpleTable.SimpleTableBuilder;
import kpi.model.simple.table.definition.optional.SimpleDefinitionAggregationElements;
import kpi.model.simple.table.definition.optional.SimpleDefinitionDataLookBackLimit;
import kpi.model.simple.table.definition.optional.SimpleDefinitionDataReliabilityOffset;
import kpi.model.simple.table.definition.optional.SimpleDefinitionExportable;
import kpi.model.simple.table.definition.optional.SimpleDefinitionInpDataIdentifier;
import kpi.model.simple.table.definition.optional.SimpleDefinitionReexportLateData;
import kpi.model.simple.table.definition.required.SimpleDefinitionAggregationType;
import kpi.model.simple.table.definition.required.SimpleDefinitionExpression;
import kpi.model.simple.table.definition.required.SimpleDefinitionName;
import kpi.model.simple.table.definition.required.SimpleDefinitionObjectType;
import kpi.model.simple.table.optional.SimpleTableAggregationPeriod;
import kpi.model.simple.table.optional.SimpleTableDataLookBackLimit;
import kpi.model.simple.table.optional.SimpleTableDataReliabilityOffset;
import kpi.model.simple.table.optional.SimpleTableExportable;
import kpi.model.simple.table.optional.SimpleTableReexportLateData;
import kpi.model.simple.table.required.SimpleTableAggregationElements;
import kpi.model.simple.table.required.SimpleTableAlias;
import kpi.model.simple.table.required.SimpleTableInpDataIdentifier;
import org.junit.jupiter.api.Test;

class DefinitionResourceUtilsTest {
    private static final UUID TEST_ID = UUID.fromString("d05774b3-df43-4cc7-a92b-d9db7a0f88cd");

    @Test
    void givenDefinitionsWithBothDefaultAndNonDefaultAggregationPeriods_whenDefinitionsAreGroupedByTableName_thenThereAreTwoTables() {
        final KpiDefinitionEntity definitionA = entity("KPI_A", 1_440);
        final KpiDefinitionEntity definitionB = entity("KPI_B", DEFAULT_AGGREGATION_PERIOD_INT);

        final Set<KpiDefinitionEntity> definitions = Set.of(definitionA, definitionB);
        final Map<String, String> kpisByTableName = DefinitionResourceUtils.getKpisByTableName(definitions);

        assertThat(kpisByTableName).hasSize(2);
        assertSoftly(softly -> {
            softly.assertThat(kpisByTableName).containsEntry("KPI_A", "kpi_alias_1440");
            softly.assertThat(kpisByTableName).containsEntry("KPI_B", "kpi_alias_");
        });
    }

    @Test
    void givenDefinitionsWithNonDefaultAggregationPeriods_whenDefinitionsAreGroupedByTableName_thenThereAreTwoTables_withPayload() {
        final KpiDefinitionRequest definition =  createDefinition();

        final Map<String, String> kpisByTableName = DefinitionResourceUtils.getKpisByTableName(definition);

        assertThat(kpisByTableName).hasSize(1);
        assertSoftly(softly -> {
            softly.assertThat(kpisByTableName).containsEntry("kpi", "kpi_alias_1440");
        });
    }

    @Test
    void givenSuccessMessageAndNullProposals_getVerificationSuccessResponse_returnsVerificationSuccessResponse() {
        final VerificationSuccessResponse result = DefinitionResourceUtils
                .getVerificationSuccessResponse("test_Success", null);
        assertThat(result.getSuccessMessage()).isEqualTo("test_Success");
        assertThat(result.getSubmittedKpiDefinitions()).isNull();
    }

    @Test
    void givenSuccessMessageAndValidProposals_getVerificationSuccessResponse_returnsVerificationSuccessResponse() {
        final Map<String, String> submittedKpiDefinitions = new HashMap<String, String>();
        submittedKpiDefinitions.put("test_Key", "test_Value");
        final VerificationSuccessResponse result = DefinitionResourceUtils
                .getVerificationSuccessResponse("test_Success", submittedKpiDefinitions);
        assertThat(result.getSuccessMessage()).isEqualTo("test_Success");
        assertThat(result.getSubmittedKpiDefinitions().toString()).hasToString("{test_Key=test_Value}");
    }

    @Test
    void givenSuccessMessageAndNullCalculationIdAndNullLocations_getCalculationRequestSuccessResponse_returnsVerificationSuccessResponse() {
        final CalculationRequestSuccessResponse result = DefinitionResourceUtils
                .getCalculationRequestSuccessResponse("test_Success", null, null);
        assertThat(result.getSuccessMessage()).isEqualTo("test_Success");
        assertThat(result.getCalculationId()).isNull();
        assertThat(result.getKpiOutputLocations()).isNull();
    }

    @Test
    void givenSuccessMessageAndCalculationIdAndLocations_getCalculationRequestSuccessResponse_returnsVerificationSuccessResponse() {
        final Map<String, String> kpiOutputLocations = new HashMap<String, String>();
        kpiOutputLocations.put("test_Key", "test_Value");
        final CalculationRequestSuccessResponse result = DefinitionResourceUtils
                .getCalculationRequestSuccessResponse("test_Success", TEST_ID, kpiOutputLocations);
        assertThat(result.getSuccessMessage()).hasToString("test_Success");
        assertThat(result.getCalculationId()).isEqualTo(TEST_ID);
        assertThat(result.getKpiOutputLocations().toString()).hasToString("{test_Key=test_Value}");
    }

    private KpiDefinitionRequest createDefinition() {
        final SimpleKpiDefinitionBuilder simpleKpiDefinitionBuilder = SimpleKpiDefinition.builder();
        simpleKpiDefinitionBuilder.name(SimpleDefinitionName.of("kpi"));
        simpleKpiDefinitionBuilder.expression(SimpleDefinitionExpression.of("expression_1"));
        simpleKpiDefinitionBuilder.objectType(SimpleDefinitionObjectType.of("INTEGER"));
        simpleKpiDefinitionBuilder.aggregationType(SimpleDefinitionAggregationType.of(AggregationType.SUM));
        simpleKpiDefinitionBuilder.aggregationElements(SimpleDefinitionAggregationElements.of(toAggregationElements("a.b")));
        simpleKpiDefinitionBuilder.exportable(SimpleDefinitionExportable.of(false));
        simpleKpiDefinitionBuilder.inpDataIdentifier(SimpleDefinitionInpDataIdentifier.of("dataSpace|category|schema"));
        simpleKpiDefinitionBuilder.dataReliabilityOffset(SimpleDefinitionDataReliabilityOffset.of(50));
        simpleKpiDefinitionBuilder.dataLookBackLimit(SimpleDefinitionDataLookBackLimit.of(200));
        simpleKpiDefinitionBuilder.reexportLateData(SimpleDefinitionReexportLateData.of(true));

        final SimpleTableBuilder simpleTableBuilder = SimpleTable.builder();
        simpleTableBuilder.aggregationPeriod(SimpleTableAggregationPeriod.of(1_440));
        simpleTableBuilder.alias(SimpleTableAlias.of("alias"));
        simpleTableBuilder.aggregationElements(SimpleTableAggregationElements.of(toAggregationElements("a.b")));
        simpleTableBuilder.exportable(SimpleTableExportable.of(false));
        simpleTableBuilder.inpDataIdentifier(SimpleTableInpDataIdentifier.of("dataSpace|category|schema"));
        simpleTableBuilder.dataReliabilityOffset(SimpleTableDataReliabilityOffset.of(50));
        simpleTableBuilder.dataLookBackLimit(SimpleTableDataLookBackLimit.of(200));
        simpleTableBuilder.reexportLateData(SimpleTableReexportLateData.of(true));
        simpleTableBuilder.kpiDefinitions(SimpleKpiDefinitions.of(List.of(simpleKpiDefinitionBuilder.build())));

        final ScheduledSimpleBuilder scheduledSimpleBuilder = ScheduledSimple.builder();
        scheduledSimpleBuilder.kpiOutputTables(List.of(simpleTableBuilder.build()));

        return KpiDefinitionRequest.builder()
                .retentionPeriod(RetentionPeriod.of(null))
                .scheduledSimple(scheduledSimpleBuilder.build()).build();
    }

    static KpiDefinitionEntity entity(final String name, final int aggregationPeriod) {
        final KpiDefinitionEntityBuilder builder = KpiDefinitionEntity.builder();
        builder.withName(name);
        builder.withAlias("alias");
        builder.withAggregationPeriod(aggregationPeriod);
        return builder.build();
    }
}