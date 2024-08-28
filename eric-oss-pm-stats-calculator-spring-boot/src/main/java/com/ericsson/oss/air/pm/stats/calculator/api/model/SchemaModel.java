/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model;

/**
 * Generic interface for any schema model that contains a {@link SchemaElement}.
 */
public interface SchemaModel {

    String getName();

    String getNamespace();

    String getVersion();

    SchemaElement getElement();
}
