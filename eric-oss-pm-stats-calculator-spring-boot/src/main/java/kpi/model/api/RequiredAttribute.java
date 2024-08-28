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

import kpi.model.api.validation.ValidationResult;
import kpi.model.util.ValidationResults;

public abstract class RequiredAttribute<T> extends AttributeBase<T> {
    protected RequiredAttribute(final T value) {
        super(value);
    }

    @Override
    public final boolean isRequired() {
        return true;
    }

    @Override
    protected void overrideValue(final T value) {
        throw new UnsupportedOperationException("Required attribute is not overridable");
    }

    @Override
    protected ValidationResult validateValue(final T value) {
        return super.validateValue(value).andThen(() -> ValidationResults.valueIsRequired(name(), value));
    }
}
