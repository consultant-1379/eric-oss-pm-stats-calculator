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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;

import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;
import com.ericsson.oss.air.pm.stats.service.validator.sql.exception.InvalidDataSourceAliasException;
import com.ericsson.oss.air.pm.stats.service.validator.sql.reference.RelationReferenceResolution;
import com.ericsson.oss.air.pm.stats.service.validator.sql.reference.ResolutionResult;

import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class AliasValidator {

    public void validateNoAliasedTabularParameterTable(final List<ResolutionResult> resolutionResults) {
        final Set<Relation> foundAliases = resolutionResults.stream()
                .flatMap(resolution -> resolution.resolutions().stream())
                .map(RelationReferenceResolution::relation)
                .flatMap(Optional::stream)
                .filter(relation -> relation.hasDatasource(Datasource.TABULAR_PARAMETERS) && relation.alias().isPresent())
                .collect(Collectors.toSet());

        if (!foundAliases.isEmpty()) {
            foundAliases.forEach(relation -> log.warn("Tabular Parameter table was aliased '{}'", relation.table().getName()));
            throw new InvalidDataSourceAliasException(foundAliases);
        }
    }
}
