/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql.exception;

import java.util.HashSet;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class InvalidDataSourceAliasException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final Set<Relation> invalidAliasedRelations = new HashSet<>();

    public InvalidDataSourceAliasException(@NonNull final Set<Relation> invalidAliasedRelations) {
        super();
        this.invalidAliasedRelations.addAll(invalidAliasedRelations);
    }
}
