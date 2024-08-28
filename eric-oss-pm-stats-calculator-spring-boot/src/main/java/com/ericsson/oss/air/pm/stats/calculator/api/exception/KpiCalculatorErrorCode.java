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

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * KPI Calculation Service error codes.
 */
@AllArgsConstructor
@Getter
public enum KpiCalculatorErrorCode {

    KPI_CALCULATION_ERROR(1000, "Error calculating KPIs"),
    KPI_DEFINITION_ERROR(2000, "Error retrieving KPI Definitions from database table"),
    KPI_EXTERNAL_DATASOURCE_ERROR(3000, "Error retrieving External DataSource details"),
    KPI_CALCULATION_STATE_PERSISTENCE_ERROR(4000, "Error persisting state for calculation ID"),
    KPI_SENDING_EXECUTION_REPORT_ERROR(5000, "Error sending execution report to Kafka");

    private final int errorCode;

    private final String errorMessage;
}
