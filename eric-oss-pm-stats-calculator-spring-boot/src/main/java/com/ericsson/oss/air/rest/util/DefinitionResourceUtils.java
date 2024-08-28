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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.CalculationRequestSuccessResponse;
import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.VerificationSuccessResponse;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;

import kpi.model.KpiDefinitionRequest;
import kpi.model.api.table.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utils for Model and Definition REST endpoints.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefinitionResourceUtils {

    /**
     * Creates a REST success response for persistence of KPI definitions.
     *
     * @param successMessage
     *            a {@link String} success message to be returned with the REST response
     * @param submittedKpiDefinitions
     *            a{@link Map} of kpi by table name
     * @return a {@link VerificationSuccessResponse} which contains the success message
     */
    public static VerificationSuccessResponse getVerificationSuccessResponse(final String successMessage,
                                                                             final Map<String, String> submittedKpiDefinitions) {
        final VerificationSuccessResponse verificationResponse = new VerificationSuccessResponse();
        verificationResponse.setSuccessMessage(successMessage);
        if (submittedKpiDefinitions != null) {
            verificationResponse.setSubmittedKpiDefinitions(submittedKpiDefinitions);
        }
        return verificationResponse;
    }

    /**
     * Creates a REST success response for calculation requests.
     *
     * @param successMessage
     *            a {@link String} success message to be returned with the REST response
     * @param calculationId
     *            a {@link UUID} ID representing KPI calculation that has been launched
     * @param kpiOutputLocations
     *            a{@link Map} of kpi by table name
     * @return a {@link VerificationSuccessResponse} which contains the success message
     */
    static CalculationRequestSuccessResponse getCalculationRequestSuccessResponse(final String successMessage, final UUID calculationId,
            final Map<String, String> kpiOutputLocations) {
        final CalculationRequestSuccessResponse calculationRequestSuccessResponse = new CalculationRequestSuccessResponse();
        calculationRequestSuccessResponse.setSuccessMessage(successMessage);
        calculationRequestSuccessResponse.setCalculationId(calculationId);
        if (kpiOutputLocations != null) {
            calculationRequestSuccessResponse.setKpiOutputLocations(kpiOutputLocations);
        }
        return calculationRequestSuccessResponse;
    }

    /**
     * Converts a {@link KpiDefinitionRequest}s to kpi names by table {@link HashMap}. Format will be as following on the return {@link HashMap}
     * key: kpiName, value: tableName
     *
     * @param definitionPayload
     *            {@link KpiDefinitionRequest}
     * @return a {@link HashMap} of kpi names by table name
     */
    public static Map<String, String> getKpisByTableName(final KpiDefinitionRequest definitionPayload) {
        final Map<String, String> kpisByTableNameMap = new HashMap<>();
        final List<Table> definitionTables = definitionPayload.tables();

        for(final Table definitionTable : definitionTables){
            definitionTable.kpiDefinitions().forEach(definition -> {
                final String definitionName = definition.name().value();
                final String tableName = definitionTable.tableName();
                kpisByTableNameMap.put(definitionName, tableName);
            });
        }

        return kpisByTableNameMap;
    }

    /**
     * Converts a {@link Collection} of {@link KpiDefinitionEntity}s to kpi names by table {@link HashMap}. Format will be as following on the return {@link HashMap}
     * key: kpiName:aggregationPeriod, value: tableName
     *
     * @param definitions
     *            a {@link Collection} of {@link KpiDefinitionEntity}
     * @return a {@link HashMap} of kpi names by table name
     */
    public static Map<String, String> getKpisByTableName(final Collection<KpiDefinitionEntity> definitions) {
        return definitions.stream().collect(Collectors.toMap(KpiDefinitionEntity::name,KpiDefinitionEntity::tableName));
    }

}
