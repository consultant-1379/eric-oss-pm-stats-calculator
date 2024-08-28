/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.ToString;

/**
 * Used as a wrapper for definition objects when they fail validation.
 */
@ToString
public class DefinitionErrorResponse {
    private final List<String> errorCauses = new ArrayList<>();
    private final Definition definition;

    public DefinitionErrorResponse(final String errorCause, final Definition definition) {
        errorCauses.add(errorCause);
        this.definition = definition;
    }

    public void addErrorCause(final String error) {
        errorCauses.add(error);
    }

    public List<String> getErrorCauses() {
        return Collections.unmodifiableList(this.errorCauses);
    }

    public Definition getDefinition() {
        return new Definition(this.definition.getAttributes());
    }
}
