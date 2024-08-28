/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.model;

import static com.ericsson.oss.air.pm.stats.common.model.constants.DatasourceConstants.DATASOURCE_DELIMITER;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Handles reading of persisted KPI definitions.
 */
//TODO Prepare more unit tests
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KpiDefinitionHandler {

    /**
     * Retrieves all {@link KpiDefinition}s with the supplied stage and the supplied alias.
     *
     * @param kpiDefinitions the {@link Map} of {@link KpiDefinition}s keyed by stage
     * @param stage          the stage of the KPI
     * @param alias          the alias of the KPI
     * @return the wanted {@link KpiDefinition}s
     */
    public static Set<KpiDefinition> getKpisForAGivenStageAndAliasFromStagedKpis(final Map<Integer, List<KpiDefinition>> kpiDefinitions,
                                                                                 final int stage, final String alias) {
        return kpiDefinitions.getOrDefault(stage, Collections.emptyList())
                .stream()
                .filter(kpi -> kpi.getAlias().equals(alias))
                .collect(toSet());
    }

    /**
     * Retrieves all {@link KpiDefinition}s with the supplied alias.
     *
     * @param kpiDefinitions the {@link Map} of {@link KpiDefinition}s keyed by stage
     * @param alias          the alias of the KPI
     * @return the wanted {@link KpiDefinition}s
     */
    public static Set<KpiDefinition> getKpisForAGivenAliasFromStagedKpis(final Map<Integer, List<KpiDefinition>> kpiDefinitions, final String alias) {
        return kpiDefinitions.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(kpi -> kpi.getAlias().equals(alias))
                .collect(toSet());
    }

    /**
     * Get the collection of KPI definitions that contain post aggregation URI (kpi_post_agg://).
     *
     * @param kpiDefinitions the {@link Collection} of {@link KpiDefinition}s to be used
     * @return the {@link Set} of post {@link KpiDefinition}s with expressions containing post aggregation URI.
     */
    public static Set<KpiDefinition> getPostAggregationKpiDefinitions(final Collection<KpiDefinition> kpiDefinitions) {
        return kpiDefinitions.stream()
                .filter(kpi -> kpi.getExpression().contains(Datasource.KPI_POST_AGGREGATION.getName() + DATASOURCE_DELIMITER))
                .collect(toSet());
    }

    public static boolean areKpiDefinitionsSimpleKpis(final Collection<KpiDefinition> kpiDefinitions) {
        for (final KpiDefinition kpiDefinition : kpiDefinitions) {
            if (!kpiDefinition.isSimple()) {
                return false;
            }
        }
        return true;
    }

    public static Set<DataIdentifier> getDataIdentifiersFromKpiDefinitions(final Collection<KpiDefinition> kpiDefinitions) {
        return kpiDefinitions.stream()
                .map(KpiDefinition::getInpDataIdentifier)
                .collect(toSet());
    }
}