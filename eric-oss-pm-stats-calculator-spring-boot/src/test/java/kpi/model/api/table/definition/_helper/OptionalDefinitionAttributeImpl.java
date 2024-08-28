/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.api.table.definition._helper;

import kpi.model.api.table.api.TableAttribute;
import kpi.model.api.table.definition.OptionalDefinitionAttribute;

public class OptionalDefinitionAttributeImpl<T> extends OptionalDefinitionAttribute<T> {
    private final Class<? extends TableAttribute<T>> parentClass;

    public OptionalDefinitionAttributeImpl(final T value, final Class<? extends TableAttribute<T>> parentClass) {
        super(value);
        this.parentClass = parentClass;
    }

    @Override
    public String name() {
        return "optionalDefinitionAttribute";
    }

    @Override
    public Class<? extends TableAttribute<T>> parentClass() {
        return parentClass;
    }
}
