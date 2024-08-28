/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.api;

import java.util.Set;

import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.implementation.FilterCast;
import com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.implementation.FilterEqualTo;
import com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.implementation.FilterGreaterThenOrEqual;
import com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.implementation.FilterLessThenOrEqual;
import com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.implementation.FilterLiteral;
import com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.implementation.FilterSubtract;
import com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.implementation.FilterUnresolvedAttribute;
import com.ericsson.oss.air.pm.stats.common.sqlparser.filter.visitor.implementation.FilterUnresolvedFunction;

import lombok.NonNull;

public interface FilterVisitor {
    Set<Reference> visit(@NonNull FilterEqualTo filterEqualTo);
    Set<Reference> visit(@NonNull FilterGreaterThenOrEqual filterGreaterThenOrEqual);
    Set<Reference> visit(@NonNull FilterLessThenOrEqual filterLessThenOrEqual);
    Set<Reference> visit(@NonNull FilterSubtract filterSubtract);
    Set<Reference> visit(@NonNull FilterLiteral filterLiteral);
    Set<Reference> visit(@NonNull FilterCast filterCast);
    Set<Reference> visit(@NonNull FilterUnresolvedAttribute filterUnresolvedAttribute);
    Set<Reference> visit(@NonNull FilterUnresolvedFunction filterUnresolvedFunction);
}
