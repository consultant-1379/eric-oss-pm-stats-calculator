/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.model.attribute;

public interface Attribute<T> {
    /**
     * The value encapsulated by the attribute.
     *
     * @return the value
     */
    T value();

    /**
     * The name of the attribute used in the <strong>JSON</strong> representation.
     *
     * @return the name of the attribute
     */
    String name();

    /**
     * The require-ability of the attribute.
     *
     * @return true if the attribute is required, otherwise false
     */
    boolean isRequired();

    /**
     * The optional-ability of the attribute.
     *
     * @return true if the attribute is optional, otherwise false
     */
    default boolean isOptional() {
        return !isRequired();
    }
}
