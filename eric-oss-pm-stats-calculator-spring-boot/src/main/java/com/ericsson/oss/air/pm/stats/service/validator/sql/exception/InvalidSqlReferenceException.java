/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql.exception;

import static java.util.Collections.unmodifiableList;
import static org.apache.arrow.util.Preconditions.checkArgument;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.oss.air.pm.stats.service.validator.sql.reference.ResolutionResult;

import lombok.NonNull;

public class InvalidSqlReferenceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final List<ResolutionResult> unresolvedResolutions = new ArrayList<>();

    public InvalidSqlReferenceException(@NonNull final List<ResolutionResult> unresolvedResolutions) {
        checkArgument(isNotEmpty(unresolvedResolutions), "'unresolvedResolutions' is empty");
        checkArgument(areAllUnresolved(unresolvedResolutions), "'unresolvedResolutions' contain RESOLVED");

        this.unresolvedResolutions.addAll(unresolvedResolutions);
    }

    public List<ResolutionResult> unresolvedResolutions() {
        return unmodifiableList(unresolvedResolutions);
    }

    private static boolean areAllUnresolved(@NonNull final List<ResolutionResult> unresolvedResolutions) {
        return unresolvedResolutions.stream().allMatch(ResolutionResult::isUnResolved);
    }
}
