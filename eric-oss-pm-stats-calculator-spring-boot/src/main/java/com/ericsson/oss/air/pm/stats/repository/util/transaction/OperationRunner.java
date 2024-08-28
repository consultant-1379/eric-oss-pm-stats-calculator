/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util.transaction;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface OperationRunner {
    /**
     * Runs operation(s) on the Database.
     *
     * @param connection {@link Connection} to the database.
     * @throws SQLException If there are any errors performing database operations.
     */
    void run(Connection connection) throws SQLException;
}
