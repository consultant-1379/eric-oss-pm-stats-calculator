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

import static com.ericsson.oss.air.pm.stats.common.sqlparser.prepare.SqlExpressionPrepares.prepareSyntax;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.RelationCollectors.collect;
import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PUBLIC;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.common.sqlparser.SqlParserImpl;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;
import com.ericsson.oss.air.pm.stats.common.sqlparser.exception.InvalidSqlSyntaxException;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;

import kpi.model.api.table.definition.KpiDefinition;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@NoArgsConstructor /* EJB definition requires no-arg constructor */
@AllArgsConstructor(access = PUBLIC) /* Internal testing only */
public class SqlRelationExtractor {
    @Inject
    private SqlParserImpl sqlParser;

    public Set<Relation> extractColumns(@NonNull final KpiDefinition kpiDefinition) {
        try {
            return collect(sqlParser.parsePlan(kpiDefinition.expression()));
        } catch (final InvalidSqlSyntaxException e) {
            log.error("Invalid SQL syntax exception occurred:", e);
            throw new InvalidSqlSyntaxException(
                    "Syntax error in the expression of the following definition: " + kpiDefinition.name().value() + ". "
                            + ofNullable(getRootCause(e)).orElse(e).getMessage());
        }
    }

    public Set<Relation> extractRelations(@NonNull final KpiDefinitionEntity kpiDefinition) {
        try {
            return collect(sqlParser.parsePlan(prepareSyntax(kpiDefinition.expression())));
        } catch (final InvalidSqlSyntaxException e) {
            log.error("Invalid SQL syntax exception occurred:", e);
            throw new InvalidSqlSyntaxException(
                    "Syntax error in the expression of the following definition: " + kpiDefinition.name() + ". "
                            + ofNullable(getRootCause(e)).orElse(e).getMessage());
        }
    }

}
