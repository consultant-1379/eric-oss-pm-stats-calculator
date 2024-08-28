/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.sqlparser.util;

import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;

import com.ericsson.oss.air.pm.stats.common.sqlparser.expression.extractor.ExpressionExtractor;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.catalyst.expressions.Expression;
import scala.collection.Iterable;
import scala.collection.Iterator;

@Slf4j
@ApplicationScoped
public class LeafCollector {

    public <T> Set<T> collect(@NonNull final Iterable<Expression> leaves, final ExpressionExtractor<T> expressionExtractor) {
        final Set<T> result = new HashSet<>();

        final Iterator<Expression> iterator = leaves.iterator();
        while (iterator.hasNext()) {
            final Expression leaf = iterator.next();
            result.addAll(expressionExtractor.extract(leaf));
        }

        return result;
    }

}
