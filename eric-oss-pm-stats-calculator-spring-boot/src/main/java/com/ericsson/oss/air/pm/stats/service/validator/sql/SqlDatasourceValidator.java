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

import static com.ericsson.oss.air.pm.stats.model.exception.KpiDefinitionValidationException.badRequest;
import static com.ericsson.oss.air.pm.stats.service.validator.sql.SqlDatasourceValidator.ComplexExecutionGroup.complexExecutionGroup;
import static com.ericsson.oss.air.pm.stats.service.validator.sql.SqlDatasourceValidator.KpiDefinitionName.kpiDefinitionName;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PUBLIC;
import static org.apache.arrow.util.Preconditions.checkArgument;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.DatasourceType;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Alias;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.References;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.service.validator.sql.exception.UnscopedComplexInMemoryResolutionException;
import com.ericsson.oss.air.pm.stats.service.validator.sql.reference.RelationReferenceResolution;
import com.ericsson.oss.air.pm.stats.service.validator.sql.reference.ResolutionResult;

import kpi.model.api.table.definition.KpiDefinition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class SqlDatasourceValidator {
    @Inject
    private KpiDefinitionService kpiDefinitionService;

    /**
     * Validation aspects:
     * <ul>
     *     <li>
     *         For <strong>COMPLEX</strong> definitions: in case the {@link DatasourceType#KPI_IN_MEMORY} and
     *         {@link DatasourceType#KPI_POST_AGGREGATION} data sources are used in the KPI Definition the KPI can only depend on KPIs which are in
     *         the same execution group itself.
     *     </li>
     * </ul>
     *
     * @param resolutionResults the resolved resolutions to process
     */
    public void validateDatasources(final List<ResolutionResult> resolutionResults) {
        checkArgument(areAllResolved(resolutionResults), "'resolutionResults' contain UNRESOLVED");

        validateComplexInMemoryScope(resolutionResults);
    }

    @SuppressWarnings("squid:S3776")
    private void validateComplexInMemoryScope(final List<ResolutionResult> resolutionResults) {
        final List<ResolutionResult> complexResolutionResults = complexResolutionResults(resolutionResults);

        if (complexResolutionResults.isEmpty()) {
            return;
        }

        final Set<KpiDefinitionName> allKpiDefinitionNames = allKpiNames(resolutionResults);
        final MultiValuedMap<ComplexExecutionGroup, KpiDefinitionName> allComplexExecutionGroups = allComplexExecutionGroups(resolutionResults);

        final HashSetValuedHashMap<KpiDefinition, RelationReferenceResolution> unscopedResolutions = new HashSetValuedHashMap<>();
        complexResolutionResults.forEach(complexResolutionResult -> {
            final Set<RelationReferenceResolution> resolutions = complexResolutionResult.resolutions();
            if (hasInMemoryDatasource(resolutions)) {
                final KpiDefinition kpiDefinition = complexResolutionResult.kpiDefinition();
                final KpiDefinitionName targetKpiDefinitionName = kpiDefinitionName(complexResolutionResult.kpiDefinitionName());
                final ComplexExecutionGroup targetExecutionGroup = complexExecutionGroup(kpiDefinition.executionGroup().value());

                log.info("Complex KPI Definition '{}' has in-memory datasource", targetKpiDefinitionName);

                for (final RelationReferenceResolution resolution : resolutions) {
                    final KpiDefinitionName kpiDefinitionName = deduceKpiDefinitionName(resolution.reference());

                    if (allComplexExecutionGroups.containsMapping(targetExecutionGroup, kpiDefinitionName)) {
                        log.info("'{}' is within the scope of execution group '{}'", resolution, targetExecutionGroup);
                        continue;
                    }

                    if (allKpiDefinitionNames.contains(kpiDefinitionName)) {
                        log.info("'{}' is not within the scope of execution group '{}'", resolution, targetExecutionGroup);
                        unscopedResolutions.put(kpiDefinition, resolution);
                    } else {
                        //  The kpiDefinitionName is an aggregation column
                        log.info("'{}' is within the scope of execution group '{}'", resolution, targetExecutionGroup);
                    }
                }
            }
        });

        if (!unscopedResolutions.isEmpty()) {
            throw new UnscopedComplexInMemoryResolutionException(unscopedResolutions);
        }
    }

    private MultiValuedMap<ComplexExecutionGroup, KpiDefinitionName> allComplexExecutionGroups(final List<ResolutionResult> resolutionResults) {
        final MultiValuedMap<ComplexExecutionGroup, KpiDefinitionName> result = new HashSetValuedHashMap<>();

        complexResolutionResults(resolutionResults).stream().map(ResolutionResult::kpiDefinition).forEach(kpiDefinition -> {
            final ComplexExecutionGroup complexExecutionGroup = complexExecutionGroup(kpiDefinition.executionGroup().value());
            final KpiDefinitionName kpiDefinitionName = kpiDefinitionName(kpiDefinition.name().value());
            result.put(complexExecutionGroup, kpiDefinitionName);
        });

        kpiDefinitionService.findAllComplexKpiNamesGroupedByExecGroups().forEach((executionGroup, kpiDefinitionNames) -> {
            final ComplexExecutionGroup complexExecutionGroup = complexExecutionGroup(executionGroup);
            for (final String kpiDefinitionName : kpiDefinitionNames) {
                result.put(complexExecutionGroup, kpiDefinitionName(kpiDefinitionName));
            }
        });

        return result;
    }

    private Set<KpiDefinitionName> allKpiNames(final Collection<ResolutionResult> resolutionResults) {
        final Set<KpiDefinitionName> result = new HashSet<>();

        for (final String kpiDefinitionName : kpiDefinitionService.findAllKpiNames()) {
            result.add(kpiDefinitionName(kpiDefinitionName));
        }

        for (final ResolutionResult resolutionResult : resolutionResults) {
            result.add(kpiDefinitionName(resolutionResult.kpiDefinitionName()));
        }

        return result;
    }

    private static KpiDefinitionName deduceKpiDefinitionName(@NonNull final Reference reference) {
        //  For KPI Definition dependency resolution the first target from the Reference is the column and a fallback option is the alias.
        //  In case of missing column it is most likely a parameterized aggregation element where the alias usually an aggregation column.
        return reference.column()
                .or(() -> reference.alias().map(Alias::name).map(References::column))
                .map(Column::getName)
                .map(KpiDefinitionName::kpiDefinitionName)
                .orElseThrow(() -> badRequest(String.format(
                        "'%s' '%s' contained no <column> and <alias>", Reference.class.getSimpleName(), reference
                )));
    }

    private static List<ResolutionResult> complexResolutionResults(@NonNull final Collection<ResolutionResult> resolutionResults) {
        return resolutionResults.stream().filter(resolutionResult -> resolutionResult.kpiDefinition().isScheduledComplex()).collect(toList());
    }

    private static boolean hasInMemoryDatasource(@NonNull final Collection<RelationReferenceResolution> resolutions) {
        return resolutions.stream()
                .map(resolution -> resolution.relation().flatMap(Relation::datasource))
                .flatMap(Optional::stream)
                .anyMatch(datasource -> datasource.isInMemory());
    }

    private static boolean areAllResolved(@NonNull final List<ResolutionResult> resolutionResults) {
        return resolutionResults.stream().allMatch(ResolutionResult::isResolved);
    }

    @Data
    public static final class KpiDefinitionName {
        private final String name;

        public static KpiDefinitionName kpiDefinitionName(final String name) {
            return new KpiDefinitionName(name);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Data
    public static final class ComplexExecutionGroup {
        private final String value;

        public static ComplexExecutionGroup complexExecutionGroup(final String complexExecutionGroup) {
            return new ComplexExecutionGroup(complexExecutionGroup);
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
