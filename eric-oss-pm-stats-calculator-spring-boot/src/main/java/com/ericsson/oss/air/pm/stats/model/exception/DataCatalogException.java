/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.model.exception;

import java.util.List;

import lombok.experimental.StandardException;

@StandardException
public class DataCatalogException extends RuntimeException {
    private static final long serialVersionUID = 3L;

    public DataCatalogException(final List<String> identifiers) {
        super("There is no entry available in the DataCatalog for identifier(s): " + String.join(", ", identifiers));
    }
}
