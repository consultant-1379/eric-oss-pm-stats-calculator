/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.api;

public abstract class OptionalAttribute<T> extends AttributeBase<T> {
    protected OptionalAttribute(final T value) {
        super(value);
    }

    @Override
    public final boolean isRequired() {
        return false;
    }

    protected boolean canOverride(final T value) {
        return this.value == null && value != null;
    }

    protected final void overrideIfCan(final T newValue) {
        if (canOverride(newValue)) {
            overrideValue(newValue);
        }
    }

}
