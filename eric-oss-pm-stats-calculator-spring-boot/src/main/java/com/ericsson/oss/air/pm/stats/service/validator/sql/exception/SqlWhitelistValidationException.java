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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.codehaus.commons.nullanalysis.NotNull;

@Getter
@Accessors(fluent = true)
public class SqlWhitelistValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Map<String, Set<String>> notAllowedSqlElementsByKpiDefinition;

    public SqlWhitelistValidationException(@NotNull final Map<String, Set<String>> notAllowedSqlElementsByKpiDefinition) {
        this(notAllowedSqlElementsByKpiDefinition, null);
    }

    public SqlWhitelistValidationException(@NotNull final Map<String, Set<String>> notAllowedSqlElementsByKpiDefinition, final String message) {
        super(message);
        this.notAllowedSqlElementsByKpiDefinition = new LinkedHashMap<>(notAllowedSqlElementsByKpiDefinition);
    }
}

