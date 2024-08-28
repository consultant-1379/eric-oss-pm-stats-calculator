/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.api.table;

import kpi.model.api.OptionalAttribute;
import kpi.model.api.table.api.TableAttribute;

public abstract class OptionalTableAttribute<T> extends OptionalAttribute<T> implements TableAttribute<T> {
    protected OptionalTableAttribute(final T value) {
        super(value);
        defaultIfMissing();
    }

    protected abstract T defaultValue();

    private void defaultIfMissing() {
        overrideIfCan(defaultValue());
    }

}
