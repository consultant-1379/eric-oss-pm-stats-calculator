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

import java.util.ArrayList;
import java.util.List;

import kpi.model.api.table.definition.KpiDefinition;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class ExpressionContainsOnlyTabularParametersException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final List<KpiDefinition> kpiDefinitionsWithOnlyTabularParamRelations = new ArrayList<>();
    private final String name;
    private final String expression;

    public ExpressionContainsOnlyTabularParametersException(@NonNull final List<KpiDefinition> kpiDefinitionsWithOnlyTabularParamRelations) {
        super();
        this.kpiDefinitionsWithOnlyTabularParamRelations.addAll(kpiDefinitionsWithOnlyTabularParamRelations);
        name = null;
        expression = null;
    }

    public ExpressionContainsOnlyTabularParametersException(@NonNull final String name, @NonNull final String expression) {
        super();
        this.name = name;
        this.expression = expression;
    }
}
