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

import javax.ejb.ApplicationException;

import lombok.Getter;

/**
 * Exception produced by KPI calculation related services.
 */
@Getter
@ApplicationException
public class KpiCalculatorException extends RuntimeException {
    private static final long serialVersionUID = 8_120_069_262_630_359_532L;

    private final KpiCalculatorErrorCode errorCode;

    public KpiCalculatorException(final KpiCalculatorErrorCode errorCode, final String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
    }

    public KpiCalculatorException(final KpiCalculatorErrorCode errorCode, final Throwable throwable) {
        super(throwable);
        this.errorCode = errorCode;
    }

}
