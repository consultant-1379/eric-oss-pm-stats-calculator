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

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PUBLIC;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.service.validator.sql.database.VirtualDatabaseResolver;
import com.ericsson.oss.air.pm.stats.service.validator.sql.database.VirtualDatabaseService;
import com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.VirtualDatabases;
import com.ericsson.oss.air.pm.stats.service.validator.sql.exception.InvalidSqlReferenceException;
import com.ericsson.oss.air.pm.stats.service.validator.sql.reference.ResolutionResult;

import kpi.model.KpiDefinitionRequest;
import kpi.model.KpiDefinitionTable;
import kpi.model.api.table.Table;
import kpi.model.api.table.definition.KpiDefinition;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@NoArgsConstructor /* EJB definition requires no-arg constructor */
@AllArgsConstructor(access = PUBLIC) /* Internal testing only */
public class SqlReferenceValidator {
    @Inject
    private VirtualDatabaseService virtualDatabaseService;
    @Inject
    private VirtualDatabaseResolver virtualDatabaseResolver;

    public List<ResolutionResult> validateReferences(@NonNull final KpiDefinitionRequest kpiDefinitionRequest) {
        if (kpiDefinitionRequest.onDemand().isEmpty() && kpiDefinitionRequest.scheduledComplex().isEmpty()) {
            log.info("The request contained no scheduled-complex and on-demand KPI definitions, SQL reference validation is skipped");
            return emptyList();
        }

        final VirtualDatabases virtualDatabases = virtualDatabaseService.virtualize(kpiDefinitionRequest);
        log.info("Merge of the request and current databases did result in '{}'", virtualDatabases.toString());

        final List<ResolutionResult> resolutionResults = new ArrayList<>();
        nonSimpleTables(kpiDefinitionRequest).forEach(table -> {
            final Integer aggregationPeriod = table.aggregationPeriod().value();
            for (final KpiDefinition kpiDefinition : table.kpiDefinitions()) {
                resolutionResults.add(virtualDatabaseResolver.resolveReferences(virtualDatabases, kpiDefinition, aggregationPeriod));
            }
        });

        final List<ResolutionResult> unresolvedResolutions = resolutionResults.stream().filter(ResolutionResult::isUnResolved).collect(toList());

        log.info(
                "Total resolutions '{}' from which RESOLVED '{}' and UNRESOLVED '{}'",
                resolutionResults.size(), resolutionResults.size() - unresolvedResolutions.size(), unresolvedResolutions.size()
        );

        if (isNotEmpty(unresolvedResolutions)) {
            throw new InvalidSqlReferenceException(unresolvedResolutions);
        }

        return resolutionResults;
    }

    private static Collection<Table> nonSimpleTables(@NonNull final KpiDefinitionRequest kpiDefinitionRequest) {
        return Stream.of(kpiDefinitionRequest.onDemand(), kpiDefinitionRequest.scheduledComplex())
                .map(KpiDefinitionTable::kpiOutputTables)
                .flatMap(Collection::stream)
                .collect(toList());
    }
}
