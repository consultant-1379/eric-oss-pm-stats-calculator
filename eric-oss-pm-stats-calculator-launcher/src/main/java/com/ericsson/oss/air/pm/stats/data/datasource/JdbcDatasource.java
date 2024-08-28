/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.data.datasource;

import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.util.PropertiesUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static com.ericsson.oss.air.pm.stats.common.model.constants.DatasourceConstants.PROPERTY_EXPRESSION_TAG;

/**
 * This class stores a JDBC connection and the associated JDBC properties.
 */
@Getter
@RequiredArgsConstructor
public final class JdbcDatasource {
    private final String jbdcConnection;
    private final Properties jdbcProperties;

    public static JdbcDatasource of(final String url, final Properties properties) {
        return new JdbcDatasource(url, PropertiesUtils.copyOf(properties));
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jbdcConnection, jdbcProperties);
    }

    public Datasource getDatasource() {
        return Datasource.of(jdbcProperties.getProperty(PROPERTY_EXPRESSION_TAG));
    }
}
