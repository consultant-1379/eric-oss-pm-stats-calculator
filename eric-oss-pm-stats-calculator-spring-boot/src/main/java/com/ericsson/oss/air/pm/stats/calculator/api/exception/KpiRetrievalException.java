/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.exception;

import lombok.experimental.StandardException;

/**
 * A {@code KpiRetrievalException} is thrown when KPI retrieval from either file or database has failed.
 */
@StandardException
public class KpiRetrievalException extends Exception {
    private static final long serialVersionUID = 4_590_912_959_493_933_753L;
}
