/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.util;

import java.util.Collections;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiRetrievalException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.util.KpiDefinitionFileRetriever;

public final class KpiDefinitionRetrievalTestUtils {

    private KpiDefinitionRetrievalTestUtils() {

    }

    public static Set<KpiDefinition> retrieveKpiDefinitions(final KpiDefinitionFileRetriever kpiDefinitionRetriever) {
        try {
            return kpiDefinitionRetriever.retrieveAllKpiDefinitions();
        } catch (final KpiRetrievalException e) {
            return Collections.EMPTY_SET;
        }
    }

    public static KpiDefinition retrieveKpiDefinition(final KpiDefinitionFileRetriever kpiDefinitionRetriever, final String kpiDefinitionName) throws KpiRetrievalException {
        return kpiDefinitionRetriever
                .retrieveNamedKpiDefinitions(Collections.singletonList(kpiDefinitionName))
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("Kpi Definition with name '%s' does not exist.", kpiDefinitionName)));
    }

    public static KpiDefinition initKpi(String kpiName) {
        try {
            return retrieveKpiDefinition(new KpiDefinitionFileRetriever(), kpiName);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
