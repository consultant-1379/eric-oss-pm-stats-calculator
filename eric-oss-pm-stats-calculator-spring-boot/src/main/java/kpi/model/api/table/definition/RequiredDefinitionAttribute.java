/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.api.table.definition;

import kpi.model.api.RequiredAttribute;
import kpi.model.api.table.definition.api.DefinitionAttribute;

public abstract class RequiredDefinitionAttribute<T> extends RequiredAttribute<T> implements DefinitionAttribute<T> {
    protected RequiredDefinitionAttribute(final T value) {
        super(value);
    }
}
