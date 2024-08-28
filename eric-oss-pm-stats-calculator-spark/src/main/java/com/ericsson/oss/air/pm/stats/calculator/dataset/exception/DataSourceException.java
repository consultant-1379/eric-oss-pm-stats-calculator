/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.exception;

import lombok.experimental.StandardException;

/**
 * Exception which is thrown if there is an error loading from a datasource.
 */
@StandardException
public class DataSourceException extends RuntimeException {
    private static final long serialVersionUID = 1L;
}
