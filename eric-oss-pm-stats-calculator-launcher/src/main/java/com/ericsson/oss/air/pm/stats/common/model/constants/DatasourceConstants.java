/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.model.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Constants that are used with Datasources.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatasourceConstants {

    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_EXPRESSION_TAG = "expressionTag";
    public static final String PROPERTY_CONNECTION_URL = "jdbcUrl";
    public static final String PROPERTY_USER = "user";
    public static final String PROPERTY_PASSWORD = "password"; //NOSONAR Password key, not value
    public static final String PROPERTY_DRIVER = "driver";
    public static final String DATASOURCE_DELIMITER = "://";
    public static final String TABLE_DELIMITER = ".";
}
