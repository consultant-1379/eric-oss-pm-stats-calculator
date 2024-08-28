/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.exporter.util;

import lombok.experimental.StandardException;

/**
 * A {@code KpiExporterException} is thrown by Exporter service.
 */
@StandardException
public class KpiExporterException extends Exception {
    private static final long serialVersionUID = 4_590_912_959_493_933_754L;
}
