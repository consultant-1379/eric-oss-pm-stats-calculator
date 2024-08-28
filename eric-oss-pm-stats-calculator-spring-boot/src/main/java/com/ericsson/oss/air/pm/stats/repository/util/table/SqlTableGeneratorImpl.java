/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util.table;

import static lombok.AccessLevel.PUBLIC;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.Parameter;
import com.ericsson.oss.air.pm.stats.repository.util.table.api.SqlAppender;
import com.ericsson.oss.air.pm.stats.repository.util.table.api.SqlTableGenerator;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.TableCreationInformation;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class SqlTableGeneratorImpl implements SqlTableGenerator {
    @Inject
    private SqlAppender sqlAppender;

    @Override
    public String generateOutputTable(final TableCreationInformation tableCreationInformation, final Map<String, KpiDataType> validAggregationElements) {
        final StringBuilder sql = new StringBuilder(String.format("CREATE TABLE IF NOT EXISTS kpi_service_db.kpi.%s (",
                tableCreationInformation.getTableName()));
        final Set<String> aggregationElements = tableCreationInformation.collectAggregationElements();

        sqlAppender.appendAggregationElementColumns(sql, aggregationElements, validAggregationElements);

        for (final KpiDefinitionEntity definition : tableCreationInformation.getDefinitions()) {
            if (!aggregationElements.contains(definition.name())) {
                sqlAppender.appendColumnNameAndType(sql, definition.name(), definition.objectType());
            }
        }

        if (tableCreationInformation.isDefaultAggregationPeriod()) {
            sqlAppender.appendPrimaryKey(sql, aggregationElements);
            sql.append(");");
        } else {
            sqlAppender.appendTimestampColumns(sql);
            sql.append(')');
            sqlAppender.appendPartition(sql);
        }
        return sql.toString();
    }

    @Override
    public String generateTabularParameterTableSql(final List<Parameter> parameters, final String tabularParameterName) {
        final StringBuilder sql = new StringBuilder(String.format("CREATE TABLE kpi_service_db.kpi.%s (", tabularParameterName));
        parameters.forEach(parameter -> sqlAppender.appendColumnNameAndType(sql, parameter.name(), parameter.type().name()));
        sql.deleteCharAt(sql.lastIndexOf(", "));
        sql.append(')');

        return sql.toString();
    }
}
