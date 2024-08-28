/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.exception;

import lombok.experimental.StandardException;

/**
 * Exception produced by PM Stats Calculator when final Spark state is anything other than FINISHED.
 */
@StandardException
public class KpiCalculatorSparkStateException extends Exception {
    private static final long serialVersionUID = 1L;
}