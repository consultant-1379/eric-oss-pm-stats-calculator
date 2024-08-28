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

import lombok.Getter;
import lombok.experimental.Accessors;
import org.codehaus.commons.nullanalysis.NotNull;

@Getter
@Accessors(fluent = true)
public class NotAllowedSqlElementException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String element;

    public NotAllowedSqlElementException(@NotNull final String element) {
        this(element, null);
    }

    public NotAllowedSqlElementException(@NotNull final String element, final String message) {
        super(message);
        this.element = element;
    }
}
