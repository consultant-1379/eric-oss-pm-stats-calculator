/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.api._helper;

import kpi.model.api.OptionalAttribute;

public final class OptionalAttributeImpl<T> extends OptionalAttribute<T> {
    public OptionalAttributeImpl(final T value) {
        super(value);
    }

    @Override
    public String name() {
        return "optionalAttribute";
    }
}
