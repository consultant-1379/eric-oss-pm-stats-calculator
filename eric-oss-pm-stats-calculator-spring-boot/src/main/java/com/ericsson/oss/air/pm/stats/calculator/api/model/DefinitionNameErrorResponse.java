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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Used as a wrapper for definition names when they fail validation.
 */
@EqualsAndHashCode
@ToString
public class DefinitionNameErrorResponse implements Serializable {
    private static final long serialVersionUID = 2_122_239_768_252_914_458L;

    private final List<String> errorCauses = new ArrayList<>();

    @JsonAlias("kpi_name")
    private final String definitionName;

    public DefinitionNameErrorResponse(final List<String> errorCauses, final String definitionName) {
        this.errorCauses.addAll(errorCauses);
        this.definitionName = definitionName;
    }

    public List<String> getErrorCauses() {
        return Collections.unmodifiableList(this.errorCauses);
    }

    public String getDefinitionName() {
        return this.definitionName;
    }
}
