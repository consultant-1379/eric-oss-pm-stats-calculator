/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.sqlparser.prepare;

import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.prependIfMissingIgnoreCase;

import com.ericsson.oss.air.pm.stats.common.model.attribute.ExpressionAttribute;
import com.ericsson.oss.air.pm.stats.common.model.constants.DatasourceConstants;
import com.ericsson.oss.air.pm.stats.common.model.element.FilterElement;

import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = PRIVATE)
public final class SqlExpressionPrepares {
    public static String prepareSyntax(@NonNull final FilterElement filterElement) {
        String preparedSql = filterElement.value();
        preparedSql = preparedSql.replace(DatasourceConstants.DATASOURCE_DELIMITER, DatasourceConstants.TABLE_DELIMITER);
        return preparedSql;
    }

    public static String prepareSyntax(@NonNull final ExpressionAttribute expressionAttribute) {
        return prepareSyntax(expressionAttribute.value());
    }

    public static String prepareSyntax(@NonNull final String expression) {
        String preparedSql = expression;
        preparedSql = prependIfMissingIgnoreCase(preparedSql, "SELECT ");
        preparedSql = preparedSql.replace(DatasourceConstants.DATASOURCE_DELIMITER, DatasourceConstants.TABLE_DELIMITER);
        return preparedSql;
    }
}
