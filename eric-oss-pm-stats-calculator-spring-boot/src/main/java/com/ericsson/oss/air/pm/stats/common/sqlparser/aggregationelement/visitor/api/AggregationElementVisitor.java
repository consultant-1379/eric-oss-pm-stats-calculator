/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.sqlparser.aggregationelement.visitor.api;

import java.util.Set;

import com.ericsson.oss.air.pm.stats.common.sqlparser.aggregationelement.visitor.implementation.AggregationElementAlias;
import com.ericsson.oss.air.pm.stats.common.sqlparser.aggregationelement.visitor.implementation.AggregationElementUnresolvedAttribute;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;

import lombok.NonNull;

public interface AggregationElementVisitor {
    Set<Reference> visit(@NonNull AggregationElementAlias aggregationElementAlias);
    Set<Reference> visit(@NonNull AggregationElementUnresolvedAttribute aggregationElementUnresolvedAttribute);
}
