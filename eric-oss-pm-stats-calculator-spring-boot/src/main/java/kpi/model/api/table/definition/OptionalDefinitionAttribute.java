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

import java.util.Objects;

import kpi.model.api.OptionalAttribute;
import kpi.model.api.table.api.TableAttribute;
import kpi.model.api.table.definition.api.DefinitionAttribute;
import lombok.NonNull;

public abstract class OptionalDefinitionAttribute<T> extends OptionalAttribute<T> implements DefinitionAttribute<T> {
    protected OptionalDefinitionAttribute(final T value) {
        super(value);
    }

    protected final void overrideBy(@NonNull final TableAttribute<? extends T> parentAttribute) {
        if (!Objects.equals(parentClass(), parentAttribute.getClass())) {
            throw new IllegalArgumentException(String.format("Attribute '%s' is not parent of '%s'", parentAttribute.name(), name()));
        }

        overrideIfCan(parentAttribute.value());
    }

    public Class<? extends TableAttribute<T>> parentClass() {
        throw new UnsupportedOperationException(String.format("Attribute '%s' has no parent", name()));
    }
}
