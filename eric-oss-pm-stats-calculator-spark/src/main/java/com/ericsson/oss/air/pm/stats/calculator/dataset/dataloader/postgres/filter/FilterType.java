/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter;

public enum FilterType {
    /**
     * <strong>DEFAULT</strong> filter represents a KPI Definition that has <strong>NO</strong> filter attribute defined.
     */
    DEFAULT,
    /**
     * <strong>CUSTOM</strong> filter represents a KPI Definition that has filter attribute defined.
     */
    CUSTOM
}
