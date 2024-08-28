/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.api;

public interface KpiExposureService {

    /**
     * Makes a calculation output table available for exposure by Query Service.
     *
     * @param name Name of the table.
     */
    void exposeCalculationTable(String name);

    /**
     * Sends the up-to-date table exposure information to the Query Service.
     */
    void updateExposure();

}
