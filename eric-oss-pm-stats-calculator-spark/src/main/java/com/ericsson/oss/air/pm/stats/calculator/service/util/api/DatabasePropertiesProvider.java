/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.api;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatabaseProperties;

import org.apache.spark.SparkConf;

public interface DatabasePropertiesProvider {
    /**
     * Reads database properties from {@link SparkConf}.
     *
     * @param sparkConf
     *         {@link SparkConf} to read database properties from.
     * @return {@link DatabaseProperties} containing database property information.
     */
    DatabaseProperties readDatabaseProperties(SparkConf sparkConf);
}
