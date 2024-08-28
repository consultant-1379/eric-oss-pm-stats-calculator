/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.api.element;

import static kpi.model.api.validation.ValidationResult.valid;

import com.ericsson.oss.air.pm.stats.common.model.element.Element;

import kpi.model.api.validation.ValidationResult;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public abstract class ElementBase<T> implements Element<T> {
    protected final T value;

    protected ElementBase(final T value) {
        validate(value);
        this.value = value;
    }

    @Override
    public T value() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    protected ValidationResult validateValue(final T value) { //NOSONAR
        return valid();
    }

    private void validate(final T value) {
        final ValidationResult validValueValidation = validateValue(value);
        if (validValueValidation.isInvalid()) {
            throw validValueValidation.exception();
        }
    }
}
